/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include <windows.h>
#include <stdio.h>

#define WEXE_POSTFIX        "\\jre\\bin\\javaw.exe\" "

char *getJDKRoot();

int WINAPI
WinMain (HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine,
   int nShowCmd)
{
    PROCESS_INFORMATION procInfo;
    STARTUPINFO startInfo;
    DWORD res = 0;
    char *jdkRoot = getJDKRoot();
    
    char *exePath = (char *)malloc((strlen(jdkRoot)+strlen(WEXE_POSTFIX)+strlen(lpCmdLine)+2)*sizeof(char));
    exePath[0] = '\"';
    strcpy(exePath+1, jdkRoot);
    strcat(exePath, WEXE_POSTFIX);
    strcat(exePath, lpCmdLine);
    
    // create child process
    memset(&procInfo, 0, sizeof(PROCESS_INFORMATION));
    memset(&startInfo, 0, sizeof(STARTUPINFO));
    startInfo.cb = sizeof(STARTUPINFO);
    startInfo.wShowWindow = nShowCmd;

    if (!CreateProcess(NULL, exePath, NULL, NULL, TRUE, 0, NULL, NULL, &startInfo, &procInfo)) { 
        free(exePath);
        return -1;
    }

    free(exePath);
    
    WaitForSingleObject(procInfo.hProcess, INFINITE);
    GetExitCodeProcess(procInfo.hProcess, &res);

    CloseHandle(procInfo.hProcess);
    CloseHandle(procInfo.hThread);

    return (int)res;
}