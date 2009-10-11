/**
 * @author Petr Ivanov
 *
 */

/* *********************************************************************** */

#include "events.h"
#include "utils.h"
#include "ncai.h"

#ifdef POSIX
#include <stdlib.h>
#include <time.h>
#else
#include <windows.h>
#include <CRTDBG.H>
#endif

#ifdef POSIX
#define SLEEP_UNIVERSAL(_x_)  { timespec delay = {(_x_)/1000, 1000000*((_x_)%1000)}; nanosleep(&delay, NULL); }
#else // #ifdef POSIX
#define SLEEP_UNIVERSAL(_x_)  Sleep((_x_))
#endif // #ifdef POSIX

static int g_stop_thread = 0;
static volatile int g_resume_agent_thread = 0;
static jthread g_thread_jthread;
//static jthread g_agent_thread;

//volatile int no_opt = 1; //used to prevent inlining;

//static int counter = 0;

static bool test = false;
static bool util = false;
static bool flag = false;

const char test_case_name[] = "SuspendThread03n";
/*
void Test1(JNIEnv *env, jobject obj);
void Test2(JNIEnv *env, jobject obj);
void Test3(JNIEnv *env, jobject obj);
void JNICALL test_function(jvmtiEnv*, JNIEnv*, void*);*/
static bool CheckSuspend(ncaiEnv*, ncaiThread);

extern "C" JNIEXPORT void JNICALL
Java_ncai_funcs_SuspendThread03n_resumeagent(JNIEnv *env, jclass cls)
{
    //warning fix
    int w_fix = sizeof(cls);
    w_fix += sizeof(env);
    //

    g_resume_agent_thread = 1;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_ncai_funcs_SuspendThread03n_stopsignal(JNIEnv *env, jclass cls)
{
    //warning fix
    int w_fix = sizeof(cls);
    w_fix += sizeof(env);
    //

    return g_stop_thread ? true : false;
}

extern "C" JNIEXPORT void JNICALL Java_ncai_funcs_SuspendThread03n_TestFunction1
  (JNIEnv *env, jobject obj)
{
    fprintf(stderr, "thread - native TestFunction1\n");

    //warning fix
    int w_fix = sizeof(obj);
    w_fix += sizeof(env);
    //

    g_resume_agent_thread = 1;
    while(!g_stop_thread)
    {
        fprintf(stderr, "thread... \n");
        SLEEP_UNIVERSAL(100);
    }

    return;
}

void JNICALL ThreadStart(jvmtiEnv *jvmti_env,
            JNIEnv* jni_env,
            jthread thread)
{
    jvmtiPhase phase;
    jvmtiError result;
    jvmtiThreadInfo tinfo;

    result = jvmti_env->GetPhase(&phase);
    if (result != JVMTI_ERROR_NONE || phase != JVMTI_PHASE_LIVE)
        return;

    result = jvmti_env->GetThreadInfo(thread, &tinfo);
    if (result != JVMTI_ERROR_NONE)
        return;

    if (strcmp(tinfo.name, "java_thread") != 0)
        return;

    printf("ThreadStart: java_thread\n");
    g_thread_jthread = jni_env->NewGlobalRef(thread);
/*
    jclass clazz = jni_env->FindClass("java/lang/Thread");
    if (!clazz)
    {
        fprintf(stderr, "\tnative: JNI: FindClass failed\n");
        return;
    }

    jmethodID mid = jni_env->GetMethodID(clazz, "<init>", "()V");
    if (!mid)
    {
        fprintf(stderr, "\tnative: JNI: GetMethodID failed\n");
        return;
    }

    g_agent_thread = jni_env->NewObject(clazz, mid, "native_agent_thread");
    if (!g_agent_thread)
    {
        fprintf(stderr, "\tnative: JNI: NewObject failed\n");
        return;
    }

    g_agent_thread = jni_env->NewGlobalRef(g_agent_thread);
    result = jvmti_env->GetThreadInfo(g_agent_thread, &tinfo);
    if (result != JVMTI_ERROR_NONE)
    {
        fprintf(stderr, "\tnative: JNI: GetThreadInfo failed\n");
    }

    result = jvmti_env->RunAgentThread(g_agent_thread, test_function, NULL, JVMTI_THREAD_NORM_PRIORITY);
    if (result != JVMTI_ERROR_NONE)
    {
        fprintf(stderr, "\tnative: jvmti: RunAgentThread failed\n");
        return;
    }*/
}

/* *********************************************************************** */

JNIEXPORT jint JNICALL Agent_OnLoad(prms_AGENT_ONLOAD)
{

    Callbacks CB;
    CB.cbThreadStart = &ThreadStart;
    check_AGENT_ONLOAD;
    jvmtiEvent events[] = { JVMTI_EVENT_EXCEPTION, JVMTI_EVENT_THREAD_START, JVMTI_EVENT_VM_DEATH };
    cb_exc;
    cb_death;
    return func_for_Agent_OnLoad(vm, options, reserved, &CB,
        events, sizeof(events)/sizeof(jvmtiEvent), test_case_name, DEBUG_OUT);
}

/* *********************************************************************** */

void JNICALL callbackException(jvmtiEnv *jvmti_env, JNIEnv* jni_env,
                               jthread thread, jmethodID method,
                               jlocation location, jobject exception,
                               jmethodID catch_method, jlocation catch_location)
{
    check_EXCPT;
    if (flag) return;

    /*
     * Function separate all other exceptions in all other method
     */
    if (!check_phase_and_method_debug(jvmti_env, method, SPP_LIVE_ONLY,
                "special_method", DEBUG_OUT)) return;

    flag = true;
    util = true;

    fprintf(stderr, "agent... \n");
    SLEEP_UNIVERSAL(300);
    ////////////////////ncai env get
    jvmtiError err;
    ncaiError ncai_err;

    jvmtiExtensionFunctionInfo* ext_info = NULL;
    jint ext_count = 0;

    err = jvmti_env->GetExtensionFunctions(&ext_count, &ext_info);

    if (err != JVMTI_ERROR_NONE)
    {
        fprintf(stderr, "test_function: GetExtensionFunctions() returned error: %d, '%s'\n",
            err, get_jvmti_eror_text(err));
        test = false;
        return;
    }

    fprintf(stderr, "agent... \n");
    if (ext_count == 0 || ext_info == NULL)
    {
        fprintf(stderr, "test_function: GetExtensionFunctions() returned no extensions\n");
        test = false;
        return;
    }

    jvmtiExtensionFunction get_ncai_func = NULL;

    fprintf(stderr, "agent... \n");
    for (int k = 0; k < ext_count; k++)
    {
        if (strcmp(ext_info[k].id, "org.apache.harmony.vm.GetExtensionEnv") == 0)
        {
            get_ncai_func = ext_info[k].func;
            break;
        }
    }

    fprintf(stderr, "agent... \n");
    if (get_ncai_func == NULL)
    {
        fprintf(stderr, "test_function: GetNCAIEnvironment() nas not been found among JVMTI extensions\n");
        test = false;
        return;
    }

    ncaiEnv* ncai_env = NULL;

    fprintf(stderr, "agent... \n");
    err = get_ncai_func(jvmti_env, &ncai_env, NCAI_VERSION_1_0);

    if (err != JVMTI_ERROR_NONE)
    {
        fprintf(stderr, "test_function: get_ncai_func() returned error: %d, '%s'\n",
            err, get_jvmti_eror_text(err));
        test = false;
        return;
    }

    if (ncai_env == NULL)
    {
        fprintf(stderr, "test_function: get_ncai_func() returned NULL environment\n");
        test = false;
        return;
    }
    fprintf(stderr, "agent... \n");
    ///////////////////////////////////
    ncaiThread ncai_thread;

    fprintf(stderr, "calling ncai->GetThreadHandle()...\n");
    ncai_err = ncai_env->GetThreadHandle(g_thread_jthread, &ncai_thread);
    if (ncai_err != NCAI_ERROR_NONE)
    {
        fprintf(stderr, "ncai->GetThreadHandle() returned error: %d", ncai_err);
        test = false;
        return;
    }

    while(!g_resume_agent_thread)
            SLEEP_UNIVERSAL(200);

    g_resume_agent_thread = 0;

    test = CheckSuspend(ncai_env, ncai_thread);

    g_stop_thread = 1;
    SLEEP_UNIVERSAL(500);
}

void JNICALL callbackVMDeath(prms_VMDEATH)
{
    check_VMDEATH;
    func_for_callback_VMDeath(jni_env, jvmti_env, test_case_name, test, util);
}

/* *********************************************************************** */

bool CheckSuspend(ncaiEnv *ncai_env, ncaiThread ncai_thread)
{
    //warning fix
    int w_fix = 0;
    w_fix += sizeof(ncai_thread);
    //

    ncaiError ncai_err = ncai_env->SuspendThread(NULL);
    if (ncai_err != NCAI_ERROR_INVALID_THREAD)
    {
        fprintf(stderr, "ncai->SuspendThread() returned invalid error: %d,\n", ncai_err);
        return false;
    }

    return true;
}
