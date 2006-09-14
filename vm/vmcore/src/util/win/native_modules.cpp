/*
 *  Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Petr Ivanov, Ilya Berezhniuk
 * @version $Revision: 1.1.2.1 $
 */

#include <memory.h>
#include <Windows.h>
#include <Tlhelp32.h>
#include "port_malloc.h"
#include "native_modules.h"

static native_module_t* fill_module(MODULEENTRY32 src);

bool get_all_native_modules(native_module_t** list_ptr, int* count_ptr)
{
    HANDLE hModuleSnap = INVALID_HANDLE_VALUE; 
    MODULEENTRY32 module; 
    native_module_t** cur_next_ptr = list_ptr;
    int count = 0;

    hModuleSnap =
        CreateToolhelp32Snapshot(TH32CS_SNAPMODULE, GetCurrentProcessId());

    if (hModuleSnap == INVALID_HANDLE_VALUE)
        return false;

    *list_ptr = NULL;

    //It is required to set the size of the structure. 
    module.dwSize = sizeof(MODULEENTRY32);
    if ( !Module32First(hModuleSnap, &module) )
    {
        CloseHandle(hModuleSnap);
        return false;
    }

    do
    {
        native_module_t* filled = fill_module(module);

        if (!filled)
        {
            CloseHandle(hModuleSnap);
            clear_native_modules(list_ptr);
            return false;
        }

        *cur_next_ptr = filled;
        cur_next_ptr = &filled->next;
        count++;

    } while (Module32Next(hModuleSnap, &module));

    CloseHandle(hModuleSnap);
    *count_ptr = count;

    return true;
}

void clear_native_modules(native_module_t** list_ptr)
{
    native_module_t* cur = *list_ptr;

    while (cur)
    {
        native_module_t* next = cur->next;

        if (cur->filename)
            STD_FREE(cur->filename);

        STD_FREE(cur);
        cur = next;
    }

    *list_ptr = NULL;
}

native_module_t* fill_module(MODULEENTRY32 src)
{
    native_module_t* module =
        (native_module_t*)STD_MALLOC(sizeof(native_module_t));

    if (module == NULL)
        return NULL;

    size_t path_size = strlen(src.szExePath) + 1;
    module->filename = (char*)STD_MALLOC(path_size);
    if (module->filename == NULL)
    {
        STD_FREE(module);
        return NULL;
    }

    memcpy(module->filename, src.szExePath, path_size);
    strlwr(module->filename);

    module->seg_count = 1;
    module->segments[0].type = SEGMENT_TYPE_UNKNOWN;
    module->segments[0].base = src.modBaseAddr;
    module->segments[0].size = (size_t)src.modBaseSize;
    module->next = NULL;

    return module;
}
