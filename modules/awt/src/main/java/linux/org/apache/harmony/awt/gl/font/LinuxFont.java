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
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Hashtable;
import java.util.Locale;

import org.apache.harmony.awt.ContextStorage;
import org.apache.harmony.awt.gl.CommonGraphics2DFactory;
import org.apache.harmony.awt.gl.font.FontManager;
import org.apache.harmony.awt.gl.font.FontPeerImpl;
import org.apache.harmony.awt.gl.font.Glyph;
import org.apache.harmony.awt.gl.font.LineMetricsImpl;
import org.apache.harmony.awt.wtk.linux.LinuxWindowFactory;


/**
 * Linux platform font peer implementation based on Xft and FreeType libraries.
 */
public class LinuxFont extends FontPeerImpl {

    // Pairs of [begin, end],[..].. unicode ranges values 
    private int[] fontUnicodeRanges;
    
    // table with loaded cached Glyphs
    private Hashtable glyphs = new Hashtable();
    
    // X11 display value
    private long display = 0;

    // X11 screen value
    private int screen = 0;
    
    public LinuxFont(String fontName, int fontStyle, int fontSize) {
        /*
         * Workaround : to initialize awt platform-dependent fields and libraries.
         */
        Toolkit.getDefaultToolkit();

        this.name = fontName;
        this.size = fontSize;
        this.style = fontStyle;
        
        this.display = ((LinuxWindowFactory)ContextStorage.getWindowFactory()).getDisplay();
        this.screen = ((LinuxWindowFactory)ContextStorage.getWindowFactory()).getScreen();

        pFont = LinuxNativeFont.initializeFont(this, name, style, size, null);

        initLinuxFont();
    }

    /**
     * Initializes some native dependent font information, e.g. number of glyphs, 
     * font metrics, italic angle etc. 
     */
    public void initLinuxFont(){
        if (pFont != 0){
                this.numGlyphs = LinuxNativeFont.getNumGlyphsNative(pFont);
                this.italicAngle = LinuxNativeFont.getItalicAngleNative(pFont, this.fontType);
        }
        
        this.nlm = new LinuxLineMetrics(this, null, " ");

        this.ascent = nlm.getLogicalAscent();
        this.descent = nlm.getLogicalDescent();
        this.height = nlm.getHeight();
        this.leading = nlm.getLogicalLeading();
        this.maxAdvance = nlm.getLogicalMaxCharWidth();

        if (this.fontType == FontManager.FONT_TYPE_T1){
            this.defaultChar = 1;
        } else {
            this.defaultChar = 0;
        }

        this.maxCharBounds = new Rectangle2D.Float(0, -nlm.getAscent(), nlm.getMaxCharWidth(), this.height);

//        addGlyphs((char) 0x20, (char) 0x7E);

    }


    public boolean canDisplay(char chr) {
        // TODO: to improve performance there is a sence to implement get
        // unicode ranges to check if char can be displayed without
        // native calls in isGlyphExists() method

        return isGlyphExists(chr);
    }

    public LineMetrics getLineMetrics(String str, FontRenderContext frc, AffineTransform at) {
        //TODO: frc isn't used now
        
        // Initialize baseline offsets
        nlm.getBaselineOffsets();
        
        LineMetricsImpl lm = (LineMetricsImpl)(this.nlm.clone());
        lm.setNumChars(str.length());

        if ((at != null) && (!at.isIdentity())){
            lm.scale((float)at.getScaleX(), (float)at.getScaleY());
        }

        return lm;
    }

    public String getPSName() {
        if ((pFont != 0) && (psName == null)){
                psName = LinuxNativeFont.getFontPSNameNative(pFont);
        }
        return psName;
    }

    public String getFamily(Locale l) {
        // TODO: implement localized family
        if (fontType == FontManager.FONT_TYPE_TT){
            return this.getFamily();
        }

        return this.fontFamilyName;
    }

    public String getFontName(Locale l) {
        if ((pFont == 0) || (this.fontType == FontManager.FONT_TYPE_T1)){
            return this.name;
        }

        return this.getFontName();
    }


    public int getMissingGlyphCode() {
        return getDefaultGlyph().getGlyphCode();
    }

    public Glyph getGlyph(char index) {
        Glyph result = null;

        Object key = new Integer(index);
        if (glyphs.containsKey(key)) {
            result = (Glyph) glyphs.get(key);
        } else {
            if (this.addGlyph(index)) {
                result = (Glyph) glyphs.get(key);
            } else {
                result = this.getDefaultGlyph();
            }
        }

        return result;
    }

    public Glyph getDefaultGlyph() {
        Glyph result;
        Object key = new Integer(defaultChar);
        if (glyphs.containsKey(key)) {
            result = (Glyph) glyphs.get(key);
        } else {
            if (this.fontType == FontManager.FONT_TYPE_T1){
                // !! Type1 has no default glyphs
                glyphs.put(key, new LinuxGlyph(defaultChar, defaultChar));
                result = (Glyph) glyphs.get(key);
            } else {
                    int code = LinuxNativeFont.getGlyphCodeNative(this.pFont, defaultChar, this.display);
                    glyphs.put(key, new LinuxGlyph(this.pFont,
                            this.getSize(), defaultChar, code));
                result = (Glyph) glyphs.get(key);
            }
        }

        return result;
    }

    /**
     * Disposes native font handle. If this font peer was created from InputStream 
     * temporary created font resource file is deleted.
     */
    public void dispose(){
        String tempDirName;
        if (pFont != 0){
            LinuxNativeFont.pFontFree(pFont, display);
            pFont = 0;

            if (isCreatedFromStream()) {
                File fontFile = new File(getTempFontFileName());
                tempDirName = fontFile.getParent();
                fontFile.delete();
                LinuxNativeFont.RemoveFontResource(tempDirName);
            }
        }
    }

    /**
     * Add glyph to cached Glyph objects in this LinuxFont object.
     * 
     * @param uChar the specified character
     * @return true if glyph of the specified character exists in this
     * LinuxFont or this character is escape sequence character.
     */
    public boolean addGlyph(char uChar) {
        boolean result = false;
        boolean isEscape = false;

        isEscape = ((uChar == '\t') || (uChar == '\n') || (uChar == '\r'));

        int glyphCode = LinuxNativeFont.getGlyphCodeNative(this.pFont, uChar, display);
        if (isEscape || (glyphCode != 0xFFFF)) {
                glyphs.put(new Integer(uChar), new LinuxGlyph(this.pFont,
                            this.getSize(), uChar, glyphCode));
                result = true;
        }
        return result;
    }

   /**
    * Adds range of existing glyphs to this LinuxFont object
    * 
    * @param uFirst the lowest range's bound, inclusive 
    * @param uLast the highest range's bound, exclusive
    */
    public void addGlyphs(char uFirst, char uLast) {
        char index = uFirst;
        if (uLast < uFirst) {
            throw new IllegalArgumentException(
                    "min range bound value is grater than max range bound");
        }
        while (index < uLast) {
            addGlyph(index);
            index++;
        }
    }

    /**
     * Returns true if specified character has corresopnding glyph, false otherwise.  
     * 
     * @param uIndex specified char
     */
    public boolean isGlyphExists(char uIndex) {

/*      for (int i = 0; i < fontUnicodeRanges.length - 1; i += 2) {
            if (uIndex <= fontUnicodeRanges[i + 1]) {
                if (uIndex >= fontUnicodeRanges[i]) {
                    return true;
                } else {
                    return false;
                }
            }
        }*/
        int code = LinuxNativeFont.getGlyphCodeNative(this.getFontHandle(), uIndex, display);
        return (code != 0xFFFF);
    }

    /**
     *  Returns an array of unicode ranges that are supported by this LinuxFont. 
     */
    public int[] getUnicodeRanges() {
        int[] ranges = new int[fontUnicodeRanges.length];
        System.arraycopy(fontUnicodeRanges, 0, ranges, 0,
                fontUnicodeRanges.length);

        return ranges;
    }

    /**
     * Return Font object if it was successfully embedded in System
     */
    public static Font embedFont(String absolutePath){
        return LinuxNativeFont.embedFont(absolutePath);
    }

    public String getFontName(){
        if ((pFont != 0) && (faceName == null)){
            if (this.fontType == FontManager.FONT_TYPE_T1){
                faceName = getFamily();
            } else {
                faceName = LinuxNativeFont.getFontNameNative(pFont);
            }
        }
        return faceName;
    }

    public String getFamily() {
        if ((pFont != 0) && (fontFamilyName == null)){
            fontFamilyName = LinuxNativeFont.getFamilyNative(pFont);
        }
        return fontFamilyName;
    }
    
    /**
     * Returns initiated FontExtraMetrics instance of this WindowsFont.
     */
    public FontExtraMetrics getExtraMetrics(){
        if (extraMetrix == null){
            float[] metrics = LinuxNativeFont.getExtraMetricsNative(pFont, size, fontType);
            if (metrics == null){
                return null;
            }
                
            //!! for Type1 fonts 'x' char width used as average char width
            if (fontType == FontManager.FONT_TYPE_T1){
                metrics[0] = this.charWidth('x');
            }
            
            extraMetrix = new FontExtraMetrics(metrics);
        }
        
        return extraMetrix;
    }
}
