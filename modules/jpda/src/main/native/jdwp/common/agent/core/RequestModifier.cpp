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
 * @version $Revision: 1.3 $
 */
// RequestModifier.cpp

#include "RequestModifier.h"

using namespace jdwp;

// match signature with pattern omitting first 'L' and last ";"
bool RequestModifier::MatchPattern(const char *signature, const char *pattern)
    const throw()
{
    if (signature == 0) {
        return false;
    }

    const size_t signatureLength = strlen(signature);
    if (signatureLength < 2) {
        return false;
    }

    const size_t patternLength = strlen(pattern);
    if (pattern[0] == '*') {
        return (signatureLength > patternLength &&
            strncmp(&pattern[1], &signature[signatureLength-patternLength],
                patternLength-1) == 0);
    } else if (pattern[patternLength-1] == '*') {
        return (strncmp(pattern, &signature[1], patternLength-1) == 0);
    } else {
        return (patternLength == signatureLength-2 &&
            strncmp(pattern, &signature[1], patternLength) == 0);
    }
}

bool SourceNameMatchModifier::Apply(JNIEnv* jni, EventInfo &eInfo) throw()
{
    JDWP_ASSERT(eInfo.cls != 0);
    jclass jvmClass = eInfo.cls;
    try {
        char* sourceDebugExtension = 0;
        char* sourceFileName = 0;
        jvmtiError err;
        // Get source name determined by SourceDebugExtension
        JVMTI_TRACE(err, GetJvmtiEnv()->GetSourceDebugExtension(jvmClass,
            &sourceDebugExtension));

        if (err != JVMTI_ERROR_NONE) {
            // Can be: JVMTI_ERROR_MUST_POSSESS_CAPABILITY,JVMTI_ERROR_ABSENT_INFORMATION,
            // JVMTI_ERROR_INVALID_CLASS, JVMTI_ERROR_NULL_POINTER
            if(err == JVMTI_ERROR_ABSENT_INFORMATION) {                   
                // SourceDebugExtension is absent, get source name from SourceFile
                JVMTI_TRACE(err, GetJvmtiEnv()->GetSourceFileName(jvmClass,
                    &sourceFileName));

                if (err != JVMTI_ERROR_NONE) {
                    // Can be: JVMTI_ERROR_MUST_POSSESS_CAPABILITY, JVMTI_ERROR_ABSENT_INFORMATION,
                    // JVMTI_ERROR_INVALID_CLASS, JVMTI_ERROR_NULL_POINTER
                    throw AgentException(err);
                 }
                 JvmtiAutoFree autoFreeFieldName(sourceFileName);
                 return MatchPatternSourceName(sourceFileName, m_pattern);
                 } else { 
                    throw AgentException(err);
                 }
            }
            JvmtiAutoFree autoFreeDebugExtension(sourceDebugExtension);            
          
            string str(sourceDebugExtension);
            JDWP_TRACE_DATA("JDWP sourceDebugExtension: " << str);
            vector<string> tokens;
            ParseSourceDebugExtension(str, tokens, "\n");
            return MatchPatternSourceName(tokens[1].c_str(), m_pattern);
    } catch (AgentException& e) {
        JDWP_TRACE_DATA("JDWP error in SourceNameMatchModifier.Apply: " << e.what() << " [" << e.ErrCode() << "]");
        return false;
    }
}

// match source name with pattern
bool SourceNameMatchModifier::MatchPatternSourceName(const char *sourcename, const char *pattern)
    const 
{
    JDWP_TRACE_DATA("JDWP in SourceNameMatchModifier::MatchPatternSourceName");
   if(sourcename == 0) {
        return false;
    }

    const size_t sourcenameLength = strlen(sourcename);
    const size_t patternLength = strlen(pattern);

    if((sourcenameLength -  patternLength + 1) < 0) { 
        return false;
    }
    if (pattern[0] == '*') {
        return (strcmp(&pattern[1], &sourcename[sourcenameLength-patternLength+1]) == 0);
    } else if (pattern[patternLength-1] == '*') {
        return (strncmp(pattern, &sourcename[0], patternLength-1) == 0);
    } else {
         return (patternLength == sourcenameLength &&
            strncmp(pattern, &sourcename[0], patternLength) == 0);
    }
}

// parse SourceDebugExtension by "\n" delimiter
void SourceNameMatchModifier::ParseSourceDebugExtension(const string& str, vector<string>& tokens, const string& delimiters)
{
    string::size_type lastPos = str.find_first_not_of(delimiters, 0);
    string::size_type pos = str.find_first_of(delimiters, lastPos);
    while (string::npos != pos || string::npos != lastPos) {
        tokens.push_back(str.substr(lastPos, pos - lastPos));
        lastPos = str.find_first_not_of(delimiters, pos);
        pos = str.find_first_of(delimiters, lastPos);
    }
}
