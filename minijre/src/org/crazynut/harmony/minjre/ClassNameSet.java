package org.crazynut.harmony.minjre;

import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * The data structure designed to contain a set of class & pack names 
 * and support fast class name retrieval. <p>
 * The data structure is associated with a type of file with the suffix 
 * of .cns, which contains a list of class & pack names. The format of
 * cns file is as following: <p>
 * =======================<p>
 * &nbspClass Name Set File<p>
 *
 * &nbspjava.io.File<p>
 * &nbspjava.util.HashMap<p>
 * &nbspjava.lang.*<p>
 * &nbsp.................<p>
 * =======================<p>
 * 
 * @author  <A HREF="mailto:daniel.gong.fudan@gmail.com">Ling-Hui Gong</A>
 */
public class ClassNameSet {
	
	/**
	 * The class name set.
	 */
	private HashMap<String, Object> classSet;
	
	/**
	 * The package list.
	 */
	private LinkedList<String> packList;
	
	/**
	 * The class name pattern.
	 */
	private static Pattern classNamePattern = 
		Pattern.compile("([a-z0-9]+\\.)*[A-Z][a-zA-Z0-9_]*(\\$[a-zA-Z0-9][a-zA-Z0-9_]*)*(\\$[1-9][0-9]*)*");
	
	/**
	 * The package name pattern.
	 */
	private static Pattern packNamePatter = 
		Pattern.compile("([a-z0-9]+\\.)+\\*");
	
	/**
	 * Decide whether the name is a valid class name.
	 * 
	 * @param name name
	 * @return True if the name is a valid class name
	 */
	private boolean isClassName(String name) {
		return classNamePattern.matcher(name).matches();
	}
	
	/**
	 * Decide whether the name is a valid package name.
	 * 
	 * @param name name
	 * @return True if the name is a valid package name
	 */
	private boolean isPackName(String name) {
		return packNamePatter.matcher(name).matches();
	}
	
	/**
	 * Add a package name to the list.<p>
	 * If there are some package that covers the package, 
	 * it will not be added to the list; if there are some
	 * package covered by the package, it will be added and
	 * replace the covered one.
	 * 
	 * @param packName
	 */
	private void addPack(String packName) {
		Iterator<String> i = packList.iterator();
		while (i.hasNext()) {
			String pack = i.next();
			if (packName.equals(pack)) {
				return;
			}
		}
		packList.add(packName);
	}
	
	/**
	 * Create new empty class name set.
	 */
	public ClassNameSet() {
		classSet = new HashMap<String, Object>();
		packList = new LinkedList<String>();
	}
	
	public ClassNameSet(InputStream is) throws IOException, CNSFileFormatException {
		this();
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String newLine;
		if (!(newLine = in.readLine()).equals("Class Name Set File")) {
			in.close();
			throw new CNSFileFormatException("The type of jre does not have a valid cns file.");
		} else {
			newLine = in.readLine();
			while (newLine != null) {
				// Ignore the empty lines
				while (newLine != null && newLine.equals("\n")) {
					newLine = in.readLine();
				}
				addName(newLine);
				newLine = in.readLine();
			}
			in.close();
		}
	}

	/**
	 * Create new class name set from the given file.
	 * 
	 * @param file The java.io.File instance of the file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws CNSFileFormatException
	 */
	public ClassNameSet(File file) throws FileNotFoundException, IOException, CNSFileFormatException {
		this();
		if (null == file) {
			return;
		}
		if (file.exists() && file.isFile()) {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String newLine;
			if (!(newLine = in.readLine()).equals("Class Name Set File")) {
				in.close();
				throw new CNSFileFormatException("The file " + 
						file.getPath() + " cannot be interpreted as a csn file.");
			} else {
				newLine = in.readLine();
				while (newLine != null) {
					// Ignore the empty lines
					while (newLine != null && newLine.equals("\n")) {
						newLine = in.readLine();
					}
					addName(newLine);
					newLine = in.readLine();
				}
				in.close();
			}
		} else {
			throw new FileNotFoundException("The file " + 
					file.getPath() + " does not exist.");
		}	
	}
	
	/**
	 * Create new class name set from the file at the given path.
	 * 
	 * @param fileName The path of the file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws CNSFileFormatException
	 */
	public ClassNameSet(String fileName) throws FileNotFoundException, IOException, CNSFileFormatException {
		this(null == fileName ? (File) null : new File(fileName));
	}
	
	/**
	 * Output the class name set to a cns file.
	 * 
	 * @param file The java.io.File instance of the file
	 * @throws FileNotFoundException 
	 */
	public void toClassListFile(File file) throws FileNotFoundException, IOException {
		if (null == file) {
			return;
		}
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		out.write("Class Name Set File\n\n");
		Iterator<String> i = classSet.keySet().iterator();
		while (i.hasNext()) {
			out.write(i.next() + "\n");
		}
		i = packList.iterator();
		while (i.hasNext()) {
			out.write(i.next() + "\n");
		}
		out.close();
	}
	
	/**
	 * Output the class name set to a cns file.
	 * 
	 * @param fileName The path of the file
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void toClassListFile(String fileName) throws FileNotFoundException, IOException {
		toClassListFile(new File(fileName));
	}
	
	/**
	 * Add a name (class or package) to the class name set.<p>
	 * If the name exists in the set or covered by some 
	 * package in the set, it will not be added.
	 * 
	 * @param name The name to add
	 */
	public void addName(String name) {
		// If the newline is a package name
		if (isPackName(name)) {
			addPack(name);
		} else if (isClassName(name)){
			classSet.put(name, null);
		}
	}
	
	/**
	 * Merge another class name set to the current set.
	 * 
	 * @param anotherSet
	 */
	public void merge(ClassNameSet anotherSet) {
		classSet.putAll(anotherSet.classSet);
		Iterator<String> i = anotherSet.packList.iterator();
		while (i.hasNext()) {
			String newPack = i.next();
			Iterator<String> j = packList.iterator();
			while (j.hasNext()) {
				if (newPack.equals(j.next())) {
					i.remove();
					break;
				}
			}
		}
		packList.addAll(anotherSet.packList);
	}
	
	/**
	 * Merge another class name set to the current set.
	 * 
	 * @param file The java.io.File instance of the file 
	 * that contains the class name set
	 * @throws IOException 
	 * @throws CNSFileFormatException 
	 * @throws FileNotFoundException 
	 */
	public void merge(File file) throws FileNotFoundException, CNSFileFormatException, IOException {
		merge(new ClassNameSet(file));
	}
	
	/**
	 * Merge another class name set to the current set.
	 * 
	 * @param fileName The path of the file 
	 * that contains the class name set
	 * @throws IOException 
	 * @throws CNSFileFormatException 
	 * @throws FileNotFoundException 
	 */
	public void merge(String fileName) throws FileNotFoundException, CNSFileFormatException, IOException {
		merge(null == fileName ? (File) null : new File(fileName));
	}
	
	/**
	 * Decide whether a class name exists in the set.
	 * 
	 * @param className The class name
	 * @return True if the class name exists
	 */
	public boolean isClassIn(String className) {
		if (false == isClassName(className)) {
			return false;
		}
		if (classSet.containsKey(className)) {
			return true;
		}
		Iterator<String> i = packList.iterator();
		while (i.hasNext()) {
			String packName = i.next();
			if (packName.substring(0, packName.lastIndexOf('*'))
					.equals(className.substring(0, className.lastIndexOf('.') + 1))) {
				return true;
			}
		}
		return false;
	}
}
