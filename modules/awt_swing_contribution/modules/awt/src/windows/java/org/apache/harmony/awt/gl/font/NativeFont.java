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
 * @author Ilya S. Okomin
 * @version $Revision$
 */
package org.apache.harmony.awt.gl.font;

import java.awt.Font;

import java.util.*;

import org.apache.harmony.awt.gl.font.FontManager;
import org.apache.harmony.awt.gl.font.FontPeerImpl;
import org.apache.harmony.awt.gl.font.Glyph;

public class NativeFont {

    /**
     * List of family indexes in families array corresponding to the faces 
     * indexing.
     */
    public static int[] famIndices = null;

    /**
     * List of font styles of system fonts initialized using GDI
     * corresponding to faces indexing.
     */
    public static int[] fontStyles = null;

    /**
     * List of font types of system fonts initialized using GDI 
     * corresponding to the faces indexing.
     */
    public static int[] fontTypes = null;

    /**
     * List of font types of system fonts initialized using GDI.
     */
    public static String[] faces =  null;

    /**
     * The number of different fonts installed onto the system.
     */
    public static int fontsCount = 0;

    /**
     * List of all families installed onto the system.
     */
    public static String[] families= null;

    /**
     * Native method returns list of all families installed onto the system.
     */
    public static native String[] getFontFamiliesNames();

    /** 
     * Adds font resourse from file to the system. Returns true if font was added
     * successfully, false otherwise.
     *  
     * @param absolutePath String that represent absolute path to the font resource. 
     */
    public static native boolean embedFontNative(String absolutePath);

    /** 
     * Initiailze native GDI font object and return font handle, also sets font 
     * type and unicode ranges to the font peer parameter.
     * 
     * @param winFont Windows font peer
     * @param name the name of the font
     * @param style style of the font
     * 
     * @return native GDI font handle.
     */
    public static native long initializeFont(WindowsFont winFont, String name, int style);

    /**
     * Returns true if native GDI font can display char, false otherwise.
     * NullPointerException is thrown in case of GDI error.
     * 
     * @param hndFont GDI font handle
     * @param c specified char 
     */
    public static native boolean canDisplayCharNative(long hndFont, char c);

    /**
     * Returns GDI font object's family name if success, 
     * otherwise null and NullPointerException is thrown.
     * 
     * @param hndFont GDI font handle
     */
    public static native String getFamilyNative(long hndFont);

    /**
     * Returns GDI font's face name.
     * NullPointerException is thrown in case of GDI error.
     * 
     * @param hndFont GDI font handle
     */
    public static native String getFontNameNative(long hndFont);

    /**
     * Disposes native font resources.
     * 
     * @param hndFont GDI font handle
     * @return result of GDI library DeleteObject method
     */
    public static native int pFontFree(long hndFont);

    /**
     * Returns tangent of Italic angle of given Font.
     * NullPointerException is thrown if there is GDI error.
     * 
     * @param hndFont GDI font handle
     */
    public static native float getItalicAngleNative(long hndFont);

    /** 
     * Enumerates system font face names and updates fullNames, fontStyles, fontTypes arrays.
     * In case of exceptions in native code NullPointerException is thorwn.
     */
    public static native void enumSystemFonts();

    /** 
     * Returns an array of available system TrueType fonts names.
     * In case of errors in native code returned value is null.
     */
    public static native String[] getFonts();

    /**
     * Returns array of values of font metrics corresponding to the given GDI 
     * font object. null is returned and NullPointerException is thrown in case of GDI errors.
     * 
     * @param hFont GDI font handle
     * @param fontSize size of the font
     * @param usesFractionalMetrics true if results calculated using fractional metrics
     * @param fontType type of the specified font
     * */
    public static native float[] getNativeLineMetrics(long hFont, int fontSize, boolean isAntialiased, boolean usesFractionalMetrics, int fontType);

    /** 
     * Returns an array of glyph precise metrics values for the specified character.
     * null is returned and NullPointerException is thrown in case of GDI errors.
     * 
     * @param pFnt GDI font handle
     * @param c specified char
     * @param fontSize size of the font
     * */
    public static native float[] getGlyphInfoNative(long pFnt, char c, int fontSize);

    /** 
     * Returns array of glyph metrics values in pixels for the specified character
     * null is returned and NullPointerException is thrown in case of GDI errors.
     * 
     * @param pFnt GDI font handle
     * @param c specified char
     */
    public static native int[] getGlyphPxlInfoNative(long pFnt, char c);

    /**
     * Returns an array of the glyph codes corresponding to the characters from 
     * String parameter. null is returned and NullPointerException is thrown 
     * in case of GDI errors.
     * 
     * @param fnt GDI font handle
     * @param str specified string
     * @param len the length of the specified string
     */
    public static native int[] getGlyphCodesNative(long fnt, String str, int len);

    /**
     * Returns glyph code corresponding to the specified character. null is 
     * returned and NullPointerException is thrown in case of GDI errors.
     * 
     * @param fnt GDI font handle
     * @param uChar specified character
     */
    public static native int getGlyphCodeNative(long fnt, char uChar);

    /**
     * Removes font resource from file with specified path from the system.
     * 
     * @param tempFontFileName temporary font resource file path
     * @return zero if native call fails, non-zero if success 
     */
    public static native int RemoveFontResource(String tempFontFileName);

    /** 
     * Returns bitmap representation of the glyph image in 1 bit per pixel format
     *  for the specified Glyph object. null is 
     * returned and NullPointerException is thrown in case of GDI errors.
     * 
     * @param glyph specified Glyph object 
     */
    public static native byte[] NativeInitGlyphImage(Glyph glyph);

    /**
     * Initialization of the table with LCID values key = ctry_lang, value = LCID value 
     * in Windows API format. In case of null parameters method returns required size of 
     * arrays to store results.
     * 
     * @param shortStrings array of Strings to fill with languages from locale settings 
     * available on the system
     * @param LCIDs array of shorts to fill with LCID values from locale settings 
     * available on the system.
     * 
     * @return size of arrays if success, otherwise returns 0.
     */
    public static native int nativeInitLCIDsTable(String[] shortStrings, short[] LCIDs);

    /**
     * Returns size of polyheaders structure and set 
     * polyheaders pointer to the native array of TTPOLYGONHEADER elements. 
     * 
     * @param fnt GDI font handle
     * @param uChar specified character
     * @param polyheaders pointer to TTPOLYGONHEADER structure
     * @param size size of buffer with TTPOLYGONHEADER structure
     */
    public static native int getGlyphOutline(long fnt, char uChar, long polyheaders, int size);

    /***************************************************************************
    *
    *  GDI+ text native functions
    *
    ***************************************************************************/
    
    /**
     * Draws string at the specified coordinates using GDI+ objects defined in graphics info.
     * This method is applicable for drawing without affine transformes.
     */
    public static native int gdiPlusDrawText(long graphicsInfo, String text, int length, long font,
            float xOffset, float yOffset);
    
    /** GDI+ DriverStringOptions constants */
    public static final int DriverStringOptionsCmapLookup = 1;
    public static final int DriverStringOptionsVertical = 2;
    public static final int DriverStringOptionsRealizedAdvance = 4;
    public static final int DriverStringOptionsLimitSubpixel = 8;
    
    /**
     * Draws transformed char according to the matrix at the specified position.
     * @param gi GraphicsInfo pointer
     * @param chr specified character
     * @param font GDI font handle
     * @param x coordinate X
     * @param y coordinate Y
     * @param flags drawing flags, a set of DriverStringOptions constants
     * @param matrix transformation matrix
     * @return GDI+ result of the char drawing.
     */
    public static native int gdiPlusDrawDriverChar(long gi, char chr, long font, float x, float y, int flags, double[] matrix);

    /**
     * Draws string transformed according to the matrix, each character is drawn at 
     * the specified positions.
     * 
     * @param gi GraphicsInfo pointer
     * @param text specified String
     * @param length length of the specified String
     * @param font GDI font handle
     * @param x origin X coordinate
     * @param y origin Y coordinate
     * @param positions an array of positions defined for chars 
     * @param flags drawing flags, a set of DriverStringOptions constants
     * @param matrix transformation matrix
     * @return GDI+ result of the char drawing.
     */
    public static native int gdiPlusDrawDriverString(long gi, String text, int length, long font, float x, float y, double[] positions, int flags, double[] matrix);
    
    /**
     * Draws string transformed according to the matrix, each character is drawn at 
     * the specified positions.
     * 
     * @param gi GraphicsInfo pointer
     * @param text specified array of chars
     * @param length length of the specified chars array
     * @param font GDI font handle
     * @param positions an array of positions defined for chars
     * @param flags drawing flags, a set of DriverStringOptions constants
     * @param matrix transformation matrix
     * @return GDI+ result of the char drawing.
     */
    public static native int gdiPlusDrawDriverChars(long gi, char text[], int length, long font, double[] positions, int flags, double[] matrix);

    /** Releases hdc object in GDI+ Graphics object from the GraphicsInfo. */
    public static native void gdiPlusReleaseHDC(long gi, long hdc);

    /** Returns hdc object of the GDI+ Graphics object from the GraphicsInfo. */
    public static native long gdiPlusGetHDC(long gi);
    
    /***************************************************************************/

    /**
     * Initializes LCID table
     */
    public static void initLCIDsTable(Hashtable ht){
        int count = nativeInitLCIDsTable(null, null);

        if (count != 0){
            String[] shortStrings = new String[count];
            short[] LCIDs = new short[count];

            nativeInitLCIDsTable(shortStrings, LCIDs);

            for (int i = 0; i < count; i++){
                ht.put(new String(shortStrings[i]), new Short(LCIDs[i]));
            }
            /*
             * Because of native method nativeInitLCIDsTable returns only short 
             * strings in language_country format we have to add manually just 
             * language strings that Java locale support with LCID values (0x04**).
             */
             ht.put(new String("ar"), new Short((short)0x0401)); // ar-dz
             ht.put(new String("bg"), new Short((short)0x0402));
             ht.put(new String("ca"), new Short((short)0x0403));
             ht.put(new String("zh"), new Short((short)0x0404)); // zh-tw
             ht.put(new String("cs"), new Short((short)0x0405));
             ht.put(new String("da"), new Short((short)0x0406));
             ht.put(new String("de"), new Short((short)0x0407)); // de-de
             ht.put(new String("el"), new Short((short)0x0408));
             ht.put(new String("fi"), new Short((short)0x040b));
             ht.put(new String("fr"), new Short((short)0x040c)); // fr-fr
             ht.put(new String("iw"), new Short((short)0x040d)); // "he"
             ht.put(new String("hu"), new Short((short)0x040e));
             ht.put(new String("is"), new Short((short)0x040f));
             ht.put(new String("it"), new Short((short)0x0410)); // it-it
             ht.put(new String("ja"), new Short((short)0x0411));
             ht.put(new String("ko"), new Short((short)0x0412));
             ht.put(new String("nl"), new Short((short)0x0413)); // nl-nl
             ht.put(new String("no"), new Short((short)0x0414)); // no_no
             ht.put(new String("pl"), new Short((short)0x0415));
             ht.put(new String("pt"), new Short((short)0x0416)); // pt-br
             ht.put(new String("rm"), new Short((short)0x0417));
             ht.put(new String("ro"), new Short((short)0x0418));
             ht.put(new String("ru"), new Short((short)0x0419));
             ht.put(new String("hr"), new Short((short)0x041a));
             ht.put(new String("sk"), new Short((short)0x041b));
             ht.put(new String("sq"), new Short((short)0x041c));
             ht.put(new String("sv"), new Short((short)0x041d)); // sv-se
             ht.put(new String("th"), new Short((short)0x041e));
             ht.put(new String("tr"), new Short((short)0x041f));
             ht.put(new String("ur"), new Short((short)0x0420)); 
             ht.put(new String("in"), new Short((short)0x0421)); // "id"
             ht.put(new String("uk"), new Short((short)0x0422));
             ht.put(new String("be"), new Short((short)0x0423));
             ht.put(new String("sl"), new Short((short)0x0424));
             ht.put(new String("et"), new Short((short)0x0425));
             ht.put(new String("lv"), new Short((short)0x0426));
             ht.put(new String("lt"), new Short((short)0x0427));
             ht.put(new String("fa"), new Short((short)0x0429));
             ht.put(new String("vi"), new Short((short)0x042a));
             ht.put(new String("hy"), new Short((short)0x042b));
             ht.put(new String("eu"), new Short((short)0x042d));
             ht.put(new String("sb"), new Short((short)0x042e)); 
             ht.put(new String("mk"), new Short((short)0x042f));
             ht.put(new String("sx"), new Short((short)0x0430)); 
             ht.put(new String("ts"), new Short((short)0x0431)); 
             ht.put(new String("tn"), new Short((short)0x0432));
             ht.put(new String("xh"), new Short((short)0x0434)); 
             ht.put(new String("zu"), new Short((short)0x0435)); 
             ht.put(new String("af"), new Short((short)0x0436));
             ht.put(new String("fo"), new Short((short)0x0438));
             ht.put(new String("hi"), new Short((short)0x0439));
             ht.put(new String("mt"), new Short((short)0x043a));
             ht.put(new String("gd"), new Short((short)0x043c));
             ht.put(new String("yi"), new Short((short)0x043d));
             ht.put(new String("sw"), new Short((short)0x0441));
             ht.put(new String("tt"), new Short((short)0x0444));
             ht.put(new String("ta"), new Short((short)0x0449));
             ht.put(new String("mr"), new Short((short)0x044e));
             ht.put(new String("sa"), new Short((short)0x044f));
        }
    }

    /** Caches and returns the list of system font families names. */
    public static String[] getFamilies(){

        if (families == null){
            families = getFontFamiliesNames();
        }
        return families;
    }
    /** Fills fonts and families arrays with current system font data. */
    public static void updateFontLists(){
        if (families == null){
            families = getFontFamiliesNames();
        }

        if (fontsCount == 0){
            enumSystemFonts();
        }
    }

    /** Returns an array of available font face names. */
    public static String[] getAvailableFaces(){
        updateFontLists();
        return faces;
    }
    
    /**
     * Returns font family name that corresposnds to the face name with 
     * specified index.
     *   
     * @param faceIndex index of the font face name wich family name 
     * is to be returned 
     */
    public static String getFamily(int faceIndex){
        return families[famIndices[faceIndex]];
    }

    /**
     * Returns font family style that corresposnds to the face name with 
     * specified index.
     *   
     * @param faceIndex index of the font face name wich style is to be returned 
     */
    public static int getFontStyle(int faceIndex){
        return fontStyles[faceIndex];
    }

    /**
     * Adds new plain font with 1 pt. size from font resource file to the 
     * system if similar font wasn't into the system before. Method returns 
     * font object, corresponding to the specified resource. 
     *  
     * @param absolutePath absolute path to the font resource file
     */
    public static Font embedFont(String absolutePath){
        // TODO: implement method
        return null;
    }
    
    /**
     * Sets static arrays to the specified.
     *    
     * @param types new values for the fontTypes static array
     * @param styles new values for the fontStyles static array
     * @param indices new values for the famIndices static array
     * @param fFaces new values for the faces static array
     */
    public static void setArrays(int[] types, int[] styles, int[] indices, String[] fFaces){
        fontsCount = styles.length;

        fontStyles = new int[fontsCount];
        fontTypes = new int[fontsCount];
        famIndices = new int[fontsCount];
        faces = new String[fontsCount];

        System.arraycopy(styles, 0, fontStyles, 0, fontsCount);
        System.arraycopy(types, 0, fontTypes, 0, fontsCount);
        System.arraycopy(indices, 0, famIndices, 0, fontsCount);
        System.arraycopy(fFaces, 0, faces, 0, fontsCount);
    }
    
    /**
     * Returns font type (TrueType, Type1 or UndefinedType) for the specified
     * font face name and style.
     * 
     * @param name face name
     * @param style style of the font
     * 
     * @return one of the font type constants FontManager.FONT_TYPE_T1 or 
     * FontManager.FONT_TYPE_TT.
     */
    public static int getFontType(String name, int style){
        updateFontLists();

        for (int i=0; i < fontsCount; i++){
            if (fontTypes[i] == FontManager.FONT_TYPE_T1){
                if (name.equalsIgnoreCase(families[famIndices[i]])){
                    return fontTypes[i];
                }
            }

            if (fontStyles[i] == style){
                if (name.equalsIgnoreCase(families[famIndices[i]])){
                    return fontTypes[i];
                }
            }
        }

        return FontManager.FONT_TYPE_TT;
    }
    
    /** flag, returns true if native fontlib was loaded */
    private static boolean isLibLoaded = false;

    static void loadLibrary() {
        if(!isLibLoaded) {
            java.security.AccessController.doPrivileged(
                  new java.security.PrivilegedAction() {
                    public Object run() {
                        System.loadLibrary("fontlib");
                        return null;
                    }
            } );
        isLibLoaded = true;
        }
    }


    /** load native Font library */
    static {
            loadLibrary();
            updateFontLists();
    }
}
