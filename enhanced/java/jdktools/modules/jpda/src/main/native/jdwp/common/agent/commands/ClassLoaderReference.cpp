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
#include "ClassLoaderReference.h"
#include "PacketParser.h"
#include "ClassManager.h"
#include "ExceptionManager.h"


using namespace jdwp;
using namespace ClassLoaderReference;

int
ClassLoaderReference::VisibleClassesHandler::Execute(JNIEnv *jni) 
{
    jobject initiating_loader = m_cmdParser->command.ReadObjectIDOrNull(jni);
    JDWP_TRACE(LOG_RELEASE, (LOG_DATA_FL, "VisibleClasses: received: classLoaderObject=%p", initiating_loader));

    jint class_count = 0;
    jclass* classes = 0;

    jvmtiError err;
    JVMTI_TRACE(LOG_DEBUG, err, GetJvmtiEnv()->GetClassLoaderClasses(initiating_loader,
        &class_count, &classes));
    JvmtiAutoFree afv(classes);
    if (err != JVMTI_ERROR_NONE) {
        AgentException e(err);
		JDWP_SET_EXCEPTION(e);
        return err;
    }
    JDWP_ASSERT(classes != 0);
    
    JDWP_TRACE(LOG_RELEASE, (LOG_DATA_FL, "VisibleClasses: classes=%d", class_count));
    m_cmdParser->reply.WriteInt(class_count);

    for (int i = 0; i < class_count; i++) {
        jdwpTypeTag typeTag = AgentBase::GetClassManager().GetJdwpTypeTag(classes[i]);

#ifndef NDEBUG
        if (JDWP_TRACE_ENABLED(LOG_KIND_DATA)) {
            jvmtiError err;
            char* signature = 0;
            JVMTI_TRACE(LOG_DEBUG, err, GetJvmtiEnv()->GetClassSignature(classes[i], &signature, 0));
            JvmtiAutoFree afs(signature);

            JDWP_TRACE(LOG_RELEASE, (LOG_DATA_FL, "VisibleClasses: send: class#=%d, typeTag=%d, typeID=%d, signature=%s", 
                            i, typeTag, classes[i], JDWP_CHECK_NULL(signature)));
        }
#endif

        m_cmdParser->reply.WriteByte((jbyte)typeTag);
        m_cmdParser->reply.WriteReferenceTypeID(jni, classes[i]);
    }

    return JDWP_ERROR_NONE;
}
