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
 * @author Alexey A. Petrenko
 * @version $Revision$
 */
/* 
 * THE FILE HAS BEEN AUTOGENERATED BY INTEL IJH TOOL.
 * Please be aware that all changes made to this file manually
 * will be overwritten by the tool if it runs again.
 */

#include <jni.h>


/* Header for class org.apache.harmony.awt.gl.font.NativeFont */

#ifndef _ORG_APACHE_HARMONY_AWT_GL_FONT_NATIVEFONT_H
#define _ORG_APACHE_HARMONY_AWT_GL_FONT_NATIVEFONT_H

#ifdef __cplusplus
extern "C" {
#endif


/* Static final fields */

#undef org_apache_harmony_awt_gl_font_NativeFont_DriverStringOptionsCmapLookup
#define org_apache_harmony_awt_gl_font_NativeFont_DriverStringOptionsCmapLookup 1L

#undef org_apache_harmony_awt_gl_font_NativeFont_DriverStringOptionsVertical
#define org_apache_harmony_awt_gl_font_NativeFont_DriverStringOptionsVertical 2L

#undef org_apache_harmony_awt_gl_font_NativeFont_DriverStringOptionsRealizedAdvance
#define org_apache_harmony_awt_gl_font_NativeFont_DriverStringOptionsRealizedAdvance 4L

#undef org_apache_harmony_awt_gl_font_NativeFont_DriverStringOptionsLimitSubpixel
#define org_apache_harmony_awt_gl_font_NativeFont_DriverStringOptionsLimitSubpixel 8L


/* Native methods */

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.getFontFamiliesNames()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_getFontFamiliesNames(JNIEnv *, jclass);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.embedFontNative(Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_embedFontNative(JNIEnv *, jclass, 
    jstring);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.initializeFont(Lorg/apache/harmony/awt/gl/font/WindowsFont;Ljava/lang/String;II)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_initializeFont(JNIEnv *, jclass, 
    jobject, jstring, jint, jint);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.canDisplayCharNative(JC)Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_canDisplayCharNative(JNIEnv *, jclass, 
    jlong, jchar);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.getFamilyNative(J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_getFamilyNative(JNIEnv *, jclass, 
    jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.getFontNameNative(J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_getFontNameNative(JNIEnv *, jclass, 
    jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.pFontFree(J)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_pFontFree(JNIEnv *, jclass, 
    jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.getItalicAngleNative(J)F
 */
JNIEXPORT jfloat JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_getItalicAngleNative(JNIEnv *, jclass, 
    jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.enumSystemFonts()V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_enumSystemFonts(JNIEnv *, jclass);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.getFonts()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_getFonts(JNIEnv *, jclass);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.getNativeLineMetrics(JIZZI)[F
 */
JNIEXPORT jfloatArray JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_getNativeLineMetrics(JNIEnv *, jclass, 
    jlong, jint, jboolean, jboolean, jint);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.getGlyphInfoNative(JCI)[F
 */
JNIEXPORT jfloatArray JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_getGlyphInfoNative(JNIEnv *, jclass, 
    jlong, jchar, jint);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.getGlyphPxlInfoNative(JC)[I
 */
JNIEXPORT jintArray JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_getGlyphPxlInfoNative(JNIEnv *, jclass, 
    jlong, jchar);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.getGlyphCodesNative(JLjava/lang/String;I)[I
 */
JNIEXPORT jintArray JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_getGlyphCodesNative(JNIEnv *, jclass, 
    jlong, jstring, jint);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.getGlyphCodeNative(JC)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_getGlyphCodeNative(JNIEnv *, jclass, 
    jlong, jchar);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.RemoveFontResource(Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_RemoveFontResource(JNIEnv *, jclass, 
    jstring);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.NativeInitGlyphImage(Lorg/apache/harmony/awt/gl/font/Glyph;)[B
 */
JNIEXPORT jbyteArray JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_NativeInitGlyphImage(JNIEnv *, jclass, 
    jobject);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.nativeInitLCIDsTable([Ljava/lang/String;[S)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_nativeInitLCIDsTable(JNIEnv *, jclass, 
    jobjectArray, jshortArray);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.getGlyphOutline(JCJI)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_getGlyphOutline(JNIEnv *, jclass, 
    jlong, jchar, jlong, jint);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.getExtraMetricsNative(JII)[F
 */
JNIEXPORT jfloatArray JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_getExtraMetricsNative(JNIEnv *, jclass, 
    jlong, jint, jint);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.setAntialiasing
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_setAntialiasing(JNIEnv *, jclass, 
    jlong, jboolean);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.gdiPlusDrawText(JLjava/lang/String;IJFF)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_gdiPlusDrawText(JNIEnv *, jclass, 
    jlong, jstring, jint, jlong, jfloat, jfloat);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.gdiPlusDrawDriverChar(JCJFFI[D)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_gdiPlusDrawDriverChar(JNIEnv *, jclass, 
    jlong, jchar, jlong, jfloat, jfloat, jint, jdoubleArray);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.gdiPlusDrawDriverString(JLjava/lang/String;IJFF[DI[D)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_gdiPlusDrawDriverString(JNIEnv *, jclass, 
    jlong, jstring, jint, jlong, jfloat, jfloat, jdoubleArray, jint, jdoubleArray);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.gdiPlusDrawDriverChars(J[CIJ[DI[D)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_gdiPlusDrawDriverChars(JNIEnv *, jclass, 
    jlong, jcharArray, jint, jlong, jdoubleArray, jint, jdoubleArray);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.gdiPlusReleaseHDC(JJ)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_gdiPlusReleaseHDC(JNIEnv *, jclass, 
    jlong, jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.NativeFont.gdiPlusGetHDC(J)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_awt_gl_font_NativeFont_gdiPlusGetHDC(JNIEnv *, jclass, 
    jlong);


#ifdef __cplusplus
}
#endif

#endif /* _ORG_APACHE_HARMONY_AWT_GL_FONT_NATIVEFONT_H */
