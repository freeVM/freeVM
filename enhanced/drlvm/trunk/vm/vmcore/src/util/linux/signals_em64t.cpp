/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
#include <sys/mman.h>

#undef __USE_XOPEN
#include <signal.h>

#include <pthread.h>
#include <sys/time.h>
#include "method_lookup.h"

#include "Class.h"
#include "environment.h"
 
#include "open/gc.h"
 
#include "init.h"
#include "exceptions.h"
#include "exceptions_jit.h"
#include "vm_threads.h"
#include "open/vm_util.h"
#include "compile.h"
#include "vm_stats.h"
#include "sync_bits.h"

#include "object_generic.h"
#include "thread_manager.h"

#include "exception_filter.h"
#include "interpreter.h"
#include "crash_handler.h"
#include "stack_dump.h"

// Variables used to locate the context from the signal handler
static int sc_nest = -1;
static bool use_ucontext = false;
static uint64 exam_point;

bool SuspendThread(unsigned xx){ return 0; }
bool ResumeThread(unsigned xx){ return 1; }

void linux_ucontext_to_regs(Registers* regs, ucontext_t *uc)
{
    regs->rax = uc->uc_mcontext.gregs[REG_RAX];
    regs->rcx = uc->uc_mcontext.gregs[REG_RCX];
    regs->rdx = uc->uc_mcontext.gregs[REG_RDX];
    regs->rdi = uc->uc_mcontext.gregs[REG_RDI];
    regs->rsi = uc->uc_mcontext.gregs[REG_RSI];
    regs->rbx = uc->uc_mcontext.gregs[REG_RBX];
    regs->rbp = uc->uc_mcontext.gregs[REG_RBP];
    regs->rip = uc->uc_mcontext.gregs[REG_RIP];
    regs->rsp = uc->uc_mcontext.gregs[REG_RSP];
}

void linux_regs_to_ucontext(ucontext_t *uc, Registers* regs)
{
    uc->uc_mcontext.gregs[REG_RAX] = regs->rax;
    uc->uc_mcontext.gregs[REG_RCX] = regs->rcx;
    uc->uc_mcontext.gregs[REG_RDX] = regs->rdx;
    uc->uc_mcontext.gregs[REG_RDI] = regs->rdi;
    uc->uc_mcontext.gregs[REG_RSI] = regs->rsi;
    uc->uc_mcontext.gregs[REG_RBX] = regs->rbx;
    uc->uc_mcontext.gregs[REG_RBP] = regs->rbp;
    uc->uc_mcontext.gregs[REG_RIP] = regs->rip;
    uc->uc_mcontext.gregs[REG_RSP] = regs->rsp;
}

// exception catch support for stack restore
extern "C" {
    static void __attribute__ ((used)) exception_catch_callback_wrapper(){
        exception_catch_callback();
    }
}

void asm_exception_catch_callback() {
    asm (
        "pushq %%rax;\n"
        "pushq %%rbx;\n"
        "pushq %%rcx;\n"
        "pushq %%rdx;\n"
        "pushq %%rsi;\n"
        "pushq %%rdi;\n"
        "pushq %%r8;\n"
        "pushq %%r9;\n"
        "pushq %%r10;\n"
        "pushq %%r11;\n"
        "call exception_catch_callback_wrapper;\n"
        "popq %%r11;\n"
        "popq %%r10;\n"
        "popq %%r9;\n"
        "popq %%r8;\n"
        "popq %%rdi;\n"
        "popq %%rsi;\n"
        "popq %%rdx;\n"
        "popq %%rcx;\n"
        "popq %%rbx;\n"
        "popq %%rax;\n"
        : /* no output operands */
        : /* no input operands */
    );
#ifdef _DEBUG
    asm (
        "movq %%rbp, %%rsp\n"
        "popq %%rbp\n"
        "retq $0x80;\n"
        : /* no output operands */
        : /* no input operands */
    );
#else // _DEBUG
    asm (
        "retq $0x80;\n"
        : /* no output operands */
        : /* no input operands */
    );
#endif // ! _DEBUG}
}

// exception catch support for JVMTI
void asm_jvmti_exception_catch_callback() {
    // FIXME: not implemented
    fprintf(stderr, "FIXME: asm_jvmti_exception_catch_callback: not implemented\n");
    assert(0);
    abort();

}
static void throw_from_sigcontext(ucontext_t *uc, Class* exc_clss)
{
    Registers regs;
    linux_ucontext_to_regs(&regs, uc);

    DebugUtilsTI* ti = VM_Global_State::loader_env->TI;
    bool java_code = (vm_identify_eip((void *)regs.rip) == VM_TYPE_JAVA);

    exn_athrow_regs(&regs, exc_clss, java_code);
    linux_regs_to_ucontext(uc, &regs);
}

static bool java_throw_from_sigcontext(ucontext_t *uc, Class* exc_clss)
{
    ASSERT_NO_INTERPRETER;
    unsigned *rip = (unsigned *) uc->uc_mcontext.gregs[REG_RIP];
    VM_Code_Type vmct = vm_identify_eip((void *)rip);
    if(vmct != VM_TYPE_JAVA) {
        return false;
    }

    throw_from_sigcontext(uc, exc_clss);
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
        LWARN(41, "Execution stack follows, consider using addr2line\n{0}" << buf);
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

        execle("/usr/bin/addr2line", "addr2line", "-e", executable, "-C", "-s", "-f", NULL, env);

    } else { // parent
        close(pipes[0]);        // close unneeded read pipe

        write(pipes[1],buf,strlen(buf));
        close(pipes[1]);        // close write pipe

        int status;
        wait(&status); // wait for the child to complete
    }
}

/*
 * Information about stack
 */
inline void* find_stack_addr() {
    int err;
    void* stack_addr;
    size_t stack_size;
    pthread_attr_t pthread_attr;

    pthread_t thread = pthread_self();
    err = pthread_getattr_np(thread, &pthread_attr);
    assert(!err);
    err = pthread_attr_getstack(&pthread_attr, &stack_addr, &stack_size);
    assert(!err);
    pthread_attr_destroy(&pthread_attr);

    return (void *)((unsigned char *)stack_addr + stack_size);
}

#if 0
inline size_t find_stack_size() {
    int err;
    size_t stack_size;
    pthread_attr_t pthread_attr;

    pthread_attr_init(&pthread_attr);
    err = pthread_attr_getstacksize(&pthread_attr, &stack_size);
    pthread_attr_destroy(&pthread_attr);
    return stack_size;
}
#endif

inline size_t find_guard_stack_size() {
    return 64*1024;
}

inline size_t find_guard_page_size() {
    int err;
    size_t guard_size;
    pthread_attr_t pthread_attr;

    pthread_attr_init(&pthread_attr);
    err = pthread_attr_getguardsize(&pthread_attr, &guard_size);
    pthread_attr_destroy(&pthread_attr);
    return guard_size;
}

static size_t common_guard_stack_size;
static size_t common_guard_page_size;

inline void* get_stack_addr() {
    return jthread_self_vm_thread_unsafe()->stack_addr;
}

inline size_t get_stack_size() {
    return jthread_self_vm_thread_unsafe()->stack_size;
}

inline size_t get_guard_stack_size() {
    return common_guard_stack_size;
}

inline size_t get_guard_page_size() {
    return common_guard_page_size;
}

void set_guard_stack();

void init_stack_info() {
    vm_thread_t vm_thread = jthread_self_vm_thread_unsafe();
    char* stack_addr = (char *)find_stack_addr();
    unsigned int stack_size = hythread_get_thread_stacksize(hythread_self());
    vm_thread->stack_addr = stack_addr;
    vm_thread->stack_size = stack_size;
    common_guard_stack_size = find_guard_stack_size();
    common_guard_page_size =find_guard_page_size();

    // stack should be mapped so it's result of future mapping
    char* res;

    // begin of the stack can be protected by OS, but this part already mapped
    // found address of current stack page
    char* current_page_addr =
            (char*)(((size_t)&res) & (~(common_guard_page_size-1)));

    // leave place for mmap work
    char* mapping_page_addr = current_page_addr - common_guard_page_size;

    // makes sure that stack allocated till mapping_page_addr
    //stack_holder(mapping_page_addr);

    // found size of the stack area which should be maped
    size_t stack_mapping_size = (size_t)mapping_page_addr
            - (size_t)stack_addr + stack_size;

    // maps unmapped part of the stack
    res = (char*) mmap(stack_addr - stack_size,
            stack_mapping_size,
            PROT_READ | PROT_WRITE,
            MAP_FIXED | MAP_PRIVATE | MAP_ANONYMOUS | MAP_GROWSDOWN,
            -1,
            0);
    // stack should be mapped, checks result
    assert(res == (stack_addr - stack_size));

    // set guard page
    set_guard_stack();
}

void set_guard_stack() {
    int err;
    char* stack_addr = (char*) get_stack_addr();
    size_t stack_size = get_stack_size();
    size_t guard_stack_size = get_guard_stack_size();
    size_t guard_page_size = get_guard_page_size();

    assert(((size_t)(&stack_addr)) > ((size_t)((char*)stack_addr - stack_size
        + guard_stack_size + 2 * guard_page_size)));

    err = mprotect(stack_addr - stack_size + guard_page_size + guard_stack_size,
        guard_page_size, PROT_NONE);

    stack_t sigalt;
    sigalt.ss_sp = stack_addr - stack_size + guard_page_size;
    sigalt.ss_flags = SS_ONSTACK;
    sigalt.ss_size = guard_stack_size;

    err = sigaltstack (&sigalt, NULL);

    jthread_self_vm_thread_unsafe()->restore_guard_page = false;
}

size_t get_available_stack_size() {
    char* stack_addr = (char*) get_stack_addr();
    size_t used_stack_size = stack_addr - ((char*)&stack_addr);
    int available_stack_size;

    if (((char*)&stack_addr) > (stack_addr - get_stack_size() + get_guard_page_size() + get_guard_stack_size())) {
        available_stack_size = get_stack_size() - used_stack_size
            - 2 * get_guard_page_size() - get_guard_stack_size();
    } else {
        available_stack_size = get_stack_size() - used_stack_size - get_guard_page_size();
    }

    if (available_stack_size > 0) {
        return (size_t) available_stack_size;
    } else {
        return 0;
    }
}

bool check_available_stack_size(size_t required_size) {
    size_t available_stack_size = get_available_stack_size();

    if (available_stack_size < required_size) {
        if (available_stack_size < get_guard_stack_size()) {
            remove_guard_stack();
        }
        Global_Env *env = VM_Global_State::loader_env;
        exn_raise_by_class(env->java_lang_StackOverflowError_Class);
        return false;
    } else {
        return true;
    }
}

size_t get_restore_stack_size() {
    return 0x0800;
}

bool check_stack_size_enough_for_exception_catch(void* sp) {
    char* stack_adrr = (char*) get_stack_addr();
    size_t used_stack_size = ((size_t)stack_adrr) - ((size_t)sp);
    size_t available_stack_size =
            get_stack_size() - used_stack_size
            - 2 * get_guard_page_size() - get_guard_stack_size();
    return get_restore_stack_size() < available_stack_size;
}

void remove_guard_stack() {
    int err;
    char* stack_addr = (char*) get_stack_addr();
    size_t stack_size = get_stack_size();
    size_t guard_stack_size = get_guard_stack_size();
    size_t guard_page_size = get_guard_page_size();

    err = mprotect(stack_addr - stack_size + guard_page_size + guard_stack_size,
        guard_page_size, PROT_READ | PROT_WRITE);

    stack_t sigalt;
    sigalt.ss_sp = stack_addr - stack_size + guard_page_size;
    sigalt.ss_flags = SS_DISABLE;
    sigalt.ss_size = guard_stack_size;

    err = sigaltstack (&sigalt, NULL);

    p_TLS_vmthread->restore_guard_page = true;
}

bool check_stack_overflow(siginfo_t *info, ucontext_t *uc) {
    char* stack_addr = (char*) get_stack_addr();
    size_t stack_size = get_stack_size();
    size_t guard_stack_size = get_guard_stack_size();
    size_t guard_page_size = get_guard_page_size();

    char* guard_page_begin = stack_addr - stack_size + guard_page_size + guard_stack_size;
    char* guard_page_end = guard_page_begin + guard_page_size;

    // FIXME: Workaround for main thread
    guard_page_end += guard_page_size;

    char* fault_addr = (char*)(info->si_addr);
    //char* esp_value = (char*)(uc->uc_mcontext.gregs[REG_ESP]);

    return((guard_page_begin <= fault_addr) && (fault_addr < guard_page_end));
}


/*
 * We find the true signal stack frame set-up by kernel,which is located
 * by locate_sigcontext() below; then change its content according to 
 * exception handling semantics, so that when the signal handler is 
 * returned, application can continue its execution in Java exception handler.
 */
void stack_overflow_handler(int signum, siginfo_t* UNREF info, void* context)
{
    ucontext_t *uc = (ucontext_t *)context;
    Global_Env *env = VM_Global_State::loader_env;

    if (java_throw_from_sigcontext(
                uc, env->java_lang_StackOverflowError_Class)) {
        return;
    } else {
        if (is_unwindable()) {
            if (hythread_is_suspend_enabled()) {
                tmn_suspend_disable();
            }
            throw_from_sigcontext(
                uc, env->java_lang_StackOverflowError_Class);
        } else {
            remove_guard_stack();
            exn_raise_by_class(env->java_lang_StackOverflowError_Class);
        }
    }
}


void null_java_reference_handler(int signum, siginfo_t* info, void* context)
{
    ucontext_t *uc = (ucontext_t *)context;
    Global_Env *env = VM_Global_State::loader_env;

    if (check_stack_overflow(info, uc)) {
        stack_overflow_handler(signum, info, context);
        return;
    }

    if (!interpreter_enabled()) {
        if (java_throw_from_sigcontext(
                    uc, env->java_lang_NullPointerException_Class)) {
            return;
        }
    }

    fprintf(stderr, "SIGSEGV in VM code.\n");
    Registers regs;
    linux_ucontext_to_regs(&regs, uc);

    // setup default handler
    signal(signum, SIG_DFL);

    if (!is_gdb_crash_handler_enabled() ||
        !gdb_crash_handler())
    {
        // print stack trace
        st_print_stack(&regs);
    }
}


void null_java_divide_by_zero_handler(int signum, siginfo_t* info, void* context)
{
    ucontext_t *uc = (ucontext_t *)context;
    Global_Env *env = VM_Global_State::loader_env;

    if (!interpreter_enabled()) {
        if (java_throw_from_sigcontext(
                    uc, env->java_lang_ArithmeticException_Class)) {
            return;
        }
    }
    
    fprintf(stderr, "SIGFPE in VM code.\n");
    Registers regs;
    linux_ucontext_to_regs(&regs, uc);

    // setup default handler
    signal(signum, SIG_DFL);

    if (!is_gdb_crash_handler_enabled() ||
        !gdb_crash_handler())
    {
        // print stack trace
        st_print_stack(&regs);
    }
}

/*
 * MOVED TO PORT, DO NOT USE USR2
 * USR2 signal used to yield the thread at suspend algorithm
 * 
 
void yield_other_handler(int signum, siginfo_t* info, void* context) {

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

    LDIE(35, "Cannot find Java thread using signal context");
}
*/

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
locate_sigcontext() is not the one being invoked immrdiately. It's 
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

void locate_sigcontext(int signum)
{
    sigcontext *sc, *found_sc;
    uint64 *rbp;
    int i;

    asm("mov  %%rbp,%0" : "=r" (rbp));      // rbp = rbp register

#define SC_SEARCH_WIDTH 3
    for (i = 0; i < SC_SEARCH_WIDTH; i++) {
        sc = (sigcontext *)(rbp + 3 );
        if (sc->rip == ((uint64)exam_point)) {  // found
            sc_nest = i;
            use_ucontext = false;
            found_sc = sc;
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
            uc = (struct ucontext *)rbp[4];
            if ((rbp < (uint64 *)uc) && ((uint64 *)uc < rbp + 0x100)) {
                sc = (sigcontext *)&uc->uc_mcontext;
                if (sc->rip == ((uint64)exam_point)) {  // found
                    sc_nest = i;
                    use_ucontext = true;
                    found_sc = sc;
                    break; 
                }
            }
        }

        rbp = (uint64 *)rbp[0];
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
void abort_handler (int signum, siginfo_t* info, void* context) {
    fprintf(stderr, "SIGABRT in VM code.\n");
    ucontext_t *uc = (ucontext_t *)context;
    Registers regs;
    linux_ucontext_to_regs(&regs, uc);

    // setup default handler
    signal(signum, SIG_DFL);

    if (!is_gdb_crash_handler_enabled() ||
        !gdb_crash_handler())
    {
        // print stack trace
        st_print_stack(&regs);
    }
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
/*
 * MOVED TO PORT, DO NOT USE USR2

    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_SIGINFO | SA_RESTART;
    sa.sa_sigaction = yield_other_handler;
    sigaction(SIGUSR2, &sa, NULL);
*/
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_SIGINFO | SA_ONSTACK;;
    sa.sa_sigaction = &null_java_reference_handler;
    sigaction(SIGSEGV, &sa, NULL);

    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_SIGINFO;
    sa.sa_sigaction = &null_java_divide_by_zero_handler;
    sigaction(SIGFPE, &sa, NULL);
    
    signal(SIGINT, (void (*)(int)) vm_interrupt_handler);
    signal(SIGQUIT, (void (*)(int)) vm_dump_handler);

    /* install abort_handler to print out call stack on assertion failures */
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_SIGINFO;
    sa.sa_sigaction = &abort_handler;
    sigaction( SIGABRT, &sa, NULL);
    /* abort_handler installed */

    // Prepare gdb crash handler
    init_gdb_crash_handler();

} //initialize_signals

void shutdown_signals() {
    //FIXME: should be defined in future
} //shutdown_signals

