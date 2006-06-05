/*
 *  Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/** 
 * @author Intel, Evgueni Brevnov
 * @version $Revision: 1.1.2.1.4.4 $
 */  
// We use signal handlers to detect null pointer and divide by zero
// exceptions.
// There must be an easier way of locating the context in the signal
// handler than what we do here.

#define LOG_DOMAIN "port.old"
#include "cxxlog.h"

#include "platform.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <sys/syscall.h>
#include <stdio.h>
#include <iostream>
#include <fstream>
#include <string.h>
#include <assert.h>
#include <errno.h>

#include <sys/ucontext.h>
#include <sys/wait.h>


#undef __USE_XOPEN
#include <signal.h>

#include <pthread.h>
#include <sys/time.h>
#include "method_lookup.h"

#include "Class.h"
#include "environment.h"
 
#include "open/gc.h"
 
#include "exceptions.h"
#include "vm_synch.h"
#include "vm_threads.h"
#include "open/vm_util.h"
#include "compile.h"
#include "vm_synch.h"
#include "vm_stats.h"
#include "sync_bits.h"

#include "object_generic.h"
#include "thread_manager.h"

#include "exception_filter.h"
#include "interpreter.h"
#include "crash_handler.h"

// Variables used to locate the context from the signal handler
static int sc_nest = -1;
static uint32 exam_point;

bool SuspendThread(unsigned UNREF xx){ return 0; }
bool ResumeThread(unsigned UNREF xx){ return 1; }

static void linux_sigcontext_to_regs(Registers* regs, ucontext_t *uc)
{
    regs->eax = uc->uc_mcontext.gregs[REG_EAX];
    regs->ecx = uc->uc_mcontext.gregs[REG_ECX];
    regs->edx = uc->uc_mcontext.gregs[REG_EDX];
    regs->edi = uc->uc_mcontext.gregs[REG_EDI];
    regs->esi = uc->uc_mcontext.gregs[REG_ESI];
    regs->ebx = uc->uc_mcontext.gregs[REG_EBX];
    regs->ebp = uc->uc_mcontext.gregs[REG_EBP];
    regs->eip = uc->uc_mcontext.gregs[REG_EIP];
    regs->esp = uc->uc_mcontext.gregs[REG_ESP];
}

static void linux_regs_to_sigcontext(ucontext_t *uc, Registers* regs)
{
    uc->uc_mcontext.gregs[REG_EAX] = regs->eax;
    uc->uc_mcontext.gregs[REG_ECX] = regs->ecx;
    uc->uc_mcontext.gregs[REG_EDX] = regs->edx;
    uc->uc_mcontext.gregs[REG_EDI] = regs->edi;
    uc->uc_mcontext.gregs[REG_ESI] = regs->esi;
    uc->uc_mcontext.gregs[REG_EBX] = regs->ebx;
    uc->uc_mcontext.gregs[REG_EBP] = regs->ebp;
    uc->uc_mcontext.gregs[REG_EIP] = regs->eip;
    uc->uc_mcontext.gregs[REG_ESP] = regs->esp;
}

static bool linux_throw_from_sigcontext(ucontext_t *uc, Class* exc_clss)
{
    ASSERT_NO_INTERPRETER;
    unsigned *eip = (unsigned *) uc->uc_mcontext.gregs[REG_EIP];
    VM_Code_Type vmct = vm_identify_eip((void *)eip);
    if(vmct != VM_TYPE_JAVA) {
        return false;
    }

    Registers regs;
    linux_sigcontext_to_regs(&regs, uc);

    exn_athrow_regs(&regs, exc_clss);

    linux_regs_to_sigcontext(uc, &regs);
    return true;
}

/**
 * the saved copy of the executable name.
 */
static char executable[1024];

/**
 * invokes addr2line to decode stack.
 */
void addr2line (char *buf) {

    if ('\0' == executable[0]) {
        // no executable name is available, degrade gracefully
        WARN("Execution stack follows, consider using addr2line\n" << buf);
        return;
    }

    //
    // NOTE: this function is called from signal handler,
    //       so it should use only limited list 
    //       of async signal-safe system calls
    //
    // Currently used list:
    //          pipe, fork, close, dup2, execle, write, wait
    //
    int pipes[2];               
    pipe(pipes);                // create pipe
    if (0 == fork()) { // child

        close(pipes[1]);        // close unneeded write pipe
        close(0);               // close stdin
        dup2(pipes[0],0);       // replicate read pipe as stdin

        close(1);               // close stdout
        dup2(2,1);              // replicate stderr as stdout

        char *env[] = {NULL};

        execle("/usr/bin/addr2line", "addr2line", "-e", executable, "-C", "-s", NULL, env);

    } else { // parent
        close(pipes[0]);        // close unneeded read pipe

        write(pipes[1],buf,strlen(buf));
        close(pipes[1]);        // close write pipe

        int status;
        wait(&status); // wait for the child to complete
    }
}

/**
 * Print out the call stack.
 */
void print_native_stack (unsigned *ebp) {
    int depth = 17;
    WARN("Fatal error");
    char buf[1024];
    unsigned int n = 0;
    while (ebp && ebp[1] && --depth >= 0 && (n<sizeof(buf)-20)) {
        n += sprintf(buf+n,"%08x\n",ebp[1]);
        ebp = (unsigned *)ebp[0];
    }
    addr2line(buf);
}


/*
 * We find the true signal stack frame set-up by kernel,which is located
 * by locate_sigcontext() below; then change its content according to 
 * exception handling semantics, so that when the signal handler is 
 * returned, application can continue its execution in Java exception handler.
 */

void null_java_reference_handler(int signum, siginfo_t* UNREF info, void* context)
{
    ucontext_t *uc = (ucontext_t *)context;
    Global_Env *env = VM_Global_State::loader_env;

    if (env->shutting_down != 0) {
        fprintf(stderr, "null_java_reference_handler(): called in shutdown stage\n");
    } else if (!interpreter_enabled()) {
        if (linux_throw_from_sigcontext(
                    uc, env->java_lang_NullPointerException_Class)) {
            return;
        }
    }

    // crash with default handler
    signal(signum, 0);
}


void null_java_divide_by_zero_handler(int signum, siginfo_t* UNREF info, void* context)
{
    ucontext_t *uc = (ucontext_t *)context;
    Global_Env *env = VM_Global_State::loader_env;

    if (env->shutting_down != 0) {
        fprintf(stderr, "null_java_divide_by_zero_handler(): called in shutdown stage\n");
    } else if (!interpreter_enabled()) {
        if (linux_throw_from_sigcontext(
                    uc, env->java_lang_ArithmeticException_Class)) {
            return;
        }
    }

    // crash with default handler
    signal(signum, 0);
}

/*
See function initialize_signals() below first please.

Two kinds of signal contexts (and stack frames) are tried in
locate_sigcontext(), one is sigcontext, which is the way of
Linux kernel implements( see Linux kernel source
arch/i386/kernel/signal.c for detail); the other is ucontext,
used in some other other platforms.

The sigcontext locating in Linux is not so simple as expected,
because it involves not only the kernel, but also glibc/linuxthreads,
which VM is linked against. In glibc/linuxthreads, user-provided
signal handler is wrapped by linuxthreads signal handler, i.e.
the signal handler really registered in system is not the one
provided by user. So when Linux kernel finishes setting up signal
stack frame and returns to user mode for singal handler execution,
locate_sigcontext() is not the one being invoked immediately. It's 
called by linuxthreads function. That means the user stack viewed by
locate_sigcontext() is NOT NECESSARILY the signal frame set-up by
kernel, we need find the true one according to glibc/linuxthreads
specific signal implementation in different versions.

Because locate_sigcontext() uses IA32 physical register epb for
call stack frame, compilation option `-fomit-frame-pointer' MUST
not be used when gcc compiles it; and as gcc info, `-O2' will do
`-fomit-frame-pointer' by default, although we haven't seen that
in our experiments.
*/

void locate_sigcontext(int UNREF signum)
{
    sigcontext *sc;
    uint32 *ebp;
    int i;

    asm("movl  %%ebp,%0" : "=r" (ebp));     // ebp = EBP register

#define SC_SEARCH_WIDTH 3
    for (i = 0; i < SC_SEARCH_WIDTH; i++) {
        sc = (sigcontext *)(ebp + 3 );
        if (sc->eip == ((uint32)exam_point)) {  // found
            sc_nest = i;
        // we will try to find the real sigcontext setup by Linux kernel,
        // because if we want to change the execution path after the signal
        // handling, we must modify the sigcontext used by kernel. 
        // LinuxThreads in glibc 2.1.2 setups a sigcontext for our singal
        // handler, but we should not modify it, because kernel doesn't use 
        // it when resumes the application. Then we must find the sigcontext
        // setup by kernel, and modify it in singal handler. 
        // but, with glibc 2.2.3, it's useless to modify only the sigcontext
        // setup by Linux kernel, because LinuxThreads does a interesting 
        // copy after our signal handler returns, which destroys the 
        // modification we have just done in the handler. So with glibc 2.2.3,
        // what we need do is simply to modify the sigcontext setup by 
        // LinuxThreads, which will be copied to overwrite the one setup by 
        // kernel. Really complicated..., not really. We use a simple trick
        // to overcome the changes in glibc from version to version, that is,
        // we modify both sigcontexts setup by kernel and LinuxThreads. Then
        // it will always work. 

        } else {                    // not found
            struct ucontext *uc;
            uc = (struct ucontext *)ebp[4];
            if ((ebp < (uint32 *)uc) && ((uint32 *)uc < ebp + 0x100)) {
                sc = (sigcontext *)&uc->uc_mcontext;
                if (sc->eip == ((uint32)exam_point)) {  // found
                    sc_nest = i;
                    break; 
                }
            }
        }

        ebp = (uint32 *)ebp[0];
    }

    if (sc_nest < 0) {
        printf("cannot locate sigcontext.\n");
        printf("Please add or remove any irrelevant statement(e.g. add a null printf) in VM source code, then rebuild it. If problem remains, please submit a bug report. Thank you very much\n");
        exit(1);
    }
}


/**
 * Print out the call stack of the aborted thread.
 * @note call stacks may be used for debugging
 */
void abort_handler (int signum, siginfo_t* UNREF info, void* context) {
    // crash with default handle.
    signal(signum, 0);
    fprintf(stderr, "abort_handler()\n");
}

/*
 * USR2 signal used to yield the thread at suspend algorithm
 */ 
 
void yield_other_handler(int signum, siginfo_t* info, void* context) {
    Global_Env *env = VM_Global_State::loader_env;
    if (env->shutting_down != 0) {
        // Too late for this kind of signals
        // crash with default handle.
        fprintf(stderr, "yield_other_handler(): called in shut down stage\n");
        signal(signum, 0);
        return;
    }


    VM_thread* thread = p_active_threads_list;
    pthread_t self = GetCurrentThreadId();
    TRACE2("SIGNALLING", "get_context_handler, try to find pthread_t " << self);

    while (thread) {

        TRACE2("SIGNALLING", "get_context_handler, finding pthread_t " << self);

        if (thread->thread_id != self) {
        thread = thread->p_active;
        continue;
    }

    sem_post(&thread->yield_other_sem);
    return;
    }

    DIE("Cannot find Java thread using signal context");
}

void initialize_signals()
{
    // First figure out how to locate the context in the
    // signal handler.  Apparently you have to do it differently
    // on different versions of Linux.
    
    //Now register the real signal handlers. signal() function in Linux
    //kernel has SYSV semantics, i.e. the handler is a kind of ONE SHOT
    //behaviour, which is different from BSD. But glibc2 in Linux
    //implements BSD semantics.
    struct sigaction sa;

    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_SIGINFO | SA_RESTART;
    sa.sa_sigaction = yield_other_handler;
    sigaction(SIGUSR2, &sa, NULL);

    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_SIGINFO;
    sa.sa_sigaction = &null_java_reference_handler;
    sigaction(SIGSEGV, &sa, NULL);

    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_SIGINFO;
    sa.sa_sigaction = &null_java_divide_by_zero_handler;
    sigaction(SIGFPE, &sa, NULL);
    
    extern void interrupt_handler(int);
    signal(SIGINT, (void (*)(int)) interrupt_handler);
    extern void quit_handler(int);
    signal(SIGQUIT, (void (*)(int)) quit_handler);

    /* install abort_handler to print out call stack on assertion failures */
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_SIGINFO;
    sa.sa_sigaction = &abort_handler;
    sigaction( SIGABRT, &sa, NULL);
    /* abort_handler installed */

    extern int get_executable_name(char*, int);
    /* initialize the name of the executable (to be used by addr2line) */
    get_executable_name(executable, sizeof(executable));

    if (vm_get_boolean_property_value_with_default("vm.crash_handler")) {
        init_crash_handler();
        // can't install crash handler immediately,
        // as we have already SIGABRT and SIGSEGV handlers
    }

} //initialize_signals
