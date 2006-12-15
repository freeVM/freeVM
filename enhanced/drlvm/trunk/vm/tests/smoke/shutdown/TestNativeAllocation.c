
#include "TestNativeAllocation.h"

/*
 * Class:     shutdown_TestNativeAllocation_WorkerThread
 * Method:    callJNI
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_shutdown_TestNativeAllocation_00024WorkerThread_callJNI
  (JNIEnv * jni_env, jobject thread)
{
    static int allocated = 0;
    jclass thread_class;
    jmethodID methID;

    ++allocated;
    thread_class = (*jni_env)->GetObjectClass(jni_env, thread);
    methID = (*jni_env)->GetMethodID(jni_env, thread_class, "calledFromJNI", "()V");
    if (methID == NULL) {
        --allocated;
        if (allocated == 0) {
            printf("PASSED");
        }
        return;
    }

    (*jni_env)->CallVoidMethod(jni_env, thread, methID);
    if ((*jni_env)->ExceptionOccurred(jni_env)) {
        --allocated;
        if (allocated == 0) {
            printf("PASSED");
        }
        return;
    }
    printf("FAILED");
}