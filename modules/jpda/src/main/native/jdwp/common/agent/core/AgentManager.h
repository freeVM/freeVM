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
 * @file
 * ClassManager.h
 *
 * Provides access to certain standard Java classes.
 */

/**
 * @author Aleksander V. Budniy
 * @version $Revision: 1.10.2.1 $
 */

/**
 * @file
 * ClassManager.h
 *
 * Provides initialization of JDWP agent.
 */

#ifndef _AGENT_MANAGER_H_
#define _AGENT_MANAGER_H_

#include "jni.h"
#include "jvmti.h"
#include "AgentBase.h"

namespace jdwp {

    /**
     * This class provides initialization of JDWP agent.
     */
    class AgentManager : public AgentBase {

    public :
        
        /**
         * A constructor.
         */
        AgentManager() throw() {
            m_isStarted = false;
        }

        /**
         * A destructor.
         */
        ~AgentManager() throw () {
        }
        
        /**
         * Initializes all agent modules.
         */
        void Init(jvmtiEnv *jvmti, JNIEnv *jni) throw (AgentException);

        /**
         * Start all agent threads.
         */
        void Start(jvmtiEnv *jvmti, JNIEnv *jni) throw (AgentException);

        /**
         * Stop all agent threads.
         */
        void Stop(JNIEnv *jni) throw (AgentException);

        /**
         * Clean all agent modules.
         */
        void Clean(JNIEnv *jni) throw (AgentException);

        /*                      
         * Returns started status of this agent.
         */
        bool IsStarted() throw() {
            return m_isStarted;
        }

        /*
         * Sets started status of this agent.
         */
        void SetStarted(bool isStarted) throw() {
            m_isStarted = isStarted;
        }

        /**
         * Enables catching initial EXCEPTION event to launch debugger.
         */
        void EnableInitialExceptionCatch(jvmtiEnv *jvmti, JNIEnv *jni) throw (AgentException);

        /**
         * Disables catching initial EXCEPTION event to launch debugger.
         */
        void DisableInitialExceptionCatch(jvmtiEnv *jvmti, JNIEnv *jni) throw (AgentException);

    private :

        bool volatile m_isStarted;

    }; //class AgentManager

}// namespace jdwp

#endif //_AGENT_MANAGER_H_
