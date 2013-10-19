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
; Author: Alexander D. Shipilov
; Version: $Revision: 1.3 $
;

.class public org/apache/harmony/vts/test/vm/jvms/instructions/reftypes/putfield/putfield04/putfield0401/putfield0401p
.super java/lang/Object
.field public testField J ; long field

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
   .limit stack 3
   .limit locals 2


   aload_0 ; push this
   sipush 104 ; push short 104
   i2l ; convert short 104 to long 104
   putfield org/apache/harmony/vts/test/vm/jvms/instructions/reftypes/putfield/putfield04/putfield0401/putfield0401p/testField J ; put long 104 to the field

   aload_0 ; push this
   getfield org/apache/harmony/vts/test/vm/jvms/instructions/reftypes/putfield/putfield04/putfield0401/putfield0401p/testField J ; load from field
   l2i ; convert long 104 to int 104
   ireturn

.end method

;
; standard main function
.method public static main([Ljava/lang/String;)V
  .limit stack 2
  .limit locals 1

  new org/apache/harmony/vts/test/vm/jvms/instructions/reftypes/putfield/putfield04/putfield0401/putfield0401p
  dup
  invokespecial org/apache/harmony/vts/test/vm/jvms/instructions/reftypes/putfield/putfield04/putfield0401/putfield0401p/<init>()V
  aload_0
  invokevirtual org/apache/harmony/vts/test/vm/jvms/instructions/reftypes/putfield/putfield04/putfield0401/putfield0401p/test([Ljava/lang/String;)I
  invokestatic java/lang/System/exit(I)V

  return
.end method
