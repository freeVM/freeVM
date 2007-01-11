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
 * @author Xiao-Feng Li, 2006/10/05
 */

#include <string.h>
#include <jni.h>
#include "jit_intf.h"
#include "java_support.h"

Class_Handle GCHelper_clss;
Boolean java_helper_inlined;

void HelperClass_set_GenMode(Boolean status)
{
  if(!java_helper_inlined) return;

  unsigned int nfields = class_number_fields(GCHelper_clss);
  unsigned int i;
  for(i=0; i<nfields; i++){
    Field_Handle field = class_get_field(GCHelper_clss, i);
    if(!strcmp(field_get_name(field), "GEN_MODE")){
      jboolean* p_gen_mode = (jboolean*)field_get_address(field);
      *p_gen_mode = (jboolean)status;
      break;
    }
  }
  
  assert(i<nfields);
  
/*
  hythread_suspend_enable();
  
  //"org.apache.harmony.drlvm.gc_gen.GCHelper" 
  jclass GCHelper = jni_env->FindClass("GCHelper");
  jfieldID gen_mode = jni_env->GetStaticFieldID(GCHelper, "GEN_MODE", "Z");
  assert(gen_mode);
  
  jni_env->SetStaticBooleanField(GCHelper, gen_mode, status?JNI_TRUE:JNI_FALSE);
  
  hythread_suspend_disable();
*/  
  return;
}


void HelperClass_set_NosBoundary(void* boundary)
{
  if(!java_helper_inlined) return;

  unsigned int nfields = class_number_fields(GCHelper_clss);
  unsigned int i;
  for(i=0; i<nfields; i++){
    Field_Handle field = class_get_field(GCHelper_clss, i);
    if(!strcmp(field_get_name(field), "NOS_BOUNDARY")){
      jint* p_nos_boundary = (jint*)field_get_address(field);
      *p_nos_boundary = (jint)boundary;
      break;
    }
  }
  
  assert(i<nfields);

  return;
}