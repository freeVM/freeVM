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

package org.apache.harmony.niochar.charset.additional;

import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;

import org.apache.harmony.nio.AddressUtil;

public class x_IBM1124 extends Charset {

        public x_IBM1124(String csName, String[] aliases) {
            super(csName, aliases);
        }

        public boolean contains(Charset cs) {
            return cs.name().equalsIgnoreCase("x-ibm-1124_P100-1996") ;
        }

        public CharsetDecoder newDecoder() {
            return new Decoder(this);
        }

        public CharsetEncoder newEncoder() {
            return new Encoder(this);
        }

	private static final class Decoder extends CharsetDecoder{
		private Decoder(Charset cs){
			super(cs, 1, 1);

		}

		public native int nDecode(char[] array, int arrPosition, int remaining, long outAddr, int absolutePos);


		protected CoderResult decodeLoop(ByteBuffer bb, CharBuffer cb){
                        int cbRemaining = cb.remaining();
		        if(bb.isDirect() && bb.hasRemaining() && cb.hasArray()){
		            int toProceed = bb.remaining();
		            int cbPos = cb.position();
		            int bbPos = bb.position();
		            boolean throwOverflow = false; 
		            if( cbRemaining < toProceed ) { 
		                toProceed = cbRemaining;
                                throwOverflow = true;
                            }
                            int res = nDecode(cb.array(), cb.arrayOffset()+cbPos, toProceed, AddressUtil.getDirectBufferAddress(bb), bbPos);
                            bb.position(bbPos+res);
                            cb.position(cbPos+res);
                            if(throwOverflow) return CoderResult.OVERFLOW;
                        }else{
                            if(bb.hasArray() && cb.hasArray()) {
                                int rem = bb.remaining();
                                rem = cbRemaining >= rem ? rem : cbRemaining;
                                byte[] bArr = bb.array();
                                char[] cArr = cb.array();
                                int bStart = bb.position();
                                int cStart = cb.position();
                                int i;
                                for(i=bStart; i<bStart+rem; i++) {
                                    int in = (int)bArr[i];
                                    if(in < 0 && in >= -95){
                                        int index = (int)in + 95;
                                        cArr[cStart++] = (char)arr[index];
                                    }else {
                                        cArr[cStart++] = (char)(in & 0xFF);
                                    }
                                }
                                bb.position(i);
                                cb.position(cStart);
                                if(rem == cbRemaining && bb.hasRemaining()) return CoderResult.OVERFLOW;
                            } else {
                                while(bb.hasRemaining()){
                                    if( cbRemaining == 0 ) return CoderResult.OVERFLOW;
                                    int in = (int)bb.get();
                                    if(in < 0 && in >= -95){
                                        int index = (int)in + 95;
                                        cb.put(arr[index]);
                                    }else {
                                        cb.put((char)(in & 0xFF));
                                    }
                                cbRemaining--;
                                }
                            }
			} 
                        return CoderResult.UNDERFLOW;
		}

		final static char[] arr = {
                0x0401,0x0402,0x0490,0x0404,0x0405,0x0406,0x0407,
                0x0408,0x0409,0x040A,0x040B,0x040C,0x00AD,0x040E,0x040F,
                0x0410,0x0411,0x0412,0x0413,0x0414,0x0415,0x0416,0x0417,
                0x0418,0x0419,0x041A,0x041B,0x041C,0x041D,0x041E,0x041F,
                0x0420,0x0421,0x0422,0x0423,0x0424,0x0425,0x0426,0x0427,
                0x0428,0x0429,0x042A,0x042B,0x042C,0x042D,0x042E,0x042F,
                0x0430,0x0431,0x0432,0x0433,0x0434,0x0435,0x0436,0x0437,
                0x0438,0x0439,0x043A,0x043B,0x043C,0x043D,0x043E,0x043F,
                0x0440,0x0441,0x0442,0x0443,0x0444,0x0445,0x0446,0x0447,
                0x0448,0x0449,0x044A,0x044B,0x044C,0x044D,0x044E,0x044F,
                0x2116,0x0451,0x0452,0x0491,0x0454,0x0455,0x0456,0x0457,
                0x0458,0x0459,0x045A,0x045B,0x045C,0x00A7,0x045E,0x045F
		};
        }

	private static final class Encoder extends CharsetEncoder{
		private Encoder(Charset cs){
			super(cs, 1, 1);
		}
                   
		private native void nEncode(long outAddr, int absolutePos, char[] array, int arrPosition, int[] res);
                                                                                                                          
		protected CoderResult encodeLoop(CharBuffer cb, ByteBuffer bb){
                        int bbRemaining = bb.remaining();
                        if(bb.isDirect() && cb.hasRemaining() && cb.hasArray()){
		            int toProceed = cb.remaining();
		            int cbPos = cb.position();
		            int bbPos = bb.position();
		            boolean throwOverflow = false; 
		            if( bbRemaining < toProceed ) { 
		                toProceed = bbRemaining; 
                                throwOverflow = true;
                            }
                            int[] res = {toProceed, 0};
                            nEncode(AddressUtil.getDirectBufferAddress(bb), bbPos, cb.array(), cb.arrayOffset()+cbPos, res);
                            if( res[0] <= 0 ) {
                                bb.position(bbPos-res[0]);
                                cb.position(cbPos-res[0]);
                                if(res[1]!=0) {
                                    if(res[1] < 0)
                                        return CoderResult.malformedForLength(-res[1]);
                                    else
                                        return CoderResult.unmappableForLength(res[1]);
                                }
                            }else{
                                bb.position(bbPos+res[0]);
                                cb.position(cbPos+res[0]);
                                if(throwOverflow) return CoderResult.OVERFLOW;
                            }
                        }else{
                            if(bb.hasArray() && cb.hasArray()) {
                                byte[] byteArr = bb.array();
                                char[] charArr = cb.array();
                                int rem = cb.remaining();
                                int byteArrStart = bb.position();
                                rem = bbRemaining <= rem ? bbRemaining : rem;
                                int x;
                                for(x = cb.position(); x < cb.position()+rem; x++) {
                                    char c = charArr[x];
                                    if(c > (char)0x2116){
                                        if (c >= 0xD800 && c <= 0xDFFF) {
                                            if(x+1 < cb.limit()) {
                                                char c1 = charArr[x+1];
                                                if(c1 >= 0xD800 && c1 <= 0xDFFF) {
                                                    cb.position(x); bb.position(byteArrStart);
                                                    return CoderResult.unmappableForLength(2);
                                                }
                                            } else {
                                                cb.position(x); bb.position(byteArrStart);
                                                return CoderResult.UNDERFLOW;
                                            }
                                            cb.position(x); bb.position(byteArrStart);
                                            return CoderResult.malformedForLength(1);
                                        }
                                        cb.position(x); bb.position(byteArrStart);
                                        return CoderResult.unmappableForLength(1);
                                    }else{
                                        if(c < 0xA1) {
                                            byteArr[byteArrStart++] = (byte)c;
                                        } else {
                                            int index = (int)c >> 8;
                                            index = encodeIndex[index];
                                            if(index < 0) {
                                                cb.position(x); bb.position(byteArrStart);
                                                return CoderResult.unmappableForLength(1);
                                            }
                                            index <<= 8;
                                            index += (int)c & 0xFF;
                                            if((byte)arr[index] != 0){
                                                byteArr[byteArrStart++] = (byte)arr[index];
                                            }else{
                                                cb.position(x); bb.position(byteArrStart);
                                                return CoderResult.unmappableForLength(1);
                                            }
                                        }
                                    }
                                }
                                cb.position(x);
                                bb.position(byteArrStart);
                                if(rem == bbRemaining && cb.hasRemaining()) {
                                    return CoderResult.OVERFLOW;
                                }
                            } else {
                                while(cb.hasRemaining()){
                                    if( bbRemaining == 0 ) return CoderResult.OVERFLOW;
                                    char c = cb.get();
                                    if(c > (char)0x2116){   
                                        if (c >= 0xD800 && c <= 0xDFFF) {
                                            if(cb.hasRemaining()) {
                                                char c1 = cb.get();
                                                if(c1 >= 0xD800 && c1 <= 0xDFFF) {
                                                    cb.position(cb.position()-2);
                                                    return CoderResult.unmappableForLength(2);
                                                } else {
                                                    cb.position(cb.position()-1);
                                                }
                                            } else {
                                                cb.position(cb.position()-1);
                                                return CoderResult.UNDERFLOW;
                                            }
                                            cb.position(cb.position()-1);
                                            return CoderResult.malformedForLength(1);
                                        }
                                        cb.position(cb.position()-1);
                                        return CoderResult.unmappableForLength(1);
                                    }else{
                                        if(c < 0xA1) {
                                            bb.put((byte)c);
                                        } else {
                                            int index = (int)c >> 8;
                                            index = encodeIndex[index];
                                            if(index < 0) {
                                                cb.position(cb.position()-1);
                                                return CoderResult.unmappableForLength(1);
                                            }
                                            index <<= 8;
                                            index += (int)c & 0xFF;
                                            if((byte)arr[index] != 0){
                                                bb.put((byte)arr[index]);
                                            }else{
                                                cb.position(cb.position()-1);
                                                return CoderResult.unmappableForLength(1);
                                            }
                                        }
                                        bbRemaining--;
                                    }
                                }
			    }
			}
			return CoderResult.UNDERFLOW;
		}

                final static char arr[] = {
                 
                0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,
                0x10,0x11,0x12,0x13,0x14,0x15,0x16,0x17,0x18,0x19,0x1A,0x1B,0x1C,0x1D,0x1E,0x1F,
                0x20,0x21,0x22,0x23,0x24,0x25,0x26,0x27,0x28,0x29,0x2A,0x2B,0x2C,0x2D,0x2E,0x2F,
                0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x3A,0x3B,0x3C,0x3D,0x3E,0x3F,
                0x40,0x41,0x42,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0x4A,0x4B,0x4C,0x4D,0x4E,0x4F,
                0x50,0x51,0x52,0x53,0x54,0x55,0x56,0x57,0x58,0x59,0x5A,0x5B,0x5C,0x5D,0x5E,0x5F,
                0x60,0x61,0x62,0x63,0x64,0x65,0x66,0x67,0x68,0x69,0x6A,0x6B,0x6C,0x6D,0x6E,0x6F,
                0x70,0x71,0x72,0x73,0x74,0x75,0x76,0x77,0x78,0x79,0x7A,0x7B,0x7C,0x7D,0x7E,0x7F,
                0x80,0x81,0x82,0x83,0x84,0x85,0x86,0x87,0x88,0x89,0x8A,0x8B,0x8C,0x8D,0x8E,0x8F,
                0x90,0x91,0x92,0x93,0x94,0x95,0x96,0x97,0x98,0x99,0x9A,0x9B,0x9C,0x9D,0x9E,0x9F,
                0xA0,0x00,0x00,0x00,0x00,0x00,0x00,0xFD,0x00,0x00,0x00,0x00,0x00,0xAD,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                 
                0x00,0xA1,0xA2,0x00,0xA4,0xA5,0xA6,0xA7,0xA8,0xA9,0xAA,0xAB,0xAC,0x00,0xAE,0xAF,
                0xB0,0xB1,0xB2,0xB3,0xB4,0xB5,0xB6,0xB7,0xB8,0xB9,0xBA,0xBB,0xBC,0xBD,0xBE,0xBF,
                0xC0,0xC1,0xC2,0xC3,0xC4,0xC5,0xC6,0xC7,0xC8,0xC9,0xCA,0xCB,0xCC,0xCD,0xCE,0xCF,
                0xD0,0xD1,0xD2,0xD3,0xD4,0xD5,0xD6,0xD7,0xD8,0xD9,0xDA,0xDB,0xDC,0xDD,0xDE,0xDF,
                0xE0,0xE1,0xE2,0xE3,0xE4,0xE5,0xE6,0xE7,0xE8,0xE9,0xEA,0xEB,0xEC,0xED,0xEE,0xEF,
                0x00,0xF1,0xF2,0x00,0xF4,0xF5,0xF6,0xF7,0xF8,0xF9,0xFA,0xFB,0xFC,0x00,0xFE,0xFF,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0xA3,0xF3,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                 
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0xF0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
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

                final static int[] encodeIndex = {
                 0,-1,-1,-1,1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                 -1,2,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                 -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                 -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                 -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                 -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                 -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                 -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
                };
	}         
}