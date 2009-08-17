package org.crazynut.harmony.minjre;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.ClassFormatException;

/**
 * A dependency analyzer implementation to examine 
 * what jre classes a certain java application depends on.
 * 
 * @author  <A HREF="mailto:daniel.gong.fudan@gmail.com">Ling-Hui Gong</A>
 */
public class StaticDependencyAnalyzer {
	
	/**
	 * The application class set.
	 */
	private Vector<JavaClass> rootClasses;
	
	/**
	 * The jre class set.
	 */
	private HashMap<String, JavaClass> jreClasses;
	
	/**
	 * The class paths, including jars and directories 
	 * containing the application classes.
	 */
	private Vector<File> classPaths;
	
	/**
	 * The jre path.
	 */
	private File jrePath;
	
	/**
	 * True if the root class set has been built already.<p>
	 * False if the root class set has not been built yet.
	 */
	private boolean isRootClassesBuilt;
	
	/**
	 * True if the jre class set has been built already.<p>
	 * False if the jre class set has been built yet.
	 */
	private boolean isJreClassesBuilt;
	
	/**
	 * Constructor.
	 */
	public StaticDependencyAnalyzer() {
		rootClasses = new Vector<JavaClass>();
		jreClasses = new HashMap<String, JavaClass>();
		classPaths = new Vector<File>();
		jrePath = null;
		isRootClassesBuilt = false;
		isJreClassesBuilt = false;
	}
	
	/**
	 * Get class set from a directory.
	 * 
	 * @param dirPath the directory path that we get java classes from
	 * @return the java classes we get from the directory
	 */
	private Vector<JavaClass> getJavaClassFromDir(final File dirpath) {
		Vector<JavaClass> dirClasses = new Vector<JavaClass>();
		File[] fileList = dirpath.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isFile()) {
				if (fileList[i].getPath().endsWith(".class")) {
					try {
						dirClasses.add(new ClassParser(
								fileList[i].getPath()).parse());
					} catch (ClassFormatException e) {
						System.err.println("The file " + fileList[i].getPath() +
								"ends with .class " +
								"but cannot be interpreted as a class file.");
						e.printStackTrace();
					} catch (IOException e) {
						System.err.println("IO problem(s) detected when paring " 
								+ "the file " + fileList[i].getPath() + ".");
						e.printStackTrace();
					}
				} else if (fileList[i].getPath().endsWith(".jar")) {
					dirClasses.addAll(getJavaClassFromJar(fileList[i]));
				}
			} else if (fileList[i].isDirectory()) {
				dirClasses.addAll(getJavaClassFromDir(fileList[i]));
			}
		}
		return dirClasses;
	}
	/**
	 * Get JavaClass object list from a jar file.
	 * 
	 * @param jarPath the jar file path that we get java classes from
	 * @return the java classes we get from jarPath
	 */
	private Vector<JavaClass> getJavaClassFromJar(final File jarPath) {
		Vector<JavaClass> jarClasses = new Vector<JavaClass>();
		if (!jarPath.getPath().endsWith(".jar")) {
			return null;
		}
		try {
			JarFile jarFile = new JarFile(jarPath);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.endsWith(".class")) {
					jarClasses.add(new ClassParser(
							jarFile.getInputStream(entry), 
							name.substring(name.lastIndexOf('/') + 1)).parse());
				}
			}
		} catch (IOException e) {
			System.err.println("IO problem encountered " 
					+ "when trying to get classes from " + jarPath + ".");
			e.printStackTrace();
		}
		return jarClasses;
	}
	
	/**
	 * Build class set from the class paths added before.
	 */
	private void buildRootClasses() {
		Iterator<File> i = classPaths.iterator();
		while (i.hasNext()) {
			File classPath = i.next();
			if (classPath.isFile()) {
				if (classPath.getName().endsWith(".jar")) {
					rootClasses.addAll(getJavaClassFromJar(classPath));
				} else if (classPath.getName().endsWith(".class")) {
					try {
						rootClasses.add(new ClassParser(
								classPath.getAbsolutePath()).parse());
					} catch (ClassFormatException e) {
						System.err.println(classPath.getAbsolutePath()  
								+ " is not a valid class file.");
						e.printStackTrace();
					} catch (IOException e) {
						System.err.println("IO problem encountered " 
								+ "when trying to parse " 
								+ classPath.getAbsolutePath() + ".");
						e.printStackTrace();
					}
				}
			} else if (classPath.isDirectory()) {
				rootClasses.addAll(getJavaClassFromDir(classPath));
			}
		}
		isRootClassesBuilt = true;
	}
	
	/**
	 * Build class set from the jre path set before.
	 */
	private void buildJreClasses() {
		if (jrePath != null) {
			Vector<JavaClass> jreClassList = getJavaClassFromDir(jrePath);
			Iterator<JavaClass> i = jreClassList.iterator();
			while (i.hasNext()) {
				JavaClass javaClazz = i.next();
				jreClasses.put(javaClazz.getClassName(), javaClazz);
			}
			isJreClassesBuilt = true;
		}
	}
	
	/**
	 * Add a class path to class path set. A class path can be 
	 * a file directory or a jar file.
	 * 
	 * @param pathName The name of the class path add to class path set
	 */
	public final void addClassPath(final String pathName) {
		File newpath = new File(pathName);
		if (newpath.exists()) {
			classPaths.add(newpath);
			// Root class path is modified, 
			// set it to false to require rebuilding.
			isRootClassesBuilt = false;
		}
	}
	
	/**
	 * Set the path of the lib directory in JRE.
	 * 
	 * @param dirName The path of the lib directory in JRE
	 */
	public final void setJreLibPath(final String dirName) {
		File jreDir = new File(dirName);
		if (jreDir.exists() && jreDir.isDirectory()) {
			jrePath = jreDir;
			// Jre class path is modified, 
			// set it to false to require rebuilding.
			isJreClassesBuilt = false;
		}
	}
	
	/**
	 * Get the dependent class set.
	 * 
	 * @return the dependent class set
	 */
	public final ClassNameSet getDependentClasses() {
		Vector<JavaClass> workList = new Vector<JavaClass>();
		ClassNameSet result = new ClassNameSet();
		
		// Build root classes and jre classes in the first time
		// or rebuild them when they are modified.
		if (!isRootClassesBuilt) {
			buildRootClasses();
		}
		if (!isJreClassesBuilt) {
			buildJreClasses();
		}
		
		// Add basic dependency 
		workList.addAll(rootClasses);
		Iterator<String> keyIterator = jreClasses.keySet().iterator();
		while (keyIterator.hasNext()) {
			String className = keyIterator.next();
			if (className.startsWith("java.lang.")) {
				workList.add(jreClasses.get(className));
				result.addName(className);
			}
		}
		
		// Traverse the worklist to decide the classes that each class depends on in the worklist.
		// These "depended" classes are added into the worklist and result if they have not been in the result yet.
		// The algorithm ends when the index pointer reach the end of the worklist.
		// A dependency closure will be built with the above algorithm.
		int i = 0;
		while (i < workList.size()) {
			JavaClass javaClazz = workList.elementAt(i);
			// Get depended classes from the constant pool
			ConstantPool cp = javaClazz.getConstantPool();
			Constant[] carray = cp.getConstantPool();
			for (int j = 0; j < carray.length; j++) {
				if (carray[j] instanceof ConstantClass) {
					int index = ((ConstantClass) carray[j]).getNameIndex();
					String className = ((ConstantUtf8) carray[index])
						.getBytes().replace('/', '.');
					if (!result.isClassIn(className) 
							&& jreClasses.containsKey(className)) {
						workList.add(jreClasses.get(className));
						result.addName(className);
					}
				}
			}
			// Get interfaces
			String[] interfaceNames = javaClazz.getInterfaceNames();
			for (int j = 0; j < interfaceNames.length; j++) {
				if (!result.isClassIn(interfaceNames[j]) 
						&& jreClasses.containsKey(interfaceNames[j])) {
					workList.add(jreClasses.get(interfaceNames[j]));
					result.addName(interfaceNames[j]);
				}
			}
			// Get superclass
			String superClassName = javaClazz.getSuperclassName();
			if (!result.isClassIn(superClassName) 
					&& jreClasses.containsKey(superClassName)) {
				workList.add(jreClasses.get(superClassName));
				result.addName(superClassName);
			}
			i++;
		}
		return result;
	}
	
	/**
	 * Get the dependent class set and write it to file.
	 * 
	 * @param fileName the file that will contain the dependent class set
	 */
	public final void getDependentClasses(final String fileName) {
		ClassNameSet result = getDependentClasses();
		try {
			result.toClassListFile(fileName);
		} catch (IOException e) {
			System.err.println("Problem encoutered " 
					+ "when trying to create file " 
					+ fileName + " or write to it.");
			e.printStackTrace();
		}
	}
}
