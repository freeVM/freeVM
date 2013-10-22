;    Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable
;
;    Licensed under the Apache License, Version 2.0 (the "License");
;    you may not use this file except in compliance with the License.
;    You may obtain a copy of the License at
;
;       http://www.apache.org/licenses/LICENSE-2.0
;
;    Unless required by applicable law or agreed to in writing, software
;    distributed under the License is distributed on an "AS IS" BASIS,
;    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;
;    See the License for the specific language governing permissions and
;    limitations under the License.

;
; Author: Ilia A. Leviev
; Version: $Revision: 1.1 $
;

.class public org/apache/harmony/vts/test/vm/jvms/classFile/finally/finally05/finally0501/finally0501n
.super java/lang/Object

;
; standard initializer
.method public <init>()V
   aload_0
   invokespecial java/lang/Object/<init>()V
   return
.end method

;
; test method

.method public static test()V
   .limit stack 2
   .limit locals 3
            
 	jsr J1      
    ret 2 
    return				           
J1:           
    astore_1            
    jsr S               
    return  
S: 
    astore_2
    ret 1                   

.end method

