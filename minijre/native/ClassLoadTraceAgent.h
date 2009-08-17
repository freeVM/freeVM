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
