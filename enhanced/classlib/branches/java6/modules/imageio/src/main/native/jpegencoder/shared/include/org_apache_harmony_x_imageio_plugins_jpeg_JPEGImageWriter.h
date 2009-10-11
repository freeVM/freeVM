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
/*
 * THE FILE HAS BEEN AUTOGENERATED BY IJH TOOL.
 * Please be aware that all changes made to this file manually
 * will be overwritten by the tool if it runs again.
 */
/**
 * @author Alexey A. Petrenko
 * 
 */

#include <jni.h>


/* Header for class org.apache.harmony.x.imageio.plugins.jpeg.JPEGImageWriter */

#ifndef _ORG_APACHE_HARMONY_X_IMAGEIO_PLUGINS_JPEG_JPEGIMAGEWRITER_H
#define _ORG_APACHE_HARMONY_X_IMAGEIO_PLUGINS_JPEG_JPEGIMAGEWRITER_H

#ifdef __cplusplus
extern "C" {
#endif


/* Native methods */

/*
 * Method: org.apache.harmony.x.imageio.plugins.jpeg.JPEGImageWriter.dispose(J)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_x_imageio_plugins_jpeg_JPEGImageWriter_dispose(JNIEnv *, jobject, 
    jlong);

/*
 * Method: org.apache.harmony.x.imageio.plugins.jpeg.JPEGImageWriter.initWriterIds(Ljava/lang/Class;)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_x_imageio_plugins_jpeg_JPEGImageWriter_initWriterIds(JNIEnv *, jclass, 
    jclass);

/*
 * Method: org.apache.harmony.x.imageio.plugins.jpeg.JPEGImageWriter.initCompressionObj()J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_x_imageio_plugins_jpeg_JPEGImageWriter_initCompressionObj(JNIEnv *, jobject);

/*
 * Method: org.apache.harmony.x.imageio.plugins.jpeg.JPEGImageWriter.setIOS(Ljavax/imageio/stream/ImageOutputStream;J)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_x_imageio_plugins_jpeg_JPEGImageWriter_setIOS(JNIEnv *, jobject, 
    jobject, jlong);

/*
 * Method: org.apache.harmony.x.imageio.plugins.jpeg.JPEGImageWriter.encode([BIIIIIIIZ[[IJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_x_imageio_plugins_jpeg_JPEGImageWriter_encode(JNIEnv *, jobject, 
    jbyteArray, jint, jint, jint, jint, jint, jint, jint, jboolean, jobjectArray, jlong);


#ifdef __cplusplus
}
#endif

#endif /* _ORG_APACHE_HARMONY_X_IMAGEIO_PLUGINS_JPEG_JPEGIMAGEWRITER_H */

