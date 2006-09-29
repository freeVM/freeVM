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


/* Header for class org.apache.harmony.awt.gl.font.LinuxNativeFont */

#ifndef _ORG_APACHE_HARMONY_AWT_GL_FONT_LINUXNATIVEFONT_H
#define _ORG_APACHE_HARMONY_AWT_GL_FONT_LINUXNATIVEFONT_H

#ifdef __cplusplus
extern "C" {
#endif


/* Static final fields */

#undef org_apache_harmony_awt_gl_font_LinuxNativeFont_FC_SLANT_ROMAN
#define org_apache_harmony_awt_gl_font_LinuxNativeFont_FC_SLANT_ROMAN 0L

#undef org_apache_harmony_awt_gl_font_LinuxNativeFont_FC_SLANT_ITALIC
#define org_apache_harmony_awt_gl_font_LinuxNativeFont_FC_SLANT_ITALIC 100L

#undef org_apache_harmony_awt_gl_font_LinuxNativeFont_FC_SLANT_OBLIQUE
#define org_apache_harmony_awt_gl_font_LinuxNativeFont_FC_SLANT_OBLIQUE 110L

#undef org_apache_harmony_awt_gl_font_LinuxNativeFont_FC_WEIGHT_MEDIUM
#define org_apache_harmony_awt_gl_font_LinuxNativeFont_FC_WEIGHT_MEDIUM 100L


/* Native methods */

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getFontFamiliesNames()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getFontFamiliesNames(JNIEnv *, jclass);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.embedFontNative(Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_embedFontNative(JNIEnv *, jclass, 
    jstring);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.initializeFont(Lorg/apache/harmony/awt/gl/font/LinuxFont;Ljava/lang/String;IILjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_initializeFont(JNIEnv *, jclass, 
    jobject, jstring, jint, jint, jstring);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.initializeFontFromFP(Lorg/apache/harmony/awt/gl/font/LinuxFont;Ljava/lang/String;I)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_initializeFontFromFP(JNIEnv *, jclass, 
    jobject, jstring, jint);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getNumGlyphsNative(J)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getNumGlyphsNative(JNIEnv *, jclass, 
    jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.canDisplayCharNative(JC)Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_canDisplayCharNative(JNIEnv *, jclass, 
    jlong, jchar);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getFamilyNative(J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getFamilyNative(JNIEnv *, jclass, 
    jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getFontPSNameNative(J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getFontPSNameNative(JNIEnv *, jclass, 
    jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.pFontFree(JJ)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_pFontFree(JNIEnv *, jclass, 
    jlong, jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getItalicAngleNative(JI)F
 */
JNIEXPORT jfloat JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getItalicAngleNative(JNIEnv *, jclass, 
    jlong, jint);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getFonts()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getFonts(JNIEnv *, jclass);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getNativeLineMetrics(JIZZI)[F
 */
JNIEXPORT jfloatArray JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getNativeLineMetrics(JNIEnv *, jclass, 
    jlong, jint, jboolean, jboolean, jint);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getGlyphInfoNative(JCI)[F
 */
JNIEXPORT jfloatArray JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getGlyphInfoNative(JNIEnv *, jclass, 
    jlong, jchar, jint);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getGlyphPxlInfoNative(JJC)[I
 */
JNIEXPORT jintArray JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getGlyphPxlInfoNative(JNIEnv *, jclass, 
    jlong, jlong, jchar);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getGlyphCodesNative(JLjava/lang/String;I)[I
 */
JNIEXPORT jintArray JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getGlyphCodesNative(JNIEnv *, jclass, 
    jlong, jstring, jint);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getGlyphCodeNative(JCJ)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getGlyphCodeNative(JNIEnv *, jclass, 
    jlong, jchar, jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.RemoveFontResource(Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_RemoveFontResource(JNIEnv *, jclass, 
    jstring);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.NativeInitGlyphImage(JC)[B
 */
JNIEXPORT jbyteArray JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_NativeInitGlyphImage(JNIEnv *, jclass, 
    jlong, jchar);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.drawStringNative(JJJJII[CIJ)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_drawStringNative(JNIEnv *, jclass, 
    jlong, jlong, jlong, jlong, jint, jint, jcharArray, jint, jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.NativeInitGlyphBitmap(JC)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_NativeInitGlyphBitmap(JNIEnv *, jclass, 
    jlong, jchar);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getFontNameNative(J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getFontNameNative(JNIEnv *, jclass, 
    jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.createXftDrawNative(JJJ)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_createXftDrawNative(JNIEnv *, jclass, 
    jlong, jlong, jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.freeXftDrawNative(J)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_freeXftDrawNative(JNIEnv *, jclass, 
    jlong);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.xftDrawSetSubwindowModeNative(JI)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_xftDrawSetSubwindowModeNative(JNIEnv *, jclass, 
    jlong, jint);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.XftDrawSetClipRectangles(JIIJI)Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_XftDrawSetClipRectangles(JNIEnv *, jclass, 
    jlong, jint, jint, jlong, jint);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getGlyphOutline(JC)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getGlyphOutline(JNIEnv *, jclass, 
    jlong, jchar);

/*
 * Method: org.apache.harmony.awt.gl.font.LinuxNativeFont.getExtraMetricsNative(JII)[F
 */
JNIEXPORT jfloatArray JNICALL
Java_org_apache_harmony_awt_gl_font_LinuxNativeFont_getExtraMetricsNative(JNIEnv *, jclass, 
    jlong, jint, jint);

#ifdef __cplusplus
}
#endif

#endif /* _ORG_APACHE_HARMONY_AWT_GL_FONT_LINUXNATIVEFONT_H */

