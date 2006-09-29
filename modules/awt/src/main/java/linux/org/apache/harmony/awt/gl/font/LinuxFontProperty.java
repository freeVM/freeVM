/*
 *  Copyright 2005 - 2006 The Apache Software Foundation or its licensors, as applicable.
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
 *
 */
package org.apache.harmony.awt.gl.font;

/**
 * Linux FontProperty implementation, applicable for Linux formats of 
 * font property files. 
 */
public class LinuxFontProperty extends FontProperty {
    
    /** xlfd string that is applicable for Linux font.properties */ 
    String xlfd;

    /** logical name of the font corresponding to this FontProperty */ 
    String logicalName;
    
    /** style name of the font corresponding to this FontProperty */
    String styleName;

    public LinuxFontProperty(String _logicalName, String _styleName, String _fileName, String _name, String _xlfd, int _style, int[] exclusionRange, String _encoding){
        this.logicalName = _logicalName;
        this.styleName = _styleName;
        this.name = _name;
        this.encoding = _encoding;
        this.exclRange = exclusionRange;
        this.fileName = _fileName;
        this.xlfd = _xlfd;
        this.style = _style;
    }
    
    /**
     * Returns logical name of the font corresponding to this FontProperty. 
     */
    public String getLogicalName(){
        return logicalName;
    }
    
    /**
     * Returns style name of the font corresponding to this FontProperty. 
     */
    public String getStyleName(){
        return styleName;
    }
    
    /**
     * Returns xlfd string of this FontProperty. 
     */
    public String getXLFD(){
        return xlfd;
    }

    public String toString(){
        return new String(this.getClass().getName() +
                "[name=" + name +
                ",fileName="+ fileName +
                ",Charset=" + encoding +
                ",exclRange=" + exclRange +
                ",xlfd=" + xlfd + "]");

    }

}
