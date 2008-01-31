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

#include "TIS_620.h"

#define jlong2addr(a, x) ((a *)((int)(x)))

JNIEXPORT void JNICALL Java_org_apache_harmony_niochar_charset_additional_TIS_1620_00024Encoder_nEncode
  (JNIEnv *env, jobject obj, jlong outAddr, jint absolutePos, jcharArray array, jint arrayOffset, jintArray result){

    static jboolean table[] = {
      
     0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,
     0x10,0x11,0x12,0x13,0x14,0x15,0x16,0x17,0x18,0x19,0x7F,0x1B,0x1A,0x1D,0x1E,0x1F,
     0x20,0x21,0x22,0x23,0x24,0x25,0x26,0x27,0x28,0x29,0x2A,0x2B,0x2C,0x2D,0x2E,0x2F,
     0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x3A,0x3B,0x3C,0x3D,0x3E,0x3F,
     0x40,0x41,0x42,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0x4A,0x4B,0x4C,0x4D,0x4E,0x4F,
     0x50,0x51,0x52,0x53,0x54,0x55,0x56,0x57,0x58,0x59,0x5A,0x5B,0x5C,0x5D,0x5E,0x5F,
     0x60,0x61,0x62,0x63,0x64,0x65,0x66,0x67,0x68,0x69,0x6A,0x6B,0x6C,0x6D,0x6E,0x6F,
     0x70,0x71,0x72,0x73,0x74,0x75,0x76,0x77,0x78,0x79,0x7A,0x7B,0x7C,0x7D,0x7E,0x1C,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0xFF,0x00,0xFC,0x00,0x00,0x00,0xFE,0x00,0x00,0x00,0x00,0x00,0xFD,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
      
     0x00,0xA1,0xA2,0xA3,0xA4,0xA5,0xA6,0xA7,0xA8,0xA9,0xAA,0xAB,0xAC,0xAD,0xAE,0xAF,
     0xB0,0xB1,0xB2,0xB3,0xB4,0xB5,0xB6,0xB7,0xB8,0xB9,0xBA,0xBB,0xBC,0xBD,0xBE,0xBF,
     0xC0,0xC1,0xC2,0xC3,0xC4,0xC5,0xC6,0xC7,0xC8,0xC9,0xCA,0xCB,0xCC,0xCD,0xCE,0xCF,
     0xD0,0xD1,0xD2,0xD3,0xD4,0xD5,0xD6,0xD7,0xD8,0xD9,0xDA,0x00,0x00,0x00,0x00,0xDF,
     0xE0,0xE1,0xE2,0xE3,0xE4,0xE5,0xE6,0xE7,0xE8,0xE9,0xEA,0xEB,0xEC,0xED,0xEE,0xEF,
     0xF0,0xF1,0xF2,0xF3,0xF4,0xF5,0xF6,0xF7,0xF8,0xF9,0xFA,0xFB,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
     0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
     };

     static int encodeIndex[] = {
      0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
      -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
      -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
      -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
      -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
      -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
      -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
      -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
     };

    jint position = absolutePos;
    int i;
    jchar input1;
    jchar *in = (*env)->GetCharArrayElements(env, array, NULL);
    jint *res = (*env)->GetIntArrayElements(env, result, NULL);
    for(i=0; i<res[0]; i++) {
         jchar input = in[arrayOffset+i];
         if( input > 0x0E5B) { 
             if (input >= 0xD800 && input <= 0xDFFF) {
               if(i+1<res[0]) {
                 input1 = in[arrayOffset+i+1];
                 if(input1 >= 0xD800 && input1 <= 0xDFFF) {
                   res[0] = absolutePos - position; res[1] = 2;
                   (*env)->ReleaseIntArrayElements(env, result, res, 0);
                   (*env)->ReleaseCharArrayElements(env, array, in, 0);
                   return;
                 }
               } else {
                 res[0]=absolutePos - position; res[1] = 0;
                 (*env)->ReleaseIntArrayElements(env, result, res, 0);
                 (*env)->ReleaseCharArrayElements(env, array, in, 0);
                 return;
               }
               res[0]=absolutePos - position; res[1] = -1;
               (*env)->ReleaseIntArrayElements(env, result, res, 0);
               (*env)->ReleaseCharArrayElements(env, array, in, 0);
               return;
             }
             res[0]=absolutePos - position; res[1] = 1;
             (*env)->ReleaseCharArrayElements(env, array, in, 0);
             (*env)->ReleaseIntArrayElements(env, result, res, 0);
             return;
         }
         if( input < 0x1A ) {
             *(jlong2addr(jbyte, outAddr) + position++) = (jbyte)input;
         }else{
             int index = (int)input >> 8;
             index = encodeIndex[index];
             if(index < 0) {
                 res[0]=absolutePos - position; res[1] = 1;
                 (*env)->ReleaseCharArrayElements(env, array, in, 0);
                 (*env)->ReleaseIntArrayElements(env, result, res, 0);
                 return;
             }
             index <<= 8;
             index += (int)input & 0xFF;
             if(table[input] != 0){
                 *(jlong2addr(jbyte, outAddr) + position++) = table[input];
             }else{
                 res[0]= absolutePos - position; res[1]=1;
                 (*env)->ReleaseIntArrayElements(env, result, res, 0);
                 (*env)->ReleaseCharArrayElements(env, array, in, 0);
                 return;
             }
         }
    }
    res[0]=position - absolutePos;
    (*env)->ReleaseIntArrayElements(env, result, res, 0);
    (*env)->ReleaseCharArrayElements(env, array, in, 0);
    return;
}

JNIEXPORT jint JNICALL Java_org_apache_harmony_niochar_charset_additional_TIS_1620_00024Decoder_nDecode
  (JNIEnv *env, jobject obj, jcharArray outArr, jint arrPosition, jint remaining, jlong inAddr, jint absolutePos)
{ 

    unsigned int table[] = {
     0x001C,0x001B,0x007F,0x001D,0x001E,0x001F,
     0x0020,0x0021,0x0022,0x0023,0x0024,0x0025,0x0026,0x0027,
     0x0028,0x0029,0x002A,0x002B,0x002C,0x002D,0x002E,0x002F,
     0x0030,0x0031,0x0032,0x0033,0x0034,0x0035,0x0036,0x0037,
     0x0038,0x0039,0x003A,0x003B,0x003C,0x003D,0x003E,0x003F,
     0x0040,0x0041,0x0042,0x0043,0x0044,0x0045,0x0046,0x0047,
     0x0048,0x0049,0x004A,0x004B,0x004C,0x004D,0x004E,0x004F,
     0x0050,0x0051,0x0052,0x0053,0x0054,0x0055,0x0056,0x0057,
     0x0058,0x0059,0x005A,0x005B,0x005C,0x005D,0x005E,0x005F,
     0x0060,0x0061,0x0062,0x0063,0x0064,0x0065,0x0066,0x0067,
     0x0068,0x0069,0x006A,0x006B,0x006C,0x006D,0x006E,0x006F,
     0x0070,0x0071,0x0072,0x0073,0x0074,0x0075,0x0076,0x0077,
     0x0078,0x0079,0x007A,0x007B,0x007C,0x007D,0x007E,0x001A,
     0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
     0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
     0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
     0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
     0x0E48,0x0E01,0x0E02,0x0E03,0x0E04,0x0E05,0x0E06,0x0E07,
     0x0E08,0x0E09,0x0E0A,0x0E0B,0x0E0C,0x0E0D,0x0E0E,0x0E0F,
     0x0E10,0x0E11,0x0E12,0x0E13,0x0E14,0x0E15,0x0E16,0x0E17,
     0x0E18,0x0E19,0x0E1A,0x0E1B,0x0E1C,0x0E1D,0x0E1E,0x0E1F,
     0x0E20,0x0E21,0x0E22,0x0E23,0x0E24,0x0E25,0x0E26,0x0E27,
     0x0E28,0x0E29,0x0E2A,0x0E2B,0x0E2C,0x0E2D,0x0E2E,0x0E2F,
     0x0E30,0x0E31,0x0E32,0x0E33,0x0E34,0x0E35,0x0E36,0x0E37,
     0x0E38,0x0E39,0x0E3A,0x0E49,0x0E4A,0x0E4B,0x0E4C,0x0E3F,
     0x0E40,0x0E41,0x0E42,0x0E43,0x0E44,0x0E45,0x0E46,0x0E47,
     0x0E48,0x0E49,0x0E4A,0x0E4B,0x0E4C,0x0E4D,0x0E4E,0x0E4F,
     0x0E50,0x0E51,0x0E52,0x0E53,0x0E54,0x0E55,0x0E56,0x0E57,
     0x0E58,0x0E59,0x0E5A,0x0E5B,0x00A2,0x00AC,0x00A6,0x00A0
    };

   jchar *out = (*env)->GetCharArrayElements(env, outArr, NULL);

   jint position = absolutePos;	
   int i; 
   unsigned char input;
   for(i=0; i < remaining; i++) {
        input = *(jlong2addr(jbyte, inAddr) + position++);
        if(input < 0x1A) {              	
            out[arrPosition+i] = (jchar)input;
        }else{
            if(table[input - 26] != 0){
                out[arrPosition+i] = (jchar)table[input - 26];
            }else{
                (*env)->ReleaseCharArrayElements(env, outArr, out, 0);
                return - (position-1 - absolutePos) ;
            }
        }
   }
   (*env)->ReleaseCharArrayElements(env, outArr, out, 0);
   return position-absolutePos;
}
