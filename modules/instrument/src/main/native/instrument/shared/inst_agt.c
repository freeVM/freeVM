/* Copyright 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define USING_VMI

#include "instrument.h"
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <zipsup.h>
#include <jni.h>
#include <vmi.h>

/*
 * This file implements a JVMTI agent to init Instrument instance, and handle class define/redefine events
 */

AgentList *tail = &list;
int gsupport_redefine = 0;
static JNIEnv *jnienv;

//call back function for ClassLoad event
void JNICALL callbackClassFileLoadHook(jvmtiEnv *jvmti_env,
	JNIEnv* jni_env,
	jclass class_being_redefined,
	jobject loader,
	const char* name,
	jobject protection_domain,
	jint class_data_len,
	const unsigned char* class_data,
	jint* new_class_data_len,
	unsigned char** new_class_data){

	jclass inst_class = *(gdata->inst_class);
	jbyteArray jnew_bytes = NULL;
	jbyteArray jold_bytes = (*jni_env)->NewByteArray(jni_env, class_data_len);
	jmethodID transform_method = *(gdata->transform_method);
	int name_len = strlen(name);
	jbyteArray jname_bytes = (*jni_env)->NewByteArray(jni_env, name_len);
	
	//construct java byteArray for old class data and class name
	(*jni_env)->SetByteArrayRegion(jni_env, jold_bytes, 0, class_data_len, (unsigned char *)class_data);
	(*jni_env)->SetByteArrayRegion(jni_env, jname_bytes, 0, name_len, (char *)name);
	
	//invoke transform method
	jnew_bytes = (jbyteArray)(*jni_env)->CallObjectMethod(jni_env, *(gdata->inst), transform_method, loader, jname_bytes, class_being_redefined, protection_domain, jold_bytes);	

	//get transform result to native char array
	if(0 != jnew_bytes){
		*new_class_data_len = (*jni_env)->GetArrayLength(jni_env, jnew_bytes);
		(*jvmti_env)->Allocate(jvmti_env, *new_class_data_len, new_class_data);		
		*new_class_data = (*jni_env)->GetPrimitiveArrayCritical(jni_env, jnew_bytes, JNI_FALSE);
		(*jni_env)->ReleasePrimitiveArrayCritical(jni_env, jnew_bytes, *new_class_data, 0);
	}
	return;
}

//call back function for VM init event
void JNICALL callbackVMInit(jvmtiEnv *jvmti, JNIEnv *env, jthread thread){
	jmethodID constructor;
	static jmethodID transform_method;
	static jmethodID premain_method;
	static jobject inst_obj;
	static jclass inst_class;
	jvmtiError err;
	AgentList *elem;
	
	PORT_ACCESS_FROM_ENV (env);	
	inst_class = (*env)->FindClass(env, "org/apache/harmony/instrument/internal/InstrumentationImpl");
	if(NULL == inst_class){		
		(*env)->FatalError(env,"class cannot find: org/apache/harmony/instrument/internal/InstrumentationImpl");
		return;	
	}
	inst_class = (jclass)(*env)->NewGlobalRef(env, inst_class);
	gdata->inst_class = &inst_class;

	constructor = (*env)->GetMethodID(env, inst_class,"<init>", "(Z)V");
	if(NULL == constructor){
		(*env)->FatalError(env,"constructor cannot be found.");				
		return;
	}
	
	inst_obj = (*env)->NewObject(env, inst_class, constructor, gsupport_redefine?JNI_TRUE:JNI_FALSE);
	if(NULL == inst_obj){
		(*env)->FatalError(env,"object cannot be inited.");				
		return;		
	}
	
	inst_obj = (*env)->NewGlobalRef(env, inst_obj);
	gdata->inst = &inst_obj;

	transform_method = (*env)->GetMethodID(env, inst_class, "transform", "(Ljava/lang/ClassLoader;[BLjava/lang/Class;Ljava/security/ProtectionDomain;[B)[B");
	if(NULL == transform_method){
		(*env)->FatalError(env,"transform method cannot find.");
		return;
	}
	gdata->transform_method = &transform_method;

	premain_method = (*env)->GetMethodID(env, inst_class, "executePremain", "([B[B)V");
	if(NULL == premain_method){
		(*env)->FatalError(env,"executePremain method cannot find.");
		return;
	}
	gdata->premain_method = &premain_method;		
	err = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, NULL);	
	check_jvmti_error(env, err, "Cannot set JVMTI ClassFileLoadHook event notification mode.");
	
	//parse command options and run premain class here	
	if(tail == &list){
		return;
	}
	for(elem = list.next; elem != NULL; elem = list.next){
		char *agent_options = elem->option;	
		char *class_name = elem->class_name;
		jbyteArray joptions=NULL, jclass_name;
		if(class_name){
			jclass_name = (*env)->NewByteArray(env, strlen(class_name));
			(*env)->SetByteArrayRegion(env, jclass_name, 0, strlen(class_name), class_name);
		}else{
			goto DEALLOCATE;
		}
		if(agent_options){
			joptions = (*env)->NewByteArray(env, strlen(agent_options));
			(*env)->SetByteArrayRegion(env, joptions, 0, strlen(agent_options), agent_options);
		}
		
		(*env)->CallObjectMethod(env, *(gdata->inst), *(gdata->premain_method), jclass_name, joptions);
DEALLOCATE:
		list.next = elem->next;
		hymem_free_memory(elem->class_name);
		hymem_free_memory(elem->option);
		hymem_free_memory(elem);
	}	
	tail = &list;	
}

char* Read_Manifest(JavaVM *vm, JNIEnv *env,const char *jar_name){
	I_32 retval;
	HyZipFile zipFile;
	HyZipEntry zipEntry;
	char *result;
	int size = 0;
	char errorMessage[1024];

	/* Reach for the VM interface */	
	VMI_ACCESS_FROM_JAVAVM(vm);
	PORT_ACCESS_FROM_JAVAVM(vm);

	/* open zip file */
	retval = zip_openZipFile(privatePortLibrary, (char *)jar_name, &zipFile, NULL);
	if(retval){
		sprintf(errorMessage,"failed to open file:%s, %d\n", jar_name, retval);
		(*env)->FatalError(env, errorMessage);
		return NULL;
	}

	/* get manifest entry */
	zip_initZipEntry(privatePortLibrary, &zipEntry);
	retval = zip_getZipEntry(privatePortLibrary, &zipFile, &zipEntry, "META-INF/MANIFEST.MF", TRUE);
	if (retval) {
		zip_freeZipEntry(PORTLIB, &zipEntry);
		sprintf(errorMessage,"failed to get entry: %d\n", retval);
		(*env)->FatalError(env, errorMessage);
		return NULL;
	}
	
	/* read bytes */
	size = zipEntry.uncompressedSize;
	result = (char *)hymem_allocate_memory(size*sizeof(char));
	retval = zip_getZipEntryData(privatePortLibrary, &zipFile, &zipEntry, result, size);
	if(retval){
		zip_freeZipEntry(PORTLIB, &zipEntry);
		sprintf(errorMessage,"failed to get bytes from zip entry, %d\n", zipEntry.extraFieldLength);
		(*env)->FatalError(env, errorMessage);
		return NULL;
	}

	/* free resource */
	zip_freeZipEntry(privatePortLibrary, &zipEntry);
	retval = zip_closeZipFile(privatePortLibrary, &zipFile);
	if (retval) {
		sprintf(errorMessage,"failed to close zip file: %s, %d\n", jar_name, retval);
		(*env)->FatalError(env, errorMessage);
		return NULL;
	}
	return result;
}

char* read_attribute(JavaVM *vm, char *manifest,char *lwrmanifest, const char * target){
	char *pos = manifest+ (strstr(lwrmanifest,target) - lwrmanifest);
	char *end;
	char *value;
	int length;

	PORT_ACCESS_FROM_JAVAVM(vm);
	
	if(NULL == pos){
		return NULL;
	}
	pos += strlen(target)+2;//": "
	end = strchr(pos, '\r');
	if(NULL == end){
		end = manifest + strlen(manifest);
	}
	length = end - pos;
	
	value = (char *)hymem_allocate_memory(sizeof(char)*(length+1));
	strncpy(value, pos, length);
	*(value+length) = '\0';
	return value;
}

int str2bol(char *str){	
	return 0 == strcmp("true", strlwr(str));
}

jint Parse_Options(JavaVM *vm, JNIEnv *env, jvmtiEnv *jvmti,  const char *agent){
	PORT_ACCESS_FROM_JAVAVM(vm);
	VMI_ACCESS_FROM_JAVAVM(vm);
	
	AgentList *new_elem = (AgentList *)hymem_allocate_memory(sizeof(AgentList));
	char *agent_cpy = (char *)hymem_allocate_memory(sizeof(char)*(strlen(agent)+1));
	char *jar_name, *manifest;
	char *options = NULL;
	char *class_name, *bootclasspath, *str_support_redefine;	
	char *bootclasspath_item;
	char *classpath;
	char *classpath_cpy;
	int support_redefine = 0;
	char *pos;
	char *lwrmanifest;
	
	strcpy(agent_cpy, agent);
	//parse jar name and options
	pos = strchr(agent_cpy, '=');
	if(pos>0){
		*pos++ = 0;
		options = (char *)hymem_allocate_memory(sizeof(char) * (strlen(pos)+1));
		strcpy(options, pos);
		hymem_free_memory(pos);
	}
	jar_name =agent_cpy;

	//read jar files, find manifest entry and read bytes
	//read attributes(premain class, support redefine, bootclasspath)
	manifest = Read_Manifest(vm,env, jar_name);	
	lwrmanifest = (char *)hymem_allocate_memory(sizeof(char) * (strlen(manifest)+1));
	strcpy(lwrmanifest,manifest);
	strlwr(lwrmanifest);
	
	//jar itself added to bootclasspath
	check_jvmti_error(env, (*jvmti)->GetSystemProperty(jvmti,"java.class.path",&classpath),"Failed to get classpath.");
	classpath_cpy = (char *)hymem_allocate_memory((sizeof(char)*(strlen(classpath)+strlen(jar_name)+2)));
	strcpy(classpath_cpy,classpath);
	strcat(classpath_cpy,";");
	strcat(classpath_cpy,jar_name);
	check_jvmti_error(env, (*jvmti)->SetSystemProperty(jvmti, "java.class.path",classpath_cpy),"Failed to set classpath.");
	hymem_free_memory(classpath_cpy);
	hymem_free_memory(jar_name);	

	//save options, save class name, add to agent list
	class_name = read_attribute(vm, manifest, lwrmanifest,"premain-class");
	if(NULL == class_name){
		hymem_free_memory(lwrmanifest);
		hymem_free_memory(manifest);
		(*env)->FatalError(env,"Cannot find Premain-Class attribute.");
	}
	new_elem->option = options;
	new_elem->class_name = class_name;
	new_elem->next = NULL;
	tail->next = new_elem;
	tail = new_elem;

	//calculate support redefine
	str_support_redefine = read_attribute(vm, manifest, lwrmanifest,"can-redefine-classes");
	if(NULL != str_support_redefine){
		support_redefine = str2bol(str_support_redefine);	
		gsupport_redefine |= support_redefine;
		hymem_free_memory(str_support_redefine);
	}
	
	//add bootclasspath
	
	bootclasspath = read_attribute(vm, manifest, lwrmanifest,"boot-class-path");
	if(NULL != bootclasspath){
		bootclasspath_item = strtok(bootclasspath, " ");
		while(NULL != bootclasspath_item){			
			check_jvmti_error(env, (*jvmti)->AddToBootstrapClassLoaderSearch(jvmti, bootclasspath_item),"Failed to add bootstrap classpath.");			
			bootclasspath_item = strtok(NULL, " ");
		}
		hymem_free_memory(bootclasspath);
	}	
	hymem_free_memory(lwrmanifest);
	hymem_free_memory(manifest);
	return 0;
}

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved){	
	PORT_ACCESS_FROM_JAVAVM(vm);
	VMI_ACCESS_FROM_JAVAVM(vm);
	jint err = (*vm)->GetEnv(vm, (void **)&jnienv, JNI_VERSION_1_2);
	if(JNI_OK != err){
		return err;
	}
	
	if(!gdata){		
		jvmtiCapabilities capabilities;		
		jvmtiError err;
		jvmtiEventCallbacks callbacks;
		JNIEnv *env = NULL;
		static jvmtiEnv *jvmti;

		gdata = hymem_allocate_memory(sizeof(AgentData));
		
		//get jvmti environment
		err = (*vm)->GetEnv(vm, (void **)&jvmti, JVMTI_VERSION_1_0);
		if(JNI_OK != err){
			return err;
		}		
		gdata->jvmti = jvmti;
		
		//set prerequisite capabilities for classfileloadhook, redefine, and VMInit event
		memset(&capabilities, 0, sizeof(capabilities));
		capabilities.can_generate_all_class_hook_events=1;
		capabilities.can_redefine_classes = 1;
		//FIXME VM doesnot support the capbility right now.
		//capabilities.can_redefine_any_class = 1;
		err = (*jvmti)->AddCapabilities(jvmti, &capabilities);
		check_jvmti_error(env, err, "Cannot add JVMTI capabilities.");

		//set events callback function
		(void)memset(&callbacks, 0, sizeof(callbacks));
		callbacks.ClassFileLoadHook = &callbackClassFileLoadHook;
		callbacks.VMInit = &callbackVMInit;
		err = (*jvmti)->SetEventCallbacks(jvmti, &callbacks, sizeof(jvmtiEventCallbacks));
		check_jvmti_error(env, err, "Cannot set JVMTI event callback functions.");

		//enable classfileloadhook event
		err = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_VM_INIT, NULL);
		check_jvmti_error(env, err, "Cannot set JVMTI VMInit event notification mode.");
	}		
	
	return Parse_Options(vm,jnienv, gdata->jvmti,options);	
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *vm){	
	PORT_ACCESS_FROM_JAVAVM(vm);
	VMI_ACCESS_FROM_JAVAVM(vm);
	//free the resource here	
	if(gdata){
		jvmtiEnv *jvmti = gdata->jvmti;
		jvmtiError err = (*jvmti)->DisposeEnvironment(jvmti);
		if(err != JVMTI_ERROR_NONE)	{
		   (*jnienv)->FatalError(jnienv,"Cannot dispose JVMTI environment.");		   
		}		
		hymem_free_memory(gdata);
		gdata = NULL;
	}
	return;
}
