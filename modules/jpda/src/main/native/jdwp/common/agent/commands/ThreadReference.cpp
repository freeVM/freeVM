/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Vitaly A. Provodin
 * @version $Revision: 1.16.2.2 $
 */
#include "ThreadReference.h"
#include "PacketParser.h"
#include "ThreadManager.h"
#include "VirtualMachine.h"
#include "MemoryManager.h"
#include "ClassManager.h"

using namespace jdwp;
using namespace ThreadReference;

//-----------------------------------------------------------------------------
//NameHandler------------------------------------------------------------------

void
ThreadReference::NameHandler::Execute(JNIEnv *jni) throw (AgentException)
{
    jvmtiThreadInfo info;
    info.name = 0;
    jthread thrd = m_cmdParser->command.ReadThreadID(jni);
    JDWP_TRACE_DATA("Name: received: threadID=" << thrd);

    jvmtiError err;
    JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thrd, &info));
    JvmtiAutoFree destroyer(info.name);

    if (err != JVMTI_ERROR_NONE)
        throw AgentException(err);

    JDWP_TRACE_DATA("Name: send: name=" << JDWP_CHECK_NULL(info.name));
    m_cmdParser->reply.WriteString(info.name);
}

//-----------------------------------------------------------------------------
//SuspendHandler----------------------------------------------------------------

void
ThreadReference::SuspendHandler::Execute(JNIEnv *jni) throw(AgentException)
{
    jthread thrd = m_cmdParser->command.ReadThreadID(jni);

    JDWP_TRACE_DATA("Suspend: suspend: threadID=" << thrd);
    GetThreadManager().Suspend(jni, thrd);
}

//-----------------------------------------------------------------------------
//ResumeHandler----------------------------------------------------------------

void
ThreadReference::ResumeHandler::Execute(JNIEnv *jni) throw(AgentException)
{
    jthread thrd = m_cmdParser->command.ReadThreadID(jni);

    JDWP_TRACE_DATA("Resume: resume: threadID=" << thrd);
    GetThreadManager().Resume(jni, thrd);
}

//-----------------------------------------------------------------------------
//StatusHandler----------------------------------------------------------------

void
ThreadReference::StatusHandler::Execute(JNIEnv *jni) throw (AgentException)
{
    jint thread_state;

    jthread thrd = m_cmdParser->command.ReadThreadID(jni);
    JDWP_TRACE_DATA("Status: received: threadID=" << thrd);

    jvmtiError err;
    JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadState(thrd, &thread_state));
    JDWP_TRACE_DATA("Status: threadState=" << hex << thread_state);

    if (err != JVMTI_ERROR_NONE)
        throw AgentException(err);

    jint ret_value;
    jint const THREAD_STATE_SLEEPING =
        JVMTI_THREAD_STATE_SLEEPING | JVMTI_THREAD_STATE_ALIVE;

    if ( (thread_state & THREAD_STATE_SLEEPING) == THREAD_STATE_SLEEPING ) {
        ret_value = JDWP_THREAD_STATUS_SLEEPING;
    } else {
        switch (thread_state & JVMTI_JAVA_LANG_THREAD_STATE_MASK)
        {
        case JVMTI_JAVA_LANG_THREAD_STATE_TERMINATED:
            ret_value = JDWP_THREAD_STATUS_ZOMBIE;
            break;
        case JVMTI_JAVA_LANG_THREAD_STATE_RUNNABLE:
            ret_value = JDWP_THREAD_STATUS_RUNNING;
            break;
        case JVMTI_JAVA_LANG_THREAD_STATE_BLOCKED:
            ret_value = JDWP_THREAD_STATUS_MONITOR;
            break;
        case JVMTI_JAVA_LANG_THREAD_STATE_WAITING:
        case JVMTI_JAVA_LANG_THREAD_STATE_TIMED_WAITING:
            ret_value = JDWP_THREAD_STATUS_WAIT;
            break;
        default:
            JDWP_TRACE_DATA("Status: bad Java thread state: " 
                << hex << thread_state);
            throw InternalErrorException();
        }
    }
    m_cmdParser->reply.WriteInt(ret_value);
    if (thread_state & JVMTI_THREAD_STATE_SUSPENDED)
        m_cmdParser->reply.WriteInt(JDWP_SUSPEND_STATUS_SUSPENDED);
    else
        m_cmdParser->reply.WriteInt(0);
    JDWP_TRACE_DATA("Status: send: status=" << ret_value);
}

//-----------------------------------------------------------------------------
//ThreadGroupHandler-----------------------------------------------------------

void 
ThreadReference::ThreadGroupHandler::Execute(JNIEnv *jni) throw(AgentException)
{
    jvmtiThreadInfo info;
    info.name = 0;

    jthread thrd = m_cmdParser->command.ReadThreadID(jni);
    JDWP_TRACE_DATA("ThreadGroup: received: threadID=" << thrd);

    jvmtiError err;
    JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thrd, &info));
    JvmtiAutoFree destroyer(info.name);

    if (err != JVMTI_ERROR_NONE)
        throw AgentException(err);

    JDWP_TRACE_DATA("ThreadGroup: send: threadGroupID=" << info.thread_group);
    m_cmdParser->reply.WriteThreadGroupID(jni, info.thread_group);
}

//-----------------------------------------------------------------------------
//FramesHandler----------------------------------------------------------------

void
ThreadReference::FramesHandler::Execute(JNIEnv *jni) throw(AgentException)
{
    jvmtiEnv *jvmti = GetJvmtiEnv();

    jthread thrd = m_cmdParser->command.ReadThreadID(jni);
    
    if (!GetThreadManager().IsSuspended(thrd))
        throw AgentException(JVMTI_ERROR_THREAD_NOT_SUSPENDED);
    
    jint startFrame = m_cmdParser->command.ReadInt();
    jint length = m_cmdParser->command.ReadInt();
    JDWP_TRACE_DATA("Frames: received: threadID=" << thrd 
        << ", startFrame=" << startFrame 
        << ", length=" << length);

    jint frameCount;
    jvmtiError err;
    JVMTI_TRACE(err, jvmti->GetFrameCount(thrd, &frameCount));


    if (err != JVMTI_ERROR_NONE)
        throw AgentException(err);

    if (length == -1) {
        length = frameCount - startFrame;
    }

    if (length == 0) {
        JDWP_TRACE_DATA("Frames: frameCount=" << frameCount 
            << ", startFrame=" << startFrame 
            << ", length=" << length);
        m_cmdParser->reply.WriteInt(0);
        return;
    }

    if (startFrame >= frameCount || startFrame < 0)
        throw AgentException(JDWP_ERROR_INVALID_INDEX);

    jint maxFrame = startFrame + length;
    if ( (length < 0) || (maxFrame > frameCount) ) {
        throw AgentException(JDWP_ERROR_INVALID_LENGTH);
    }

    jvmtiFrameInfo *frame_buffer = 
        reinterpret_cast<jvmtiFrameInfo*>(GetMemoryManager().Allocate(
                        sizeof(jvmtiFrameInfo)*frameCount JDWP_FILE_LINE));
    AgentAutoFree destroyer(frame_buffer JDWP_FILE_LINE);

    jint count;
    JVMTI_TRACE(err, jvmti->GetStackTrace(thrd, 0, frameCount, 
                                                 frame_buffer, &count));
    if (err != JVMTI_ERROR_NONE) {
        throw AgentException(err);
    }
    JDWP_ASSERT(count == frameCount);

    m_cmdParser->reply.WriteInt(length);

    JDWP_TRACE_DATA("Frames: frameCount=" << frameCount 
        << ", startFrame=" << startFrame 
        << ", length=" << length 
        << ", maxFrame=" << maxFrame);

    jclass declaring_class;
    jdwpTypeTag typeTag;

    for (jint j = startFrame; j < maxFrame; j++) {
        m_cmdParser->reply.WriteFrameID(jni, thrd, j, frameCount);

        JVMTI_TRACE(err, jvmti->GetMethodDeclaringClass(frame_buffer[j].method,
            &declaring_class));
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

        typeTag = GetClassManager().GetJdwpTypeTag(declaring_class);

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_DATA)) {
            jvmtiThreadInfo info;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thrd, &info));
            JDWP_TRACE_DATA("Frames: send: frame#=" << j 
                << ", threadName=" << info.name 
                << ", loc=" << frame_buffer[j].location 
                << ", methodID=" << frame_buffer[j].method 
                << ", classID=" << declaring_class
                << ", typeTag=" << typeTag);
        }
#endif

        m_cmdParser->reply.WriteLocation(jni, typeTag,
                        declaring_class, frame_buffer[j].method,
                        frame_buffer[j].location);
    }
}

//-----------------------------------------------------------------------------
//FrameCountHandler------------------------------------------------------------

void
ThreadReference::FrameCountHandler::Execute(JNIEnv *jni) throw (AgentException)
{
    jint count;

    jthread thrd = m_cmdParser->command.ReadThreadID(jni);
    JDWP_TRACE_DATA("FrameCount: received: threadID=" << thrd);

    if (!GetThreadManager().IsSuspended(thrd))
        throw AgentException(JVMTI_ERROR_THREAD_NOT_SUSPENDED);

    jvmtiError err;
    JVMTI_TRACE(err, GetJvmtiEnv()->GetFrameCount(thrd, &count));

    if (err != JVMTI_ERROR_NONE)
        throw AgentException(err);

    m_cmdParser->reply.WriteInt(count);
    JDWP_TRACE_DATA("FrameCount: send: count=" << count);
}

//-----------------------------------------------------------------------------
//OwnedMonitorsHandler---------------------------------------------------------

void
ThreadReference::OwnedMonitorsHandler::Execute(JNIEnv *jni) throw(AgentException)
{
    jint count;
    jobject* owned_monitors = 0;
    jthread thrd = m_cmdParser->command.ReadThreadID(jni);
    JDWP_TRACE_DATA("OwnedMonitors: received: threadID=" << thrd);

    jvmtiError err;
    JVMTI_TRACE(err, GetJvmtiEnv()->GetOwnedMonitorInfo(thrd, &count,
        &owned_monitors));
    JvmtiAutoFree destroyer(owned_monitors);

    JDWP_ASSERT(err != JVMTI_ERROR_NULL_POINTER);

    if (err != JVMTI_ERROR_NONE)
        throw AgentException(err);

    JDWP_TRACE_DATA("OwnedMonitors: send: monitors=" << count);
    m_cmdParser->reply.WriteInt(count);
    for (int i = 0; i < count; i++)
    {
        JDWP_TRACE_DATA("OwnedMonitors: send: monitor#=" << i
            << ", objectID=" << owned_monitors[i]);
        m_cmdParser->reply.WriteTaggedObjectID(jni, owned_monitors[i]);
    }
}

//-----------------------------------------------------------------------------
//OwnedMonitorsHandler---------------------------------------------------------

void
ThreadReference::CurrentContendedMonitorHandler::Execute(JNIEnv *jni) throw(AgentException)
{
    jobject monitor;
    jthread thrd = m_cmdParser->command.ReadThreadID(jni);\
    JDWP_TRACE_DATA("CurrentContendedMonitor: received: threadID=" << thrd);
                                                                           
    jvmtiError err;
    JVMTI_TRACE(err, GetJvmtiEnv()->GetCurrentContendedMonitor(thrd, &monitor));

    JDWP_ASSERT(err != JVMTI_ERROR_NULL_POINTER);

    if (err != JVMTI_ERROR_NONE)
        throw AgentException(err);

    JDWP_TRACE_DATA("CurrentContendedMonitor: send: monitor=" << monitor);
    m_cmdParser->reply.WriteTaggedObjectID(jni, monitor);
}

//-----------------------------------------------------------------------------
//StopHandler------------------------------------------------------------------

void
ThreadReference::StopHandler::Execute(JNIEnv *jni) throw(AgentException)
{
    jthread thrd = m_cmdParser->command.ReadThreadID(jni);
    jobject excp = m_cmdParser->command.ReadObjectID(jni);

    JDWP_TRACE_DATA("Stop: stop: threadID=" << thrd 
        << ", throwableID=" << excp);
    GetThreadManager().Stop(jni, thrd, excp);
}

//-----------------------------------------------------------------------------
//InterruptHandler-------------------------------------------------------------

void
ThreadReference::InterruptHandler::Execute(JNIEnv *jni) throw(AgentException)
{
    jthread thrd = m_cmdParser->command.ReadThreadID(jni);

    JDWP_TRACE_DATA("Interrupt: interrupt: threadID=" << thrd);
    GetThreadManager().Interrupt(jni, thrd);
}

//-----------------------------------------------------------------------------
//SuspendCountHandler----------------------------------------------------------

void
ThreadReference::SuspendCountHandler::Execute(JNIEnv *jni)
    throw (AgentException)
{
    jthread thrd = m_cmdParser->command.ReadThreadID(jni);
    JDWP_TRACE_DATA("SuspendCount: received: threadID=" << thrd);
    jint count = GetThreadManager().GetSuspendCount(jni, thrd);

    JDWP_TRACE_DATA("SuspendCount: send: count=" << count);
    m_cmdParser->reply.WriteInt(count);
}

//-----------------------------------------------------------------------------
//OwnedMonitorsStackDepthInfoHandler-------------------------------------------

void
ThreadReference::OwnedMonitorsStackDepthInfoHandler::Execute(JNIEnv *jni)
    throw (AgentException)
{
    // Read thread id from OwnedMonitorsStackDepthInfoHandler command
    jthread thrd = m_cmdParser->command.ReadThreadID(jni);
    JDWP_TRACE_DATA("OwnedMonitorsStackDepthInfo: received: threadID=" << thrd);

    // If the thread is not suspended, throw exception
    if (!GetThreadManager().IsSuspended(thrd))
        throw AgentException(JVMTI_ERROR_THREAD_NOT_SUSPENDED);

    // Invoke jvmti function to attain the expected monitor data
    jvmtiError err;
    jint count;
    jvmtiMonitorStackDepthInfo* pMonitorInfos;
    JVMTI_TRACE(err, GetJvmtiEnv()->GetOwnedMonitorStackDepthInfo(thrd, &count, &pMonitorInfos));
    if (err != JVMTI_ERROR_NONE) {
        // Can be: JVMTI_ERROR_MUST_POSSESS_CAPABILITY, JVMTI_ERROR_INVALID_THREAD
        // JVMTI_ERROR_THREAD_NOT_ALIVE, JVMTI_ERROR_NULL_POINTER 
        throw AgentException(err);
    }
    
    // Must release memeory manually
    JvmtiAutoFree af(pMonitorInfos);

    // Write monitor count to reply package
    JDWP_TRACE_DATA("OwnedMonitorsStackDepthInfo: received: monitor count=" << count);
    m_cmdParser->reply.WriteInt(count);
    
    // Write each monitor and its stack depth to reply package 
    for (int i =0; i < count; i++){
        // Attain monitor and its stack depth from returned data.
        jobject monitor = pMonitorInfos[i].monitor;
        m_cmdParser->reply.WriteTaggedObjectID(jni, monitor);
        JDWP_TRACE_DATA("OwnedMonitorsStackDepthInfo: received: monitor object=" << monitor);
        
        jint stack_depth = pMonitorInfos[i].stack_depth;
        JDWP_TRACE_DATA("OwnedMonitorsStackDepthInfo: received: monitor stack depth=" << stack_depth);
        m_cmdParser->reply.WriteInt(stack_depth);
    }
}

//-----------------------------------------------------------------------------
//ForceEarlyReturnHandler------------------------------------------------------

void
ThreadReference::ForceEarlyReturnHandler::Execute(JNIEnv *jni)
    throw (AgentException)
{
    // Read thread id from ForceEarlyReturnHandler command
    jthread thrd = m_cmdParser->command.ReadThreadID(jni);
    JDWP_TRACE_DATA("ForceEarlyReturn Command: received: threadID = " << thrd);

    // If the thread is not suspended, throw exception
    if (!GetThreadManager().IsSuspended(thrd))
        throw AgentException(JDWP_ERROR_THREAD_NOT_SUSPENDED);

    // Attain return value type from the command
    jdwpTaggedValue taggedValue = m_cmdParser->command.ReadValue(jni);
    JDWP_TRACE_DATA("ForceEarlyReturn Command: received value type:" << taggedValue.tag);
    
    // Invoke relevant jvmti function according to return value's type
    jvmtiError err = JVMTI_ERROR_NONE;      
    switch(taggedValue.tag){
        case JDWP_TAG_OBJECT:
        case JDWP_TAG_ARRAY:
        case JDWP_TAG_STRING:
        case JDWP_TAG_THREAD:
        case JDWP_TAG_THREAD_GROUP:
        case JDWP_TAG_CLASS_LOADER:
        case JDWP_TAG_CLASS_OBJECT:{
            JDWP_TRACE_DATA("ForceEarlyReturn Command: jobject return value:"<< taggedValue.value.l);
            JVMTI_TRACE(err, GetJvmtiEnv()->ForceEarlyReturnObject(thrd, taggedValue.value.l));
            break;
        }
        case JDWP_TAG_BOOLEAN:
        case JDWP_TAG_BYTE:
        case JDWP_TAG_CHAR:
        case JDWP_TAG_SHORT:
        case JDWP_TAG_INT:{
            jint ivalue = 0;
            switch (taggedValue.tag) {
                case JDWP_TAG_BOOLEAN:
                    ivalue = static_cast<jint>(taggedValue.value.z);
                    JDWP_TRACE_DATA("ForceEarlyReturn Command: value=(boolean)" << taggedValue.value.z);
                    break;
                case JDWP_TAG_BYTE:
                    ivalue = static_cast<jint>(taggedValue.value.b);
                    JDWP_TRACE_DATA("ForceEarlyReturn Command: value=(byte)" << taggedValue.value.b);
                    break;
                case JDWP_TAG_CHAR:
                    ivalue = static_cast<jint>(taggedValue.value.c);
                    JDWP_TRACE_DATA("ForceEarlyReturn Command: value=(char)" << taggedValue.value.c);
                    break;
                case JDWP_TAG_SHORT:
                    ivalue = static_cast<jint>(taggedValue.value.s);
                    JDWP_TRACE_DATA("ForceEarlyReturn Command: value=(short)" << taggedValue.value.s);
                    break;
                case JDWP_TAG_INT:
                    ivalue = taggedValue.value.i;
                    JDWP_TRACE_DATA("ForceEarlyReturn Command: value=(int)" << taggedValue.value.i);
                    break;
            }
            JVMTI_TRACE(err, GetJvmtiEnv()->ForceEarlyReturnInt(thrd, ivalue));
            break;
        }
        case JDWP_TAG_LONG:{
            JDWP_TRACE_DATA("ForceEarlyReturn Command: jlong returne value:"<< taggedValue.value.j);
            JVMTI_TRACE(err, GetJvmtiEnv()->ForceEarlyReturnLong(thrd, taggedValue.value.j));
            break;
        }
        case JDWP_TAG_FLOAT:{
            JDWP_TRACE_DATA("ForceEarlyReturn Command: jfloat return value:"<< taggedValue.value.f);
            JVMTI_TRACE(err, GetJvmtiEnv()->ForceEarlyReturnFloat(thrd, taggedValue.value.f));
            break;
            
        }
        case JDWP_TAG_DOUBLE:{
            JDWP_TRACE_DATA("ForceEarlyReturn Command: jdouble return value:"<< taggedValue.value.d);
            JVMTI_TRACE(err, GetJvmtiEnv()->ForceEarlyReturnDouble(thrd, taggedValue.value.d));
            break;
        }
        case JDWP_TAG_VOID:{
            JDWP_TRACE_DATA("ForceEarlyReturn Command: void return value");
            JVMTI_TRACE(err, GetJvmtiEnv()->ForceEarlyReturnVoid(thrd));
            break;
        }
        default:{
            JDWP_TRACE_DATA("ForceEarlyReturn Command: Value's type is not supported " << taggedValue.tag);
            throw AgentException(JDWP_ERROR_INVALID_TAG);
        }
    }
    
    if (err != JVMTI_ERROR_NONE) {
        throw AgentException(err);
    }
    JDWP_TRACE_DATA("ForceEarlyReturn Command finished.");
}

//-----------------------------------------------------------------------------
