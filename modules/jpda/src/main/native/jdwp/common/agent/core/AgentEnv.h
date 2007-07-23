/*
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, 
 * as applicable.
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
 * @version $Revision: 1.4.2.1 $
 */

/**
 * @file
 * AgentEnv.h
 *
 */

#ifndef _AGENT_ENV_H_
#define _AGENT_ENV_H_

#include "jni.h"
#include "jvmti.h"
#include "jdwpTypes.h"

namespace jdwp {

    class MemoryManager;
    class LogManager;
    class OptionParser;
    class ClassManager;
    class ObjectManager;
    class ThreadManager;
    class TransportManager;
    class PacketDispatcher;
    class EventDispatcher;
    class RequestManager;
    class AgentManager;

    /**
     * Agent-environment structure containing all objects participating in
     * the JDWP realization.
     */
    struct AgentEnv {

        AgentManager *agentManager;
        MemoryManager *memoryManager;
        LogManager *logManager;
        OptionParser *optionParser;
        ClassManager *classManager;
        ObjectManager *objectManager;
        ThreadManager *threadManager;
        TransportManager *transportManager;
        PacketDispatcher *packetDispatcher;
        EventDispatcher *eventDispatcher;
        RequestManager *requestManager;

        jvmtiEnv *jvmti;
        JavaVM *jvm;

        jdwpCapabilities caps;
        bool volatile isDead;
    };
}

#endif // _AGENT_ENV_H_
