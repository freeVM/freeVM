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
; Version: $Revision: 1.1.1.1 $
;

.interface public abstract org/apache/harmony/vts/test/vm/jvms/instructions/reftypes/getstatic/getstatic25/getstatic2503/getstatic2503pTest2
.super java/lang/Object
.field public static final testFieldi I=777

; interface initializer
.method public static <clinit>()V
      .limit stack 1      
 
     getstatic org/apache/harmony/vts/test/vm/jvms/instructions/reftypes/getstatic/getstatic25/getstatic2503/getstatic2503pTest/getstatic2503pTest/testField I  
     putstatic org/apache/harmony/vts/test/vm/jvms/instructions/reftypes/getstatic/getstatic25/getstatic2503/getstatic2503pTest2/testFieldi I
     return
.end method