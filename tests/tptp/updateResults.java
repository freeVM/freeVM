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

public class updateResults {

    public static LinkedList failedL = new LinkedList();
    public static LinkedList failedL1 = new LinkedList();
    public static LinkedList commL = new LinkedList();
    public static LinkedList passedL = new LinkedList();
    public static int totalTests = 0;
    public static int failedTests = 0;
    public static int errorTests = 0;
    public static int passedTests = 0;
    public static String allTime = "";

    public static void main(String[] argv) {        
        System.exit(new updateResults().test(argv));
    }
    public int test(String[] argv) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        String resF = "";
        String resR = "";
        File f1;
        String s = "";
        if (argv.length > 0) {
            resF = argv[0];
        } else {
            System.out.println("Results file was not defined");
            return 105;
        }
        if (argv.length > 1) {
            resR = argv[1];
        } else {
            System.out.println("Updated file was not defined");
            return 105;
        }
        try {

            f1 = new File(resF);
            if (!f1.exists()) {
                System.out.println("There is no results file");
            }                       
            if (cntResults(f1) <= 0) {
                return 105;
            }
//System.out.println(cntResults(f1));

            bw = new BufferedWriter(new FileWriter(new File(resR)));                
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            bw.newLine();
            s = "<testsuite errors=\""
                     .concat(Integer.toString(errorTests)).concat("\" failures=\"")
                     .concat(Integer.toString(failedTests)).concat("\"  name=\" \" tests=\"")
                     .concat(Integer.toString(totalTests)).concat("\" time=\"").concat(allTime)
                     .concat("\">");
            bw.write(s);
            bw.newLine();
            bw.write("<properties>");
            bw.newLine();
            bw.write("</properties>");
            bw.newLine();
            for (int i = 0; i < passedL.size(); i++) {
                bw.write((String)passedL.get(i));
                bw.newLine();
            }
            for (int i = 0; i < failedL.size(); i++) {
                bw.write((String)failedL.get(i));
                bw.newLine();
                s = "<failure message=\"\" type=\"\">";
                bw.write(s.concat((String)commL.get(i)).concat(" </failure>"));
                bw.newLine();
                bw.write("</testcase>");
                bw.newLine();
            }

            bw.write("  <system-out><![CDATA[]]>");
            bw.newLine();
            bw.write("  </system-out>");
            bw.newLine();
            bw.write("  <system-err><![CDATA[]]>");
            bw.newLine();
            bw.write("  </system-err>");
            bw.newLine();
            bw.write("  </testsuite>");
            bw.newLine();

            bw.close();
            return 0;
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            return 105;
        }
    }


    public static int cntResults(File ff)  {
        BufferedReader br = null;
        String s = "";
        String s1 = "";
        String s2 = "";
        StringBuffer sb = new StringBuffer();
        StringTokenizer st = null;
        StringTokenizer st1 = null;

        try {
            br = new BufferedReader(new FileReader(ff));        
            while (true) {
                s = br.readLine();
                if (s == null) {
                    break;
                }
                if (s.indexOf("There were no test cases") != -1) {
                    continue;
                }
                if (s.indexOf("Check the local configuration") != -1) {
                    continue;
                }
                if (s.startsWith("Time:")) 
				{
                    int ind = s.indexOf(" ");
                    allTime = s.substring(ind+1);
                    break;
                }
                sb.append(s);
            }
            st = new StringTokenizer(sb.toString(), ".");
            
            while (st.hasMoreTokens()) {
                s = st.nextToken();
                s = s.trim();               
                if (s.length() == 0) {
                    continue;
                }

                st1 = new StringTokenizer(s, " ");
                totalTests ++;              
                String nm = st1.nextToken();
                String tm = st1.nextToken();
                s = "<testcase name=\"".concat(nm).concat("\"  classname=\" \" time=\"").concat(tm).concat("\">");
                if (st1.countTokens() == 1) {                       
                    failedL.add(s);
                    failedL1.add(nm);
                    failedTests ++;
                } else {
                    passedTests ++;                  
                    passedL.add(s.concat("</testcase>"));
                }
            }        
            if (failedTests ==  0) {
                br.close();
                return totalTests;
            }

            int nmb = 0;
            String comm = "";
            boolean isTest = false;
            while (true) {
                s = br.readLine();
                if (s == null) {
                    break;
                }
                if (s.startsWith("FAILURES!")) {
                    commL.add(comm);
                    break;
                }
                if (!isTest) {
                    s1 = Integer.toString(nmb+1).concat(") ").concat((String)failedL1.get(nmb));
                    if (s.startsWith(s1)) {                    
                        int p = s.indexOf(" ");
                        comm = comm.concat(s.substring(p)).concat("\n");
                        isTest = true;
                        nmb++;
                        if (nmb < failedL1.size()) {
                            s1 = Integer.toString(nmb+1).concat(") ").concat((String)failedL1.get(nmb));
                        } else {
                            s1 = "";
                        }
                    }
                } else {
                     if (s1.length() == 0) { 
                         comm = comm.concat(s).concat("\n");
                         continue;
                     }
                     if (s.startsWith(s1)) {                                       
                         commL.add(comm);
                         comm = "";
                         int p = s.indexOf(" ");
                         comm = comm.concat(s.substring(p)).concat("\n");
                         s1 = Integer.toString(nmb+1).concat(") ").concat((String)failedL1.get(nmb));
                         nmb++;
                         if (nmb < failedL1.size()) {
                             s1 = Integer.toString(nmb+1).concat(") ").concat((String)failedL1.get(nmb));
                         } else {
                             s1 = "";
                         }
                     } else {
                         comm = comm.concat(s).concat("\n");
                     }
                }           
                                            
            }

            br.close();            
            return totalTests;
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
            return -1;
        }
    }
}

