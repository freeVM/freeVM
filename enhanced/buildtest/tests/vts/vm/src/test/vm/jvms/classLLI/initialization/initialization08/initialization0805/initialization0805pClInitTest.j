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
; @author Mikhail Bolotov
; @version $Revision: 1.2 $
;   
.class public org/apache/harmony/vts/test/vm/jvms/classLLI/initialization/initialization08/initialization0805/initialization0805pClInitTest
.super java/lang/Object

.field public static v I

.method static <clinit>()V
.limit stack 1
.limit locals 0

	bipush 104
	putstatic org/apache/harmony/vts/test/vm/jvms/classLLI/initialization/initialization08/initialization0805/initialization0805pClInitTest/v I
	getstatic org/apache/harmony/vts/test/vm/jvms/classLLI/initialization/initialization08/initialization0805/initialization0805pClInitTest/v I
	putstatic org/apache/harmony/vts/test/vm/jvms/classLLI/initialization/initialization08/initialization0805/initialization0805p/result I
	return

.end method

.method public <init>()V
.limit stack 1
.limit locals 1

	aload_0
	invokespecial java/lang/Object/<init>()V
	return

.end method

.method public static test()V
.limit stack 0
.limit locals 0

	return

.end method