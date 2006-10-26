/* Copyright 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tools.msgstool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * MsgClassGenerator class helps to generate nesessary source files and
 * properties files to support messages internationalization task. See also
 * msgstool README for details.
 */
public class MsgClassGenerator {

    /* Word for replacement in templates */
    private static final String MODULE_PATTERN_NAME = "<module>";

    /* Messages.java file name without extension */
    private static final String MSG_CLASS_NAME = "Messages";

    /* messages.properties file name */
    private static final String MSGS_PROPERTIES_NAME = "messages.properties";

    /* Default modules.properties file name */
    private static final String MODULES_PROPERTIES_NAME = "modules.properties";

    /* Pattern files extension */
    private static final String PATTERN_EXT_NAME = ".tpl";

    /* Source files extension */
    private static final String JAVA_EXT_NAME = ".java";

    /* user.dir property value with file separator */
    private static final String userDir = System.getProperty("user.dir")
            + File.separatorChar;

    /* entry path in the msgstool.jar file - common for all nesessary entries */
    private static final String ENTRY_PATH = MsgClassGenerator.class
            .getPackage().getName().replace('.', '/') + '/';

    /* messages.properties File object */
    private static final File MSGS_PROPERTIES_FILE = getResourceFromJar(
            ENTRY_PATH, MSGS_PROPERTIES_NAME);

    /* Messages template File object */
    private static final File MESSAGES_TEMPLATE_FILE = getResourceFromJar(
            ENTRY_PATH, MSG_CLASS_NAME + PATTERN_EXT_NAME);

    /* Destination dir to store generation results */
    private File dstDir = null;

    /**
     * Default constructor.
     */
    public MsgClassGenerator() {
        super();
    }

    /**
     * Generates files for internationalization task to the specified directory
     * for the modules listed in specified property file.
     * 
     * @param dir -
     *            destination folder
     * @param props -
     *            file with properties list that is the list of modules names
     *            that are to be processed.
     * @throws Exception
     */
    public void generate(File dir, File props) {
        File modProps;
        if (props == null) {
            modProps = getResourceFromJar(ENTRY_PATH,
                    MODULES_PROPERTIES_NAME);
            modProps.deleteOnExit();
        } else {
            modProps = props;
        }

        dstDir = (dir != null) ? dir : new File(userDir);
        Properties prs = null;
        FileInputStream fis = null;
        int fails = 0; // number of fails

        try {
            fis = new FileInputStream(modProps);
        } catch (FileNotFoundException e) {
            logMessage("Generation error: Specified file " + modProps.getPath()
                    + " doesn't exist!");
            return;
        }
        prs = new Properties();
        try {
            prs.load(fis);
            fis.close();
        } catch (Exception e) {
            logMessage("\nGeneration error:" + e.getMessage());
        }

        logMessage("\nGeneration started:\n");
        logMessage("Modules generation status: ");

        Enumeration en = prs.keys();
        while (en.hasMoreElements()) {
            String modName = prs.getProperty((String) en.nextElement()).trim();
            logMessage(modName + " : ");
            try {
                createSourceFromTemplate(modName, MESSAGES_TEMPLATE_FILE,
                        MSG_CLASS_NAME);
                createMsgsProperties(modName);
            } catch (Exception e) {
                logMessage("error: " + e.getMessage());
                fails++;
                continue;
            }

            logMessage("completed");
        }

        if (fails == 0) {
            logMessage("\nGeneration successfully finished!");
        } else {
            logMessage("\nGeneration finished with " + fails + " fails!");
            logMessage("See output information for details!");
        }
    }

    /**
     * Writes text to destination file from the source file with replacing
     * specified word in source file to the moduleName value.
     * 
     * @param srcFile -
     *            source file
     * @param dstFile -
     *            destination file
     * @param moduleName -
     *            string to replace
     * @throws FileNotFoundException
     *             if src template or dst source file doesn't exist.
     * @throws IOException
     *             if an I/O error occurred.
     */
    private void templateReplace(File srcFile, File dstFile, String moduleName)
            throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(srcFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(dstFile));

        String s;
        while ((s = br.readLine()) != null) {
            if (s.indexOf(MODULE_PATTERN_NAME) != -1) {
                s = s.replaceAll(MODULE_PATTERN_NAME, moduleName);
            }
            bw.write(s);
            bw.newLine();
        }

        bw.close();
        br.close();

    }

    /**
     * Creates java source file from the template file with specified
     * templateName and place it in the "<code>dstDir</code>\modules\<code>moduleName</code>\src\main\java\org\apache\
     * harmony\<code>moduleName</code>\internal\nls" folder.
     * 
     * @param moduleName
     *            name of the module
     * @param srcFile
     *            template File objet
     * @param templateName
     *            name of the template file
     * @throws FileNotFoundException
     *             if src template or dst source file doesn't exist.
     * @throws IOException
     *             if an I/O error occurred.
     */
    private void createSourceFromTemplate(String moduleName, File srcFile,
            String templateName) throws Exception {
        /* Open destination file */
        File dstFile = new File(getDstDir(moduleName), templateName + JAVA_EXT_NAME);
        dstFile.createNewFile();
        logMessage("\t" + dstFile.getAbsolutePath());
        templateReplace(srcFile, dstFile, getPackageFromModule(moduleName));

    }

    /**
     * Copies messages.properties file to the "<code>dstDir</code>\modules\<code>moduleName</code>\src\main\java\org\apache\
     * harmony\<code>moduleName</code>\internal\nls" folder. If another
     * messages.file already exists nothing is done.
     * 
     * @param moduleName
     *            name of the module
     * @throws FileNotFoundException
     *             if src or dst messages.properties file doesn't exist.
     * @throws IOException
     *             if an I/O error occurred.
     */
    private void createMsgsProperties(String moduleName) throws Exception {
        
        File dstFile = new File(getDstDir(moduleName), MSGS_PROPERTIES_NAME);

        if (dstFile.exists()) {
            /* Existing messages.properties file isn't to be overridden */
            return;

        }

        /* Open pattern file */
        FileInputStream fis = new FileInputStream(MSGS_PROPERTIES_FILE);
        FileOutputStream fos = new FileOutputStream(dstFile);

        byte[] data = new byte[fis.available()];
        while (fis.read(data) > 0) {
            fos.write(data);
            data = new byte[fis.available()];
        }

        fis.close();
        fos.close();

        logMessage("\t" + dstFile.getAbsolutePath());
    }

    /**
     * Returns destination dir for output. Directory is created if it wasn't
     * exist. 
     * 
     * @param moduleName - module name
     * @return File object that is the dir for output.
     */
    private File getDstDir(String moduleName){
        // Path to the source code dir
        File srcDir = new File(dstDir, "modules/" + moduleName
                + "/src/main/java");
        
        // Some modules have platform dependent code, thus source code dir
        // splitted into the common dir ("common") and platform dependent dirs. 
        if (srcDir.exists() && srcDir.isDirectory()){
            String names[] = srcDir.list(new FilenameCommonFilter());
            /* If there is a "common" subdir - choose it */
            if (names.length > 0){
                srcDir = new File(srcDir, names[0]);
            }
        }
        
        // Path to the generated source files from the source dir 
        String path = "org/apache/harmony/" + getPackageFromModule(moduleName)
                + "/internal/nls";
        
        File dir = new File(srcDir, path);
        dir.mkdirs();
        
        return dir;
    }
    /**
     * Convenient way to run generation for the specified destination directory
     * and properties file with the list of modules which are to be processed.
     * 
     * @param dir -
     *            destination dir
     * @param props -
     *            properties file
     * @throws Exception
     */
    public static void run(File dir, File props) {
        new MsgClassGenerator().generate(dir, props);
    }

    /**
     * Returns File object from the msgstool.jar JAR file according to the entry
     * name specified.
     * 
     * @param entryName -
     *            entry name
     * @param fileName - resource file name
     * @return new File object from the JAR file corresponding to the given
     *         entry name
     */
    private static File getResourceFromJar(String entryPath, String fileName) {
        try {
            InputStream jis = MsgClassGenerator.class.getClassLoader()
                    .getResourceAsStream(entryPath + fileName);
            if (jis == null){
                logMessage("Error: there is no " + entryPath + fileName
                        + " in the resources!");
                return null;
            }
            File resFile = new File(fileName);
            resFile.createNewFile();
            resFile.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(resFile);

            byte[] data = new byte[jis.available()];
            int i;
            while ((i = jis.read(data)) > 0) {
                fos.write(data, 0 , i);
                data = new byte[jis.available()];
            }

            jis.close();
            fos.close();

            return resFile;

        } catch (Exception e) {
            logMessage("Couldn't extract resource!");
            e.printStackTrace();
            return null;
        }

    }
    
    /**
     * Returns package name from the given module name.
     * Some modules have package names differing from their names, 
     * these names are to be formatted.
     * E.g. 'nio_char' or 'x-net' would have package names o.a.h.niochar. 
     * and o.a.h.xnet respectively. 
     * @param modName name of the module.
     */
    private String getPackageFromModule(String modName){
        String pkgName = modName;
        return pkgName.replaceAll("[_,-]", "");
    }
    
    public static void logMessage(String msg) {
        System.out.println(msg);
    }
    
    private class FilenameCommonFilter implements FilenameFilter{

        public boolean accept(File dir, String name) {
            if (name.equals("common")){
                File commonDir = new File(dir, name);
                return commonDir.isDirectory();
            }
            return false;
        }
        
    }
}
