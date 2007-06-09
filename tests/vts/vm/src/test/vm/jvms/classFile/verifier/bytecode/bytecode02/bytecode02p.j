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
; Author: Khen G. Kim
; Version: $Revision: 1.1 $
;

.class public org/apache/harmony/vts/test/vm/jvms/classFile/verifier/bytecode/bytecode02/bytecode02p
.super java/lang/Object
.field public f Lorg/apache/harmony/vts/test/vm/jvms/classFile/verifier/bytecode/bytecode02/bytecode02pTest1;

;
; standard initializer
.method public <init>()V
   .limit stack 2
   .limit locals 2
   aload_0
   invokespecial java/lang/Object/<init>()V
   return
.end method

;
; test method
.method public test()V
   .limit stack 2
   .limit locals 3

   aload_0
   dup
   invokevirtual org/apache/harmony/vts/test/vm/jvms/classFile/verifier/bytecode/bytecode02/bytecode02p/get()Lorg/apache/harmony/vts/test/vm/jvms/classFile/verifier/bytecode/bytecode02/bytecode02pTest2;

   ; put instance of the not existing class to the field with not same type.
   putfield org/apache/harmony/vts/test/vm/jvms/classFile/verifier/bytecode/bytecode02/bytecode02p/f Lorg/apache/harmony/vts/test/vm/jvms/classFile/verifier/bytecode/bytecode02/bytecode02pTest1;
   return
.end method

;
; method, which return null as required instance
.method public get()Lorg/apache/harmony/vts/test/vm/jvms/classFile/verifier/bytecode/bytecode02/bytecode02pTest2;
   .limit stack 2
   .limit locals 2

   aconst_null
   areturn
.end method
