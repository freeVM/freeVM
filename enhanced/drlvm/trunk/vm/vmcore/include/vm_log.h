/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/** 
 * @author Salikh Zakirov, Alexey V. Varlamov
 * @version $Revision: 1.1.2.2.2.1.2.3 $
 */  
#ifndef _VM_LOG_H_
#define _VM_LOG_H_

#include "loggerstring.h"
#include "object_layout.h"
#include "object_handles.h"
#include "vtable.h"
#include "String_Pool.h"
#include "class_member.h"
#include "Class.h"

/**
 * @file
 * VM-specific enhancements to logger.
 */

/**
 * The convenience method for logging VM Strings.
 */
inline LoggerString& operator<<(LoggerString& log, const String* str) {
    if (str) {
        log << str->bytes; 
    } else {
        log << "<null>";
    }
    return log;
}

/**
 * The convenience method for logging Class instances.
 */
inline LoggerString& operator<<(LoggerString& log, const Class* clss) {
    if (clss) {
        log << clss->get_name(); 
    } else {
        log << "<null class>";
    }
    return log;
}

/**
 * The convenience method for logging jboolean values.
 */
inline LoggerString& operator<<(LoggerString& log, const jboolean b) {
    if (b == JNI_FALSE) {
        log << "false";
    } else if (b == JNI_TRUE) {
        log << "true";
    } else {
        log << "(jboolean) " << ((unsigned) b);
    }

    return log;
}


/**
 * The convenience method for logging Method handles.
 */
inline LoggerString& operator<<(LoggerString& log, const Class_Member* m) {
    if (m) {
        log << m->get_class() << "."
        << m->get_name() 
        << m->get_descriptor();
    } else {
        log << "<null member>";
    }

    return log;
}

/**
 * The convenience method for logging JNI method IDs.
 */
inline LoggerString& operator<<(LoggerString& log, const jmethodID m) {
    return log << reinterpret_cast<const Method*>(m);
}

/**
 * The convenience method for logging managed objects.
 */
inline LoggerString& operator<<(LoggerString& log, /*const*/ ManagedObject* object) {
    assert(!hythread_is_suspend_enabled());
    if (object) {
        log << object->vt()->clss << "@" << (void*) object;
    } else {
        log << "<null object>";
    }
    return log;
}

/**
 * The convenience method for logging JNI object handles.
 */
inline LoggerString& operator<<(LoggerString& log, const jobject jobj) {
    hythread_suspend_disable();
    if (jobj) {
        log << jobj->object;
    } else {
        log << "<null jobject>";
    }
    hythread_suspend_enable();
    return log;
}

#endif // _VM_LOG_H_
