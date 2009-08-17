package org.crazynut.harmony.minjre;

/** 
 * Thrown when trying to read a .cns file and determines
 * that the file is malformed or otherwise cannot be interpreted as a
 * class name set file.
 *
 * @author  <A HREF="mailto:daniel.gong.fudan@gmail.com">Ling-Hui Gong</A>
 */
public class CNSFileFormatException extends RuntimeException {
    
	public CNSFileFormatException() {
        super();
    }

    public CNSFileFormatException(String s) {
        super(s);
    }
}
