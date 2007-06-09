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
; Version: $Revision: 1.1.1.1 $
;

.class public org/apache/harmony/vts/test/vm/jvms/classFile/constraints/structural/constraint41/methods07/methods07n
.super java/lang/Object
.implements org/apache/harmony/vts/test/vm/jvms/classFile/constraints/structural/constraint41/methods07/methods07nInterface

;
; standard initializer
.method public <init>()V
   aload_0
   invokespecial java/lang/Object/<init>()V
   return
.end method

;
; test method
.method public test()V
   .limit stack 3
   .limit locals 2

  new org/apache/harmony/vts/test/vm/jvms/classFile/constraints/structural/constraint41/methods07/methods07nTest
  dup
  invokespecial org/apache/harmony/vts/test/vm/jvms/classFile/constraints/structural/constraint41/methods07/methods07nTest/<init>()V
  aload_0
  invokevirtual org/apache/harmony/vts/test/vm/jvms/classFile/constraints/structural/constraint41/methods07/methods07nTest/test2(Lorg/apache/harmony/vts/test/vm/jvms/classFile/constraints/structural/constraint41/methods07/methods07nInterface;)V
  return

.end method

.method public myMethod()V
 .limit stack 2
 .limit locals 1
   return
.end method
