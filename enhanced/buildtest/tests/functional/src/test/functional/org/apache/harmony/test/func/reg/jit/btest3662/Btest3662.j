;  Licensed to the Apache Software Foundation (ASF) under one or more
;  contributor license agreements.  See the NOTICE file distributed with
;  this work for additional information regarding copyright ownership.
;  The ASF licenses this file to You under the Apache License, Version 2.0
;  (the "License"); you may not use this file except in compliance with
;  the License.  You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
;  Unless required by applicable law or agreed to in writing, software
;  distributed under the License is distributed on an "AS IS" BASIS,
;  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;  See the License for the specific language governing permissions and
;  limitations under the License.
;
;
;

.class org/apache/harmony/test/func/reg/jit/btest3662/Btest3662
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
   .limit stack 2
   .limit locals 2
.catch java/lang/NoSuchMethodError from lab1 to lab2 using lab3
.catch all from lab1 to lab2 using lab4
lab1:
  invokestatic org/apache/harmony/test/func/reg/jit/btest3662/Test3662/testMethod()I
  ireturn
lab2:
lab3:
  sipush 0
  ireturn
lab4:
  sipush 105
  ireturn
.end method

;
; main functon
.method public static main([Ljava/lang/String;)V
  .limit stack 2
  .limit locals 1

  new org/apache/harmony/test/func/reg/jit/btest3662/Btest3662
  dup
  invokespecial org/apache/harmony/test/func/reg/jit/btest3662/Btest3662/<init>()V
  aload_0
  invokevirtual org/apache/harmony/test/func/reg/jit/btest3662/Btest3662/test([Ljava/lang/String;)I
  invokestatic java/lang/System/exit(I)V

  return
.end method
