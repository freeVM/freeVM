/*
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Pavel N. Vyssotski
 * @version $Revision: 1.27 $
 */
// RequestManager.cpp

#include "RequestManager.h"
#include "ThreadManager.h"
#include "EventDispatcher.h"
#include "PacketParser.h"
#include "ClassManager.h"
#include "OptionParser.h"
#include "Log.h"
#include "AgentManager.h"

using namespace jdwp;

void RequestManager::Init(JNIEnv* jni)
    throw(AgentException)
{
    JDWP_TRACE_ENTRY("Init(" << jni << ")");

    m_requestMonitor = new AgentMonitor("_jdwp_RequestManager_monitor");
    m_requestIdCount = 1;
}

void RequestManager::Clean(JNIEnv* jni) throw()
{
    JDWP_TRACE_ENTRY("Clean(" << jni << ")");

    if (m_requestMonitor != 0){
        {
            MonitorAutoLock lock(m_requestMonitor JDWP_FILE_LINE);
        }
        delete m_requestMonitor;
        m_requestMonitor = 0;
    }
    m_requestIdCount = 0;
}

void RequestManager::Reset(JNIEnv* jni) throw()
{
    JDWP_TRACE_ENTRY("Reset(" << jni << ")");

    if (m_requestMonitor != 0) {
        try {
            DeleteAllRequests(jni, JDWP_EVENT_SINGLE_STEP);
            DeleteAllRequests(jni, JDWP_EVENT_BREAKPOINT);
            DeleteAllRequests(jni, JDWP_EVENT_FRAME_POP);
            DeleteAllRequests(jni, JDWP_EVENT_EXCEPTION);
            DeleteAllRequests(jni, JDWP_EVENT_USER_DEFINED);
            DeleteAllRequests(jni, JDWP_EVENT_THREAD_START);
            DeleteAllRequests(jni, JDWP_EVENT_THREAD_END);
            DeleteAllRequests(jni, JDWP_EVENT_CLASS_PREPARE);
            DeleteAllRequests(jni, JDWP_EVENT_CLASS_UNLOAD);
            DeleteAllRequests(jni, JDWP_EVENT_CLASS_LOAD);
            DeleteAllRequests(jni, JDWP_EVENT_FIELD_ACCESS);
            DeleteAllRequests(jni, JDWP_EVENT_FIELD_MODIFICATION);
            DeleteAllRequests(jni, JDWP_EVENT_EXCEPTION_CATCH);
            DeleteAllRequests(jni, JDWP_EVENT_METHOD_ENTRY);
            DeleteAllRequests(jni, JDWP_EVENT_METHOD_EXIT);
            DeleteAllRequests(jni, JDWP_EVENT_VM_DEATH);
        } catch (AgentException& e) {
            JDWP_INFO("JDWP error: " << e.what() << " [" << e.ErrCode() << "]");
        }
        {
            MonitorAutoLock lock(m_requestMonitor JDWP_FILE_LINE);
            m_requestIdCount = 1;
        }
    }
}

void RequestManager::ControlBreakpoint(JNIEnv* jni,
        AgentEventRequest* request, bool enable)
    throw(AgentException)
{
    JDWP_TRACE_ENTRY("ControlBreakpoint(" << jni << ',' << request << ',' << enable << ")");

    LocationOnlyModifier* lom = request->GetLocation();
    if (lom == 0) {
        throw InternalErrorException();
    }
    jclass cls = lom->GetClass();
    jmethodID method = lom->GetMethod();
    jlocation location = lom->GetLocation();
    bool found = false;
    RequestList& rl = GetRequestList(request->GetEventKind());
    for (RequestListIterator i = rl.begin(); i != rl.end(); i++) {
        AgentEventRequest* req = *i;
        LocationOnlyModifier* m = req->GetLocation();
        if (m != 0 && method == m->GetMethod() &&
            location == m->GetLocation() &&
            JNI_TRUE == jni->IsSameObject(cls, m->GetClass()))
        {
            found = true;
            break;
        }
    }
    if (!found) {
        JDWP_TRACE_EVENT("ControlBreakpoint: breakpoint "
            << (enable ? "set" : "clear") << ", loc=" << location);
        jvmtiError err;
        if (enable) {
            JVMTI_TRACE(err, GetJvmtiEnv()->SetBreakpoint(method, location));
        } else {
            JVMTI_TRACE(err, GetJvmtiEnv()->ClearBreakpoint(method, location));
        }
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            char* name = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodName(method, &name, 0, 0));
            JvmtiAutoFree af(name);
            JDWP_TRACE_EVENT("ControlBreakpoint: request: method=" 
                << name << " location=" << location << " enable=" << enable);
        }
#endif // NDEBUG

    }
}

void RequestManager::ControlWatchpoint(JNIEnv* jni,
        AgentEventRequest* request, bool enable)
    throw(AgentException)
{
    JDWP_TRACE_ENTRY("ControlWatchpoint(" << jni << ',' << request << ',' << enable << ")");

    FieldOnlyModifier *fom = request->GetField();
    if (fom == 0) {
        throw InternalErrorException();
    }
    jclass cls = fom->GetClass();
    jfieldID field = fom->GetField();
    bool found = false;
    RequestList& rl = GetRequestList(request->GetEventKind());
    for (RequestListIterator i = rl.begin(); i != rl.end(); i++) {
        AgentEventRequest* req = *i;
        FieldOnlyModifier *m = req->GetField();
        if (m != 0 && field == m->GetField() &&
            JNI_TRUE == jni->IsSameObject(cls, m->GetClass()))
        {
            found = true;
            break;
        }
    }
    if (!found) {
        JDWP_TRACE_EVENT("ControlWatchpoint: watchpoint "
            << GetEventKindName(request->GetEventKind())
            << "[" << request->GetEventKind() << "] "
            << (enable ? "set" : "clear") << ", field=" << field);
        jvmtiError err;
        if (request->GetEventKind() == JDWP_EVENT_FIELD_ACCESS) {
            if (enable) {
                JVMTI_TRACE(err, GetJvmtiEnv()->SetFieldAccessWatch(cls, field));
            } else {
                JVMTI_TRACE(err, GetJvmtiEnv()->ClearFieldAccessWatch(cls, field));
            }
        } else if (request->GetEventKind() == JDWP_EVENT_FIELD_MODIFICATION) {
            if (enable) {
                JVMTI_TRACE(err, GetJvmtiEnv()->SetFieldModificationWatch(cls, field));
            } else {
                JVMTI_TRACE(err, GetJvmtiEnv()->ClearFieldModificationWatch(cls, field));
            }
        } else {
            throw InternalErrorException();
        }
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }
#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            char* name = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetFieldName(cls, field, &name, 0, 0));
            JvmtiAutoFree af(name);
            JDWP_TRACE_EVENT("ControlBreakpoint: request: field=" << name 
                << " kind=" << request->GetEventKind() << " enable=" << enable);
        }
#endif // NDEBUG
    }
}

void RequestManager::ControlEvent(JNIEnv* jni,
        AgentEventRequest* request, bool enable)
    throw(AgentException)
{
    JDWP_TRACE_ENTRY("ControlEvent(" << jni << ',' << request << ',' << enable << ")");

    jvmtiEvent eventType;
    bool nullThreadForSetEventNotificationMode = false;
    switch (request->GetEventKind()) {
    case JDWP_EVENT_SINGLE_STEP:
        // manually controlled inside StepRequest
        //eventType = JVMTI_EVENT_SINGLE_STEP;
        //break;
        return;
    case JDWP_EVENT_BREAKPOINT:
        eventType = JVMTI_EVENT_BREAKPOINT;
        ControlBreakpoint(jni, request, enable);
        break;
    case JDWP_EVENT_FRAME_POP:
        eventType = JVMTI_EVENT_FRAME_POP;
        break;
    case JDWP_EVENT_EXCEPTION:
        eventType = JVMTI_EVENT_EXCEPTION;
        break;
    case JDWP_EVENT_CLASS_PREPARE:
        eventType = JVMTI_EVENT_CLASS_PREPARE;
        break;
    case JDWP_EVENT_CLASS_LOAD:
        eventType = JVMTI_EVENT_CLASS_LOAD;
        break;
    case JDWP_EVENT_FIELD_ACCESS:
        eventType = JVMTI_EVENT_FIELD_ACCESS;
        ControlWatchpoint(jni, request, enable);
        break;
    case JDWP_EVENT_FIELD_MODIFICATION:
        eventType = JVMTI_EVENT_FIELD_MODIFICATION;
        ControlWatchpoint(jni, request, enable);
        break;
    case JDWP_EVENT_EXCEPTION_CATCH:
        eventType = JVMTI_EVENT_EXCEPTION_CATCH;
        break;
    case JDWP_EVENT_METHOD_ENTRY:
        eventType = JVMTI_EVENT_METHOD_ENTRY;
        break;
    case JDWP_EVENT_METHOD_EXIT:
        eventType = JVMTI_EVENT_METHOD_EXIT;
        break;
    case JDWP_EVENT_THREAD_START:
        eventType = JVMTI_EVENT_THREAD_START;
        nullThreadForSetEventNotificationMode = true;
        break;
    case JDWP_EVENT_THREAD_END:
        eventType = JVMTI_EVENT_THREAD_END;
        nullThreadForSetEventNotificationMode = true;
        break;
    default:
        return;
    }

    jthread thread = request->GetThread();
    RequestList& rl = GetRequestList(request->GetEventKind());
    for (RequestListIterator i = rl.begin(); i != rl.end(); i++) {
        if (nullThreadForSetEventNotificationMode) {
            //
            // SetEventNotificationMode() for some events must be called with
            // jthread = 0, even if we need request only for specified thread.
            // Thus, if there is already any request for such events 
            // it is for all threads and SetEventNotificationMode() should not 
            // be called. 
            //
            return;
        }
        AgentEventRequest* req = *i;
        if (JNI_TRUE == jni->IsSameObject(thread, req->GetThread())) {
            // there is similar request, so do nothing
            return;
        }
    }

    JDWP_TRACE_EVENT("ControlEvent: request " << GetEventKindName(request->GetEventKind())
        << "[" << request->GetEventKind() << "] "
        << (enable ? "on" : "off") << ", thread=" << thread);
    jvmtiError err;
    if (nullThreadForSetEventNotificationMode) {
        //
        // SetEventNotificationMode() for some events must be called with
        // jthread = 0, even if we need request only for specified thread.
        // Thus, if request is for such event, SetEventNotificationMode() 
        // should be called with jthread = 0 and generated events will be
        // filtered later 
        //
        thread = 0;
    }
    JVMTI_TRACE(err, GetJvmtiEnv()->SetEventNotificationMode(
        (enable) ? JVMTI_ENABLE : JVMTI_DISABLE, eventType, thread));
    if (err != JVMTI_ERROR_NONE &&
        (err != JVMTI_ERROR_THREAD_NOT_ALIVE || enable))
    {
        throw AgentException(err);
    }
}

RequestList& RequestManager::GetRequestList(jdwpEventKind kind)
    throw(AgentException)
{
    switch (kind) {
    case JDWP_EVENT_SINGLE_STEP:
        return m_singleStepRequests;
    case JDWP_EVENT_BREAKPOINT:
        return m_breakpointRequests;
    case JDWP_EVENT_FRAME_POP:
        return m_framePopRequests;
    case JDWP_EVENT_EXCEPTION:
        return m_exceptionRequests;
    case JDWP_EVENT_USER_DEFINED:
        return m_userDefinedRequests;
    case JDWP_EVENT_THREAD_START:
        return m_threadStartRequests;
    case JDWP_EVENT_THREAD_END:
        return m_threadEndRequests;
    case JDWP_EVENT_CLASS_PREPARE:
        return m_classPrepareRequests;
    case JDWP_EVENT_CLASS_UNLOAD:
        return m_classUnloadRequests;
    case JDWP_EVENT_CLASS_LOAD:
        return m_classLoadRequests;
    case JDWP_EVENT_FIELD_ACCESS:
        return m_fieldAccessRequests;
    case JDWP_EVENT_FIELD_MODIFICATION:
        return m_fieldModificationRequests;
    case JDWP_EVENT_EXCEPTION_CATCH:
        return m_exceptionCatchRequests;
    case JDWP_EVENT_METHOD_ENTRY:
        return m_methodEntryRequests;
    case JDWP_EVENT_METHOD_EXIT:
        return m_methodExitRequests;
    case JDWP_EVENT_VM_DEATH:
        return m_vmDeathRequests;
    default:
        throw AgentException(JDWP_ERROR_INVALID_EVENT_TYPE);
    }
}

void RequestManager::AddInternalRequest(JNIEnv* jni,
        AgentEventRequest* request)
    throw(AgentException)
{
    JDWP_TRACE_EVENT("AddInternalRequest: event="
        << GetEventKindName(request->GetEventKind())
        << "[" << request->GetEventKind()
        << "], modCount=" << request->GetModifierCount()
        << ", policy=" << request->GetSuspendPolicy());
    JDWP_ASSERT(m_requestIdCount > 0);
    RequestList& rl = GetRequestList(request->GetEventKind());
    MonitorAutoLock lock(m_requestMonitor JDWP_FILE_LINE);
    ControlEvent(jni, request, true);
    rl.push_back(request);
}

void RequestManager::EnableInternalStepRequest(JNIEnv* jni, jthread thread) throw(AgentException)
{
    jvmtiError err;
    
#ifndef NDEBUG
    if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
        char* threadName = 0;
        jvmtiThreadInfo threadInfo;
        JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thread, &threadInfo));
        threadName = threadInfo.name;
        JvmtiAutoFree af(threadName);
        JDWP_TRACE_EVENT("EnableInternalStepRequest: thread=" << JDWP_CHECK_NULL(threadName));
    }
#endif // NDEBUG
    
    JVMTI_TRACE(err, GetJvmtiEnv()->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_SINGLE_STEP, thread));
    if (err != JVMTI_ERROR_NONE) {
        throw AgentException(err);
    }
}

void RequestManager::DisableInternalStepRequest(JNIEnv* jni, jthread thread) throw(AgentException)
{
    jvmtiError err;
    
#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            char* threadName = 0;
            jvmtiThreadInfo threadInfo;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thread, &threadInfo));
            threadName = threadInfo.name;
            JvmtiAutoFree af(threadName);
            JDWP_TRACE_EVENT("DisableInternalStepRequest: thread=" << JDWP_CHECK_NULL(threadName));
        }
#endif // NDEBUG
    
    StepRequest* stepRequest = FindStepRequest(jni, thread);
    if (stepRequest != 0) {
        stepRequest->Restore();
    } else {
        JVMTI_TRACE(err, GetJvmtiEnv()->SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_SINGLE_STEP, thread));
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }
    }
}

RequestID RequestManager::AddRequest(JNIEnv* jni, AgentEventRequest* request)
    throw(AgentException)
{
    JDWP_TRACE_EVENT("AddRequest: event="
        << GetEventKindName(request->GetEventKind())
        << "[" << request->GetEventKind()
        << "], req=" << m_requestIdCount
        << ", modCount=" << request->GetModifierCount()
        << ", policy=" << request->GetSuspendPolicy());
    JDWP_ASSERT(m_requestIdCount > 0);
    RequestList& rl = GetRequestList(request->GetEventKind());
    MonitorAutoLock lock(m_requestMonitor JDWP_FILE_LINE);
    ControlEvent(jni, request, true);
    int id = m_requestIdCount++;
    request->SetRequestId(id);
    rl.push_back(request);
    return id;
}

void RequestManager::DeleteRequest(JNIEnv* jni,
         jdwpEventKind kind, RequestID id)
     throw(AgentException)
{
    JDWP_TRACE_EVENT("DeleteRequest: event=" << GetEventKindName(kind)
        << "[" << kind << "], req=" << id);
    RequestList& rl = GetRequestList(kind);
    MonitorAutoLock lock(m_requestMonitor JDWP_FILE_LINE);
    for (RequestListIterator i = rl.begin(); i != rl.end(); i++) {
        AgentEventRequest* req = *i;
        if (id == req->GetRequestId()) {
            rl.erase(i);
            ControlEvent(jni, req, false);
            delete req;
            break;
        }
    }
}

void RequestManager::DeleteRequest(JNIEnv* jni, AgentEventRequest* request)
     throw(AgentException)
{
    JDWP_TRACE_EVENT("DeleteRequest: event="
        << GetEventKindName(request->GetEventKind())
        << "[" << request->GetEventKind()
        << "], req=" << request->GetRequestId());
    RequestList& rl = GetRequestList(request->GetEventKind());
    MonitorAutoLock lock(m_requestMonitor JDWP_FILE_LINE);
    for (RequestListIterator i = rl.begin(); i != rl.end(); i++) {
        if (*i == request) {
            AgentEventRequest* req = *i;
            rl.erase(i);
            ControlEvent(jni, req, false);
            delete req;
            break;
        }
    }
}

void RequestManager::DeleteAllBreakpoints(JNIEnv* jni)
    throw(AgentException)
{
    DeleteAllRequests(jni, JDWP_EVENT_BREAKPOINT);
}

void RequestManager::DeleteAllRequests(JNIEnv* jni, jdwpEventKind eventKind) throw(AgentException)
{
    JDWP_TRACE_EVENT("DeleteAllRequests: event=" << GetEventKindName(eventKind)
        << "[" << eventKind << "]"); 
    RequestList& rl = GetRequestList(eventKind);
    MonitorAutoLock lock(m_requestMonitor JDWP_FILE_LINE);
    while (!rl.empty()) {
        AgentEventRequest* req = rl.back();
        rl.pop_back();
        ControlEvent(jni, req, false);
        if(req != 0)
            delete req;
    }

}

const char*
RequestManager::GetEventKindName(jdwpEventKind kind) const throw()
{
    switch (kind) {
    case JDWP_EVENT_SINGLE_STEP:
        return "SINGLE_STEP";
    case JDWP_EVENT_BREAKPOINT:
        return "BREAKPOINT";
    case JDWP_EVENT_FRAME_POP:
        return "FRAME_POP";
    case JDWP_EVENT_EXCEPTION:
        return "EXCEPTION";
    case JDWP_EVENT_USER_DEFINED:
        return "USER_DEFINED";
    case JDWP_EVENT_THREAD_START:
        return "THREAD_START";
    case JDWP_EVENT_THREAD_END:
        return "THREAD_END";
    case JDWP_EVENT_CLASS_PREPARE:
        return "CLASS_PREPARE";
    case JDWP_EVENT_CLASS_UNLOAD:
        return "CLASS_UNLOAD";
    case JDWP_EVENT_CLASS_LOAD:
        return "CLASS_LOAD";
    case JDWP_EVENT_FIELD_ACCESS:
        return "FIELD_ACCESS";
    case JDWP_EVENT_FIELD_MODIFICATION:
        return "FIELD_MODIFICATION";
    case JDWP_EVENT_EXCEPTION_CATCH:
        return "EXCEPTION_CATCH";
    case JDWP_EVENT_METHOD_ENTRY:
        return "METHOD_ENTRY";
    case JDWP_EVENT_METHOD_EXIT:
        return "METHOD_EXIT";
    case JDWP_EVENT_VM_DEATH:
        return "VM_DEATH";
    default:
        return "UNKNOWN";
    }
}

StepRequest* RequestManager::FindStepRequest(JNIEnv* jni, jthread thread)
    throw(AgentException)
{
    RequestList& rl = GetRequestList(JDWP_EVENT_SINGLE_STEP);
    MonitorAutoLock lock(m_requestMonitor JDWP_FILE_LINE);
    for (RequestListIterator i = rl.begin(); i != rl.end(); i++) {
        StepRequest* req = reinterpret_cast<StepRequest*> (*i);
        if (JNI_TRUE == jni->IsSameObject(thread, req->GetThread())) {
            return req;
        }
    }
    return 0;
}

void RequestManager::DeleteStepRequest(JNIEnv* jni, jthread thread)
    throw(AgentException)
{
    JDWP_TRACE_ENTRY("DeleteStepRequest(" << jni << ',' << thread << ")");

    RequestList& rl = GetRequestList(JDWP_EVENT_SINGLE_STEP);
    MonitorAutoLock lock(m_requestMonitor JDWP_FILE_LINE);
    for (RequestListIterator i = rl.begin(); i != rl.end(); i++) {
        StepRequest* req = reinterpret_cast<StepRequest*> (*i);
        if (JNI_TRUE == jni->IsSameObject(thread, req->GetThread())) {
            JDWP_TRACE_EVENT("DeleteStepRequest: req=" << req->GetRequestId());
            rl.erase(i);
            delete req;
            break;
        }
    }
}

// extract filtered RequestID(s) into list
void RequestManager::GenerateEvents(JNIEnv* jni, EventInfo &eInfo,
        jint &eventCount, RequestID* &eventList, jdwpSuspendPolicy &sp)
    throw(AgentException)
{
    JDWP_TRACE_ENTRY("GenerateEvents(" << jni << ", ...)");

    RequestList& rl = GetRequestList(eInfo.kind);
    MonitorAutoLock lock(m_requestMonitor JDWP_FILE_LINE);
    eventList = reinterpret_cast<RequestID*>
        (GetMemoryManager().Allocate(sizeof(RequestID)*rl.size() JDWP_FILE_LINE));
    for (RequestListIterator i = rl.begin(); i != rl.end();) {
        AgentEventRequest* req = *i;
        if (req->GetModifierCount() <= 0 || req->ApplyModifiers(jni, eInfo)) {
            if (req->GetRequestId() == 0 &&
                eInfo.kind == JDWP_EVENT_METHOD_ENTRY)
            {
                StepRequest* step = FindStepRequest(jni, eInfo.thread);
                if (step != 0) {
                    step->OnMethodEntry(jni, eInfo);
                }
            } else {
                JDWP_TRACE_EVENT("GenerateEvents: event #" << eventCount
                    << ": kind=" << GetEventKindName(eInfo.kind)
                    << ", req=" << req->GetRequestId()
                    << (req->IsExpired() ? " (expired)" : ""));
                if (sp == JDWP_SUSPEND_NONE) {
                    sp = req->GetSuspendPolicy();
                } else if (sp == JDWP_SUSPEND_EVENT_THREAD &&
                           req->GetSuspendPolicy() == JDWP_SUSPEND_ALL) {
                    sp = JDWP_SUSPEND_ALL;
                }
                eventList[eventCount++] = req->GetRequestId();
            }
            if (req->IsExpired()) {
                i = rl.erase(i);
                ControlEvent(jni, req, false);
                delete req;
                continue;
            }
        }
        i++;
    }
}

//-----------------------------------------------------------------------------
// event callbacks
//-----------------------------------------------------------------------------

void JNICALL RequestManager::HandleVMInit(jvmtiEnv *jvmti, JNIEnv *jni, jthread thread)
{
    JDWP_TRACE_ENTRY("HandleVMInit(" << jvmti << ',' << jni << ',' << thread << ')');

    try {
        jdwpSuspendPolicy sp = 
            GetOptionParser().GetSuspend() ? JDWP_SUSPEND_ALL : JDWP_SUSPEND_NONE;
        EventComposer *ec = 
            new EventComposer(GetEventDispatcher().NewId(),
                JDWP_COMMAND_SET_EVENT, JDWP_COMMAND_E_COMPOSITE, sp);
        ec->event.WriteInt(1);
        ec->event.WriteByte(JDWP_EVENT_VM_INIT);
        ec->event.WriteInt(0);
        ec->WriteThread(jni, thread);

        JDWP_TRACE_EVENT("VMInit: post single VM_INIT event");
        GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_VM_INIT);
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in VM_INIT: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

void JNICALL RequestManager::HandleVMDeath(jvmtiEnv* jvmti, JNIEnv* jni)
{
    JDWP_TRACE_ENTRY("HandleVMDeath(" << jvmti << ',' << jni << ')');

    try {
        EventInfo eInfo;
        memset(&eInfo, 0, sizeof(eInfo));
        eInfo.kind = JDWP_EVENT_VM_DEATH;

        jint eventCount = 0;
        RequestID *eventList = 0;
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        GetRequestManager().GenerateEvents(jni, eInfo, eventCount, eventList, sp);
        AgentAutoFree aafEL(eventList JDWP_FILE_LINE);

        // for VM_DEATH event use SUSPEND_POLICY_ALL for any suspension
        if (sp != JDWP_SUSPEND_NONE) {
            sp = JDWP_SUSPEND_ALL;
        }

        // post generated events
        if (eventCount > 0) {
            EventComposer *ec = new EventComposer(GetEventDispatcher().NewId(),
                JDWP_COMMAND_SET_EVENT, JDWP_COMMAND_E_COMPOSITE, sp);
            ec->event.WriteInt(eventCount);
            for (jint i = 0; i < eventCount; i++) {
                ec->event.WriteByte(JDWP_EVENT_VM_DEATH);
                ec->event.WriteInt(eventList[i]);
            }
            ec->SetAutoDeathEvent(true);
            JDWP_TRACE_EVENT("VMDeath: post set of " << eventCount << " events");
            GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_VM_DEATH);
        }
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in VM_DEATH: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

void JNICALL RequestManager::HandleClassPrepare(jvmtiEnv* jvmti, JNIEnv* jni,
        jthread thread, jclass cls)
{
    JDWP_TRACE_ENTRY("HandleClassPrepare(" << jvmti << ',' << jni << ',' << thread << ',' << cls << ')');
    
    bool isAgent = GetThreadManager().IsAgentThread(jni, thread);
    try {
        jvmtiError err;
        EventInfo eInfo;
        memset(&eInfo, 0, sizeof(eInfo));
        eInfo.kind = JDWP_EVENT_CLASS_PREPARE;
        eInfo.thread = thread;
        eInfo.cls = cls;

        JVMTI_TRACE(err, GetJvmtiEnv()->GetClassSignature(eInfo.cls,
            &eInfo.signature, 0));
        JvmtiAutoFree jafSignature(eInfo.signature);
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            jvmtiThreadInfo info;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thread, &info));

            JDWP_TRACE_EVENT("CLASS_PREPARE event:"
                << " class=" << JDWP_CHECK_NULL(eInfo.signature)
                << " thread=" << JDWP_CHECK_NULL(info.name));
        }
#endif // NDEBUG

        jint eventCount = 0;
        RequestID *eventList = 0;
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        GetRequestManager().GenerateEvents(jni, eInfo, eventCount, eventList, sp);
        eInfo.thread = isAgent ? 0 : thread;
        sp = isAgent ? JDWP_SUSPEND_NONE : sp;
    
        AgentAutoFree aafEL(eventList JDWP_FILE_LINE);

        // post generated events
        if (eventCount > 0) {
            jdwpTypeTag typeTag = GetClassManager().GetJdwpTypeTag(cls);
            jint status = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetClassStatus(cls, &status));
            if (err != JVMTI_ERROR_NONE) {
                throw AgentException(err);
            }
            EventComposer *ec = new EventComposer(GetEventDispatcher().NewId(),
                JDWP_COMMAND_SET_EVENT, JDWP_COMMAND_E_COMPOSITE, sp);
            ec->event.WriteInt(eventCount);
            for (jint i = 0; i < eventCount; i++) {
                ec->event.WriteByte(JDWP_EVENT_CLASS_PREPARE);
                ec->event.WriteInt(eventList[i]);
                ec->WriteThread(jni, thread);
                ec->event.WriteByte(typeTag);
                ec->event.WriteReferenceTypeID(jni, cls);
                ec->event.WriteString(eInfo.signature);
                ec->event.WriteInt(status);
            }
            JDWP_TRACE_EVENT("ClassPrepare: post set of " << eventCount << " events");
            GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_CLASS_PREPARE);
        }
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in CLASS_PREPARE: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

void JNICALL RequestManager::HandleThreadEnd(jvmtiEnv* jvmti, JNIEnv* jni,
        jthread thread)
{
    JDWP_TRACE_ENTRY("HandleThreadEnd(" << jvmti << ',' << jni << ',' << thread << ')');

//    if (m_requestIdCount == 0) {
//        return;
//    }

    if (GetThreadManager().IsAgentThread(jni, thread)) {
        return;
    }

    try {
        GetRequestManager().DeleteStepRequest(jni, thread);
        EventInfo eInfo;
        memset(&eInfo, 0, sizeof(eInfo));
        eInfo.kind = JDWP_EVENT_THREAD_END;
        eInfo.thread = thread;

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            jvmtiError err;
            jvmtiThreadInfo info;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thread, &info));
            JDWP_TRACE_EVENT("THREAD_END event: thread=" << JDWP_CHECK_NULL(info.name));
        }
#endif // NDEBUG

        jint eventCount = 0;
        RequestID *eventList = 0;
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        GetRequestManager().GenerateEvents(jni, eInfo, eventCount, eventList, sp);
        AgentAutoFree aafEL(eventList JDWP_FILE_LINE);

        // post generated events
        if (eventCount > 0) {
            EventComposer *ec = new EventComposer(GetEventDispatcher().NewId(),
                JDWP_COMMAND_SET_EVENT, JDWP_COMMAND_E_COMPOSITE, sp);
            ec->event.WriteInt(eventCount);
            for (jint i = 0; i < eventCount; i++) {
                ec->event.WriteByte(JDWP_EVENT_THREAD_END);
                ec->event.WriteInt(eventList[i]);
                ec->WriteThread(jni, thread);
            }
            JDWP_TRACE_EVENT("ThreadEnd: post set of " << eventCount << " events");
            GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_THREAD_END);
        }
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in THREAD_END: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

void JNICALL RequestManager::HandleThreadStart(jvmtiEnv* jvmti, JNIEnv* jni,
        jthread thread)
{
    JDWP_TRACE_ENTRY("HandleThreadStart(" << jvmti << ',' << jni << ',' << thread << ')');

    if (GetThreadManager().IsAgentThread(jni, thread)) {
        return;
    }

    try {
        EventInfo eInfo;
        memset(&eInfo, 0, sizeof(eInfo));
        eInfo.kind = JDWP_EVENT_THREAD_START;
        eInfo.thread = thread;

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            jvmtiError err;
            jvmtiThreadInfo info;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thread, &info));
            JDWP_TRACE_EVENT("THREAD_START event: thread=" << JDWP_CHECK_NULL(info.name));
        }
#endif // NDEBUG

        jint eventCount = 0;
        RequestID *eventList = 0;
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        GetRequestManager().GenerateEvents(jni, eInfo, eventCount, eventList, sp);
        AgentAutoFree aafEL(eventList JDWP_FILE_LINE);

        // post generated events
        if (eventCount > 0) {
            EventComposer *ec = new EventComposer(GetEventDispatcher().NewId(),
                JDWP_COMMAND_SET_EVENT, JDWP_COMMAND_E_COMPOSITE, sp);
            ec->event.WriteInt(eventCount);
            for (jint i = 0; i < eventCount; i++) {
                ec->event.WriteByte(JDWP_EVENT_THREAD_START);
                ec->event.WriteInt(eventList[i]);
                ec->WriteThread(jni, thread);
            }
            JDWP_TRACE_EVENT("ThreadStart: post set of " << eventCount << " events");
            GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_THREAD_START);
        }
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in THREAD_START: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

void JNICALL RequestManager::HandleBreakpoint(jvmtiEnv* jvmti, JNIEnv* jni,
        jthread thread, jmethodID method, jlocation location)
{
    JDWP_TRACE_ENTRY("HandleBreakpoint(" << jvmti << ',' << jni << ',' << thread
        << ',' << method << ',' << location << ')');
    
    // if popFrames process, ignore event
    if (GetThreadManager().IsPopFramesProcess(jni, thread)) {
        return;
    }

    if (GetThreadManager().IsAgentThread(jni, thread)) {
        return;
    }

    try {
        jvmtiError err;
        EventInfo eInfo;
        memset(&eInfo, 0, sizeof(eInfo));
        eInfo.kind = JDWP_EVENT_BREAKPOINT;
        eInfo.thread = thread;
        eInfo.method = method;
        eInfo.location = location;

        JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodDeclaringClass(method,
            &eInfo.cls));
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

        JVMTI_TRACE(err, GetJvmtiEnv()->GetClassSignature(eInfo.cls,
            &eInfo.signature, 0));
        JvmtiAutoFree jafSignature(eInfo.signature);
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            char* name = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodName(eInfo.method, &name, 0, 0));

            jvmtiThreadInfo info;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thread, &info));

            JDWP_TRACE_EVENT("BREAKPOINT event:"
                << " class=" << JDWP_CHECK_NULL(eInfo.signature) 
                << " method=" << JDWP_CHECK_NULL(name) 
                << " location=" << eInfo.location
                << " thread=" << JDWP_CHECK_NULL(info.name));
        }
#endif // NDEBUG

        jint eventCount = 0;
        RequestID *eventList = 0;
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        GetRequestManager().GenerateEvents(jni, eInfo, eventCount, eventList, sp);
        AgentAutoFree aafEL(eventList JDWP_FILE_LINE);

        // post generated events
        if (eventCount > 0) {
            jdwpTypeTag typeTag = GetClassManager().GetJdwpTypeTag(eInfo.cls);
            EventComposer *ec = new EventComposer(GetEventDispatcher().NewId(),
                JDWP_COMMAND_SET_EVENT, JDWP_COMMAND_E_COMPOSITE, sp);
            ec->event.WriteInt(eventCount);
            for (jint i = 0; i < eventCount; i++) {
                ec->event.WriteByte(JDWP_EVENT_BREAKPOINT);
                ec->event.WriteInt(eventList[i]);
                ec->WriteThread(jni, thread);
                ec->event.WriteLocation(jni,
                    typeTag, eInfo.cls, method, location);
            }

            JDWP_TRACE_EVENT("Breakpoint: post set of " << eventCount << " events");
            GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_BREAKPOINT);
        }
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in BREAKPOINT: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

void JNICALL RequestManager::HandleException(jvmtiEnv* jvmti, JNIEnv* jni,
        jthread thread, jmethodID method, jlocation location,
        jobject exception, jmethodID catch_method, jlocation catch_location)
{
    JDWP_TRACE_ENTRY("HandleException(" << jvmti << ',' << jni << ',' << thread
        << ',' << method << ',' << location
        << ',' << exception << ',' << catch_method << ',' << catch_location << ')');

    try {
        jvmtiError err;
        jclass exceptionClass = 0;
        AgentEventRequest* exceptionRequest = 0;

        // if agent was not initialized and this exception is expected, initialize agent
        if (!GetAgentManager().IsStarted()) {
            JDWP_TRACE_PROG("HandleException: initial exception cought");

            // if invocation option onuncaught=y is set, check that exception is uncaught, otherwise return
            if (GetOptionParser().GetOnuncaught() != 0) {
                if (catch_location != 0) {
                    JDWP_TRACE_PROG("HandleException: ignore cougth exception");
                    return;
                }
            }

            // if invocation option onthrow=y is set, check that exception class is expected, otherwise return
            if (GetOptionParser().GetOnthrow() != 0) {

                char* expectedExceptionName = const_cast<char*>(GetOptionParser().GetOnthrow());
                if (expectedExceptionName != 0) {

                    char* exceptionSignature = 0;
                    exceptionClass = jni->GetObjectClass(exception);

                    JVMTI_TRACE(err, jvmti->GetClassSignature(exceptionClass, &exceptionSignature, 0)); 
                    if (err != JVMTI_ERROR_NONE) {
                        throw AgentException(err);
                    }
                    JvmtiAutoFree jafSignature(exceptionSignature);

                    char* exceptionName = GetClassManager().GetClassName(exceptionSignature);
                    JvmtiAutoFree jafName(exceptionName);

                    JDWP_TRACE_PROG("HandleException: exception: class=" << exceptionName 
                         << ", signature=" << exceptionSignature);

                    // compare exception class taking into account similar '/' and '.' delimiters
                    int i;
                    for (i = 0; ; i++) {
                        if (expectedExceptionName[i] != exceptionName[i]) {
                            if ((expectedExceptionName[i] == '.' && exceptionName[i] == '/')
                                    || (expectedExceptionName[i] == '/' && exceptionName[i] == '.')) {
                                 continue;
                            }
                            // ignore not matched exception
                            return;
                        }
                        if (expectedExceptionName[i] == '\0') {
                            // matched exception found
                            break;
                        }
                    }
                }
            }

            // disable catching initial exception and start agent
            JDWP_TRACE_PROG("HandleException: start agent");
            GetAgentManager().DisableInitialExceptionCatch(jvmti, jni);
            GetAgentManager().Start(jvmti, jni);

            // check if VM should be suspended on initial EXCEPTION event
            bool needSuspend = GetOptionParser().GetSuspend();
            if (needSuspend) {
                // add internal EXCEPTION request
                exceptionRequest = new AgentEventRequest(JDWP_EVENT_EXCEPTION, JDWP_SUSPEND_ALL);
                GetRequestManager().AddInternalRequest(jni, exceptionRequest);
            } else {
                return;
            }
        }

        // must be non-agent thread
        if (GetThreadManager().IsAgentThread(jni, thread)) {
            return;
        }

        EventInfo eInfo;
        memset(&eInfo, 0, sizeof(eInfo));
        eInfo.kind = JDWP_EVENT_EXCEPTION;
        eInfo.thread = thread;
        eInfo.method = method;
        eInfo.location = location;

        JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodDeclaringClass(method,
            &eInfo.cls));
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

        JVMTI_TRACE(err, GetJvmtiEnv()->GetClassSignature(eInfo.cls,
            &eInfo.signature, 0));
        JvmtiAutoFree jafSignature(eInfo.signature);
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

        if (exceptionClass != 0) {
            eInfo.auxClass = exceptionClass;
        } else {
            eInfo.auxClass = jni->GetObjectClass(exception);
        }
        JDWP_ASSERT(eInfo.auxClass != 0);

        if (catch_method != 0) {
            eInfo.caught = true;
        }

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            char* name = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodName(eInfo.method, &name, 0, 0));

            jvmtiThreadInfo info;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thread, &info));

            JDWP_TRACE_EVENT("EXCEPTION event:"
                << " class=" << JDWP_CHECK_NULL(eInfo.signature) 
                << " method=" << JDWP_CHECK_NULL(name) 
                << " location=" << eInfo.location
                << " caught=" << (int)eInfo.caught
                << " thread=" << JDWP_CHECK_NULL(info.name));
        }
#endif // NDEBUG

        jint eventCount = 0;
        RequestID *eventList = 0;
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        GetRequestManager().GenerateEvents(jni, eInfo, eventCount, eventList, sp);
        AgentAutoFree aafEL(eventList JDWP_FILE_LINE);

        // post generated events
        if (eventCount > 0) {
            jdwpTypeTag typeTag = GetClassManager().GetJdwpTypeTag(eInfo.cls);
            jclass catchCls = 0;
            jdwpTypeTag catchTypeTag = JDWP_TYPE_TAG_CLASS;
            if (catch_method != 0) {
                JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodDeclaringClass(catch_method,
                    &catchCls));
                if (err != JVMTI_ERROR_NONE) {
                    throw AgentException(err);
                }
                catchTypeTag = GetClassManager().GetJdwpTypeTag(catchCls);
            }
            EventComposer *ec = new EventComposer(GetEventDispatcher().NewId(),
                JDWP_COMMAND_SET_EVENT, JDWP_COMMAND_E_COMPOSITE, sp);
            ec->event.WriteInt(eventCount);
            for (jint i = 0; i < eventCount; i++) {
                ec->event.WriteByte(JDWP_EVENT_EXCEPTION);
                ec->event.WriteInt(eventList[i]);
                ec->WriteThread(jni, thread);
                ec->event.WriteLocation(jni,
                    typeTag, eInfo.cls, method, location);
                ec->event.WriteTaggedObjectID(jni, exception);
                ec->event.WriteLocation(jni,
                    catchTypeTag, catchCls, catch_method, catch_location);
            }
            JDWP_TRACE_EVENT("Exception: post set of " << eventCount << " events");
            GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_EXCEPTION);
        }
        // delete internal EXCEPTION request
        if (exceptionRequest != 0) {
            GetRequestManager().DeleteRequest(jni, exceptionRequest);
        }
      /*  JVMTI_TRACE(err, jvmti->SetEventNotificationMode(
             JVMTI_ENABLE , JVMTI_EVENT_BREAKPOINT, thread));*/
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in EXCEPTION: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

void JNICALL RequestManager::HandleMethodEntry(jvmtiEnv* jvmti, JNIEnv* jni,
        jthread thread, jmethodID method)
{
    JDWP_TRACE_ENTRY("HandleMethodEntry(" << jvmti << ',' << jni << ',' << thread
        << ',' << method << ')');
    
    // if popFrames process, ignore event
    if (GetThreadManager().IsPopFramesProcess(jni, thread)) {
        return;
    }

    // must be non-agent thread
    if (GetThreadManager().IsAgentThread(jni, thread)) {
        return;
    }

    try {
        jvmtiError err;
        EventInfo eInfo;
        memset(&eInfo, 0, sizeof(eInfo));
        eInfo.kind = JDWP_EVENT_METHOD_ENTRY;
        eInfo.thread = thread;

        JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodDeclaringClass(method,
            &eInfo.cls));
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

        JVMTI_TRACE(err, GetJvmtiEnv()->GetClassSignature(eInfo.cls,
            &eInfo.signature, 0));
        JvmtiAutoFree jafSignature(eInfo.signature);
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

        JVMTI_TRACE(err, GetJvmtiEnv()->GetFrameLocation(thread, 0,
            &eInfo.method, &eInfo.location));
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }
        JDWP_ASSERT(method == eInfo.method);

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            char* name = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodName(eInfo.method, &name, 0, 0));

            // don't invoke GetThreadInfo(), it may issue another METHOD_ENTRY event
            JDWP_TRACE_EVENT("METHOD_ENTRY event:"
                << " class=" << JDWP_CHECK_NULL(eInfo.signature) 
                << " method=" << JDWP_CHECK_NULL(name));
        }
#endif // NDEBUG

        jint eventCount = 0;
        RequestID *eventList = 0;
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        GetRequestManager().GenerateEvents(jni, eInfo, eventCount, eventList, sp);
        AgentAutoFree aafEL(eventList JDWP_FILE_LINE);

        // post generated events
        if (eventCount > 0) {
            jdwpTypeTag typeTag = GetClassManager().GetJdwpTypeTag(eInfo.cls);
            EventComposer *ec = new EventComposer(GetEventDispatcher().NewId(),
                JDWP_COMMAND_SET_EVENT, JDWP_COMMAND_E_COMPOSITE, sp);
            ec->event.WriteInt(eventCount);
            for (jint i = 0; i < eventCount; i++) {
                ec->event.WriteByte(JDWP_EVENT_METHOD_ENTRY);
                ec->event.WriteInt(eventList[i]);
                ec->WriteThread(jni, thread);
                ec->event.WriteLocation(jni,
                    typeTag, eInfo.cls, method, eInfo.location);
            }
            JDWP_TRACE_EVENT("MethodEntry: post set of " << eventCount << " events");
            GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_METHOD_ENTRY);
        }
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in METHOD_ENTRY: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

void JNICALL RequestManager::HandleMethodExit(jvmtiEnv* jvmti, JNIEnv* jni,
        jthread thread, jmethodID method, jboolean was_popped_by_exception,
        jvalue return_value)
{
    JDWP_TRACE_ENTRY("HandleMethodExit(" << jvmti << ',' << jni << ',' << thread
        << ',' << method << ',' << was_popped_by_exception << ',' << &return_value << ')');

    // must be non-agent thread
    if (GetThreadManager().IsAgentThread(jni, thread)) {
        return;
    }

    try {
        jvmtiError err;
        EventInfo eInfo;
        memset(&eInfo, 0, sizeof(eInfo));
        eInfo.kind = JDWP_EVENT_METHOD_EXIT;
        eInfo.thread = thread;

        JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodDeclaringClass(method,
            &eInfo.cls));
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

        JVMTI_TRACE(err, GetJvmtiEnv()->GetClassSignature(eInfo.cls,
            &eInfo.signature, 0));
        JvmtiAutoFree jafSignature(eInfo.signature);
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

        JVMTI_TRACE(err, GetJvmtiEnv()->GetFrameLocation(thread, 0,
            &eInfo.method, &eInfo.location));
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }
        JDWP_ASSERT(method == eInfo.method);

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            char* name = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodName(eInfo.method, &name, 0, 0));

            // don't invoke GetThreadInfo(), it may issue another METHOD_EXIT event
            JDWP_TRACE_EVENT("METHOD_EXIT event:"
                << " class=" << JDWP_CHECK_NULL(eInfo.signature) 
                << " method=" << JDWP_CHECK_NULL(name));
        }
#endif // NDEBUG

        jint eventCount = 0;
        RequestID *eventList = 0;
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        GetRequestManager().GenerateEvents(jni, eInfo, eventCount, eventList, sp);
        AgentAutoFree aafEL(eventList JDWP_FILE_LINE);

        // post generated events
        if (eventCount > 0) {
            jdwpTypeTag typeTag = GetClassManager().GetJdwpTypeTag(eInfo.cls);
            EventComposer *ec = new EventComposer(GetEventDispatcher().NewId(),
                JDWP_COMMAND_SET_EVENT, JDWP_COMMAND_E_COMPOSITE, sp);
            ec->event.WriteInt(eventCount);
            for (jint i = 0; i < eventCount; i++) {
                ec->event.WriteByte(JDWP_EVENT_METHOD_EXIT);
                ec->event.WriteInt(eventList[i]);
                ec->WriteThread(jni, thread);
                ec->event.WriteLocation(jni,
                    typeTag, eInfo.cls, method, eInfo.location);
            }
            JDWP_TRACE_EVENT("MethodExit: post set of " << eventCount << " events");
            GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_METHOD_EXIT);
        }
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in METHOD_EXIT: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

void JNICALL RequestManager::HandleFieldAccess(jvmtiEnv* jvmti, JNIEnv* jni,
        jthread thread, jmethodID method, jlocation location,
        jclass field_class, jobject object, jfieldID field)
{
    JDWP_TRACE_ENTRY("HandleFieldAccess(" << jvmti << ',' << jni << ',' << thread
        << ',' << method << ',' << location
        << ',' << field_class << ',' << object << ',' << field << ')');

    // must be non-agent thread
    if (GetThreadManager().IsAgentThread(jni, thread)) {
        return;
    }

    try {
        jvmtiError err;
        EventInfo eInfo;
        memset(&eInfo, 0, sizeof(eInfo));
        eInfo.kind = JDWP_EVENT_FIELD_ACCESS;
        eInfo.thread = thread;
        eInfo.method = method;
        eInfo.location = location;
        eInfo.field = field;
        eInfo.instance = object;
        eInfo.auxClass = field_class;

        JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodDeclaringClass(method,
            &eInfo.cls));
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

        JVMTI_TRACE(err, GetJvmtiEnv()->GetClassSignature(eInfo.cls,
            &eInfo.signature, 0));
        JvmtiAutoFree jafSignature(eInfo.signature);
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            char* fieldName = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodName(eInfo.method, &fieldName, 0, 0));

            char* methodName = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetFieldName(field_class, field, &fieldName, 0, 0));

            jvmtiThreadInfo info;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thread, &info));

            JDWP_TRACE_EVENT("FIELD_ACCESS event:"
                << " class=" << JDWP_CHECK_NULL(eInfo.signature) 
                << " method=" << JDWP_CHECK_NULL(methodName) 
                << " field=" << JDWP_CHECK_NULL(fieldName)
                << " thread=" << JDWP_CHECK_NULL(info.name));
        }
#endif // NDEBUG

        jint eventCount = 0;
        RequestID *eventList = 0;
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        GetRequestManager().GenerateEvents(jni, eInfo, eventCount, eventList, sp);
        AgentAutoFree aafEL(eventList JDWP_FILE_LINE);

        // post generated events
        if (eventCount > 0) {
            jdwpTypeTag typeTag = GetClassManager().GetJdwpTypeTag(eInfo.cls);
            jdwpTypeTag fieldTypeTag =
                GetClassManager().GetJdwpTypeTag(field_class);
            EventComposer *ec = new EventComposer(GetEventDispatcher().NewId(),
                JDWP_COMMAND_SET_EVENT, JDWP_COMMAND_E_COMPOSITE, sp);
            ec->event.WriteInt(eventCount);
            for (jint i = 0; i < eventCount; i++) {
                ec->event.WriteByte(JDWP_EVENT_FIELD_ACCESS);
                ec->event.WriteInt(eventList[i]);
                ec->WriteThread(jni, thread);
                ec->event.WriteLocation(jni,
                    typeTag, eInfo.cls, method, location);
                ec->event.WriteByte(fieldTypeTag);
                ec->event.WriteReferenceTypeID(jni, field_class);
                ec->event.WriteFieldID(jni, field);
                ec->event.WriteTaggedObjectID(jni, object);
            }
            JDWP_TRACE_EVENT("FieldAccess: post set of " << eventCount << " events");
            GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_FIELD_ACCESS);
        }
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in FIELD_ACCESS: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

void JNICALL RequestManager::HandleFieldModification(jvmtiEnv* jvmti,
        JNIEnv* jni, jthread thread, jmethodID method, jlocation location,
        jclass field_class, jobject object, jfieldID field,
        char value_sig, jvalue value)
{
    JDWP_TRACE_ENTRY("HandleFieldModification(" << jvmti << ',' << jni << ',' << thread
        << ',' << method << ',' << location
        << ',' << field_class << ',' << object << ',' << field
        << ',' << value_sig << ',' << &value << ')');

    // must be non-agent thread
    if (GetThreadManager().IsAgentThread(jni, thread)) {
        return;
    }
    try {
        jvmtiError err;
        EventInfo eInfo;
        memset(&eInfo, 0, sizeof(eInfo));
        eInfo.kind = JDWP_EVENT_FIELD_MODIFICATION;
        eInfo.thread = thread;
        eInfo.method = method;
        eInfo.location = location;
        eInfo.field = field;
        eInfo.instance = object;
        eInfo.auxClass = field_class;

        JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodDeclaringClass(method,
            &eInfo.cls));
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

        JVMTI_TRACE(err, GetJvmtiEnv()->GetClassSignature(eInfo.cls,
            &eInfo.signature, 0));
        JvmtiAutoFree jafSignature(eInfo.signature);
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            char* fieldName = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodName(eInfo.method, &fieldName, 0, 0));

            char* methodName = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetFieldName(field_class, field, &fieldName, 0, 0));

            jvmtiThreadInfo info;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thread, &info));

            JDWP_TRACE_EVENT("FIELD_MODIFICATION event:"
                << " class=" << JDWP_CHECK_NULL(eInfo.signature) 
                << " method=" << JDWP_CHECK_NULL(methodName) 
                << " field=" << JDWP_CHECK_NULL(fieldName)
                << " thread=" << JDWP_CHECK_NULL(info.name));
        }
#endif // NDEBUG

        jint eventCount = 0;
        RequestID *eventList = 0;
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        GetRequestManager().GenerateEvents(jni, eInfo, eventCount, eventList, sp);
        AgentAutoFree aafEL(eventList JDWP_FILE_LINE);

        // post generated events
        if (eventCount > 0) {
            jdwpTypeTag typeTag = GetClassManager().GetJdwpTypeTag(eInfo.cls);
            jdwpTypeTag fieldTypeTag =
                GetClassManager().GetJdwpTypeTag(field_class);
            EventComposer *ec = new EventComposer(GetEventDispatcher().NewId(),
                JDWP_COMMAND_SET_EVENT, JDWP_COMMAND_E_COMPOSITE, sp);
            ec->event.WriteInt(eventCount);
            for (jint i = 0; i < eventCount; i++) {
                ec->event.WriteByte(JDWP_EVENT_FIELD_MODIFICATION);
                ec->event.WriteInt(eventList[i]);
                ec->WriteThread(jni, thread);
                ec->event.WriteLocation(jni,
                    typeTag, eInfo.cls, method, location);
                ec->event.WriteByte(fieldTypeTag);
                ec->event.WriteReferenceTypeID(jni, field_class);
                ec->event.WriteFieldID(jni, field);
                ec->event.WriteTaggedObjectID(jni, object);
                jdwpTag valueTag = static_cast<jdwpTag>(value_sig);
                if (valueTag == JDWP_TAG_OBJECT) {
                    valueTag = GetClassManager().GetJdwpTag(jni, value.l);
                }
                ec->event.WriteValue(jni, valueTag, value);
            }
            JDWP_TRACE_EVENT("FieldModification: post set of "
                << eventCount << " events");
            GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_FIELD_MODIFICATION);
        }
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in FIELD_MODIFICATION: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

void JNICALL RequestManager::HandleSingleStep(jvmtiEnv* jvmti, JNIEnv* jni,
        jthread thread, jmethodID method, jlocation location)
{
    JDWP_TRACE_ENTRY("HandleSingleStep(" << jvmti << ',' << jni << ',' << thread
        << ',' << method << ',' << location << ')');

    // if popFrames process, invoke internal handler of step event
    if (GetThreadManager().IsPopFramesProcess(jni, thread)) {
        GetThreadManager().HandleInternalSingleStep(jni, thread, method, location);
        return;
    }

    // must be non-agent thread
    if (GetThreadManager().IsAgentThread(jni, thread)) {
        return;
    }

    try {
        jvmtiError err;
        EventInfo eInfo;
        memset(&eInfo, 0, sizeof(eInfo));
        eInfo.kind = JDWP_EVENT_SINGLE_STEP;
        eInfo.thread = thread;
        eInfo.method = method;
        eInfo.location = location;

        JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodDeclaringClass(method,
            &eInfo.cls));
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

        JVMTI_TRACE(err, GetJvmtiEnv()->GetClassSignature(eInfo.cls,
            &eInfo.signature, 0));
        JvmtiAutoFree jafSignature(eInfo.signature);
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            char* name = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodName(eInfo.method, &name, 0, 0));

            // don't invoke GetThreadInfo(), it may issue another SINGLE_STEP event
            JDWP_TRACE_EVENT("SINGLE_STEP event:" 
                << " class=" << JDWP_CHECK_NULL(eInfo.signature) 
                << " method=" << JDWP_CHECK_NULL(name) 
                << " location=" << eInfo.location);
        }
#endif // NDEBUG

        jint eventCount = 0;
        RequestID *eventList = 0;
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        GetRequestManager().GenerateEvents(jni, eInfo, eventCount, eventList, sp);
        AgentAutoFree aafEL(eventList JDWP_FILE_LINE);

        // post generated events
        if (eventCount > 0) {
            jdwpTypeTag typeTag = GetClassManager().GetJdwpTypeTag(eInfo.cls);
            EventComposer *ec = new EventComposer(GetEventDispatcher().NewId(),
                JDWP_COMMAND_SET_EVENT, JDWP_COMMAND_E_COMPOSITE, sp);
            ec->event.WriteInt(eventCount);
            for (jint i = 0; i < eventCount; i++) {
                ec->event.WriteByte(JDWP_EVENT_SINGLE_STEP);
                ec->event.WriteInt(eventList[i]);
                ec->WriteThread(jni, thread);
                ec->event.WriteLocation(jni,
                    typeTag, eInfo.cls, method, location);
            }
            JDWP_TRACE_EVENT("SingleStep: post set of " << eventCount << " events");
            GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_SINGLE_STEP);
        }
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in SINGLE_STEP: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

void JNICALL RequestManager::HandleFramePop(jvmtiEnv* jvmti, JNIEnv* jni,
        jthread thread, jmethodID method, jboolean was_popped_by_exception)
{
    JDWP_TRACE_ENTRY("HandleFramePop(" << jvmti << ',' << jni << ',' << thread
        << ',' << method << ',' << was_popped_by_exception << ')');

    try {

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_EVENT)) {
            jvmtiError err;
            EventInfo eInfo;
            memset(&eInfo, 0, sizeof(eInfo));
            eInfo.kind = JDWP_EVENT_METHOD_EXIT;
            eInfo.thread = thread;
        
            JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodDeclaringClass(method,
                &eInfo.cls));
            if (err != JVMTI_ERROR_NONE) {
                throw AgentException(err);
            }
        
            JVMTI_TRACE(err, GetJvmtiEnv()->GetClassSignature(eInfo.cls,
                &eInfo.signature, 0));
            JvmtiAutoFree jafSignature(eInfo.signature);
            if (err != JVMTI_ERROR_NONE) {
                throw AgentException(err);
            }
        
            JVMTI_TRACE(err, GetJvmtiEnv()->GetFrameLocation(thread, 0,
                &eInfo.method, &eInfo.location));
            if (err != JVMTI_ERROR_NONE) {
                throw AgentException(err);
            }
            JDWP_ASSERT(method == eInfo.method);

            char* name = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodName(eInfo.method, &name, 0, 0));
            JDWP_TRACE_EVENT("FRAME_POP event:"
                << " class=" << JDWP_CHECK_NULL(eInfo.signature) 
                << " method=" << JDWP_CHECK_NULL(name)
                << " loc=" << eInfo.location
                << " by_exception=" << was_popped_by_exception);
        }
#endif // NDEBUG

        StepRequest* step = GetRequestManager().FindStepRequest(jni, thread);
        if (step != 0) {
            step->OnFramePop(jni);
        }
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in FRAME_POP: " << e.what() << " [" << e.ErrCode() << "]");
    }
}
