# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#  
#      http://www.apache.org/licenses/LICENSE-2.0
#  
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

!ifndef APPVER
APPVER=4.0 #Default Windows version
!endif

TARGETOS=WIN95
_WIN32_IE=0x0500
SEHMAP = TRUE
!include <win32.mak>

LIBPATH=$(HY_HDK)\lib\# comment to avoid \ being treated as continuation
EXEPATH=..\# ditto
DLLPATH=$(HY_HDK)\jdk\jre\bin\# ditto
SHAREDSUB=..\shared\# ditto

HYCOMMONCFLAGS = \
  -WX -GF -Gs -MD -Zm400 \
  -D_DLL -D_MT -D_WIN32_WINNT=0x0400 -D_WINSOCKAPI_ \
  /I$(HY_HDK)\include /I$(HY_HDK)\jdk\include /I.
  
HYDEBUGCFLAGS = \
  -Zi
  
HYRELEASECFLAGS = \
  -Ogityb1  

!IF "$(HY_CFG)" == "debug"
HYCFLAGS = $(HYDEBUGCFLAGS) $(HYCOMMONCFLAGS)
!ELSE  
HYCFLAGS = $(HYRELEASECFLAGS) $(HYCOMMONCFLAGS)
!ENDIF
  
  
