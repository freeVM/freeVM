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
; Version: $Revision: 1.3 $
;

.class public org/apache/harmony/vts/test/vm/jvms/instructions/loadStore/dload_2/dload_207/dload_20701/dload_20701p
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
.method public test([Ljava/lang/String;)I
   .limit stack 4
   .limit locals 8

   ;if any exception is thrown during execution, then test fail
   ldc2_w 2.0    ; push  value1 = 2.0 to stack    
   dstore 2      ; store  value1 into local variable at index = 2
   ldc2_w 4.0    ; push  value2 = 4.0 to stack
   dstore 4      ; store  value2 into local variable at index = 4
   ldc2_w 6.0    ; push  value3 = 6.0 to stack    
   dstore 6      ; store  value3 into local variable at index = 6 
          
   ldc2_w 2.0    ; push  value1 = 2.0 to stack          
   dload_2       ; load value from local variable at index = 2 to stack   
   dcmpg
   iconst_0
   if_icmpne J2  ; compare two values on the top of stack
                 ; if values are not equal test fail 
  
  

   ldc2_w 4.0    ; push  value = 4.0 to stack          
   dload 4       ; load value from local variable at index = 4 to stack   
   dcmpg
   iconst_0
   if_icmpne J2  ; compare two values on the top of stack
                 ; if values are not equal test fail 

   ldc2_w 6.0    ; push  value = 6.0 to stack          
   dload 6       ; load value from local variable at index = 6 to stack   
   dcmpg
   iconst_0
   if_icmpne J2  ; compare two values on the top of stack
                 ; if values are not equal test fail 
    

   ldc2_w 2.0    ; push  value1 = 2.0 to stack          
   dload_2       ; load value from local variable at index = 2 to stack   
   dcmpg
   iconst_0
   if_icmpne J2  ; compare two values on the top of stack
                 ; if values are not equal test fail 
  

   sipush 104
   ireturn 

   astore_1
J2: 
   sipush 105
   ireturn

   return
.end method
;
; standard main function
.method public static main([Ljava/lang/String;)V
  .limit stack 2
  .limit locals 1

  new org/apache/harmony/vts/test/vm/jvms/instructions/loadStore/dload_2/dload_207/dload_20701/dload_20701p
  dup
  invokespecial org/apache/harmony/vts/test/vm/jvms/instructions/loadStore/dload_2/dload_207/dload_20701/dload_20701p/<init>()V
  aload_0
  invokevirtual org/apache/harmony/vts/test/vm/jvms/instructions/loadStore/dload_2/dload_207/dload_20701/dload_20701p/test([Ljava/lang/String;)I
  invokestatic java/lang/System/exit(I)V

  return
.end method
