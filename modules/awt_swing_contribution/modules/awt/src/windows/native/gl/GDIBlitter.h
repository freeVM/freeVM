/*
 *  Copyright 2005 - 2006 The Apache Software Software Foundation or its licensors, as applicable.
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
 * @author Igor V. Stolyarov
 * @version $Revision$
 */
 
#ifndef __GDI_BLITTER__
#define __GDI_BLITTER__

#include <Windows.h>
#include <Wingdi.h>
#include <jni.h>
#include "LUTTables.h"
#include "SurfaceDataStructure.h"

#define BIT_BLT         0
#define TRANSPARENT_BLT 1
#define ALPHA_BLEND     2
#define NULL_BLT        3

void findNonExistColor(DWORD &, DWORD *, UINT);
BOOL isRepeatColor(UINT , DWORD *, UINT);

BOOL initBlitData(SURFACE_STRUCTURE *srcSurf, JNIEnv *env, jobject srcData, UINT compType, 
                                UCHAR srcConstAlpha, BLITSTRUCT *blitStruct);

BOOL initBitmap(SURFACE_STRUCTURE *srcSurf, JNIEnv *env, jobject srcData, BOOL alphaPre);
#endif