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

#define EXE_POSTFIX   "/jre/bin/java"
#define LIB_POSTFIX   "/lib/"
#define TOOL_JAR      "tools.jar"
#define ECJ_JAR       "ecj_3.2.jar"
#define CLASSPATH_SEP ":"
#define CLASS_PREFIX  "org.apache.harmony.tools."
#define CLASS_POSTFIX ".Main"

char *cleanToolName(const char *);
char *getExeDir();
char *getJDKRoot();


int main (int argc, char **argv, char **envp)
{
    int myArgvCount = argc + 4;
    char **myArgv = (char **) malloc(sizeof(char*) * myArgvCount);    
    char *toolName = NULL;
    int i;
    int newIndex = 0;

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
    char *jdkRoot = getJDKRoot();
    printf("root = %s\n", jdkRoot);
    
    if (!jdkRoot) { 
        fprintf(stderr, "Unable to find JDK Root");
        return 2;
    }
       
    char *fullExePath = (char *) malloc(strlen(jdkRoot) + strlen(EXE_POSTFIX) + 1);
    
    strcpy(fullExePath, jdkRoot);
    strcat(fullExePath, EXE_POSTFIX);
    
    char *classpath = (char *) malloc(strlen(jdkRoot) * 2 + strlen(LIB_POSTFIX) * 2
                    + strlen(TOOL_JAR) + strlen(ECJ_JAR) + strlen(CLASSPATH_SEP) + 1);
         
    strcpy(classpath, jdkRoot);
    strcat(classpath, LIB_POSTFIX);
    strcat(classpath, TOOL_JAR);
    strcat(classpath, CLASSPATH_SEP);
    strcat(classpath, jdkRoot);
    strcat(classpath, LIB_POSTFIX);
    strcat(classpath, ECJ_JAR);

    /*
     *  we're invoking java with the following 
     *    -cp toolpath  clasname .......
     */
    myArgv[newIndex++] = fullExePath;
    myArgv[newIndex++] = "-cp";
    myArgv[newIndex++] = classpath;

    char *buffer = (char *) malloc(strlen(CLASS_PREFIX) + strlen(toolName) + strlen(CLASS_POSTFIX) + 1);

    strcpy(buffer, CLASS_PREFIX);
    strcat(buffer, toolName);
    strcat(buffer, CLASS_POSTFIX);
    
    myArgv[newIndex++] = buffer;
    
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
    execv(fullExePath, myArgv);    
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
    char *toolNames[] = { "javac", "javap", "javah" };
    
    for (i=0; i < sizeof(toolNames)/sizeof(toolNames[0]); i++) { 
        if (strstr(name, toolNames[i])) {
            return toolNames[i];
        }
    }
    
    return NULL;    
}


char *getJDKRoot() { 
    
    char *exeDir = getExeDir();

    char *last = strrchr(exeDir, '/');
    
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
    
    char buffer[PATH_MAX + 1];
    
    int size = readlink ("/proc/self/exe", buffer, sizeof(buffer)-1);
    
    buffer[size+1] = '\0';
   
    char *last = strrchr(buffer, '/');
   
    if (last != NULL) { 
        *last = '\0';
        return strdup(buffer);
    }
    
    return NULL;
}
