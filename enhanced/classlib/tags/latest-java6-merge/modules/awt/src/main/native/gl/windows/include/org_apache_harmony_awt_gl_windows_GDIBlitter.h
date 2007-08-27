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


/* Header for class org.apache.harmony.awt.gl.windows.GDIBlitter */

#ifndef _ORG_APACHE_HARMONY_AWT_GL_WINDOWS_GDIBLITTER_H
#define _ORG_APACHE_HARMONY_AWT_GL_WINDOWS_GDIBLITTER_H

#ifdef __cplusplus
extern "C" {
#endif


/* Native methods */

/*
 * Method: org.apache.harmony.awt.gl.windows.GDIBlitter.xorBitmap(IIJIIJIII[D[II)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_awt_gl_windows_GDIBlitter_xorBitmap(JNIEnv *, jobject, 
    jint, jint, jlong, jint, jint, jlong, jint, jint, jint, jdoubleArray, jintArray, jint);

/*
 * Method: org.apache.harmony.awt.gl.windows.GDIBlitter.xorImage(IIJLjava/lang/Object;IIJIII[D[IIZ)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_awt_gl_windows_GDIBlitter_xorImage(JNIEnv *, jobject, 
    jint, jint, jlong, jobject, jint, jint, jlong, jint, jint, jint, jdoubleArray, jintArray, jint, jboolean);

/*
 * Method: org.apache.harmony.awt.gl.windows.GDIBlitter.bltBitmap(IIJIIJIIIF[D[II)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_awt_gl_windows_GDIBlitter_bltBitmap(JNIEnv *, jobject, 
    jint, jint, jlong, jint, jint, jlong, jint, jint, jint, jfloat, jdoubleArray, jintArray, jint);

/*
 * Method: org.apache.harmony.awt.gl.windows.GDIBlitter.bltImage(IIJLjava/lang/Object;IIJIIIF[D[IIZ)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_awt_gl_windows_GDIBlitter_bltImage(JNIEnv *, jobject, 
    jint, jint, jlong, jobject, jint, jint, jlong, jint, jint, jint, jfloat, jdoubleArray, jintArray, jint, jboolean);

/*
 * Method: org.apache.harmony.awt.gl.windows.GDIBlitter.bltBGImage(IIJLjava/lang/Object;IIJIIIIF[D[IIZ)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_awt_gl_windows_GDIBlitter_bltBGImage(JNIEnv *, jobject, 
    jint, jint, jlong, jobject, jint, jint, jlong, jint, jint, jint, jint, jfloat, jdoubleArray, jintArray, jint, jboolean);


#ifdef __cplusplus
}
#endif

#endif /* _ORG_APACHE_HARMONY_AWT_GL_WINDOWS_GDIBLITTER_H */

