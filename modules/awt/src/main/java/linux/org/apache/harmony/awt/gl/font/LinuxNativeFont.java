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

import org.apache.harmony.awt.gl.font.FontPeerImpl;

/**
 *
 *  Library wrapper of native linux font functions.
 */
public class LinuxNativeFont {
    
    
    public static final int FC_SLANT_ROMAN = 0;
    public static final int FC_SLANT_ITALIC = 100;
    public static final int FC_SLANT_OBLIQUE = 110;
    public static final int FC_WEIGHT_MEDIUM = 100;
    
    /**
     * Returns array of Strings that represents list of all font families names
     * available on the system.  
     */
    public static native String[] getFontFamiliesNames();

    /**
     * Returns true if the new font was added to the system, false otherwise.
     * Methods checks if the number of system fonts changed after font configutation
     * was rebuilded.
     *   
     * @param absolutePath absolute path to the font.
     */
    public static native boolean embedFontNative(String absolutePath);
    
    /**
     * Initiailzes native Xft font object from specified parameters and returns 
     * font handle, also sets font type to the font peer parameter. 
     * NullPointerException is thrown if there are errors in native code. 
     * 
     * @param linFont LinuxFont instanse
     * @param family font family name
     * @param style style of the font
     * @param size size of the font
     * @param styleName style name of the font
     */
    public static native long initializeFont(LinuxFont linFont, String family, int style, int size, String styleName);

    /**
     * Initializes native Xft font object from xlfd string and returns font handle,  
     * also sets font type to the font peer parameter. If font 
     * that is described by the given xlfd doesn't exist onto a system returned value
     * is null. NullPointerException is thrown if there are errors in native code. 
     * 
     * @param linFont LinuxFont instanse
     * @param xlfd String representing font in xlfd format
     * @param size size of the font
     */
    public static native long initializeFontFromFP(LinuxFont linFont, String xlfd, int size);

    /** 
     * Returns number of glyphs in specified XftFont if success. 
     * 
     * @param hndFont XftFont handle
     */
    public static native int getNumGlyphsNative(long hndFont);
    
    /**
     * Returns true, if XftFont object can display specified char.
     * 
     * @param hndFont XftFont handle
     * @param c specified char
     */
    // !! Instead of this method getGlyphCode can be used
    // TODO: implement method and find out if this method faster than getGlyphCode 
    // usage 
    public static native boolean canDisplayCharNative(long hndFont, char c);

    /**
     * Returns family name of the XftFont object.
     * 
     * @param hndFont XftFont handle
     */
    public static native String getFamilyNative(long hndFont);
    
    /**
     * Returns face name of the XftFont object.
     * 
     * @param hndFont XftFont handle
     */
    public static native String getFontNameNative(long hndFont);

    /**
     * Returns XftFont's postscript name.
     * Returned value is the name of the font in system default locale or
     * for english langid if there is no results for default locale settings. 
     * 
     * @param fnt XftFont handle
     */
    public static native String getFontPSNameNative(long fnt);

    /**
     * Disposing XftFont object.
     * 
     * @param hndFont XftFont handle
     * @param display Display handle
     */
    public static native void pFontFree(long hndFont, long display);

    /**
     * Returns tangent of Italic angle of given Font.
     * Returned value is null and NullPointerException is thrown if there is Xft error.
     * 
     * @param hndFont XftFont handle
     * @param fontType type of the font
     */
    public static native float getItalicAngleNative(long hndFont, int fontType);
    
    /** 
     * Returns an array of available system fonts names.
     * In case of errors in native code NullPointerException is thrown.
     */
    public static native String[] getFonts();

    /**
     * Returns array of values of font metrics corresponding to the given XftFont 
     * font object. NullPointerException is thrown in case errors in native code.
     * 
     * @param hFont XftFont handle
     * @param fontSize size of the font
     * @param isAntialiased parameter true if antialiased metrics required
     * @param usesFractionalMetrics true if results calculated using fractional metrics
     * @param fontType type of the specified font
     */
    public static native float[] getNativeLineMetrics(long hFont, int fontSize,
            boolean isAntialiased, boolean usesFractionalMetrics, int fontType);

    /** 
     * Returns array of glyph metrics values for the specified character
     * null is returned and NullPointerException is thrown in case of FreeType 
     * errors.
     * 
     * @param pFnt XftFont handle
     * @param c specified char
     */
    public static native float[] getGlyphInfoNative(long pFnt, char c,
            int fontSize);

    /** 
     * Returns array of glyph metrics values in pixels for the specified character
     * null is returned and NullPointerException is thrown in case of FreeType errors.
     * 
     * @param pFnt XftFont handle
     * @param c specified char
     */
    public static native int[] getGlyphPxlInfoNative(long display, long pFnt, char c);
    
    /**
     * Returns glyphs code corresponding to the characters in String specified, null 
     * is returned if failure. NullPointerException is thrown in case of Display 
     * is null.
     * 
     * @param fnt XftFont handle
     * @param uChar specified char
     * @param display Display handle
     */
    // TODO: implement native call
    public static native int[] getGlyphCodesNative(long fnt, String str, int len);
    
    /**
     * Returns glyph code corresponding to the specified character, null is 
     * returned if failure. NullPointerException is thrown in case of Display is null.
     * 
     * @param fnt XftFont handle
     * @param uChar specified char
     * @param display Display handle
     */
    public static native int getGlyphCodeNative(long fnt, char uChar, long display);
    
    /**
     * Updates specified folder where temporary font created from InputStream stored.
     * This method used in LinuxFont dispose method, it re-caches ~/.fonts
     * directory, after temporary font file is deleted.
     *   
     * @param tempFontFileName directory that is being re-cached name.
     * @return not null value if succcess, 0 otherwise
     */
    public static native int RemoveFontResource(String tempFontFileName);
    
    /**
     * Returns byte array that represents bitmap
     * of the character specified. 
     * ( Better to use NativeInitGlyphBitmap method to get
     * bitmap of the glyph. This method is to be deleted in the nearest future.)
     * 
     * @param fnt XftFont handle
     * @param chr specified char
     */
    // TODO: avoid use of this method in font classes due to incomplete returned 
    // bitmap information
    public static native byte[] NativeInitGlyphImage(long fnt, char uChar);

    /**
     * Draws text on XftDraw with specified parameters using Xft library.
     * 
     * @param xftDraw XftDraw handle
     * @param display Display handle
     * @param colormap Colormap handle
     * @param font XftFont handle
     * @param x X coordinate where to draw at
     * @param y Y coordinate where to draw at
     * @param chars array of chars to draw
     * @param len length of the array of chars
     * @param xcolor XColor handle, the color of the text
     */
    public static native void drawStringNative(long xftDraw, long display, long colormap, long font, int x, int y, char[] chars, int len, long xcolor);
    
// FreeType routines
    /**
     * Returns pointer to FreeType FT_Bitmap that represents bitmap
     * of the character specified or 0 if failures in native code.
     * 
     * @param fnt XftFont handle
     * @param chr specified char
     */
    public static native long NativeInitGlyphBitmap(long fnt, char chr);

// Xft routines
    
    /**
     * Returns XftDraw handle created from specified parameters using Xft library.
     * 
     * @param display Display handle 
     * @param drawable Drawable handle
     * @param visual Visual handle
     */
    public static native long createXftDrawNative(long display, long drawable, long visual);

    /**
     * Destroys XftDraw object.
     * @param xftDraw XftDraw handle 
     */
    public static native void freeXftDrawNative(long xftDraw);

    /**
     * Set new subwindow mode to XftDraw object
     *  
     * @param xftDraw XftDraw handle 
     * @param mode new mode
     */
    public static native void xftDrawSetSubwindowModeNative(long xftDraw, int mode);
    
    /**
     * Sets clipping rectangles in Xft drawable to the specified clipping rectangles. 
     * 
     * @param xftDraw XftDraw handle
     * @param xOrigin x position to start
     * @param yOrigin y position to start
     * @param rects handle to the memory block representing XRectangles array
     * @param n number of rectangles
     * 
     * @return result true if success in native call, false otherwise 
     */
    public static native boolean XftDrawSetClipRectangles(long xftDraw, int xOrigin,
            int yOrigin, long rects, int n);

    /**
     * Returns pointer to the FreeType FT_Outline structure. 
     * 
     * @param pFont XFT font handle
     * @param c specified character
     */
    public static native long getGlyphOutline(long pFont, char c);

//  public static native boolean isCharExists(char chr);

    /**
     * Initializes LCID table
     */
    public static void initLCIDsTable(Hashtable ht){

            /*
             *  Language records with LCID values (0x04**).
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

            /*
             *  Language-country records.
             */
             ht.put(new String("ar_SA"), new Short((short)0x401));
             ht.put(new String("bg_BG"), new Short((short)0x402));
             ht.put(new String("ca_ES"), new Short((short)0x403));
             ht.put(new String("zh_TW"), new Short((short)0x404));
             ht.put(new String("cs_CZ"), new Short((short)0x405));
             ht.put(new String("da_DK"), new Short((short)0x406));
             ht.put(new String("de_DE"), new Short((short)0x407));
             ht.put(new String("el_GR"), new Short((short)0x408));
             ht.put(new String("en_US"), new Short((short)0x409));
             ht.put(new String("es_ES"), new Short((short)0x40a));
             ht.put(new String("fi_FI"), new Short((short)0x40b));
             ht.put(new String("fr_FR"), new Short((short)0x40c));
             ht.put(new String("he_IL"), new Short((short)0x40d));
             ht.put(new String("hu_HU"), new Short((short)0x40e));
             ht.put(new String("is_IS"), new Short((short)0x40f));
             ht.put(new String("it_IT"), new Short((short)0x410));
             ht.put(new String("ja_JP"), new Short((short)0x411));
             ht.put(new String("ko_KR"), new Short((short)0x412));
             ht.put(new String("nl_NL"), new Short((short)0x413));
             ht.put(new String("nb_NO"), new Short((short)0x414));
             ht.put(new String("pl_PL"), new Short((short)0x415));
             ht.put(new String("pt_BR"), new Short((short)0x416));
             ht.put(new String("ro_RO"), new Short((short)0x418));
             ht.put(new String("ru_RU"), new Short((short)0x419));
             ht.put(new String("hr_HR"), new Short((short)0x41a));
             ht.put(new String("sk_SK"), new Short((short)0x41b));
             ht.put(new String("sq_AL"), new Short((short)0x41c));
             ht.put(new String("sv_SE"), new Short((short)0x41d));
             ht.put(new String("th_TH"), new Short((short)0x41e));
             ht.put(new String("tr_TR"), new Short((short)0x41f));
             ht.put(new String("ur_PK"), new Short((short)0x420));
             ht.put(new String("id_ID"), new Short((short)0x421));
             ht.put(new String("uk_UA"), new Short((short)0x422));
             ht.put(new String("be_BY"), new Short((short)0x423));
             ht.put(new String("sl_SI"), new Short((short)0x424));
             ht.put(new String("et_EE"), new Short((short)0x425));
             ht.put(new String("lv_LV"), new Short((short)0x426));
             ht.put(new String("lt_LT"), new Short((short)0x427));
             ht.put(new String("fa_IR"), new Short((short)0x429));
             ht.put(new String("vi_VN"), new Short((short)0x42a));
             ht.put(new String("hy_AM"), new Short((short)0x42b));
             ht.put(new String("az_AZ"), new Short((short)0x42c));
             ht.put(new String("eu_ES"), new Short((short)0x42d));
             ht.put(new String("mk_MK"), new Short((short)0x42f));
             ht.put(new String("af_ZA"), new Short((short)0x436));
             ht.put(new String("ka_GE"), new Short((short)0x437));
             ht.put(new String("fo_FO"), new Short((short)0x438));
             ht.put(new String("hi_IN"), new Short((short)0x439));
             ht.put(new String("ms_MY"), new Short((short)0x43e));
             ht.put(new String("kk_KZ"), new Short((short)0x43f));
             ht.put(new String("ky_KG"), new Short((short)0x440));
             ht.put(new String("sw_KE"), new Short((short)0x441));
             ht.put(new String("uz_UZ"), new Short((short)0x443));
             ht.put(new String("tt_TA"), new Short((short)0x444));
             ht.put(new String("pa_IN"), new Short((short)0x446));
             ht.put(new String("gu_IN"), new Short((short)0x447));
             ht.put(new String("ta_IN"), new Short((short)0x449));
             ht.put(new String("te_IN"), new Short((short)0x44a));
             ht.put(new String("kn_IN"), new Short((short)0x44b));
             ht.put(new String("mr_IN"), new Short((short)0x44e));
             ht.put(new String("sa_IN"), new Short((short)0x44f));
             ht.put(new String("mn_MN"), new Short((short)0x450));
             ht.put(new String("gl_ES"), new Short((short)0x456));
             ht.put(new String("ko_IN"), new Short((short)0x457));
             ht.put(new String("sy_SY"), new Short((short)0x45a));
             ht.put(new String("di_MV"), new Short((short)0x465));
             ht.put(new String("ar_IQ"), new Short((short)0x801));
             ht.put(new String("zh_CN"), new Short((short)0x804));
             ht.put(new String("de_CH"), new Short((short)0x807));
             ht.put(new String("en_GB"), new Short((short)0x809));
             ht.put(new String("es_MX"), new Short((short)0x80a));
             ht.put(new String("fr_BE"), new Short((short)0x80c));
             ht.put(new String("it_CH"), new Short((short)0x810));
             ht.put(new String("nl_BE"), new Short((short)0x813));
             ht.put(new String("nn_NO"), new Short((short)0x814));
             ht.put(new String("pt_PT"), new Short((short)0x816));
             ht.put(new String("sr_SP"), new Short((short)0x81a));
             ht.put(new String("sv_FI"), new Short((short)0x81d));
             ht.put(new String("az_AZ"), new Short((short)0x82c));
             ht.put(new String("ms_BN"), new Short((short)0x83e));
             ht.put(new String("uz_UZ"), new Short((short)0x843));
             ht.put(new String("ar_EG"), new Short((short)0xc01));
             ht.put(new String("zh_HK"), new Short((short)0xc04));
             ht.put(new String("de_AT"), new Short((short)0xc07));
             ht.put(new String("en_AU"), new Short((short)0xc09));
             ht.put(new String("es_ES"), new Short((short)0xc0a));
             ht.put(new String("fr_CA"), new Short((short)0xc0c));
             ht.put(new String("sr_SP"), new Short((short)0xc1a));
             ht.put(new String("ar_LY"), new Short((short)0x1001));
             ht.put(new String("zh_SG"), new Short((short)0x1004));
             ht.put(new String("de_LU"), new Short((short)0x1007));
             ht.put(new String("en_CA"), new Short((short)0x1009));
             ht.put(new String("es_GT"), new Short((short)0x100a));
             ht.put(new String("fr_CH"), new Short((short)0x100c));
             ht.put(new String("ar_DZ"), new Short((short)0x1401));
             ht.put(new String("zh_MO"), new Short((short)0x1404));
             ht.put(new String("de_LI"), new Short((short)0x1407));
             ht.put(new String("en_NZ"), new Short((short)0x1409));
             ht.put(new String("es_CR"), new Short((short)0x140a));
             ht.put(new String("fr_LU"), new Short((short)0x140c));
             ht.put(new String("ar_MA"), new Short((short)0x1801));
             ht.put(new String("en_IE"), new Short((short)0x1809));
             ht.put(new String("es_PA"), new Short((short)0x180a));
             ht.put(new String("fr_MC"), new Short((short)0x180c));
             ht.put(new String("ar_TN"), new Short((short)0x1c01));
             ht.put(new String("en_ZA"), new Short((short)0x1c09));
             ht.put(new String("es_DO"), new Short((short)0x1c0a));
             ht.put(new String("ar_OM"), new Short((short)0x2001));
             ht.put(new String("en_JM"), new Short((short)0x2009));
             ht.put(new String("es_VE"), new Short((short)0x200a));
             ht.put(new String("ar_YE"), new Short((short)0x2401));
             ht.put(new String("en_CB"), new Short((short)0x2409));
             ht.put(new String("es_CO"), new Short((short)0x240a));
             ht.put(new String("ar_SY"), new Short((short)0x2801));
             ht.put(new String("en_BZ"), new Short((short)0x2809));
             ht.put(new String("es_PE"), new Short((short)0x280a));
             ht.put(new String("ar_JO"), new Short((short)0x2c01));
             ht.put(new String("en_TT"), new Short((short)0x2c09));
             ht.put(new String("es_AR"), new Short((short)0x2c0a));
             ht.put(new String("ar_LB"), new Short((short)0x3001));
             ht.put(new String("en_ZW"), new Short((short)0x3009));
             ht.put(new String("es_EC"), new Short((short)0x300a));
             ht.put(new String("ar_KW"), new Short((short)0x3401));
             ht.put(new String("en_PH"), new Short((short)0x3409));
             ht.put(new String("es_CL"), new Short((short)0x340a));
             ht.put(new String("ar_AE"), new Short((short)0x3801));
             ht.put(new String("es_UY"), new Short((short)0x380a));
             ht.put(new String("ar_BH"), new Short((short)0x3c01));
             ht.put(new String("es_PY"), new Short((short)0x3c0a));
             ht.put(new String("ar_QA"), new Short((short)0x4001));
             ht.put(new String("es_BO"), new Short((short)0x400a));
             ht.put(new String("es_SV"), new Short((short)0x440a));
             ht.put(new String("es_HN"), new Short((short)0x480a));
             ht.put(new String("es_NI"), new Short((short)0x4c0a));
             ht.put(new String("es_PR"), new Short((short)0x500a));
    }

    /**
     * List of font faces names of system fonts supported by a system.
     */
    public static String[] faces;
    
    /**
     * List of font style names of system fonts supported by a system 
     * corresponding to faces indexing.
     */
    public static String[] styles;

    /**
     * List of family indexes in families array corresponding to the faces 
     * indexing.
     */
    public static int[] famIndices;

    /**
     * The number of different fonts installed onto the system.
     */
    public static int facesCount;
    
    /**
     * Set of all unique families installed onto the system.
     */
    public static Vector fams = new Vector();

    /**
     * Returns font family name of the font with face having specified index.
     * 
     * @param faceIndex specified index of the face in faces array
     */
    public static String getFamily(int faceIndex){
        return (String)fams.get(famIndices[faceIndex]);
    }

    /**
     * Returns font style name of the font with face having specified index.
     * 
     * @param faceIndex specified index of the face in faces array
     */
    public static String getFontStyle(int faceIndex){
        return styles[faceIndex];
    }

    /**
     * Returns array of Strings that represent face names of all fonts
     * supported by a system. 
     */
    public static String[] getFaces(){
        if (faces == null)
            initFaces();
        return faces;
    }
    
    /**
     * Initializes famIndices, styles and faces arrays according to the 
     * font information available on the system. 
     */
    public static void initFaces(){
        if (facesCount == 0){
            String[] fontNames = getFonts();
            facesCount = fontNames.length;
            faces = new String[facesCount];
            styles = new String[facesCount];
            famIndices = new int[facesCount];

            for (int i =0; i < facesCount; i++){
                initFace(i, fontNames[i]);
            }
        }
    }
    
    /**
     * Initializes specified elements with index specified of famIndices, styles and 
     * faces arrays according to the given faceString. faceString has format 
     * "family name"-"style name".
     * 
     * @param index index of element to identify
     * @param faceString String defining family name and style in special format
     */
    public static void initFace(int index, String faceString){
        String delim = "-";
        int pos;

        if (faceString == null) {
            return;
        }
        pos = faceString.lastIndexOf(delim);

        styles[index] = faceString.substring(pos+1);

        String family = faceString.substring(0, pos);
        int famIndex = fams.indexOf(family);
        if(famIndex == -1){
            fams.add(family);
            famIndex = fams.size() - 1;
        }
        famIndices[index] = famIndex;

        faces[index] = family + " " + styles[index];
    }

    /** Returns the list of system font families names. */
    public static String[] getFamilies() {
        initFaces();

        int size = fams.size();
        String[] names = new String[size];
        for(int i=0; i < size; i++){
            names[i] = (String)fams.get(i);
        }
        return names;
    }
    
    /**
     * Returns an array of instanses of 1 pt. sized plain Font objects
     * corresponding to fonts supported by a system. 
     */
    public static Font[] getAllFonts() {
        initFaces();

        Font[] fonts = new Font[faces.length];
        for (int i =0; i < fonts.length;i++){
            fonts[i] = new Font(faces[i], Font.PLAIN, 1);
        }
        return fonts;
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

    /** flag, returns true if native linuxfont was loaded */
    private static boolean isLibLoaded = false;

    static void loadLibrary() {
        if (!isLibLoaded) {
            java.security.AccessController
                    .doPrivileged(new java.security.PrivilegedAction() {
                        public Object run() {
                            System.loadLibrary("linuxfont");
                            return null;
                        }
                    });
            isLibLoaded = true;
        }
    }

    /* load native Font library */
    static {
        loadLibrary();
    }

}

