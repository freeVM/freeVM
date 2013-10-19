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
; Author: Maxim N. Kurzenev
; Version: $Revision: 1.1 $
;

.source threads07601p$1.j
.class  org/apache/harmony/vts/test/vm/jvms/threads/threads076/threads07601/threads07601p$1
.super java/lang/Thread

.field final this$0 Lorg/apache/harmony/vts/test/vm/jvms/threads/threads076/threads07601/threads07601p;

.method  <init>(Lorg/apache/harmony/vts/test/vm/jvms/threads/threads076/threads07601/threads07601p;)V
    .limit stack 2
    .limit locals 2

	aload_0
	aload_1
	putfield org/apache/harmony/vts/test/vm/jvms/threads/threads076/threads07601/threads07601p$1/this$0 Lorg/apache/harmony/vts/test/vm/jvms/threads/threads076/threads07601/threads07601p;
	aload_0
	invokespecial java/lang/Thread/<init>()V
	return
.end method

.method public run()V
    .limit stack 2
    .limit locals 1

    ; v = 31415;
	aload_0
	getfield org/apache/harmony/vts/test/vm/jvms/threads/threads076/threads07601/threads07601p$1/this$0 Lorg/apache/harmony/vts/test/vm/jvms/threads/threads076/threads07601/threads07601p;
	sipush 31415
	putfield org/apache/harmony/vts/test/vm/jvms/threads/threads076/threads07601/threads07601p/v I
	return
.end method
