/*
    Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

    See the License for the specific language governing permissions and
    limitations under the License.
*/
/**
 * @author Valentin Al. Sitnick
 * @version $Revision: 1.1 $
 *
 */

/* *********************************************************************** */

#include "events.h"
#include "utils.h"
#include "fake.h"

static bool test = false;
static bool util = false;
static bool flag = false;

/* *********************************************************************** */

/**
 * test of function GetOwnedMonitorInfo
 */
void GetOwnedMonitorInfo0101()
{
    //Fake method for docletting only
}

/* *********************************************************************** */

JNIEXPORT jint JNICALL Agent_OnLoad(prms_AGENT_ONLOAD)
{
    check_AGENT_ONLOAD;

    Callbacks CB;

    cb_tstart;
    cb_death;

    AGENT_FOR_EVENTS_TESTS_PART_I; /* events.h */

    jvmtiEvent events[] = { JVMTI_EVENT_THREAD_START, JVMTI_EVENT_VM_DEATH };

    AGENT_FOR_EVENTS_TESTS_PART_II;

    fprintf(stderr, "\n-------------------------------------------------\n");
    fprintf(stderr, "\ntest GetOwnedMonitorInfo0101 is started\n{\n");
    fflush(stderr);

    return JNI_OK;
}

/* *********************************************************************** */

void JNICALL callbackThreadStart(prms_THRD_START)
{
    check_THRD_START;

    if (flag) return;

    jvmtiPhase phase;
    jvmtiThreadInfo tinfo;
    jvmtiError result;
    jint tcount = 0;
    jthread* threads;
    jthread my_thread = NULL;
    jint owned_monitor_count;
    jobject* owned_monitors;

    result = jvmti_env->GetPhase(&phase);
    fprintf(stderr, "\tnative: GetPhase result = %d (must be zero) \n", result);
    fprintf(stderr, "\tnative: current phase is %d (must be 4 (LIVE-phase)) \n", phase);
    if ((result != JVMTI_ERROR_NONE) || (phase != JVMTI_PHASE_LIVE)) return;
    result = jvmti_env->GetThreadInfo(thread, &tinfo);
    fprintf(stderr, "\tnative: GetThreadInfo result = %d (must be zero) \n", result);
    fprintf(stderr, "\tnative: current thread name is %s (must be zero) \n", tinfo.name);
    if (result != JVMTI_ERROR_NONE) return;
    if (strcmp(tinfo.name, "agent")) return;
    fprintf(stderr, "\tnative: test started\n");

    flag = true;
    util = true;

    result = jvmti_env->GetAllThreads(&tcount, &threads);
    fprintf(stderr, "\tnative: GetAllThreads result = %d (must be zero) \n", result);
    if (result != JVMTI_ERROR_NONE) return;

    for ( int i = 0; i < tcount; i++ )
    {
        result = jvmti_env->GetThreadInfo(threads[i], &tinfo);
        fprintf(stderr, "\tnative: GetThreadInfo result = %d (must be zero) \n", result);
        fprintf(stderr, "\tnative: current thread name is %s (must be zero) \n", tinfo.name);
        if (result != JVMTI_ERROR_NONE) continue;
        if (strcmp(tinfo.name, "Owner")) continue;
        my_thread = threads[i];
        fprintf(stderr, "\tnative: tested thread was found = %p\n", my_thread);

        break;
    }

    util = true;
    result = jvmti_env->GetOwnedMonitorInfo(my_thread,
                &owned_monitor_count, &owned_monitors);
    fprintf(stderr, "\tnative: GetOwnedMonitorInfo result = %d (must be zero) \n", result);
    flag = true;
    fprintf(stderr, "\n\tnative: number of waited threads is %d (must be 7)\n",
         owned_monitor_count );
    if ((result == JVMTI_ERROR_NONE) && (owned_monitor_count == 7)) {
        test = true;
        return;
    }
}

void JNICALL callbackVMDeath(prms_VMDEATH)
{
    check_VMDEATH;

    fprintf(stderr, "\n\tTest of function GetOwnedMonitorInfo0101         : ");

    if (test && util)
        fprintf(stderr, " passed \n");
    else
        fprintf(stderr, " failed \n");

    fprintf(stderr, "\n} /* test GetOwnedMonitorInfo0101 is finished */ \n");
    fflush(stderr);
}

/* *********************************************************************** */

