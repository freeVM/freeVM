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
 */
package org.apache.harmony.awt.gl.color;

import java.awt.color.ICC_Profile;
import java.util.HashMap;

/**
 * This class is a wrapper for the native CMM library
 */
public class NativeCMM {

    /**
     * Storage for profile handles, since they are private
     * in ICC_Profile, but we need access to them.
     */
    private static HashMap profileHandles = new HashMap();

    private static boolean isCMMLoaded = false;

    public static void addHandle(ICC_Profile key, long handle) {
        profileHandles.put(key, new Long(handle));
    }

    public static void removeHandle(ICC_Profile key) {
        profileHandles.remove(key);
    }

    public static long getHandle(ICC_Profile key) {
        return ((Long) profileHandles.get(key)).longValue();
    }

    /* ICC profile management */
    public static native long cmmOpenProfile(byte[] data);
    public static native void cmmCloseProfile(long profileID);
    public static native int cmmGetProfileSize(long profileID);
    public static native void cmmGetProfile(long profileID, byte[] data);
    public static native int cmmGetProfileElementSize(long profileID, int signature);
    public static native void cmmGetProfileElement(long profileID, int signature,
                                           byte[] data);
    public static native void cmmSetProfileElement(long profileID, int tagSignature,
                                           byte[] data);


    /* ICC transforms */
    public static native long cmmCreateMultiprofileTransform(
            long[] profileHandles,
            int[] renderingIntents
        );
    public static native void cmmDeleteTransform(long transformHandle);
    public static native void cmmTranslateColors(long transformHandle,
            NativeImageFormat src,
            NativeImageFormat dest);

    static void loadCMM() {
        if (!isCMMLoaded) {
            java.security.AccessController.doPrivileged(
                  new java.security.PrivilegedAction() {
                    public Object run() {
                        System.loadLibrary("lcmm");
                        return null;
                    }
            } );
            isCMMLoaded = true;
        }
    }

    /* load native CMM library */
    static {
        loadCMM();
    }
}
