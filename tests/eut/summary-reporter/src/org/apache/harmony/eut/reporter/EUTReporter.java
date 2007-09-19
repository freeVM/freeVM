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
package org.apache.harmony.eut.reporter;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * There are "os", "arch" and "ws" mandatory arguments required by this class -
 * these arguments can be detected from this class through SDK API, still this
 * is not done to avoid duplication - build.xml already did this work.
 */
public final class EUTReporter {

    /* EUTReporter return codes. */
    private static final int RETURN_EUT_PASSED = 0;
    private static final int RETURN_EUT_FAILED = 1;
    private static final int RETURN_USAGE_ERROR = 2;
    private static final int RETURN_INTERNAL_ERROR = 3;

    /** Keeps the list of test suites being processed. */
    static ArrayList<EUTSuiteInfo> suiteList = new ArrayList<EUTSuiteInfo>();

    /** Keeps the list of expected failure/errors/crashes. */
    static ArrayList<String> eflList = new ArrayList<String>();

    /** Keeps the relative path to html suite summary page name. */
    static final String path_to_html_prefix = "results/html/";

    private static void usage() {
        System.err.println("=================================================");
        System.err.println(
                "Usage: java EUTReporter <eut_version> <result_dir> " +
                "<os> <ws> <arch> <tested_vm_args>");
        System.err.println("    <eut_version>      : 3.2 or 3.3");
        System.err.println("    <resultis_dir>     : path to directory "
                + "which is supposed to have the files:");
        System.err.println("    *  output.txt - the log output of EUT run");
        System.err.println("    *  eut.efl    - Excpected Failures List file");
        System.err.println("    *  results    - JUnit generated reports");
        System.err.println("    <os>, <ws>, <arch> : same to 'runtests'");
        System.err.println("    <tested_vm_args>   : tested VM arguments " + 
                "to be printed to report");
        System.err.println("    Note: the current directory must contains:");
        System.err.println("    *  <eut_version>.suites.properties file");
        System.err.println("    *  tested.java.version file");
        System.err.println("    *  running.java.version file");
        System.err.println("EUT SCRIPT: "
                + "incorrect using of reporter or missed resources...");
        System.exit(RETURN_USAGE_ERROR);
    }

    private static void checkFileExistance(File f) {
        if (f.exists()) {
            return;
        }
        System.err.println("=================================================");
        System.err.println("Error: missed file or directory: " + f.getPath());
        usage();
    }

    /** Helper method to produce a well formatted passrate string. */
    static String makePassrateString(double passed, double total) {
        if (total == 0.) {
            return "0.00%";
        }
        double passrate = (passed * 100.0) / total;
        int aliquot = (int) passrate;

        if (aliquot == 100) {
            return "100%";
        }
        String fractionStr = String.valueOf((int) Math.round((passrate -
                        (double) aliquot) * 100.0));

        if (fractionStr.length() != 2) {
            fractionStr = "0" + fractionStr;
        }
        return String.valueOf(aliquot) + "." + fractionStr + "%";
    }

    /*
     * The contruct for return code is:
     * 0 - sucessfully generated report and EUT is PASSED
     * 1 - sucessfully generated report and EUT is FAILED
     * 2 - wrong usage (or input data is missed)
     * 3 - unexpected parsing error (uncought throwable)
     */
    public static void main(String[] args) {
        try {
            main_unsafe(args);
        } catch (Throwable e) {
            System.err.println("EUT SCRIPT: "
                    + "Unexpected Error during EUT results parsing:" + e);
            e.printStackTrace();
            System.exit(RETURN_INTERNAL_ERROR);
        }
    }

    private static void main_unsafe(String[] args) throws Exception {

        // check the run arguments
        if (args.length != 6) {
            usage();
        }
        String eut_version = args[0];
        String results_dir = args[1];
        String arg_os = args[2];
        String arg_ws = args[3];
        String arg_arch = args[4];
        String tested_vm_args = args[5];

        if (!eut_version.equals("3.2") && !eut_version.equals("3.3")) {
            System.err.println("Error: unknown EUT version: " + eut_version);
            usage();
        }

        if (!arg_os.equals("win32") && !arg_os.equals("linux")) {
            System.err.println("Error: unknown os: " + arg_os);
            usage();
        }

        if (!arg_ws.equals("win32") && !arg_ws.equals("gtk")) {
            System.err.println("Error: unknown ws: " + arg_ws);
            usage();
        }

        if (!arg_arch.equals("x86") && !arg_arch.equals("x86_64") &&
                !arg_arch.equals("ia64")) {
            System.err.println("Error: unknown arch: " + arg_arch);
            usage();
        }
        File resultsXML = new File(results_dir + File.separatorChar
                + "results" + File.separatorChar + "xml");
        checkFileExistance(resultsXML);
        File outputTXT = new File(results_dir + File.separatorChar
                + "output.txt");
        checkFileExistance(outputTXT);
        File eutEFL = new File(results_dir + File.separatorChar
                + "eut.efl");
        checkFileExistance(eutEFL);
        File eutSuitesProperties = new File("eut" + eut_version
                + ".suites.properties");
        checkFileExistance(eutSuitesProperties);
        File testedJavaVersionLog = new File("tested.java.version");
        checkFileExistance(testedJavaVersionLog);
        File runningJavaVersionLog = new File("running.java.version");
        checkFileExistance(runningJavaVersionLog);

        // collect statistics from XML reports and output.txt
        EUTStatusCollector.readExpectedFailureList(eutEFL);
        EUTStatusCollector.readSuiteProperties(eutSuitesProperties,
                arg_os, arg_ws, arg_arch);
        EUTStatusCollector.getRunSuiteListFromLog(outputTXT, results_dir);
        EUTStatusCollector.parseXMLReports(resultsXML);

        // caclulate summary statictics
        EUTSummaryInfo esi = new EUTSummaryInfo();
        esi.ss = new EUTSuiteInfo(); // Summary Suite
        esi.crashed_suites = new ArrayList<EUTSuiteInfo>();

        for (int i = 0; i < suiteList.size(); i++) {
            EUTSuiteInfo si = suiteList.get(i);

            esi.ss.tests_total += si.tests_total; // need for Absolute Summary
            esi.ss.tests_expected_failures_errors +=
                    si.tests_expected_failures_errors;
            esi.ss.tests_reported_passed += si.tests_reported_passed;
            esi.ss.tests_reported_end_with_error +=
                    si.tests_reported_end_with_error;
            esi.ss.tests_reported_end_with_failure +=
                    si.tests_reported_end_with_failure;
            esi.ss.tests_unexpected_end_with_error +=
                    si.tests_unexpected_end_with_error;
            esi.ss.tests_unexpected_end_with_failure +=
                    si.tests_unexpected_end_with_failure;

            if (si.wasRun) {
                esi.tests_run_total += si.tests_total;
                esi.tests_run_total -= si.tests_expected_failures_errors;

                if (si.isCrashed) {
                    esi.tests_crashed_total += si.tests_total;
                    esi.suites_crashed_total++;
                    esi.crashed_suites.add(si);
                }
            }
        }

        // store remained data to strcuture to pass to emitters...
        esi.eut_version = eut_version;
        esi.os = arg_os;
        esi.ws = arg_ws;
        esi.arch = arg_arch;
        esi.tested_vm_ags = tested_vm_args;
        esi.testedJavaVersionLog = testedJavaVersionLog;
        esi.runningJavaVersionLog = runningJavaVersionLog;

        // print statistics into summary index.htm 
        PrintWriter out = new PrintWriter(results_dir + File.separatorChar +
                "index.htm");
        EUTHTMLSummaryEmitter.emitHTMLReport(out, esi);
        out.close();

        // print statistics into summary report.txt 
        out = new PrintWriter(results_dir + File.separatorChar +
                "report.txt");
        EUTTXTReportEmitter.emitTXTReport(out, esi);
        out.close();
        System.out.println("EUT SCRIPT: "
                + "EUT summary report was successfully generated");

        if (esi.tests_run_total == esi.ss.tests_reported_passed) {
            System.out.println("EUT SCRIPT: "
                    + "No unexpected EUT issues detected");
            System.exit(RETURN_EUT_PASSED);
        } else {
            System.err.println("EUT SCRIPT: "
                    + "Unexpected EUT issues are detected");
            System.exit(RETURN_EUT_FAILED);
        }
    }
} // end of class 'EUTReporter' definition
