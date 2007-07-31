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

/**
 * Enables combining METHOD_EXIT events with other collocated events (METHOD_ENTRY, BREAKPOINT, SINGLE_STEP);
 * Disabled by default to preserve compatibility with RI behavior.
 */
static bool ENABLE_COMBINED_METHOD_EXIT_EVENT = false;

RequestManager::RequestManager() throw()
    : m_requestIdCount(0)
    , m_requestMonitor(0) 
    , m_combinedEventsMonitor(0) 
{}

RequestManager::~RequestManager() throw() 
{}

void RequestManager::Init(JNIEnv* jni) throw(AgentException)
{
    JDWP_TRACE_ENTRY("Init(" << jni << ")");

    m_requestMonitor = new AgentMonitor("_jdwp_RequestManager_requestMonitor");
    m_combinedEventsMonitor = new AgentMonitor("_jdwp_RequestManager_combinedEventsMonitor");;
    m_requestIdCount = 1;
}

void RequestManager::Clean(JNIEnv* jni) throw(AgentException)
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

    if (m_combinedEventsMonitor != 0){
        {
            MonitorAutoLock lock(m_combinedEventsMonitor JDWP_FILE_LINE);
        }
        delete m_combinedEventsMonitor;
        m_combinedEventsMonitor = 0;
    }
}

void RequestManager::Reset(JNIEnv* jni) throw(AgentException)
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

    if (m_combinedEventsMonitor != 0) {
        try {
            DeleteAllCombinedEventsInfo(jni);
        } catch (AgentException& e) {
            JDWP_INFO("JDWP error: " << e.what() << " [" << e.ErrCode() << "]");
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

jint RequestManager::ControlClassUnload(JNIEnv* jni, AgentEventRequest* request, bool enable) 
    throw(AgentException)
{
    JDWP_TRACE_ENTRY("ControlClassUnload");

    if (GetAgentEnv()->extensionEventClassUnload != 0) {
        jvmtiError err;
        JDWP_TRACE_EVENT("ControlClassUnload: class unload callback "
            << "[" << request->GetEventKind() << "] "
            << (enable ? "set" : "clear"));
        JVMTI_TRACE(err, GetJvmtiEnv()->SetExtensionEventCallback(
                GetAgentEnv()->extensionEventClassUnload->extension_event_index, 
                (enable ? reinterpret_cast<jvmtiExtensionEvent>(HandleClassUnload) : 0)));
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }
        return GetAgentEnv()->extensionEventClassUnload->extension_event_index;
    }
    return 0;
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
    case JDWP_EVENT_CLASS_UNLOAD:
        eventType = static_cast<jvmtiEvent>(
            ControlClassUnload(jni, request, enable));
        // avoid standard event enable/disable technique
        return;
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

// --------------------- begin of combined events support ---------------------

CombinedEventsInfo::CombinedEventsInfo() throw ()
{
    JDWP_TRACE_ENTRY("CombinedEventsInfo::CombinedEventsInfo()");

    // initialize empty event lists
    for (int i = 0; i < COMBINED_EVENT_COUNT; i++) {
        m_combinedEventsLists[i].list = 0;
        m_combinedEventsLists[i].count = 0;
        m_combinedEventsLists[i].ignored = 0;
    }
}

CombinedEventsInfo::~CombinedEventsInfo() throw () 
{
    JDWP_TRACE_ENTRY("CombinedEventsInfo::~CombinedEventsInfo()");

    // destroy event lists
    for (int i = 0; i < COMBINED_EVENT_COUNT; i++) {
        if (m_combinedEventsLists[i].list != 0) {
            GetMemoryManager().Free(m_combinedEventsLists[i].list JDWP_FILE_LINE);
        };
    }
}

void CombinedEventsInfo::Init(JNIEnv *jni, EventInfo &eInfo) 
        throw (OutOfMemoryException) 
{
    JDWP_TRACE_ENTRY("CombinedEventsInfo::SetEventInfo(" << jni << ',' << &eInfo << ')');

    // store info about initial event
    m_eInfo = eInfo;
    // create global references to be used during grouping events
    if (m_eInfo.thread != 0) {
        m_eInfo.thread = jni->NewGlobalRef(eInfo.thread); 
        if (m_eInfo.thread == 0) throw OutOfMemoryException(); 
    }
    if (m_eInfo.cls != 0) {
        m_eInfo.cls = jni->NewGlobalRef(eInfo.cls); 
        if (m_eInfo.cls == 0) throw OutOfMemoryException(); 
    }
}

void CombinedEventsInfo::Clean(JNIEnv *jni) throw () 
{
    JDWP_TRACE_ENTRY("CombinedEventsInfo::Clean(" << jni << ')');
    if (m_eInfo.cls != 0) {
        jni->DeleteGlobalRef(m_eInfo.cls);
        m_eInfo.cls = 0;
    }
    if (m_eInfo.thread != 0) {
        jni->DeleteGlobalRef(m_eInfo.thread);
        m_eInfo.thread = 0;
    }
}

jint CombinedEventsInfo::GetEventsCount() const throw () 
{
    jint count = 0;   
    for (int i = 0; i < COMBINED_EVENT_COUNT; i++) {
        count += m_combinedEventsLists[i].count;
    }
    return count;
}

int CombinedEventsInfo::GetIgnoredCallbacksCount() const throw () 
{
    jint count = 0;   
    for (int i = 0; i < COMBINED_EVENT_COUNT; i++) {
        count += m_combinedEventsLists[i].ignored;
    }
    return count;
}

void CombinedEventsInfo::CountOccuredCallback(CombinedEventsKind combinedKind) throw ()
{
    if (m_combinedEventsLists[combinedKind].ignored > 0) {
        m_combinedEventsLists[combinedKind].ignored--;
    }
}

static bool isSameLocation(JNIEnv *jni, EventInfo& eInfo1, EventInfo eInfo2) {
    return (eInfo1.location == eInfo2.location) && (eInfo1.method == eInfo2.method);
}

CombinedEventsInfoList::iterator RequestManager::FindCombinedEventsInfo(JNIEnv *jni, jthread thread) 
    throw(AgentException)
{
    JDWP_TRACE_ENTRY("FindCombinedEventsInfo(" << jni << ')');
    MonitorAutoLock lock(m_combinedEventsMonitor JDWP_FILE_LINE);
    CombinedEventsInfoList::iterator p;
    for (p = m_combinedEventsInfoList.begin(); p != m_combinedEventsInfoList.end(); p++) {
        if (*p != 0 && jni->IsSameObject((*p)->m_eInfo.thread, thread)) {
            break;
        }
    }
    return p;
}

void RequestManager::AddCombinedEventsInfo(JNIEnv *jni, CombinedEventsInfo* info) 
    throw(AgentException)
{
    JDWP_TRACE_ENTRY("FindCombinedEventsInfo(" << jni << ')');
    MonitorAutoLock lock(m_combinedEventsMonitor JDWP_FILE_LINE);
    for (CombinedEventsInfoList::iterator p = m_combinedEventsInfoList.begin(); 
                                    p != m_combinedEventsInfoList.end(); p++) {
        if (*p == 0) {
            *p = info;
            return;
        }
    }
    m_combinedEventsInfoList.push_back(info);
}

void RequestManager::DeleteCombinedEventsInfo(JNIEnv *jni, CombinedEventsInfoList::iterator p) 
    throw(AgentException)
{
    JDWP_TRACE_ENTRY("DeleteCombinedEventsInfo(" << jni << ')');
    MonitorAutoLock lock(m_combinedEventsMonitor JDWP_FILE_LINE);
    if (*p != 0) {
        (*p)->Clean(jni);
        delete *p;
        *p = 0;
    }
}

void RequestManager::DeleteAllCombinedEventsInfo(JNIEnv *jni) 
    throw(AgentException)
{
    JDWP_TRACE_ENTRY("FindCombinedEventsInfo(" << jni << ')');
    MonitorAutoLock lock(m_combinedEventsMonitor JDWP_FILE_LINE);
    for (CombinedEventsInfoList::iterator p = m_combinedEventsInfoList.begin(); 
                                        p != m_combinedEventsInfoList.end(); p++) {
        if (*p != 0) {
            (*p)->Clean(jni);
            delete *p;
            *p = 0;
            return;
        }
    }
}

bool RequestManager::IsPredictedCombinedEvent(JNIEnv *jni, EventInfo& eInfo, 
        CombinedEventsInfo::CombinedEventsKind combinedKind)
    throw(AgentException)
{
            CombinedEventsInfoList::iterator p = 
                    GetRequestManager().FindCombinedEventsInfo(jni, eInfo.thread);

            // check if no combined events info stored for this thread 
            //   -> not ignore this event
            if (p == GetRequestManager().m_combinedEventsInfoList.end()) {
                JDWP_TRACE_EVENT("CheckCombinedEvent: no stored combined events for same location:"
                        << " kind=" << combinedKind
                        << " method=" << eInfo.method
                        << " loc=" << eInfo.location);
                return false;
            }

            // check if stored combined events info is for different location 
            //  -> delete info and not ignore this event
            if (!isSameLocation(jni, eInfo, (*p)->m_eInfo)) 
            {
                JDWP_TRACE_EVENT("CheckCombinedEvent: delete old combined events for different location:"
                        << " kind=" << combinedKind
                        << " method=" << (*p)->m_eInfo.method
                        << " loc=" << (*p)->m_eInfo.location);
                GetRequestManager().DeleteCombinedEventsInfo(jni, p);
                JDWP_TRACE_EVENT("CheckCombinedEvent: handle combined events for new location:"
                        << " kind=" << combinedKind
                        << " method=" << eInfo.method
                        << " loc=" << eInfo.location);
                return false;
            }

            // found conbined events info for this location
            //   -> ignore this event, decrease number of ignored callbacks, and delete info if necessary
            {
                JDWP_TRACE_EVENT("CheckCombinedEvent: ignore predicted combined event for same location:"
                        << " kind=" << combinedKind
                        << " method=" << eInfo.method
                        << " loc=" << eInfo.location);
                (*p)->CountOccuredCallback(combinedKind);

                // delete combined event info if no more callbacks to ignore
                if ((*p)->GetIgnoredCallbacksCount() <= 0) {
                    JDWP_TRACE_EVENT("CheckCombinedEvent: delete handled combined events for same location:"
                        << " kind=" << combinedKind
                        << " method=" << eInfo.method
                        << " loc=" << eInfo.location);
                }
                return true;
            } 
}

EventComposer* RequestManager::CombineEvents(JNIEnv* jni, 
        CombinedEventsInfo* combEventsInfo, jdwpSuspendPolicy sp) 
    throw(AgentException)
{
    JDWP_TRACE_ENTRY("CombineEvents(" << jni << ',' << combEventsInfo << ')');

    jdwpTypeTag typeTag = GetClassManager().GetJdwpTypeTag(combEventsInfo->m_eInfo.cls);
    EventComposer *ec = new EventComposer(GetEventDispatcher().NewId(),
            JDWP_COMMAND_SET_EVENT, JDWP_COMMAND_E_COMPOSITE, sp);

    int combinedEventsCount = combEventsInfo->GetEventsCount();
    JDWP_TRACE_EVENT("CombineEvents:"
            << " events=" << combinedEventsCount
            << " METHOD_ENTRY=" << combEventsInfo->m_combinedEventsLists[CombinedEventsInfo::COMBINED_EVENT_METHOD_ENTRY].count
            << " SINGLE_STEP=" << combEventsInfo->m_combinedEventsLists[CombinedEventsInfo::COMBINED_EVENT_SINGLE_STEP].count
            << " BREAKPOINT=" << combEventsInfo->m_combinedEventsLists[CombinedEventsInfo::COMBINED_EVENT_BREAKPOINT].count
            << " METHOD_EXIT=" << combEventsInfo->m_combinedEventsLists[CombinedEventsInfo::COMBINED_EVENT_METHOD_EXIT].count
            << " ignored=" << combEventsInfo->GetIgnoredCallbacksCount());
    ec->event.WriteInt(combinedEventsCount);
    
    CombinedEventsInfo::CombinedEventsKind combinedKind = CombinedEventsInfo::COMBINED_EVENT_METHOD_ENTRY;
    for (jint i = 0; i < combEventsInfo->m_combinedEventsLists[combinedKind].count; i++) {
        ec->event.WriteByte(JDWP_EVENT_METHOD_ENTRY);
        ec->event.WriteInt(combEventsInfo->m_combinedEventsLists[combinedKind].list[i]);
        ec->WriteThread(jni, combEventsInfo->m_eInfo.thread);
        ec->event.WriteLocation(jni,
            typeTag, combEventsInfo->m_eInfo.cls, combEventsInfo->m_eInfo.method, combEventsInfo->m_eInfo.location);
    }
    
    combinedKind = CombinedEventsInfo::COMBINED_EVENT_SINGLE_STEP;
    for (jint i = 0; i < combEventsInfo->m_combinedEventsLists[combinedKind].count; i++) {
        ec->event.WriteByte(JDWP_EVENT_SINGLE_STEP);
        ec->event.WriteInt(combEventsInfo->m_combinedEventsLists[combinedKind].list[i]);
        ec->WriteThread(jni, combEventsInfo->m_eInfo.thread);
        ec->event.WriteLocation(jni,
            typeTag, combEventsInfo->m_eInfo.cls, combEventsInfo->m_eInfo.method, combEventsInfo->m_eInfo.location);
    }

    combinedKind = CombinedEventsInfo::COMBINED_EVENT_BREAKPOINT;
    for (jint i = 0; i < combEventsInfo->m_combinedEventsLists[combinedKind].count; i++) {
        ec->event.WriteByte(JDWP_EVENT_BREAKPOINT);
        ec->event.WriteInt(combEventsInfo->m_combinedEventsLists[combinedKind].list[i]);
        ec->WriteThread(jni, combEventsInfo->m_eInfo.thread);
        ec->event.WriteLocation(jni,
            typeTag, combEventsInfo->m_eInfo.cls, combEventsInfo->m_eInfo.method, combEventsInfo->m_eInfo.location);
    }

    combinedKind = CombinedEventsInfo::COMBINED_EVENT_METHOD_EXIT;
    for (jint i = 0; i < combEventsInfo->m_combinedEventsLists[combinedKind].count; i++) {
        ec->event.WriteByte(JDWP_EVENT_METHOD_EXIT);
        ec->event.WriteInt(combEventsInfo->m_combinedEventsLists[combinedKind].list[i]);
        ec->WriteThread(jni, combEventsInfo->m_eInfo.thread);
        ec->event.WriteLocation(jni,
            typeTag, combEventsInfo->m_eInfo.cls, combEventsInfo->m_eInfo.method, combEventsInfo->m_eInfo.location);
    } 
    return ec;
}

bool RequestManager::IsMethodEntryLocation(JNIEnv* jni, EventInfo& eInfo) 
    throw(AgentException)
{
    jvmtiError err;
    jlocation start_location;
    jlocation end_location;
    JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodLocation(eInfo.method, &start_location, &end_location));
    if (err != JVMTI_ERROR_NONE) {
        throw AgentException(err);
    }    
    bool isEntry = (start_location == eInfo.location);
    JDWP_TRACE_EVENT("IsMethodEntryLocation: isEntry=" << isEntry 
            << ", location=" << eInfo.location
            << ", start=" << start_location
            << ", end=" << end_location);
    return isEntry;
}

bool RequestManager::IsMethodExitLocation(JNIEnv* jni, EventInfo& eInfo) 
    throw(AgentException)
{
    jvmtiError err;
    jlocation start_location;
    jlocation end_location;
    JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodLocation(eInfo.method, &start_location, &end_location));
    if (err != JVMTI_ERROR_NONE) {
        throw AgentException(err);
    }    
    bool isExit = (end_location == eInfo.location);
    JDWP_TRACE_EVENT("IsMethodExitLocation: isExit=" << isExit 
            << ",location=" << eInfo.location
            << ", start=" << start_location
            << ", end=" << end_location);
    return isExit;
}

// --------------------- end of combined events support -----------------------

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

//void JNICALL RequestManager::HandleClassUnload(jvmtiEnv* jvmti, ...)
void JNICALL RequestManager::HandleClassUnload(jvmtiEnv* jvmti, JNIEnv* jni,
        jthread thread, jclass cls)
{
    JDWP_TRACE_ENTRY("HandleClassUnload(" << jvmti << ',' << jni << ',' << thread << ',' << cls << ')');
    
    try {
        jvmtiError err;
        EventInfo eInfo;
        memset(&eInfo, 0, sizeof(eInfo));
        eInfo.kind = JDWP_EVENT_CLASS_UNLOAD;
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

            JDWP_TRACE_EVENT("CLASS_UNLOAD event:"
                << " class=" << JDWP_CHECK_NULL(eInfo.signature)
                << " thread=" << JDWP_CHECK_NULL(info.name));
        }
#endif // NDEBUG

        jint eventCount = 0;
        RequestID *eventList = 0;
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        GetRequestManager().GenerateEvents(jni, eInfo, eventCount, eventList, sp);
        AgentAutoFree aafEL(eventList JDWP_FILE_LINE);

        bool isAgent = GetThreadManager().IsAgentThread(jni, thread);
        if (isAgent) {
            eInfo.thread = 0;
            sp = JDWP_SUSPEND_NONE;
        }

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
                ec->event.WriteByte(JDWP_EVENT_CLASS_UNLOAD);
                ec->event.WriteInt(eventList[i]);
                ec->WriteThread(jni, thread);
                ec->event.WriteByte(typeTag);
                ec->event.WriteReferenceTypeID(jni, cls);
                ec->event.WriteString(eInfo.signature);
                ec->event.WriteInt(status);
            }
            JDWP_TRACE_EVENT("HandleClassUnload: post set of " << eventCount << " events");
            GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_CLASS_UNLOAD);
        }
    } catch (AgentException& e) {
        JDWP_INFO("JDWP error in CLASS_UNLOAD: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

void JNICALL RequestManager::HandleThreadEnd(jvmtiEnv* jvmti, JNIEnv* jni,
        jthread thread)
{
    JDWP_TRACE_ENTRY("HandleThreadEnd(" << jvmti << ',' << jni << ',' << thread << ')');

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

            JDWP_TRACE_EVENT("THREAD_END event:"
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
            JDWP_TRACE_EVENT("THREAD_START event:"
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
    
    // if is popFrames process, ignore event
    if (GetThreadManager().IsPopFramesProcess(jni, thread)) {
        return;
    }

    // if occured in agent thread, ignore event
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
        CombinedEventsInfo::CombinedEventsKind combinedKind = CombinedEventsInfo::COMBINED_EVENT_BREAKPOINT;

        // if this combined event was already prediced, ignore event
        if (GetRequestManager().IsPredictedCombinedEvent(jni, eInfo, combinedKind)) {
            return;
        }

        JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodDeclaringClass(method, &eInfo.cls));
        if (err != JVMTI_ERROR_NONE) {
            throw AgentException(err);
        }

        JVMTI_TRACE(err, GetJvmtiEnv()->GetClassSignature(eInfo.cls, &eInfo.signature, 0));
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

        // create new info about combined events for this location
        CombinedEventsInfo* combinedEvents = new CombinedEventsInfo();
        combinedEvents->Init(jni, eInfo);
        
        // generate BREAKPOINT events according to existing requests
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        CombinedEventsInfo::CombinedEventsList* events = 
            &combinedEvents->m_combinedEventsLists[combinedKind];
        GetRequestManager().GenerateEvents(jni, eInfo, events->count, events->list, sp);
        JDWP_TRACE_EVENT("HandleBreakpoint: BREAKPOINT events:"
                << " count=" << events->count
                << ", suspendPolicy=" << sp 
                << ", location=" << combinedEvents->m_eInfo.location);

        // if no BREAKPOINT events then return from callback
        if (events->count <= 0) {
            combinedEvents->Clean(jni);
            delete combinedEvents;
            combinedEvents = 0;
            return;
        }

        // check if extra combined events should be generated later: METHOD_EXIT
        {
            // check for METHOD_EXIT events
            if (ENABLE_COMBINED_METHOD_EXIT_EVENT) {
                if (GetRequestManager().IsMethodExitLocation(jni, eInfo)) {
                    combinedKind = CombinedEventsInfo::COMBINED_EVENT_METHOD_EXIT;
                    events = &combinedEvents->m_combinedEventsLists[combinedKind];
                    eInfo.kind = JDWP_EVENT_METHOD_EXIT;
                    // generate extra events
                    GetRequestManager().GenerateEvents(jni, eInfo, events->count, events->list, sp);
                    JDWP_TRACE_EVENT("HandleBreakpoint: METHOD_EXIT events:" 
                        << " count=" << events->count
                        << ", suspendPolicy=" << sp 
                        << ", location=" << combinedEvents->m_eInfo.location);
                    // check if corresponding callback should be ignored
                    if (events->count > 0) {
                        events->ignored = 1;
                    }
                }
            }
        }

        // post all generated events
        EventComposer *ec = GetRequestManager().CombineEvents(jni, combinedEvents, sp);
        JDWP_TRACE_EVENT("HandleBreakpoint: post set of " << combinedEvents->GetEventsCount() << " events");
        GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_BREAKPOINT);

        // store info about combined events if other callbacks should be ignored
        if (combinedEvents->GetIgnoredCallbacksCount() > 0) {
            JDWP_TRACE_EVENT("HandleBreakpoint: store combined events for new location:"
                        << " method=" << eInfo.method
                        << " loc=" << eInfo.location);
            GetRequestManager().AddCombinedEventsInfo(jni, combinedEvents);
        } else {
            combinedEvents->Clean(jni);
            delete combinedEvents;
            combinedEvents = 0;
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
    
    // if is popFrames process, ignore event
    if (GetThreadManager().IsPopFramesProcess(jni, thread)) {
        return;
    }

    // if occured in agent thread, ignore event
    if (GetThreadManager().IsAgentThread(jni, thread)) {
        return;
    }

    try {
        jvmtiError err;
        EventInfo eInfo;
        memset(&eInfo, 0, sizeof(eInfo));
        eInfo.kind = JDWP_EVENT_METHOD_ENTRY;
        eInfo.thread = thread;
        CombinedEventsInfo::CombinedEventsKind combinedKind = CombinedEventsInfo::COMBINED_EVENT_METHOD_ENTRY;

        // if this combined event was already prediced, ignore event
        if (GetRequestManager().IsPredictedCombinedEvent(jni, eInfo, combinedKind)) {
            return;
        }

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

            jvmtiThreadInfo info;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thread, &info));

            JDWP_TRACE_EVENT("METHOD_ENTRY event:"
                << " class=" << JDWP_CHECK_NULL(eInfo.signature) 
                << " method=" << JDWP_CHECK_NULL(name)
                << " loc=" << eInfo.location
                << " thread=" << JDWP_CHECK_NULL(info.name));
        }
#endif // NDEBUG

        // create new info about combined events for this location
        CombinedEventsInfo* combinedEvents = new CombinedEventsInfo();
        combinedEvents->Init(jni, eInfo);
        
        // generate METHOD_ENTRY events according to existing requests
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        CombinedEventsInfo::CombinedEventsList* events = 
            &combinedEvents->m_combinedEventsLists[combinedKind];
        GetRequestManager().GenerateEvents(jni, eInfo, events->count, events->list, sp);
        JDWP_TRACE_EVENT("HandleMethodEntry: METHOD_ENTRY events:"
                << " count=" << events->count
                << ", suspendPolicy=" << sp 
                << ", location=" << combinedEvents->m_eInfo.location);

        // if no METHOD_ENTRY events then return from callback
        if (events->count <= 0) {
            combinedEvents->Clean(jni);
            delete combinedEvents;
            combinedEvents = 0;
            return;
        }

        // check if extra combined events should be generated: SINGLE_STEP, BREAKPOINT, NETHOD_EXIT
        {
            // check for SINGLE_STEP events
            {
                combinedKind = CombinedEventsInfo::COMBINED_EVENT_SINGLE_STEP;
                events = &combinedEvents->m_combinedEventsLists[combinedKind];
                eInfo.kind = JDWP_EVENT_SINGLE_STEP;
                // generate extra events
                GetRequestManager().GenerateEvents(jni, eInfo, events->count, events->list, sp);
                JDWP_TRACE_EVENT("HandleMethodEntry: SINGLE_STEP events:" 
                    << " count=" << events->count
                    << ", suspendPolicy=" << sp 
                    << ", location=" << combinedEvents->m_eInfo.location);
                // check if corresponding callback should be ignored
                if (events->count > 0) {
                    events->ignored = 1;
                }
            }

            // check for BREAKPOINT events
            {
                combinedKind = CombinedEventsInfo::COMBINED_EVENT_BREAKPOINT;
                events = &combinedEvents->m_combinedEventsLists[combinedKind];
                eInfo.kind = JDWP_EVENT_BREAKPOINT;
                // generate extra events
                GetRequestManager().GenerateEvents(jni, eInfo, events->count, events->list, sp);
                JDWP_TRACE_EVENT("HandleMethodEntry: BREAKPOINT events:" 
                    << " count=" << events->count
                    << ", suspendPolicy=" << sp 
                    << ", location=" << combinedEvents->m_eInfo.location);
                // check if corresponding callback should be ignored
                if (events->count > 0) {
                    events->ignored = 1;
                }
            }

            // check for METHOD_EXIT events
            if (ENABLE_COMBINED_METHOD_EXIT_EVENT) {
                if (GetRequestManager().IsMethodExitLocation(jni, eInfo)) {
                    combinedKind = CombinedEventsInfo::COMBINED_EVENT_METHOD_EXIT;
                    events = &combinedEvents->m_combinedEventsLists[combinedKind];
                    eInfo.kind = JDWP_EVENT_METHOD_EXIT;
                    // generate extra events
                    GetRequestManager().GenerateEvents(jni, eInfo, events->count, events->list, sp);
                    JDWP_TRACE_EVENT("HandleMethodEntry: METHOD_EXIT events:" 
                        << " count=" << events->count
                        << ", suspendPolicy=" << sp 
                        << ", location=" << combinedEvents->m_eInfo.location);
                    // check if corresponding callback should be ignored
                    if (events->count > 0) {
                        events->ignored = 1;
                    }
                }
            }
        }

        // post all generated events
        EventComposer *ec = GetRequestManager().CombineEvents(jni, combinedEvents, sp);
        JDWP_TRACE_EVENT("HandleBreakpoint: post set of " << combinedEvents->GetEventsCount() << " events");
        GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_METHOD_ENTRY);

        // store info about combined events if other callbacks should be ignored
        if (combinedEvents->GetIgnoredCallbacksCount() > 0) {
            JDWP_TRACE_EVENT("HandleMethodEntry: store combined events for new location:"
                        << " method=" << eInfo.method
                        << " loc=" << eInfo.location);
            GetRequestManager().AddCombinedEventsInfo(jni, combinedEvents);
        } else {
            combinedEvents->Clean(jni);
            delete combinedEvents;
            combinedEvents = 0;
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
        CombinedEventsInfo::CombinedEventsKind combinedKind = CombinedEventsInfo::COMBINED_EVENT_METHOD_EXIT;

        if (ENABLE_COMBINED_METHOD_EXIT_EVENT) {
            // if this combined event was already prediced, ignore event
            if (GetRequestManager().IsPredictedCombinedEvent(jni, eInfo, combinedKind)) {
                return;
            }
        }

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

            jvmtiThreadInfo info;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thread, &info));

            JDWP_TRACE_EVENT("METHOD_EXIT event:"
                << " class=" << JDWP_CHECK_NULL(eInfo.signature) 
                << " method=" << JDWP_CHECK_NULL(name)
                << " loc=" << eInfo.location
                << " thread=" << JDWP_CHECK_NULL(info.name));
        }
#endif // NDEBUG

        // there are no combined events to be generated after METHOD_EXIT event

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
                << " loc=" << eInfo.location 
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
                << " loc=" << eInfo.location 
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

    // if is popFrames process, invoke internal handler of step event
    if (GetThreadManager().IsPopFramesProcess(jni, thread)) {
        GetThreadManager().HandleInternalSingleStep(jni, thread, method, location);
        return;
    }

    // if in agent thread, ignore event
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
        CombinedEventsInfo::CombinedEventsKind combinedKind = CombinedEventsInfo::COMBINED_EVENT_SINGLE_STEP;

        // if this combined event was already prediced, ignore event
        if (GetRequestManager().IsPredictedCombinedEvent(jni, eInfo, combinedKind)) {
            return;
        }

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

            JDWP_TRACE_EVENT("SINGLE_STEP event:" 
                << " class=" << JDWP_CHECK_NULL(eInfo.signature) 
                << " method=" << JDWP_CHECK_NULL(name) 
                << " loc=" << eInfo.location
                << " thread=" << JDWP_CHECK_NULL(info.name));
        }
#endif // NDEBUG

        // create new info about combined events for this location
        CombinedEventsInfo* combinedEvents = new CombinedEventsInfo();
        combinedEvents->Init(jni, eInfo);
        
        // generate SINGLE_STEP events according to existing requests
        jdwpSuspendPolicy sp = JDWP_SUSPEND_NONE;
        CombinedEventsInfo::CombinedEventsList* events = 
            &combinedEvents->m_combinedEventsLists[combinedKind];
        GetRequestManager().GenerateEvents(jni, eInfo, events->count, events->list, sp);
        JDWP_TRACE_EVENT("HandleSingleStep: SINGLE_STEP events:"
                << " count=" << events->count
                << ", suspendPolicy=" << sp 
                << ", location=" << combinedEvents->m_eInfo.location);

        // if no SINGLE_STEP events then return from callback
        if (events->count <= 0) {
            combinedEvents->Clean(jni);
            delete combinedEvents;
            combinedEvents = 0;
            return;
        }

        // check if extra combined events should be generated: BREAKPOINT, METHOD_EXIT
        {
            // check for BREAKPOINT events
            {
                combinedKind = CombinedEventsInfo::COMBINED_EVENT_BREAKPOINT;
                events = &combinedEvents->m_combinedEventsLists[combinedKind];
                eInfo.kind = JDWP_EVENT_BREAKPOINT;
                // generate extra events
                GetRequestManager().GenerateEvents(jni, eInfo, events->count, events->list, sp);
                JDWP_TRACE_EVENT("HandleSingleStep: BREAKPOINT events:" 
                    << " count=" << events->count
                    << ", suspendPolicy=" << sp 
                    << ", location=" << combinedEvents->m_eInfo.location);
                // check if corresponding callback should be ignored
                if (events->count > 0) {
                    events->ignored = 1;
                }
            }

            // check for METHOD_EXIT events
            if (ENABLE_COMBINED_METHOD_EXIT_EVENT) {
                if (GetRequestManager().IsMethodExitLocation(jni, eInfo)) {
                    combinedKind = CombinedEventsInfo::COMBINED_EVENT_METHOD_EXIT;
                    events = &combinedEvents->m_combinedEventsLists[combinedKind];
                    eInfo.kind = JDWP_EVENT_METHOD_EXIT;
                    // generate extra events
                    GetRequestManager().GenerateEvents(jni, eInfo, events->count, events->list, sp);
                    JDWP_TRACE_EVENT("HandleSingleStep: METHOD_EXIT events:" 
                        << " count=" << events->count
                        << ", suspendPolicy=" << sp 
                        << ", location=" << combinedEvents->m_eInfo.location);
                    // check if corresponding callback should be ignored
                    if (events->count > 0) {
                        events->ignored = 1;
                    }
                }
            }
        }

        // post all generated events
        EventComposer *ec = GetRequestManager().CombineEvents(jni, combinedEvents, sp);
        JDWP_TRACE_EVENT("HandleSingleStep: post set of " << combinedEvents->GetEventsCount() << " events");
        GetEventDispatcher().PostEventSet(jni, ec, JDWP_EVENT_SINGLE_STEP);

        // store info about combined events if other callbacks should be ignored
        if (combinedEvents->GetIgnoredCallbacksCount() > 0) {
            JDWP_TRACE_EVENT("HandleSingleStep: store combined events for new location:"
                        << " method=" << eInfo.method
                        << " loc=" << eInfo.location);
            GetRequestManager().AddCombinedEventsInfo(jni, combinedEvents);
        } else {
            combinedEvents->Clean(jni);
            delete combinedEvents;
            combinedEvents = 0;
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

            jvmtiThreadInfo info;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetThreadInfo(thread, &info));

            char* name = 0;
            JVMTI_TRACE(err, GetJvmtiEnv()->GetMethodName(eInfo.method, &name, 0, 0));
            JDWP_TRACE_EVENT("FRAME_POP event:"
                << " class=" << JDWP_CHECK_NULL(eInfo.signature) 
                << " method=" << JDWP_CHECK_NULL(name)
                << " loc=" << eInfo.location
                << " by_exception=" << was_popped_by_exception
                << " thread=" << JDWP_CHECK_NULL(info.name));
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
