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

#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <limits.h>

#if defined(WIN32)
#include <windows.h>
#endif
#if defined(FREEBSD)
#include <dlfcn.h>
#endif

#define TOOL_JAR      "tools.jar"
#define CLASS_PREFIX  "org.apache.harmony.tools."
#define CLASS_POSTFIX ".Main"

#if defined(WIN32)
#define PATH_SEPARATOR_CHAR '\\'
#define PATH_SEPARATOR      "\\"
#define EXE_POSTFIX         "\\jre\\bin\\java.exe"
#define WEXE_POSTFIX        "\\jre\\bin\\javaw.exe"
#define LIB_POSTFIX         "\\lib\\"
#define CLASSPATH_SEP       ";"
#else
#define PATH_SEPARATOR_CHAR '/'
#define PATH_SEPARATOR      "/"
#define EXE_POSTFIX         "/jre/bin/java"
#define WEXE_POSTFIX        "/jre/bin/javaw"
#define LIB_POSTFIX         "/lib/"
#define CLASSPATH_SEP       ":"
#endif

typedef struct ToolData {
    int numJars; 
    char **jarList;
} TOOLDATA;

char     *cleanToolName(const char *);
char     *getExeDir();
char     *getJDKRoot();
TOOLDATA *getToolData(const char *, const char *);

/**
 *  main
 * 
 *  based on invocation name (ex. 'javac') discovers jars needed
 *  for invocation - creates classpath and classname for invoking
 *  the JVM in jre/bin via an exec() or CreateProcess(), 
 *  and does so with the effective command  line pattern : 
 * 
 *      java -cp <created class path> <created Class name> <tool args>
 * 
 *  where 'created Class name' follows the convention of 
 * 
 *      org.apache.harmony.tools.<toolname>.Main
 * 
 *  where <toolname> is 'javac', 'javah', 'javap'
 */

int main (int argc, char **argv, char **envp)
{
#if defined(WIN32)
    PROCESS_INFORMATION procInfo;
    STARTUPINFO startInfo;
#endif
    int myArgvCount = argc;
    int moreArgvCount = /* -cp <classpath> */ 2 + /* <tool-class> */ 1 + /* NULL */ 1;
    char **myArgv = (char **) malloc(sizeof(char*) * (myArgvCount + moreArgvCount));    
    char *toolName = NULL;
    char *cmd_line = NULL;
    int size, i, j;
    int cmd_len = 0;
    int exit_code = -1;
    int newIndex = 0;
    char *jdkRoot = NULL;
    char *fullExePath = NULL;
    TOOLDATA *pToolData = (TOOLDATA *) malloc(sizeof(TOOLDATA));
    
    int isJavaw = 0;

    /*
     *  get the jdkroot and the construct invocation path for exe
     *  and the full paths to jars.  This way, we can be called 
     *  from anywhere
     */    
    jdkRoot = getJDKRoot();

//    printf("root = %s\n", jdkRoot);
    
    if (!jdkRoot) { 
        fprintf(stderr, "Unable to find JDK Root");
        return 2;
    }
        
    /* 
     * if we can't figure out what tool we are, just bail
     */    
    toolName = cleanToolName(argv[0]);

//    printf("tool name = %s\n", toolName);
    
    if (toolName == NULL) { 
        fprintf(stderr, "Unknown tool name %s\n", argv[0]);
        return 1;
    }
    
    isJavaw = strcmp(toolName, "javaw") == 0;

    /*
     *  get the 'tool data' - right now, this is just the jars
     *  specificly needed by this tool
     */
    pToolData = getToolData(toolName, jdkRoot);
       
    if (pToolData == NULL) { 
        fprintf(stderr, "Unable to get tool data for %s");
        return 2;
    }
    
    fullExePath = (char *) malloc(strlen(jdkRoot) + strlen(WEXE_POSTFIX) + 1);
    
    strcpy(fullExePath, jdkRoot);
    
    /* 
     * If we're javaw then we need to javaw to command line
     */
    if (isJavaw) {
        strcat(fullExePath, WEXE_POSTFIX);
    } else {
        strcat(fullExePath, EXE_POSTFIX);
    }
    
    /*
     *  we're invoking java with the following 
     *    -cp toolpath  clasname .......
     */
    myArgv[newIndex++] = fullExePath;
    
    /*
     *  if we're not java or javaw, put the tools on cp, figure out the tool class to invoke...
     */
    if (strcmp(toolName, "java") && !isJavaw) {
        char *classpath;
        char *buffer;

        myArgvCount = argc + moreArgvCount;
        
        // hangle non-empty -J<flag> options
        for (i = 1; i < argc; i++) { 
            if (argv[i] != NULL && argv[i][0] == '-' && argv[i][1] == 'J' && argv[i][2] != '\0') {
                myArgv[newIndex++] = argv[i] + 2;
            }
        }
     
        size = (strlen(jdkRoot) + strlen(LIB_POSTFIX)) * pToolData->numJars +
                   strlen(CLASSPATH_SEP) * (pToolData->numJars - 1) + 1;

        for (i = 0; i < pToolData->numJars; i++) { 
            size += strlen(pToolData->jarList[i]);
        }
                    
        classpath = (char *) malloc(size * sizeof(char));

        strcpy(classpath, jdkRoot);
        strcat(classpath, LIB_POSTFIX);
        strcat(classpath, pToolData->jarList[0]);

        for (i = 1; i < pToolData->numJars; i++) { 
            strcat(classpath, CLASSPATH_SEP);
            strcat(classpath, jdkRoot);
            strcat(classpath, LIB_POSTFIX);
            strcat(classpath, pToolData->jarList[i]);
        }
 
        myArgv[newIndex++] = "-cp";
        myArgv[newIndex++] = classpath;

        buffer = (char *) malloc(strlen(CLASS_PREFIX) + strlen(toolName) + strlen(CLASS_POSTFIX) + 1);
    
        strcpy(buffer, CLASS_PREFIX);
        strcat(buffer, toolName);
        strcat(buffer, CLASS_POSTFIX);
        
        myArgv[newIndex++] = buffer;

        // copy remaining arguments (skipping -J options)
        for (i = 1; i < argc; i++) {
            if (argv[i] != NULL && argv[i][0] == '-' && argv[i][1] == 'J') continue;
            myArgv[newIndex++] = argv[i];
        }

    } else {
        
        // for 'java' wrappper copy all arguments without changes
        for (i = 1; i < argc; i++) {
            myArgv[newIndex++] = argv[i];
        }

    }
    
    myArgv[newIndex] = NULL;

//    for (i=0; i < myArgvCount; i++) { 
//        printf(" %d = %s\n", i, myArgv[i]);
//    }

    free(toolName);
        
    /*
     * now simply execv() the java app w/ the new params
     */ 
     
#if defined(WIN32)

    /*
     * win32 - CreateProcess() needs a cmd line string
     *   - double quote all arguments to avoid breaking spaces
     *   - prepend existing double quotes with '\'
     */
     
    // determine required memory size for command line arguments
    size = 0;
    for (i=1; i < myArgvCount; i++) {
        if (myArgv[i] != NULL) {
            int arg_len = strlen(myArgv[i]);
            size += /* space */ 1 + /* quotes */ 2 + arg_len;
            for (j = 0; j < arg_len; j++) {
                 if (myArgv[i][j] == '\"') size++;
            }
        }
    }
    
    // allocate memory for whole command line
    cmd_line = (char *) malloc(strlen(fullExePath) + /* quotes */ 2 + /* arguments */ size + /* NULL */ 1);
    
    if (cmd_line == NULL) { 
        fprintf(stderr, "Unable to allocate memory for tool command line %s\n", argv[0]);
        return 4;
    }
    
    // copy quoted exe path
    sprintf(cmd_line, "\"%s\"", fullExePath);
    cmd_len = strlen(cmd_line);
        
    // copy quoted arguments and prepend existing double quotes with '\'
    for (i=1; i < myArgvCount; i++) {
        if (myArgv[i] != NULL) {
            int arg_len = strlen(myArgv[i]);
            cmd_line[cmd_len++] = ' ';  // space delimiter
            cmd_line[cmd_len++] = '\"'; // starting quote
            for (j = 0; j < arg_len; j++) {
                char ch = myArgv[i][j];
                if (ch == '\"') {
                    cmd_line[cmd_len++] = '\\';
                }  
                cmd_line[cmd_len++] = ch;
            }
            cmd_line[cmd_len++] = '\"'; // ending quote
        }
    }
    cmd_line[cmd_len] = '\0';
    
    // create child process
    memset(&procInfo, 0, sizeof(PROCESS_INFORMATION));
    memset(&startInfo, 0, sizeof(STARTUPINFO));
    startInfo.cb = sizeof(STARTUPINFO);
        
    if (!CreateProcess(NULL, cmd_line, NULL, NULL,
                    TRUE, 0, NULL, NULL, &startInfo, &procInfo)) { 

        fprintf(stderr, "Error creating process : %d\n", GetLastError());
        free(cmd_line);
        return exit_code;
    }

    free(cmd_line);

    // wait for child process to finish
    if (!isJavaw && WAIT_FAILED == WaitForSingleObject(procInfo.hProcess, INFINITE)) {

        fprintf(stderr, "Error waiting for process : %d\n", GetLastError());

        // terminate child process before exiting
        if (!TerminateProcess(procInfo.hProcess, -1)) {
            fprintf(stderr, "Error terminating process : %d\n", GetLastError());
        }

    } 
    else {

        // get exit code of the finished child process
        DWORD res = 0;
        if (GetExitCodeProcess(procInfo.hProcess, &res)) {
            exit_code = (int)res;
        }
        else {
            fprintf(stderr, "Error getting process exit code : %d\n", GetLastError());
        } 

    }

    // close child process handles
    CloseHandle(procInfo.hProcess);
    CloseHandle(procInfo.hThread);

    return exit_code;

#else    

    /*
     * linux - use execv() to replace current process
     */
     
    exit_code = execv(fullExePath, myArgv);

    // execv returns here only in case of error
    perror("Error creating process");
    return exit_code;

#endif

}

/***********************************************************************
 * cleanToolName()
 * 
 * takes a executable name and finds the tool name
 * in it.
 * 
 * returns new string with real tool name, or NULL if not found
 */
char *cleanToolName(const char *name) 
{
    char *last = strrchr(name, PATH_SEPARATOR_CHAR);

 #if defined(WIN32)
    char *temp;
    char *exe;
         
    if (last && *(last + 1)) {
        temp = strdup(last + 1);
    }
    else {
        temp = strdup(name);
    }
    
    // convert name to lower case on Windows
    _strlwr(temp);

    // remove possible '.exe' suffix
	exe = strstr(temp, ".exe");
    if (exe) { 
       *exe = '\0';
    }
         
    return temp;     
 #elif defined(LINUX) || defined(FREEBSD)
 
    /*
     *  if we found a slash (and someone didn't do something 
     *  stupid like invoke "java/"?)
     */
    if (last && *(last +1)) { 
        return strdup(last +1);
    }
    else { 
        return strdup(name);
    }
 #else
 #error Need to define basename-type function
 #endif
}

/******************************************************************
 *  getJDKRoot()
 * 
 *  returns the root of the JDK if it can figure it out
 *  or NULL if it can't
 */
char *getJDKRoot() { 
    
    char *exeDir = getExeDir();

    char *last = strrchr(exeDir, PATH_SEPARATOR_CHAR);
    
    if (last != NULL) { 
        *last = '\0';
        return exeDir;
    }
    
    return NULL;
}

/*****************************************************************
 * getExeDir()
 * 
 *  returns directory of running exe
 */
char *getExeDir() {

    char *last = NULL;
    
#if defined(LINUX)
    char buffer[PATH_MAX + 1];
    
    int size = readlink ("/proc/self/exe", buffer, sizeof(buffer)-1);
    
    buffer[size+1] = '\0';
#elif defined(FREEBSD)
    Dl_info info;
    char buffer[PATH_MAX + 1];
    if (dladdr( (const void*)&main, &info) == 0) {
        return NULL;
    }
    strncpy(buffer, info.dli_fname, PATH_MAX+1);
    buffer[PATH_MAX+1] = '\0';

#elif defined(WIN32)
    char buffer[512];
    DWORD dwRet = GetModuleFileName(NULL, buffer, 512);
        
    // FIXME - handle this right - it could be that 512 isn't enough
#else
#error Need to implement executable name code
#endif

    last = strrchr(buffer, PATH_SEPARATOR_CHAR);

    if (last != NULL) { 
        *last = '\0';
        return strdup(buffer);
    }
    
    return NULL;
}

/***********************************************************************
 *  getToolData()
 * 
 *  Read the jdk/bin/data/<toolname>.dat file and 
 *  return the list of jars needed for this tool
 *  Format : 
 *  ToolJar = <jar1name>
 *  ToolJar = <jar2name>
 *  ToolJar = <jar3name>
 * 
 *  If the data file doesn't exist, it will return tools.jar
 */
TOOLDATA *getToolData(const char *toolName, const char *jdkRoot) { 
    
    FILE *fp = NULL;
    char key[256];
    char value[256];
    int count = 0;
    char *temp = NULL;
    TOOLDATA *pToolData = (TOOLDATA *) malloc(sizeof(TOOLDATA));
            
    if (toolName == NULL || jdkRoot == NULL) { 
        return NULL;
    }
   
    if (pToolData == NULL) { 
        return NULL;
    }
    
    memset(pToolData, 0, sizeof(TOOLDATA));    
    
   /*
    *  assumes that the data files are in jdk/bin/data with a ".dat" extension
    */ 
    temp = (char *) malloc(strlen(jdkRoot) + strlen(PATH_SEPARATOR) + strlen("bin") 
            + strlen(PATH_SEPARATOR) + strlen("data") + strlen(PATH_SEPARATOR) + strlen(toolName) 
            + strlen(".dat") + 1);
                
    if (temp == NULL) { 
        return NULL;
    }
    
    strcpy(temp, jdkRoot);
    strcat(temp, PATH_SEPARATOR);
    strcat(temp, "bin");
    strcat(temp, PATH_SEPARATOR);
    strcat(temp, "data");
    strcat(temp, PATH_SEPARATOR);
    strcat(temp, toolName);
    strcat(temp, ".dat");
    
    //printf("tool data file = %s\n", temp);
    
    fp = fopen(temp, "r");
 
    if (fp) {
        while (EOF != (count= fscanf(fp, "%s = %s\n", key, value))) {
            // printf("count = %d : %s = %s\n", count, key, value);

            if (count == 2 && !strcmp("ToolJar", key)) {
                pToolData->jarList = (char **) realloc(pToolData->jarList, (pToolData->numJars + 1) * sizeof(char *));
                pToolData->jarList[pToolData->numJars++] = strdup(value);
            }
        }
        
        fclose(fp);
    }
    else {        
        pToolData->jarList = (char **) realloc(pToolData->jarList,  (pToolData->numJars + 1) * sizeof(char *));
        pToolData->jarList[pToolData->numJars++] = TOOL_JAR;
    }
    
    free(temp);
    
    return pToolData;
}
