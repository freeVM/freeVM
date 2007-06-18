import java.util.*;
import java.util.zip.*;
import java.io.*;

public class testsCount {

    public static int totalTests = 0;
    public static int failedTests = 0;
    public static int passedTests = 0;
    public static int errorTests = 0;
    public static int skippedTests = 0;
    public static int unknownTests = 0;

    public static int skippedAddTests = 0;

    public static LinkedList lPass = new LinkedList();
    public static LinkedList lFail = new LinkedList();
    public static LinkedList lErr = new LinkedList();
    public static LinkedList lSkip = new LinkedList();

    public static LinkedList lSkipAdd = new LinkedList();

    public static LinkedList lUnknown = new LinkedList();

    public static String dirIn = "";

    public static boolean debugFlag = false;

    public static ZipOutputStream zf;

    public static String zfName = "";

    public static void main(String[] argv) {
        System.exit(new testsCount().calcCount(argv));
    }
    public int calcCount(String[] argv) {
        String arg0 = "";
        File f1;
        File f2;
        String s = "";
        String suite_home = "";
        String suite_version = "";
        String tested_vm = "";
        String used_os = "";
        String skipF = "";
        if (argv.length > 0) {
            arg0 = argv[0];
            dirIn = arg0;
        } else {
            System.err.println("There are no arguments");
            return 105;
        }
    if (argv.length > 1) {
            skipF = argv[1];
        }
    if (argv.length > 2) {
            suite_home = argv[2];
        }
        if (argv.length > 3) {
            suite_version = argv[3];
        }
        if (argv.length > 4) {
            tested_vm = argv[4];
        }
        if (argv.length > 5) {
            used_os = argv[5];
        }
        if (argv.length > 6) {
            zfName = argv[6];
        }

        if (argv.length > 7) {
            debugFlag = true;
        }


        if (skipF.length() > 0) {
            skippedAddTests = createSkipAdd(skipF);
            if (skippedAddTests <= 0 ) {
                skippedAddTests = 0;
            }
        }
        try {
            f1 = new File(arg0);
            if (!f1.exists()) {
                System.err.println("There is no file or directory: " + arg0);
                return 105;
            }
            if (f1.isFile()) {
                System.err.println(arg0 +"  is File");
                return 105;
            }
            dirIn = f1.getPath();

            if (zfName.length() == 0) {
                zfName = dirIn.concat(File.separator).concat("zipFailTests.zip");
            }
            zf = new ZipOutputStream(new FileOutputStream(new File(zfName)));

            fromDirectory(f1);
            totalTests += skippedAddTests;
            skippedTests +=skippedAddTests;            
            f2 = new File(arg0+File.separator+"report.xml");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f2));    
            
            try {
                bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                bw.newLine();
                bw.write("<?xml-stylesheet type='text/xsl' href='report.xsl'?>");
                bw.newLine();
                bw.write("<Report>");
                bw.newLine();
                bw.write("<property-list>");
                bw.newLine();
                bw.write("<property-item name=\"date\"></property-item>");
                bw.newLine();
                bw.write("<property-item name=\"total\">"+totalTests+"</property-item>");
                bw.newLine();
                bw.write("<property-item name=\"passed\">"+passedTests+"</property-item>");
                bw.newLine();
                bw.write("<property-item name=\"failed\">"+failedTests+"</property-item>");
                bw.newLine();
                bw.write("<property-item name=\"error\">"+errorTests+"</property-item>");
                bw.newLine();
                bw.write("<property-item name=\"skipped\">"+skippedTests+"</property-item>");
                bw.newLine();
                bw.write("<property-item name=\"unknown\">"+unknownTests+"</property-item>");
                bw.newLine();
                bw.write("</property-list>");
                bw.newLine();
                bw.write("<cfg-list>");
                bw.newLine();
                bw.write("<cfg-item name=\"test suite root\">"+suite_home+"</cfg-item>");
                bw.write("<cfg-item name=\"test suite version\">"+suite_version+"</cfg-item>");
                bw.write("<cfg-item name=\"test root\">"+suite_home+"/src </cfg-item>");
                bw.write("<cfg-item name=\"test class root\">"+suite_home+"/bin </cfg-item>");
                bw.write("<cfg-item name=\"tested runtime\">"+tested_vm+" </cfg-item>");
//              <cfg-item name="execution mode">other VM </cfg-item>
                bw.write("<cfg-item name=\"os\">"+used_os+"</cfg-item>");
//              <cfg-item name="path to env_cfg.xml file"> </cfg-item>
//              <cfg-item name="path to system env file"> </cfg-item>
                
                bw.write("</cfg-list>");
                bw.newLine();
                if (debugFlag) {
                    System.err.println("Total tests: " + totalTests);
                    System.err.println("Failed tests: " + failedTests);
                }
                putList(bw, lFail,"failed-list", debugFlag);
                if (debugFlag) {               
                    System.err.println("Tests finished with error code: " + errorTests);
                }
                putList(bw, lErr,"error-list", debugFlag);
                if (debugFlag) {
                    System.err.println("Tests finished with unknown code: " + unknownTests);
                }
                putList(bw, lUnknown,"unknowm-list", debugFlag);
                putList(bw, lSkip,lSkipAdd,"modeError-list", false);
                putList(bw, lPass,"passed-list", false);
                bw.write("</Report>");
                bw.newLine();
                bw.close();
            } catch (Throwable e) {
                e.printStackTrace(System.err);
                return 105;
            }
                addToZip(f2);
                f2 = new File(dirIn.concat(File.separator).concat("test.xsl"));
                addToZip(f2);
                f2 = new File(dirIn.concat(File.separator).concat("report.xsl"));
                addToZip(f2);

                zf.close();
            if ((errorTests > 0) || (failedTests >0) || (unknownTests >0)) {
                return 1;
            } else {
                return 0;
            }
       } catch (Exception e) {
           e.printStackTrace(System.err);
           return 105;
       }
    }


    public static void putList(BufferedWriter bw, LinkedList li, String title,
        boolean prnt) throws Exception  {
        bw.write("<"+title+">");
        bw.newLine();
        for (int i = 0; i < li.size(); i++) {
            String tt = (String)li.get(i);
            bw.write("<list-item>" + tt + "</list-item>\n");
            bw.newLine();
            if (prnt) {
                String ttt = tt.substring(2);
                if ( ttt.endsWith(".thr") ||  ttt.endsWith(".xml") ){
                    ttt = ttt.substring(0, (ttt.length() - 4));
                }
                System.err.println(Integer.toString(i+1)+"  "+ttt);
            }
        }
        bw.write("</"+title+">");
        bw.newLine();
    }

    public static void putList(BufferedWriter bw, LinkedList li1, LinkedList li2, String title,
        boolean prnt) throws Exception  {
        bw.write("<"+title+">");
        bw.newLine();
        for (int i = 0; i < li1.size(); i++) {
            String tt = (String)li1.get(i);
            bw.write("<list-item>" + tt + "</list-item>\n");
            bw.newLine();
            if (prnt) {
                String ttt = tt.substring(2);
                if ( ttt.endsWith(".thr") ||  ttt.endsWith(".xml") ){
                    ttt = ttt.substring(0, (ttt.length() - 4));
                }
                System.err.println(Integer.toString(i+1)+"  "+ttt);
            }
        }
        int n = li1.size();
        for (int i = 0; i < li2.size(); i++) {
            String tt = (String)li2.get(i);
            bw.write("<list-item>" + tt + "</list-item>\n");
            bw.newLine();
            if (prnt) {
                if ( tt.endsWith(".thr") ||  tt.endsWith(".xml") ){
                    tt = tt.substring(0, (tt.length() - 4));
                }
                System.err.println(Integer.toString(i+1+n)+"  "+tt);
            }
        }

        bw.write("</"+title+">");
        bw.newLine();
    }



    public static void fromDirectory(File f1) {
        String [] listDir = f1.list();
        File f2;
        for (int j = 0; j < listDir.length; j++) {
            if (listDir[j].indexOf(".xml") != -1) {
                f2 = new File(f1, listDir[j]);
                if (f2.isFile()) {
                    if (countResults(f2) != 0) {
                        return;
                    } else {
                        continue;
                    }
                }
            } else {
               f2 = new File(f1, listDir[j]);
            }
            if (!f2.isFile()) {
                fromDirectory(f2);
            }
        }
    }

    public static int countResults(File ff) {
        BufferedReader br;
        String relnm = "";
        String s = "";
        if (ff.getName().equals("report.xml")) {
            return 0;
        }
        if (ff.getName().equals("AllTests.xml")) {
            return 0;
        }
        try {
            br = new BufferedReader(new FileReader(ff));        
            if (ff.getPath().startsWith(dirIn)) {
                relnm = ".".concat(ff.getPath().substring(dirIn.length()));
            } else {
                relnm = ff.getPath();
            }
        } catch (Exception e) {
            System.err.println("CreateJavaFile: unexpected exception  on creating of BufferedReader"+ e);
            e.printStackTrace(System.err);
            return 1;
        }
        totalTests ++;
        try {
            while (true) {
                s = br.readLine();
                if (s == null) {
                    break;
                }
                if (s.indexOf("<property-item name=\"Status\">104") != -1) {
                    passedTests++;
                    lPass.add(relnm);
                    break;
                }
                if (s.indexOf("<property-item name=\"Status\">105") != -1) {
                    failedTests++;
                    lFail.add(relnm);
                    addToZip(ff);
                    break;
                }
                if (s.indexOf("<property-item name=\"Status\">106") != -1) {
                    errorTests++;
                    lErr.add(relnm);
                    addToZip(ff);
                    break;
                }
                if (s.indexOf("<property-item name=\"Status\">107") != -1) {
                    skippedTests++;
                    lSkip.add(ff);
                    break;
                }
                if (s.indexOf("<property-item name=\"Status\">") != -1) {
                    unknownTests++;
                    lUnknown.add(relnm);
                    addToZip(ff);
                    break;
                }
            }
            br.close();            
            return 0;
        } catch (IOException e) {
            System.err.println("createJavaFile: unexpected exception " + e);
            e.printStackTrace(System.err);
            return 1;
        }
    }

    public static int addToZip(File ff) {
        ZipEntry ze = null;
        BufferedInputStream br = null;
        byte[] buf = new byte[500];
        int sz = 0;
        try {
            String z = "";
            int tt = ff.getPath().indexOf(dirIn);
            if (tt != -1) {
                z = ff.getPath().substring(dirIn.length() + 1);
            } else {
                z = ff.getPath();
            }
           ze = new ZipEntry(z);//ff.getPath());
           br = new BufferedInputStream(new FileInputStream(ff));
           zf.putNextEntry(ze);
           while (true) {
               int t = br.read(buf, 0, buf.length);
               if (t == -1) {
                   break;
               }
               zf.write(buf, 0, t);
               sz+=t;
           }
           return sz;
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            return -1;
        }
    }


    public static int createSkipAdd(String skF)  {
        BufferedReader br = null;
        String s = "";
        int ind = -1;
        int ind1 = -1;
        File f;
        try {
            f = new File(skF);
            if (!f.exists()) {
               return 0;
            }
            br = new BufferedReader(new FileReader(f));        
            while (true) {
                s = br.readLine();
                if (s == null) {
                    break;
                }
                if (s.length() == 0) {
                    continue;
                }
                lSkipAdd.add(s);
            }
            br.close();            
            return lSkipAdd.size();
        } catch (IOException e) {
            System.err.println("Unexpected exception during creating list of excluded tests: " + e);
            e.printStackTrace(System.err);
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e1) {
                    System.err.println("Unexpected exception during closing file: " + e1);
                }
            }
            return -1;
        }
    }
}

