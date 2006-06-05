/*
 *  Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/** 
 * @author Alexey V. Varlamov, Evgueni Brevnov
 * @version $Revision: 1.1.2.2.4.4 $
 */  
#ifndef _CXX_LOG_H_
#define _CXX_LOG_H_

#include <assert.h>
#include "logger.h"
#include "loggerstring.h"
#include "log_macro.h"

namespace util
{
    /**
     * Predefined classloader filter.
     */
    const char CLASS_LOGGER[] = "class";
    /**
     * Predefined gc filter.
     */
    const char GC_LOGGER[] = "gc";
    /**
     * Predefined jni filter.
     */
    const char JNI_LOGGER[] = "jni";

} // namespace util

#define LOGGER_EXIT(code) { \
    shutdown_log_system(); \
    ::exit(code); \
}

#define ECHO(message) { \
    LoggerString logger_string; \
    logger_string << message; \
    log4cxx_from_c("root", DIE, logger_string.release(), __FILE__, __LOG4CXX_FUNC__, __LINE__); \
}

#define DIE2(category, message) { \
    LoggerString logger_string; \
    logger_string << message; \
    log4cxx_from_c(category, DIE, logger_string.release(), __FILE__, __LOG4CXX_FUNC__, __LINE__); \
    shutdown_log_system(); \
    ::abort(); \
}

#define WARN2(category, message) { \
    if (is_warn_enabled(category)) { \
    LoggerString logger_string; \
    logger_string << message; \
    log4cxx_from_c(category, WARN, logger_string.release(), __FILE__, __LOG4CXX_FUNC__, __LINE__); \
    } \
}

#define INFO2(category, message) { \
    if (is_info_enabled(category)) { \
    LoggerString logger_string; \
    logger_string << message; \
    log4cxx_from_c(category, INFO, logger_string.release(), __FILE__, __LOG4CXX_FUNC__, __LINE__); \
    } \
}

#ifdef NDEBUG

#if defined(_MSC_VER) && !defined (__INTEL_COMPILER) //MS compiler
#pragma warning (disable:4390) 
#endif

#define LOG2(category, message) 
#define TRACE2(category, message)

#else // NDEBUG

#define LOG2(category, message) { \
    static LogSite logSite = {UNKNOWN, NULL}; \
    if (logSite.state && is_log_enabled(category, &logSite)) { \
        LoggerString logger_string; \
        logger_string << message; \
        log4cxx_from_c(category, LOG, logger_string.release(), __FILE__, __LOG4CXX_FUNC__, __LINE__); \
    } \
}
#define TRACE2(category, message) { \
    static LogSite logSite = {UNKNOWN, NULL}; \
    if (logSite.state && is_trace_enabled(category, &logSite)) { \
        LoggerString logger_string; \
        logger_string << message; \
        log4cxx_from_c(category, TRACE, logger_string.release(), __FILE__, __LOG4CXX_FUNC__, __LINE__); \
    } \
}

#endif // NDEBUG

#ifdef LOG_DOMAIN

#define DIE(message) DIE2(LOG_DOMAIN, message)
#define WARN(message) WARN2(LOG_DOMAIN, message)
#define INFO(message) INFO2(LOG_DOMAIN, message)
#define LOG(message) LOG2(LOG_DOMAIN, message)
#define TRACE(message) TRACE2(LOG_DOMAIN, message)

#ifdef NDEBUG

#define VERIFY(expr, message) { \
    if (!(expr)) { \
    DIE("Assertion failed: " << #expr << "\n" << message); \
    } \
}
#define ASSERT(expr, message)

#else //NDEBUG

#define VERIFY(expr, message) { \
    if (!(expr)) { \
    LoggerString logger_string; \
    logger_string << "Assertion failed: " << #expr << "\n" << message; \
    log4cxx_from_c(LOG_DOMAIN, DIE, logger_string.release(), __FILE__, __LOG4CXX_FUNC__, __LINE__); \
    shutdown_log_system(); \
    assert(expr); \
    } \
}
#define ASSERT(expr, message)  VERIFY(expr, message)

#endif // NDEBUG 

#define PORT_VERIFY_SUCCESS(func) { \
    int ret = func; \
    VERIFY(APR_SUCCESS == ret, \
        "Internal error: a call returned non-zero error code" #func); \
}

#endif // LOG_DOMAIN

#endif // _CXX_LOG_H_

