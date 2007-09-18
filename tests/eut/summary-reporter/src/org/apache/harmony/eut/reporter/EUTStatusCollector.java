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
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Properties;

/**
 */
final class EUTStatusCollector extends DefaultHandler {

    /** Keeps the currently processed test reference. */
    private static EUTTestInfo processedTest;

    /** Keeps the currently processed suite reference. */
    private static EUTSuiteInfo processedSuite;

    /**
     * This method is called from DefaultHandler.startElement when 'testsuite'
     * tag is to be processed. This method sets 'processedSuite' field to
     * non-null reference.
     */
    private static void processTestSuiteTag(Attributes attributes) {
        String suite_name = attributes.getValue("package");
        suite_name = suite_name + "." + attributes.getValue("name");

        for (int i = 0; i < EUTReporter.suiteList.size(); i++) {
            if (EUTReporter.suiteList.get(i).name.equals(suite_name)) {
                processedSuite = EUTReporter.suiteList.get(i);
                break;
            }
        }

        // check this test suite is known (may be a configuration issue)
        if (processedSuite == null) {
            System.err.println("Warning: reported test suite is unknown: " +
                    suite_name);
            processedSuite = new EUTSuiteInfo();
            processedSuite.name = suite_name;
            processedSuite.wasRun = true;
            processedSuite.tests_total = -1;
            EUTReporter.suiteList.add(processedSuite);
        }

        // the valid XML report exists for this suite, so suite dis not crash
        processedSuite.isCrashed = false;

        // extract the toal tests number
        int reported_total_tests = Integer.parseInt(attributes.getValue(
                "tests"));

        // this test suite is not registered on eut*.suite.properties file
        if (processedSuite.tests_total == -1) {
            System.err.println("Warning: reported tests number for " +
                    suite_name + " suite is " + reported_total_tests);
            processedSuite.tests_total = reported_total_tests;
        }

        // eut*.suite.properties may keep wrong tests number (config issue)
        if (processedSuite.tests_total  != reported_total_tests) {
            System.err.println("Warning: incorrect tests number for " +
                    suite_name + " suite:");
            System.err.println("    tests number in properties file is " +
                    processedSuite.tests_total);
            System.err.println("    tests number in xml report file is " +
                    reported_total_tests);
            processedSuite.tests_total = reported_total_tests;
        }

        // extract the number of errors and failures
        processedSuite.tests_reported_end_with_error =
            Integer.parseInt(attributes.getValue("errors"));
        processedSuite.tests_reported_end_with_failure =
            Integer.parseInt(attributes.getValue("failures"));

        // calculate number of passed tests and create unexpected failure list
        processedSuite.tests_reported_passed = processedSuite.tests_total -
                processedSuite.tests_reported_end_with_error -
                processedSuite.tests_reported_end_with_failure;
        processedSuite.unexpectedErrorFailureTests =
            new ArrayList<EUTTestInfo>();
    }

    /**
     * Called by parser when new XML tag is found.
     */
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        if (qName.equals("testsuite")) {
            processTestSuiteTag(attributes);
            return;
        }

        if (qName.equals("testcase")) {
            processedTest = new EUTTestInfo();
            processedTest.testClass = attributes.getValue("classname");
            processedTest.testName = attributes.getValue("name");
            return;
        }

        if (qName.equals("error") || qName.equals("failure")) {
            processedTest.testStatus = qName.equals("error") ? 
                    EUTTestInfo.TEST_ERROR : EUTTestInfo.TEST_FAILURE;
            processedTest.testIssueMessage = attributes.getValue("message");
            processedTest.testIssueContent = new StringBuffer();
            return;
        }
    }

    /**
     * Called by parser when XML tag processing is being completed.
     */
     public void endElement(String uri, String localName, String qName)
            throws SAXException {

        if (qName.equals("testsuite")) {
            processedSuite = null;
            return;
        }

        // only 'testcase' tag is interesting from here
        if (!qName.equals("testcase")) {
            return;
        }
        int eflIndex = EUTReporter.eflList.indexOf(processedTest.testClass
                + "." + processedTest.testName);

        // process the PASSED test situation
        if (processedTest.testStatus == EUTTestInfo.TEST_PASSED) {

            // the passed test may be registered in EFL (configuration issue)
            if (eflIndex != -1) {
            //    System.err.println("Warning: passed test is in EFL file: "
            //            + processedTest.testClass + "."
            //            + processedTest.testName);

                // remove passed test from EFL to calculate passrate correctly
                EUTReporter.eflList.remove(eflIndex);
            }
            return;
        }

        // expected failure/error situation
        if (eflIndex != -1) {
            processedSuite.tests_expected_failures_errors++;
            return;
        }

        // unexpected failure/error case
        if (processedTest.testStatus == EUTTestInfo.TEST_FAILURE) {
            processedSuite.tests_unexpected_end_with_failure++;
        } else {
            processedSuite.tests_unexpected_end_with_error++;
        }
        processedSuite.unexpectedErrorFailureTests.add(processedTest);
        processedTest = null;
    }

    /** Collects error/failure output available between related tags. */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (processedTest != null && processedTest.testIssueContent != null) {
            processedTest.testIssueContent.append(ch, start, length);
        }
    }

    /** Exctacts a property which may be a platform specific */
    private static String readSuiteProperty(Properties props, String key,
            String os, String ws, String arch) {
        String value = props.getProperty(key);

        // there may be platform specific value
        if (value == null) {
            key = key + '.' + os;
            value = props.getProperty(key);
        }

        if (value == null) {
            key = key + '.' + ws;
            value = props.getProperty(key);
        }

        if (value == null) {
            key = key + '.' + arch;
            value = props.getProperty(key);
        }
        return value;
    }

    /** Reads predefined suite information from a eut*.suite.property file */
    static void readSuiteProperties(File propsFile, String os, String ws,
            String arch) throws Exception {
        Properties suite_props = new Properties();
        suite_props.load(new FileInputStream(propsFile));
        int suite_counter = 1;

        for (int i = 1;; i++) {
            String suite_prefix = "suite." + i;
            String suite_name = readSuiteProperty(suite_props, suite_prefix +
                    ".name", os, ws, arch);

            // no more suite information is available 
            if (suite_name == null) {
                break;
            }
            String suite_tests = readSuiteProperty(suite_props, suite_prefix +
                    ".tests", os, ws, arch);

            if (suite_tests == null) {
                throw new RuntimeException(
                    "Internal Error: suite.properties is messed near record #"
                    + i);
            }

            // store parsed information to suite information list
            EUTSuiteInfo si = new EUTSuiteInfo();
            si.name = suite_name;
            si.tests_total = Integer.parseInt(suite_tests);
            si.isAvailable = si.tests_total != 0;
            EUTReporter.suiteList.add(si);
        }
    }
   
    /** Reads the EFL file content w/o comments to 'eflList' field. */
    static void readExpectedFailureList(File eflFile)
            throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(eflFile));
        String line = in.readLine();

        while (line != null) {
            int sharpIndex = line.indexOf('#');

            if (sharpIndex != -1) {
                line = line.substring(0, sharpIndex);
            }
            line = line.trim();

            if (line.length() != 0) {

                // the test may be already listed (configuration issue)
                if (EUTReporter.eflList.indexOf(line) != -1) {
                    System.err.println("Warning: EFL lists test several times: "
                           + line);
                } else {
                    EUTReporter.eflList.add(line);
                }
            }
            line = in.readLine();
        }
        in.close();
    }

    /** Extracts run suite name from "Result file:" string in output.txt */
    private static EUTSuiteInfo getRunSuiteNameFromLog(String line)
            throws Exception {
        line = line.trim();

        // for now all EUT are in org.eclipse package - let's use this
        int startIndex = line.indexOf("org.eclipse");

        // EUT3.3 reports an additional information after test suite name
        int endIndex = line.indexOf(". Result file:");

        if (endIndex == -1) {
            endIndex = line.length(); // this is EUT3.2 case
        }
        String run_suite_name = line.substring(startIndex, endIndex);
        EUTSuiteInfo processedSuite = null;

        for (int i = 0; i < EUTReporter.suiteList.size(); i++) {
            EUTSuiteInfo si = EUTReporter.suiteList.get(i);

            if (si.name.equals(run_suite_name)) {
                processedSuite = si;
                break;
            }
        }

        if (processedSuite == null) {
            System.err.println("Warning: run test suite is unknown: "
                    + run_suite_name);
            processedSuite = new EUTSuiteInfo();
            processedSuite.name = run_suite_name;
            processedSuite.tests_total = -1;
            EUTReporter.suiteList.add(processedSuite);
        }
        processedSuite.wasRun = true;

        // it is "crashed" until the valid XML report is found
        processedSuite.isCrashed = true;
        return processedSuite;
    }

    /** Checks if specified HTML contains the specified suite information. */
    private static boolean isHTMLContainsSuiteInformation(String suite_name,
            String results_dir, String html_file_name) throws Exception {
        File htmlReport = new File(results_dir + File.separatorChar +
                EUTReporter.path_to_html_prefix + html_file_name);

        if (!htmlReport.exists()) {
            return false;
        }
        boolean isFound = false;
        BufferedReader in = new BufferedReader(new FileReader(htmlReport));
        String line = in.readLine();

        while (line != null) {
            if (line.indexOf(suite_name) != -1) {
                isFound = true;
                break;
            }
            line = in.readLine();
        }
        in.close();
        return isFound;
    }

    /**
     * Extracts the test suite names which were actually run and suite report
     * html file name from output.txt.
     *
     * There is no one-to-one correspondence between generated XML report and
     * test suite which was run - because some XML reports is to provide
     * information on one or more suites. So the safest way to get the list of
     * run suites is to parse the output.txt.
     *
     * There is also no one-to-one correspondance between generated HTML report
     * file name and test suite 'package' name. So the simplest way to get this
     * HTML report file name is to extract it from output.txt.
     *
     * There are more tricks here:
     * 1. Several suites may have one HTML report file, only last of them is
     *    followed by "[style] Processing" patern, so one needs to collect
     *    these suite to assign them this finnaly found HTML report.
     *
     * 2. If one of such "united" suites is crashed, then no HTML report is
     *    generated for all of them. There is no "patern" for "crash" in
     *    output.tx, so the list of collected suites is kept, next suite (from
     *    dirrerent suite pack) is found, so this next suite HTML report is
     *    incorrectly assigned to all of the suites in kept list. So one need to
     *    double check while summary report generating if the stored HTML file
     *    name does correspond to suite it is stored for.
     */
    static void getRunSuiteListFromLog(File logFile, String results_dir)
            throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(logFile));
        String line = in.readLine();
        ArrayList<EUTSuiteInfo> processedSuites = new ArrayList<EUTSuiteInfo>();

        while (line != null) {

            // extract test suite name
            if (line.indexOf("[echo] Running") != -1) {
                processedSuites.add(getRunSuiteNameFromLog(line));
            } 
            
            // extract suite report html file name
            else if (line.indexOf("[style] Processing") != -1) {
                if (processedSuites.size() == 0) {
                    System.err.println(
                            "Warning: unexpected \"[style] Processing\" patern"
                            + " in output.txt: " + line);
                } else {
                    char separator = line.indexOf('/') != -1 ? '/' : '\\';
                    line = line.trim();
                    line = line.substring(
                            line.lastIndexOf(separator) + 1, line.length());

                    for (int i = 0; i < processedSuites.size(); i++) {
                        EUTSuiteInfo si = processedSuites.get(i);

                        if (isHTMLContainsSuiteInformation(si.name,
                                    results_dir, line)) {
                            si.html_report_file_name = line;
                        }
                    }
                    processedSuites.clear();
                }
            }

            // prepare to next line processing
            line = in.readLine();
        }
        in.close();
    }

    /** Lists and parses XML reports to collect suite statistics. */
    static void parseXMLReports(File resultsRoot) throws Exception {
        File[] xmlNameList = resultsRoot.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });

        for (int i = 0; i < xmlNameList.length; i++) {
            FileReader xml_report = new FileReader(xmlNameList[i]);
            XMLReader xr = XMLReaderFactory.createXMLReader();
            EUTStatusCollector handler = new EUTStatusCollector();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            xr.parse(new InputSource(xml_report));
            xml_report.close();
        }
    }
} // end of class 'EUTStatusCollector' definition
