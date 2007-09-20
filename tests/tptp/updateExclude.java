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

import java.util.*;
import java.io.*;

public class updateExclude {

    public static LinkedList excL = new LinkedList();
    public static LinkedList excFull = new LinkedList();

    public static boolean debugFlag = false;
    public static boolean exclFlag = true;
    public static int cntUpdate = 0;

    public static void main(String[] argv) {        
        System.exit(new updateExclude().mdfConfig(argv));
    }
    public int mdfConfig(String[] argv) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        BufferedWriter bw1 = null;
        String cfgF = "";
        String cfgFN = "";
        String exclF = "";
        String exclS = "";
        File f1;
        String s = "";
        if (argv.length > 0) {
            cfgF = argv[0];
        } else {
            System.out.println("There are no arguments: configuration files and  exclude list file should be defined");
            return 105;
        }
        if (argv.length > 1) {
            cfgFN = argv[1];
        } else {
            System.out.println("New configuration file  and exclude list file should be defined");
            return 105;
        }
        if (argv.length > 2) {
            exclS = argv[2];
        } else {
            System.out.println("File for skipped tests should be defined");
            return 105;
        }

	if (argv.length > 3) {
            exclF = argv[3];
        } else {
            System.out.println("Exclude list was not defined");
            exclFlag = false;
        }
	if (argv.length > 4) {
            debugFlag = true;
        }
        if (exclF.length() == 0 && exclFlag) {
            System.out.println("Exclude list was defined by empty string");
            exclFlag = false;
        }    
        if (exclFlag) {    
            try {
                f1 = new File(exclF);
                if (!f1.exists()) {
                    System.out.println("There is no file " + exclF);
                    exclFlag = false;
                }
                if (!f1.isFile()) {
                    System.out.println(exclF +"  is not file");
                    exclFlag = false;
                }
            } catch (Exception e) {
                System.out.println("Unexcpected error. Exclude list was not updated: "+e);
                e.printStackTrace(System.err);
                return 105;
            }
            if (exclFlag) {
                if (createExcludeList(f1, debugFlag) != 0) {
                    return 105;
                }
            }
        }
        try {
//System.err.println(excL.size()+"  "+excFull.size());
            
            bw = new BufferedWriter(new FileWriter(new File(cfgFN)));                
            br = new BufferedReader(new FileReader(new File(cfgF)));                
            bw1 = new BufferedWriter(new FileWriter(new File(exclS)));

            int t;
            int cntF = 0;
            while (true) {
                t = -1;
                s = br.readLine();
                if (s == null) {
                    break;
                }
                if (!exclFlag) {
                    bw.write(s);
                    bw.newLine();
                    continue;
                }
                if (s.indexOf("<test_case name") < 0) {
                    bw.write(s);
                    bw.newLine();
                    continue;
                }
                int ind = s.indexOf("\"");
                if ((ind <= 0) || (ind == s.length() - 1) ){
                    bw.write(s);
                    bw.newLine();
                    continue;
                }
                String s1 = s.substring(ind +1);
                int ind2 = s1.indexOf("\"");
                if ((ind2 <= 0) || (ind2 == s1.length() -1)) {
                    bw.write(s);
                    bw.newLine();
                    continue;
                }
                s1 = s1.substring(0, ind2);


                for (int i = 0; i < excL.size(); i++) {
                    if (s1.equals((String)excL.get(i))) {
                        t = i;
                        break;
                    }
                }
                if (t == -1) {
                    bw.write(s);
                    bw.newLine();
                    continue;
                }
                s1 = s.replaceFirst("execute=\"yes\"","execute=\"no\"");
                if (!s1.equals(s)) {
                    cntUpdate++;
                    bw1.write((String)excFull.get(t));
                    bw1.newLine();
                    cntF++;
                }
                if (debugFlag) {
                    System.out.println(cntUpdate+"--"+s);
                    System.out.println(cntUpdate+"++"+s1);
                }
                bw.write(s1);
                bw.newLine();
            }
            bw.close();
            br.close();
            bw1.close();
            if (debugFlag) {
                System.out.println("==Number of updated tests: "+cntUpdate);
            }

            return 0;
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            return 105;
        }
    }


    public static int createExcludeList(File ff, boolean debug)  {
        BufferedReader br = null;
        String s = "";
        int ind = -1;
        int ind1 = -1;
        try {
            br = new BufferedReader(new FileReader(ff));        
            while (true) {
                s = br.readLine();
                if (s == null) {
                    break;
                }
                s = s.trim();
                if (s.indexOf("#") == 0) {
                    continue;
                }
                if (s.indexOf("//") == 0) {
                    continue;
                }
                s = s.trim();
                if (s.length() == 0) {
                    continue;
                }
                excFull.add(s);
                ind = s.indexOf(" ");
                ind1 = s.indexOf("\t");
                if (ind > 0 ) {
                    if ( ind1 > 0) {
                        s = s.substring(0, (ind < ind1 ? ind : ind1));
                    } else {
                        s = s.substring(0, ind);
                    }
                } else {
                    if ( ind1 > 0) {
                        s = s.substring(0, ind1);
                    }
                }
                ind = s.indexOf(".xml");
                if (ind > 0) {
                    s = s.substring(0, ind);
                }
                s = s.replaceAll("/",".");
                ind = s.lastIndexOf(".");

                s = s.substring(ind + 1);
                excL.add(s);
            }
            if (debug) {
                for (int i = 0; i < excL.size(); i++) {
                    System.out.println((String)excL.get(i));
                }
            }
            if (excL.size() == 0) {
                exclFlag = false;
            }                    
            br.close();            
            return 0;
        } catch (IOException e) {
            System.out.println("Unexpected exception during creating exclude list: " + e);
            e.printStackTrace(System.err);
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e1) {
                    System.out.println("Unexpected exception during closing exclude file: " + e1);
                }
            }
            return 1;
        }
    }
}

