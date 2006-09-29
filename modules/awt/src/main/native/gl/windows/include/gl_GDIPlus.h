/*
 *  Copyright 2005 - 2006 The Apache Software Foundation or its licensors, as applicable.
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
 * @author Alexey A. Petrenko
 * @version $Revision$
 */
#if !defined __GL_GDIPLUS_H__
#define __GL_GDIPLUS_H__

#include <Windows.h>
#include <GdiPlus.h>
#include <jni.h>

using namespace Gdiplus;

typedef struct _GraphicsInfo {
    HWND hwnd;
    HDC hdc;
    Graphics *graphics;
    Pen *pen;
    Brush *brush;
    HBITMAP bmp;
    Matrix *matrix;
} GraphicsInfo;

typedef struct _GLBITMAPINFO{
    BITMAPINFOHEADER bmiHeader;
    RGBQUAD          bmiColors[256];
}GLBITMAPINFO;

typedef struct _BLITSTRUCT{
    UINT blitFunctintType;
    DWORD rastOp;
    BLENDFUNCTION blendFunc;
}BLITSTRUCT;

HRGN setGdiClip(JNIEnv * env, HDC hdc, jintArray clip, jint clipLength);
void restoreGdiClip(HDC hdc, HRGN hOldClipRgn);

#endif