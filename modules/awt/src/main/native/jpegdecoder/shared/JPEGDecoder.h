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
 * @author Oleg V. Khaschansky
 * @version $Revision$
 * 
 */

#ifndef _Included_JPEGDecoder
#define _Included_JPEGDecoder

#include <string.h>
#include <malloc.h>
#include <assert.h>

#include "org_apache_harmony_awt_gl_image_JpegDecoder.h"
#include "jpeglib.h"
#include "jpegint.h"
#include "setjmp.h"

#include "exceptions.h"

#define MAX_BUFFER 32768

jfieldID img_JPEG_imageWidthID;
jfieldID img_JPEG_imageHeightID;
jfieldID img_JPEG_progressiveID;
jfieldID img_JPEG_jpegColorSpaceID;
jfieldID img_JPEG_bytesConsumedID;
jfieldID img_JPEG_currScanlineID;
jfieldID img_JPEG_hNativeDecoderID;

#endif
