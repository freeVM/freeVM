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
; Author: Alexander D. Shipilov, Maxim V. Makarov
; Version: $Revision: 1.1 $
;

.class public org/apache/harmony/vts/test/vm/jvms/instructions/reftypes/sastore/sastore06/sastore0601/sastore0601n
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
.method public test()V
   .limit stack 3
   .limit locals 2

   iconst_1
   newarray int ; array of int
   iconst_0
   iconst_1
   sastore ; must throw VerifyError

   return
.end method
