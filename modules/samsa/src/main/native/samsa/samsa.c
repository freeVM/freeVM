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

#define TOOL_JAR      "tools.jar"
#define ECJ_JAR       "ecj_3.2.jar"
#define CLASS_PREFIX  "org.apache.harmony.tools."
#define CLASS_POSTFIX ".Main"
#define PATH_SEP      "/"

typedef struct ToolData {
    int numJars; 
    char **jarList;
} TOOLDATA;

#if defined(LINUX)
#define PATH_SEPARATOR '/'
#define EXE_POSTFIX   "/jre/bin/java"
#define LIB_POSTFIX   "/lib/"
#define CLASSPATH_SEP ":"
#endif

#if defined(WIN32)
#define PATH_SEPARATOR '\\'
#define EXE_POSTFIX   "\\jre\\bin\\java.exe"
#define LIB_POSTFIX   "\\lib\\"
#define CLASSPATH_SEP ";"
#endif

char *cleanToolName(const char *);
char *getExeDir();
char *getJDKRoot();
TOOLDATA *getToolData(const char *, const char *);


int main (int argc, char **argv, char **envp)
{
#if defined(WIN32)
    PROCESS_INFORMATION procInfo;
    STARTUPINFO startInfo;
#endif
    int myArgvCount = argc + 4;
    char **myArgv = (char **) malloc(sizeof(char*) * myArgvCount);    
    char *toolName = NULL;
    int i, j;
    int newIndex = 0;
    char *jdkRoot = NULL;
    char *fullExePath = NULL;
    TOOLDATA *pToolData = (TOOLDATA *) malloc(sizeof(TOOLDATA));
        
    /* 
     * if we can't figure out what tool we are, just bail
     */    
    toolName = cleanToolName(argv[0]);

    if (toolName == NULL) { 
        fprintf(stderr, "Uknown tool name %s\n", argv[0]);
        return 1;
    }

    /*
     *  get the jdkroot and the construct invocation path for exe
     *  and the full paths to jars.  This way, we can be called 
     *  from anywhere
     */    
    jdkRoot = getJDKRoot();
    printf("root = %s\n", jdkRoot);
    
    if (!jdkRoot) { 
        fprintf(stderr, "Unable to find JDK Root");
        return 2;
    }

    pToolData = getToolData(toolName, jdkRoot);
    
       
    fullExePath = (char *) malloc(strlen(jdkRoot) + strlen(EXE_POSTFIX) + 1);
    
    strcpy(fullExePath, jdkRoot);
    strcat(fullExePath, EXE_POSTFIX);
    
    /*
     *  we're invoking java with the following 
     *    -cp toolpath  clasname .......
     */
    myArgv[newIndex++] = fullExePath;
    
    /*
     *  if we're not java, put the tools on cp, figure out the tool class to invoke...
     */
    if (strcmp(toolName, "java")) {
        char *classpath;
        char *buffer;
        int size;
        int i;
        
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
    }
        
    for (i = 1; i < argc; i++) {
        myArgv[newIndex++] = argv[i];
    }
    
    myArgv[newIndex] = '\0';

    for (i=0; i < myArgvCount; i++) { 
        printf(" %d = %s\n", i, myArgv[i]);
    }
    
    /*
     * now simply execv() the java app w/ the new params
     */ 
     
#if defined(WIN32)

    j = 0;
    for (i=1; i < myArgvCount; i++) {
        if (myArgv[i] != NULL) {
            j += strlen(myArgv[i]);
            j++; // for the needed spaces
        }
    }
    
    toolName = (char *) malloc(sizeof(char) * j);
    
    strcpy(toolName,myArgv[1]);
    strcat(toolName, " ");
        
    for (i=2; i < myArgvCount; i++) {
        if (myArgv[i] != NULL) {
            strcat(toolName,myArgv[i]);
            strcat(toolName, " ");
        }
        else {
            break;
        }
    }
   
    printf("Calling %s\n", fullExePath);
    printf("cmdline %s\n", toolName);
    
    
    memset(&procInfo, 0, sizeof(PROCESS_INFORMATION));
    memset(&startInfo, 0, sizeof(STARTUPINFO));
    startInfo.cb = sizeof(STARTUPINFO);
            
    CreateProcess(fullExePath, toolName, NULL, NULL,
                    FALSE, 0, NULL, NULL, &startInfo, &procInfo);    
#else    
    execv(fullExePath, myArgv);
#endif

}

/**
 * cleanToolName
 * 
 * takes a executable name and finds the tool name
 * in it
 * 
 * returns real tool name, or NULL if not found
 */
char *cleanToolName(const char *name) 
{
    int i;
    char *toolNames[] = { "javac", "javap", "javah", "java" };
    
    /* 
     *  FIXME :  this is an awful hack, easy to fool, but for now...
     */
    for (i=0; i < sizeof(toolNames)/sizeof(toolNames[0]); i++) { 
        if (strstr(name, toolNames[i])) {
            return toolNames[i];
        }
    }
    
    return NULL;    
}


char *getJDKRoot() { 
    
    char *exeDir = getExeDir();

    char *last = strrchr(exeDir, PATH_SEPARATOR);
    
    if (last != NULL) { 
        *last = '\0';
        return exeDir;
    }
    
    return NULL;
}
/**
 * getExeDir
 * 
 *  returns directory of running exe
 */
char *getExeDir() {

    char *last = NULL;
    
#if defined(LINUX)    
    char buffer[PATH_MAX + 1];
    
    int size = readlink ("/proc/self/exe", buffer, sizeof(buffer)-1);
    
    buffer[size+1] = '\0';
#endif

#if defined(WIN32)
    char buffer[256];
    DWORD dwRet = GetModuleFileName(NULL, buffer, 256);
        
    // FIXME - handle this right
#endif

    last = strrchr(buffer, PATH_SEPARATOR);

    if (last != NULL) { 
        *last = '\0';
        return strdup(buffer);
    }
    
    return NULL;
}

/**
 *  Read the jdk/bin/data/<toolname>.data file and 
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
    
    memset(pToolData, 0, sizeof(TOOLDATA));    
        
    if (toolName == NULL || jdkRoot == NULL) { 
        return NULL;
    }
   
   /*
    *  assumes that the data files are in jdk/bin/data with a ".dat" extension
    */ 
    temp = (char *) malloc(strlen(jdkRoot) + strlen(PATH_SEP) + strlen("bin") 
            + strlen(PATH_SEP) + strlen("data") + strlen(PATH_SEP) + strlen(toolName) 
            + strlen(".dat") + 1);
                
    strcpy(temp, jdkRoot);
    strcat(temp, PATH_SEP);
    strcat(temp, "bin");
    strcat(temp, PATH_SEP);
    strcat(temp, "data");
    strcat(temp, PATH_SEP);
    strcat(temp, toolName);
    strcat(temp, ".dat");
    
    //printf("tool data file = %s\n", temp);
    
    fp = fopen(temp, "r");
 
    if (fp) {
        while (EOF != (count= fscanf(fp, "%s = %s\n", key, value))) {
            // printf("count = %d : %s = %s\n", count, key, value);
            
            if (count == 2 && !strcasecmp("tooljar", key)) {
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
