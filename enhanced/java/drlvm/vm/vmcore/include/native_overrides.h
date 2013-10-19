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
/** 
 * @author Intel, Evgueni Brevnov, Ivan Volosyuk
 */  


#ifndef _NATIVE_OVERRIDES_H_
#define _NATIVE_OVERRIDES_H_

#include "lil.h"
#include "open/types.h"
#include "environment.h"
#include "String_Pool.h"

typedef struct NSOTableItem NSOTableItem;

LilCodeStub* nso_readinternal(LilCodeStub*, Method_Handle);
LilCodeStub* nso_system_currenttimemillis(LilCodeStub*, Method_Handle);
LilCodeStub* nso_newinstance(LilCodeStub* cs, Method_Handle);

LilCodeStub* nso_get_class(LilCodeStub*, Method_Handle);

LilCodeStub* nso_array_copy(LilCodeStub*, Method_Handle);
LilCodeStub* nso_char_array_copy(LilCodeStub*, Method_Handle);

NSOTableItem* nso_init_lookup_table(String_Pool* pstrpool);
void nso_clear_lookup_table(NSOTableItem* NSOTable);

NativeStubOverride nso_find_method_override(const Global_Env* ge,
        const String* class_name, const String* name, const String* desc);

#endif //!_NATIVE_OVERRIDES_H_
