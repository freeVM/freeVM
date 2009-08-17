#include <string>
#include <string.h>

#include "ClassLoadTraceAgent.h"

using namespace std;

jvmtiEnv* ClassLoadTraceAgent::m_jvmti = 0;
char* ClassLoadTraceAgent::m_option = 0;
ofstream* ClassLoadTraceAgent::out = 0;

ClassLoadTraceAgent::~ClassLoadTraceAgent() throw(AgentException)
{
    // Free allocated memory
    m_jvmti->Deallocate(reinterpret_cast<unsigned char*>(m_option));
	out->close();
}

void ClassLoadTraceAgent::Init(JavaVM *vm) const throw(AgentException){
    jvmtiEnv *jvmti = 0;
	jint ret = (vm)->GetEnv(reinterpret_cast<void**>(&jvmti), JVMTI_VERSION_1_0);
	if (ret != JNI_OK || jvmti == 0) {
		throw AgentException(JVMTI_ERROR_INTERNAL);
	}
	m_jvmti = jvmti;
}

bool ClassLoadTraceAgent::ParseOptions(const char* str) const throw(AgentException)
{
    if (str == 0)
        return false;
	const size_t len = strlen(str);
	if (len == 0) 
		return false;

  	// Copy str to m_filepath
	jvmtiError error;
    error = m_jvmti->Allocate(len + 1,reinterpret_cast<unsigned char**>(&m_option));
	CheckException(error);
    strcpy(m_option, str);
	if (strcmp(m_option, "help") == 0) {
		cout << "Usage: java -agentlib:tracer=[help]|[output=filename]" << endl;
		return false;
	}
	string option(m_option);
	out = new ofstream(option.substr(option.find('=') + 1).c_str());
	if (!out->is_open()) {
		throw AgentException(JVMTI_ERROR_INTERNAL);
	}
	
	*out << "Class Name Set File\n" << endl;
	return true;
}
  
void ClassLoadTraceAgent::RegisterEvent() const throw(AgentException)
{
    // Create a new callback
    jvmtiEventCallbacks callbacks;
    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.ClassLoad = &ClassLoadTraceAgent::HandleClassLoad;
    
    // Set callback
    jvmtiError error;
    error = m_jvmti->SetEventCallbacks(&callbacks, static_cast<jint>(sizeof(callbacks)));
	CheckException(error);

	// Set event notification mode
	error = m_jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_LOAD, 0);
	CheckException(error);
}

void JNICALL ClassLoadTraceAgent::HandleClassLoad(jvmtiEnv *jvmti_env, JNIEnv* jni_env, jthread thread, jclass klass)
{
	try {
        jvmtiError error;
		char* signature;
        
        // Get class signature
        error = m_jvmti->GetClassSignature(klass, &signature, 0);
        CheckException(error);
		
		transform(signature);
		
		*out << signature << endl;

        error = m_jvmti->Deallocate(reinterpret_cast<unsigned char*>(signature));
		CheckException(error);

	} catch (AgentException& e) {
		cout << "Error when enter HandleMethodEntry: " << e.what() << " [" << e.ErrCode() << "]";
    }
}


