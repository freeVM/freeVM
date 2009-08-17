#include "ClassLoadTraceAgent.h"

using namespace std;

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved)
{
    try{
        bool flag;
        ClassLoadTraceAgent* agent = new ClassLoadTraceAgent();
		agent->Init(vm);
        flag = agent->ParseOptions(options);

		if (flag == false) {

			return JNI_OK;

		}
        agent->RegisterEvent();
        
    } catch (AgentException& e) {
        cout << "Error when enter HandleMethodEntry: " << e.what() << " [" << e.ErrCode() << "]";
		return JNI_ERR;
	}
    
	return JNI_OK;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *vm) {}
