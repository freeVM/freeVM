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
 * @author Viacheslav G. Rybalov
 */

/**
 * @file
 * ClassLoaderReference.h
 *
 */

#ifndef _CLASSLOADERREFERENCE_H_
#define _CLASSLOADERREFERENCE_H_

#include "AgentException.h"
#include "CommandHandler.h"

namespace jdwp {

    /**
     * The namespace includes declaration of the classes implementing commands
     * from the <code>ClassLoaderReference</code> command set.
     */
    namespace ClassLoaderReference {

        /**
         * The class implements the <code>VisibleClasses</code> command from the
         * <code>ClassLoaderReference</code> command set.
         */
        class VisibleClassesHandler : public SyncCommandHandler {
        protected:

            /**
             * Executes the <code>VisibleClasses</code> JDWP command for the
             * <code>ClassLoaderReference</code> command set.
             *
             * @param jni - the JNI interface pointer
             */
            virtual void Execute(JNIEnv *jni) throw(AgentException);

        };//VisibleClassesHandler

    } // ClassLoaderReference

} //jdwp

#endif //_CLASSLOADERREFERENCE_H_
