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

.class public org/apache/harmony/vts/test/vm/jvms/instructions/loadStore/lstore/lstore12/lstore1201/lstore1201p
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
   .limit locals 65535

   ;if any exception is thrown during execution, then test fail
   
   lconst_1        ; push 1 to stack 
   dup2
   wide 
   lstore 65533    ; store long value=1 into local variable at index = 65533
   wide
   lload 65533     ; load value from local variable at index = 65533 to stack   
   lcmp
   iconst_0
   if_icmpne J1    ; compare two values on the top of stack
                   ; if values are equal test pass,else test fail
   sipush 104
   ireturn

   astore_1
J1: 
   sipush 105
   ireturn

   return
.end method


;
; standard main function
.method public static main([Ljava/lang/String;)V
  .limit stack 2
  .limit locals 1

  new org/apache/harmony/vts/test/vm/jvms/instructions/loadStore/lstore/lstore12/lstore1201/lstore1201p
  dup
  invokespecial org/apache/harmony/vts/test/vm/jvms/instructions/loadStore/lstore/lstore12/lstore1201/lstore1201p/<init>()V
  aload_0
  invokevirtual org/apache/harmony/vts/test/vm/jvms/instructions/loadStore/lstore/lstore12/lstore1201/lstore1201p/test([Ljava/lang/String;)I
  invokestatic java/lang/System/exit(I)V

  return
.end method
