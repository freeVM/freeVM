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

#include <iostream>
#include <fstream>
#include <string>

#include "jvmti.h"

using namespace std;

class AgentException 
{
 public:
	AgentException(jvmtiError err) {
		m_error = err;
	}

	const char* what() const throw() { 
		return "AgentException";
	}

	jvmtiError ErrCode() const throw() {
		return m_error;
	}

 private:
	jvmtiError m_error;
};


class ClassLoadTraceAgent 
{
 public:

	ClassLoadTraceAgent() throw(AgentException){}

	~ClassLoadTraceAgent() throw(AgentException);

	void Init(JavaVM *vm) const throw(AgentException);
        
	bool ParseOptions(const char* str) const throw(AgentException);
        
	void RegisterEvent() const throw(AgentException);
    
	static void JNICALL HandleClassLoad(jvmtiEnv *jvmti_env, JNIEnv* jni_env, jthread thread, jclass klass);

 private:
	
	static void transform(char *sig) {
		int i = 0;
		while (sig[i] != '\0') {
			if (sig[i+1] == '/')
				sig[i] = '.';
			else
				sig[i] = sig[i + 1];
			i++;
		}
		sig[i-2] = '\0';
	}
	 
	static void CheckException(jvmtiError error) throw(AgentException)
	{
		if (error != JVMTI_ERROR_NONE) {
			throw AgentException(error);
		}
	}
    
	static jvmtiEnv* m_jvmti;
	
	static char* m_option;
	
	static ofstream* out;
	
};
