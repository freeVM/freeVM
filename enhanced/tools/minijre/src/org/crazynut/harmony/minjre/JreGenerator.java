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

package org.crazynut.harmony.minjre;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Vector;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * The minimum JRE generator, which generate a new JRE
 * from a complete JRE according to a reserved class list.
 * 
 * @author  <A HREF="mailto:daniel.gong.fudan@gmail.com">Ling-Hui Gong</A>
 */
public class JreGenerator {
	
	/**
	 * The class set which should be reserved in jre.
	 */
	private ClassNameSet reservedClasses = null;
	
	/**
	 * The length of the buffer used in jar file IO.
	 */
	private static final int BUFF_LEN = 1024;
	
	/**
	 * Repack jar file according to the reserved class list.
	 * 
	 * @param fromPath the origin jar file path
	 * @param toPath the new jar file path
	 */
	private void repackJar(final String fromPath, final String toPath) {
		int count = 0;
		HashMap<String, Boolean> isDirEmpty = new HashMap<String, Boolean>(); 
		Vector<JarEntry> entryList = new Vector<JarEntry>();
		try {
			JarFile jar = new JarFile(fromPath);
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = (JarEntry) entries.nextElement();
				if (entry.getName().endsWith(".class")) {
					String classFileName = entry.getName().replace('/', '.');
					String className = classFileName.substring(0,
							classFileName.lastIndexOf('.'));
					if (!reservedClasses.isClassIn(className)) {
						continue;
					} else {
						count++;
					}
				}
				if (entry.isDirectory()) {
					Boolean isEmpty = Boolean.TRUE;
					Iterator<JarEntry> i = entryList.iterator();
					while (i.hasNext()) {
						JarEntry temp = i.next();
						if (!temp.isDirectory() 
								&& temp.getName().startsWith(entry.getName())) {
							isEmpty = Boolean.FALSE;
						}
					}
					isDirEmpty.put(entry.getName(), isEmpty);
				} else {
					entryList.add(entry);
					Iterator<Entry<String, Boolean>> i = 
						isDirEmpty.entrySet().iterator();
					while (i.hasNext()) {
						Entry<String, Boolean> dirEntry = i.next();
						String dirName = dirEntry.getKey();
						if (entry.getName().startsWith(dirName) 
							&& isDirEmpty.containsKey(dirName) 
							&& isDirEmpty.get(dirName).equals(Boolean.TRUE)) {
							isDirEmpty.put(dirName, Boolean.FALSE);
						}
					}
				}
			}
			if (count != 0 && isDirEmpty.containsValue(Boolean.FALSE)) {
				int byteRead = 0;
				byte[] buffer = new byte[BUFF_LEN];
				Iterator<JarEntry> i = entryList.iterator();
				JarOutputStream out = 
					new JarOutputStream(new FileOutputStream(toPath));
				while (i.hasNext()) {
					JarEntry temp = i.next();
					if (temp.isDirectory() && isDirEmpty.get(temp.getName())
							.equals(Boolean.TRUE)) {
						continue;
					}
					InputStream in = jar.getInputStream(temp);
					//out.putNextEntry(temp);

					JarEntry newTemp = new JarEntry(temp);

					newTemp.setCompressedSize(-1);
					out.putNextEntry(newTemp);
					while ((byteRead = in.read(buffer)) != -1) {
						out.write(buffer, 0, byteRead);
					}
					in.close();
					out.flush();
					out.closeEntry();
				}
				out.close();
			}
		} catch (IOException e) {
			System.err.println("Repacking from " + fromPath 
					+ " to " + toPath + " fails with IO problem.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Copy directory to another place.
	 * 
	 * @param fromPath the origin directory path
	 * @param toPath the aim directory path
	 * @param jarNeedRepack set true if jar files
	 *  in the directory need repacking
	 * @throws Exception Throws when the directory toPath cannot be created
	 */
	private void copyDirectory(final String fromPath, 
			final String toPath, final boolean jarNeedRepack) throws Exception {
		if (!(new File(toPath).mkdirs())) {
			throw new Exception("Directory " + toPath 
					+ "cannot be created. JRE generation fails.");
		}
		File[] fileList = new File(fromPath).listFiles();
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isFile() 
				&& !fileList[i].getName().endsWith("-src.jar") 
				&& !fileList[i].getName().equals("luni-kernel-stubs.jar")) {
				if (jarNeedRepack && fileList[i].getName().endsWith("jar")) {
					repackJar(fileList[i].getAbsolutePath(), 
							toPath + File.separator + fileList[i].getName());
				} else {
					copyFile(fileList[i].getAbsolutePath(), 
							toPath + File.separator + fileList[i].getName());
				}
			} else if (fileList[i].isDirectory()) {
				copyDirectory(fileList[i].getAbsolutePath(), 
						toPath + File.separator + fileList[i].getName(), 
						jarNeedRepack);
			}
		}
	}
	
	/**
	 * Copy file to another place.
	 * 
	 * @param fromPath the origin file path
	 * @param toPath the aim file path
	 */
	private void copyFile(final String fromPath, final String toPath) {
        try {
        	int byteread = 0;
            byte[] buffer = new byte[BUFF_LEN];
        	InputStream in = new FileInputStream(fromPath);
            OutputStream out = new FileOutputStream(toPath);
            while ((byteread = in.read(buffer)) != -1) {
            	out.write(buffer, 0, byteread);
            }
            in.close();
            out.close();
        } catch (IOException e) {
        	System.err.println("IO problem encountered when trying copy file " 
        			+ fromPath + " to " + toPath + ".");
            e.printStackTrace();
        } 
	}
	
	/**
	 * Constructor.
	 * 
	 * @param jre the jre type
	 *
	 */
	public JreGenerator(final String jre) {
		try {
			reservedClasses = new ClassNameSet(this.getClass().getResourceAsStream("conf/" + jre + ".cns"));
		} catch (CNSFileFormatException e) {
			System.err.println("This type of jre does not has a valid cns file.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("This type of jre is not supported.");
			e.printStackTrace();
		}
	}

	
	/**
	 * Add reserved classes from a ClassNameSet file.
	 * 
	 * @param fileName the file path of the cns file
	 */
	public final void addReservedClasses(final String fileName) {
		addReservedClasses(null == fileName ? (File) null : new File(fileName));
	}
	
	/**
	 * Add reserved classes from a ClassNameSet file.
	 * 
	 * @param file the java.io.File instance of the cns file
	 */
	public final void addReservedClasses(final File file) {
		try {
			reservedClasses.merge(file);
		} catch (FileNotFoundException e) {
			System.err.println("The file " 
					+ file.getAbsolutePath() + " does not exist.");
			e.printStackTrace(); 
		} catch (CNSFileFormatException e) {
			System.err.println("The file " 
					+ file.getAbsolutePath() + " is not a valid cns file.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO problem encountered " 
					+ "when trying to open and read from "  
					+ file.getAbsolutePath() + ".");
			e.printStackTrace();
		}
	}
	
	/**
	 * Add reserved classes from a ClassNameSet instance.
	 * 
	 * @param cns the ClassNameSet instance
	 */
	public final void addReservedClasses(final ClassNameSet cns) {
		reservedClasses.merge(cns);
	}
	
	/**
	 * Set the reserved classes set to the ClassNameSet 
	 * instance built from a cns file.
	 * 
	 * @param fileName the path of the cns file
	 */
	public final void setReservedClasses(final String fileName) {
		setReservedClasses(null == fileName ? (File) null : new File(fileName));
	}
	
	/**
	 * Set the reserved classes set to the ClassNameSet 
	 * instance built from a cns file.
	 * 
	 * @param file the java.io.File instance of the cns file
	 */
	public final void setReservedClasses(final File file) {
		try {
			reservedClasses = new ClassNameSet(file);
		} catch (FileNotFoundException e) {
			System.err.println("The file " 
					+ file.getAbsolutePath() + " does not exist.");
			e.printStackTrace(); 
		} catch (CNSFileFormatException e) {
			System.err.println("The file " 
					+ file.getAbsolutePath() + " is not a valid cns file.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO problem encountered " 
					+ "when trying to open and read from "  
					+ file.getAbsolutePath() + ".");
			e.printStackTrace();
		}
	}
	
	/**
	 * Set the reserved classes set to the ClassNameSet instance.
	 * 
	 * @param cns the ClassNameSet instance
	 */
	public final void setReservedClasses(final ClassNameSet cns) {
		reservedClasses = cns;
	}
	
	/**
	 * Generate minimum jre from the origin jre 
	 * according to the reserved class list.
	 * 
	 * @param fromPath the origin jre path
	 * @param toPath the aim jre path
	 */
	public final void generateMinJre(final String fromPath, 
			final String toPath) {
		File[] fileList = new File(fromPath).listFiles();
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isDirectory()) {
				try {
					copyDirectory(fileList[i].getAbsolutePath(), 
							toPath + File.separator + fileList[i].getName(), 
							fileList[i].getName().equals("lib"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (fileList[i].isFile()) {
				copyFile(fileList[i].getAbsolutePath(), 
						toPath + File.separator + fileList[i].getName());
			}
		}
	}
}
