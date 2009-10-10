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
 * @author Igor V. Stolyarov
 */
/*
 * THE FILE HAS BEEN AUTOGENERATED BY INTEL IJH TOOL.
 * Please be aware that all changes made to this file manually
 * will be overwritten by the tool if it runs again.
 */

#include <jni.h>


/* Header for class org.apache.harmony.awt.gl.linux.XGraphics2D */

#ifndef _ORG_APACHE_HARMONY_AWT_GL_LINUX_XGRAPHICS2D
#define _ORG_APACHE_HARMONY_AWT_GL_LINUX_XGRAPHICS2D

#ifdef __cplusplus
extern "C" {
#endif


/* Native methods */

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.createGC(JJJJ)J
 */
JNIEXPORT jlong JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_createGC(JNIEnv *, jobject, 
    jlong, jlong, jlong, jlong);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.freeGC(JJ)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_freeGC(JNIEnv *, jobject, 
    jlong, jlong);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.setFunction(JJI)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_setFunction(JNIEnv *, jobject, 
    jlong, jlong, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.setForeground(JJJI)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_setForeground(JNIEnv *, jobject, 
    jlong, jlong, jlong, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.copyArea(JJJJIIIIII)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_copyArea(JNIEnv *, jobject, 
    jlong, jlong, jlong, jlong, jint, jint, jint, jint, jint, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.setClipMask(JJJ)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_setClipMask(JNIEnv *, jobject, 
    jlong, jlong, jlong);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.setClipRectangles(JJII[III)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_setClipRectangles(JNIEnv *, jobject, 
    jlong, jlong, jint, jint, jintArray, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.drawArc(JJJIIIIII)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_drawArc(JNIEnv *, jobject, 
    jlong, jlong, jlong, jint, jint, jint, jint, jint, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.drawLine(JJJIIII)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_drawLine(JNIEnv *, jobject, 
    jlong, jlong, jlong, jint, jint, jint, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.drawLines(JJJ[SI)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_drawLines(JNIEnv *, jobject, 
    jlong, jlong, jlong, jshortArray, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.drawRectangle(JJJIIII)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_drawRectangle(JNIEnv *, jobject, 
    jlong, jlong, jlong, jint, jint, jint, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.drawPolygon(JJJ[SI)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_drawPolygon(JNIEnv *, jobject, 
    jlong, jlong, jlong, jshortArray, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.fillArc(JJJIIIIII)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_fillArc(JNIEnv *, jobject, 
    jlong, jlong, jlong, jint, jint, jint, jint, jint, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.fillRectangles(JJJ[II)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_fillRectangles(JNIEnv *, jobject, 
    jlong, jlong, jlong, jintArray, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.fillRectangle(JJJIIII)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_fillRectangle(JNIEnv *, jobject, 
    jlong, jlong, jlong, jint, jint, jint, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.fillPolygon(JJJ[SI)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_fillPolygon(JNIEnv *, jobject, 
    jlong, jlong, jlong, jshortArray, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.setStroke(JJIIII[BI)I
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_setStroke(JNIEnv *, jobject, 
    jlong, jlong, jint, jint, jint, jint, jbyteArray, jint);

/*
 * Method: org.apache.harmony.awt.gl.linux.XGraphics2D.flush(J)V
 */
JNIEXPORT jint JNICALL 
Java_org_apache_harmony_awt_gl_linux_XGraphics2D_flush(JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif

#endif /* _ORG_APACHE_HARMONY_AWT_GL_LINUX_XGRAPHICS2D */
