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
 * @author Vitaly A. Provodin, Viacheslav G. Rybalov
 * @version $Revision: 1.18 $
 */

#include "CommandHandler.h"
#include "PacketParser.h"
#include "ThreadManager.h"
#include "EventDispatcher.h"

using namespace jdwp;

//-----------------------------------------------------------------------------

void CommandHandler::ComposeError(const AgentException &e)
{
    m_cmdParser->reply.SetError(e.ErrCode());
}

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

void SyncCommandHandler::Run(JNIEnv *jni_env, CommandParser *cmd) throw(AgentException)
{
    JDWP_TRACE_ENTRY("Sync::Run(" << jni_env << ',' << cmd << ')');

    m_cmdParser = cmd;
    try
    {
        Execute(jni_env);
    }
    catch (const AgentException& e)
    {
        ComposeError(e);
    }
    
    if (cmd->reply.IsPacketInitialized())
    {
        cmd->WriteReply(jni_env);
    }
}

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

AsyncCommandHandler::~AsyncCommandHandler()
{
    if (m_cmdParser != 0)
        delete m_cmdParser;
}

//-----------------------------------------------------------------------------

const char* AsyncCommandHandler::GetThreadName() {
    return "_jdwp_AsyncCommandHandler";
}

//-----------------------------------------------------------------------------

void AsyncCommandHandler::Run(JNIEnv *jni_env, CommandParser *cmd) throw(AgentException)
{
    JDWP_TRACE_ENTRY("Async::Run(" << jni_env << ',' << cmd << ')');

    m_cmdParser = new CommandParser();
    cmd->MoveData(jni_env, m_cmdParser);
    try
    {
        GetThreadManager().RunAgentThread(jni_env, StartExecution, this,
            JVMTI_THREAD_MAX_PRIORITY, GetThreadName());
    }
    catch (const AgentException& e)
    {
        JDWP_ASSERT(e.ErrCode() != JDWP_ERROR_NULL_POINTER);
        JDWP_ASSERT(e.ErrCode() != JDWP_ERROR_INVALID_PRIORITY);

        throw e;
    }
}

//-----------------------------------------------------------------------------

void JNICALL
AsyncCommandHandler::StartExecution(jvmtiEnv* jvmti_env, JNIEnv* jni_env, void* arg)
{
    JDWP_TRACE_ENTRY("Async::StartExecution(" << jvmti_env << ',' << jni_env << ',' << arg << ')');

    AsyncCommandHandler *handler = reinterpret_cast<AsyncCommandHandler *>(arg);

    try 
    {
        handler->Execute(jni_env);
    }
    catch (const AgentException &e)
    {
        handler->ComposeError(e);
    }

    try {
        if (handler->m_cmdParser->reply.IsPacketInitialized())
        {
            JDWP_TRACE_CMD("send reply");
            handler->m_cmdParser->WriteReply(jni_env);
        }

        JDWP_TRACE_CMD("Removing command handler: "
            << handler->m_cmdParser->command.GetCommandSet() << "/"
            << handler->m_cmdParser->command.GetCommand());

        handler->Destroy();
    
    } catch (const AgentException &e) {
        // cannot report error in async thread, just print warning message
        JDWP_INFO("JDWP error in asynchronous command: " << e.what() << " [" << e.ErrCode() << "]");
    }
}

//-----------------------------------------------------------------------------

SpecialAsyncCommandHandler::SpecialAsyncCommandHandler()
{
//    m_monitor = new AgentMonitor("SpecialAsyncCommandHandler monitor");
    m_isInvoked = false;
    m_isReleased = false;
}

SpecialAsyncCommandHandler::~SpecialAsyncCommandHandler()
{
}

void SpecialAsyncCommandHandler::ExecuteDeferredInvoke(JNIEnv *jni)
{
    JDWP_TRACE_ENTRY("Async::ExecuteDeferredInvoke(" << jni << ')');
    ExecuteDeferredFunc(jni);
}

void SpecialAsyncCommandHandler::WaitDeferredInvocation(JNIEnv *jni)
{
    JDWP_TRACE_ENTRY("Async::WaitDeferredInvocation(" << jni << ')');

    GetThreadManager().RegisterInvokeHandler(jni, this);
    GetEventDispatcher().PostInvokeSuspend(jni, this);
}

//-----------------------------------------------------------------------------

jint SpecialAsyncCommandHandler::getArgsNumber(char* sig)
{
    if (sig == 0) return 0;

    jint argsCount = 0;
    const size_t len = strlen(sig);
    for (size_t i = 1; i < len && sig[i] != ')'; i++) {
        while (i < len && sig[i] == '[') i++;
        if (sig[i] == 'L') {
            while (i < len && sig[i] != ';' && sig[i] != ')') i++;
        }
        argsCount++;
    }
    JDWP_TRACE_CMD("sig=" << sig << "(args=" << argsCount);

    return argsCount;
}

jdwpTag SpecialAsyncCommandHandler::getTag(jint index, char* sig)
{
    if (sig == 0) return JDWP_TAG_NONE;

    const size_t len = strlen(sig);
    size_t i;
    for (i = 1; index > 0 && i < len && sig[i] != ')'; i++) {
        while (i < len && sig[i] == '[') i++;
        if (sig[i] == 'L') {
            while (i < len && sig[i] != ';' && sig[i] != ')') i++;
        }
        index--;
    }

    return (index == 0) ? static_cast<jdwpTag>(sig[i]) : JDWP_TAG_NONE;
}

bool SpecialAsyncCommandHandler::getClassNameArg(jint index, char* sig, char* name)
{
    if (sig == 0) return false;

    const size_t len = strlen(sig);
    size_t i;
    for (i = 1; index > 0 && i < len && sig[i] != ')'; i++) {
        while (i < len && sig[i] == '[') i++;
        if (sig[i] == 'L') {
            while (i < len && sig[i] != ';' && sig[i] != ')') i++;
        }
        index--;
    }

    if (index > 0 || (sig[i] != '[' && sig[i] != 'L')) return false;

    size_t j = 0;
    for (bool arrayFlag = false, classFlag = false; i < len; i++) {
        char c = sig[i];
        if (c == '[') {
             if (classFlag) return false;
             arrayFlag = true;
             name[j++] = c;
        } else if (c == 'L') {
             if (classFlag) return false;
             classFlag = true;
             if (arrayFlag) {
                 name[j++] = c;
             }
        } else if (c == ';') {
             if (!classFlag) return false;
             if (arrayFlag) {
                 name[j++] = c;
             }
             break;
        } else {
             name[j++] = c;
             if (arrayFlag && !classFlag) break;
        }
    }
    name[j] = '\0';

    return true;
}

jboolean
SpecialAsyncCommandHandler::IsArgValid(JNIEnv *jni, jint index,
                                       jdwpTaggedValue value, char* sig)
                                       throw(AgentException)
{
    JDWP_TRACE_ENTRY("IsArgValid: index=" << index 
        << ", value.tag=" << value.tag << ", arg tag=" << getTag(index, sig));
    switch (value.tag) {
        case JDWP_TAG_BOOLEAN:
        case JDWP_TAG_BYTE:
        case JDWP_TAG_CHAR:
        case JDWP_TAG_SHORT:
        case JDWP_TAG_INT:
        case JDWP_TAG_LONG:
        case JDWP_TAG_FLOAT:
        case JDWP_TAG_DOUBLE:
            if (value.tag != getTag(index, sig)) {
                return JNI_FALSE;
            } else {
                return JNI_TRUE;
            }
        case JDWP_TAG_ARRAY:
            if ('[' != getTag(index, sig)) {
                return JNI_FALSE;
            }
            break;
        case JDWP_TAG_OBJECT:
        case JDWP_TAG_STRING:
        case JDWP_TAG_THREAD:
        case JDWP_TAG_THREAD_GROUP:
        case JDWP_TAG_CLASS_LOADER:
        case JDWP_TAG_CLASS_OBJECT:
            if ('L' != getTag(index, sig)) {
                return JNI_FALSE;
            }
            break;
        default: 
            return JNI_FALSE;
    }
    char* name = reinterpret_cast<char*>(GetMemoryManager().Allocate(strlen(sig) JDWP_FILE_LINE));
    AgentAutoFree afv(name JDWP_FILE_LINE);
    if (!getClassNameArg(index, sig, name)) {
        return JNI_FALSE;
    }
    JDWP_TRACE_CMD("IsArgValid: name =" << JDWP_CHECK_NULL(name));
    jclass cls = jni->FindClass(name);
    if (jni->ExceptionCheck() == JNI_TRUE) {
        jni->ExceptionClear();
        return JNI_FALSE;
    }
    JDWP_TRACE_CMD("IsArgValid: class=" << cls);
    return jni->IsInstanceOf(value.value.l, cls);
}
