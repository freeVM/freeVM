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
import java.util.ArrayList;
import java.io.File;

/**
 * Keeps the information about EUT test suite - it is initialized during EUT
 * status collecting and used while summary report generating.
 */
final class EUTSuiteInfo {

    /** 
     * A test suite name taken from eut*.suite.properties file or detected
     * from output.txt or XML report (configuration issue case).
     */
    String name;

    /** 
     * A suite html report file name is taken from output.txt.
     */
    String html_report_file_name;

    /** 
     * Shows if the suite was run - get it from output.txt.
     */
    boolean wasRun;

    /**
     * Shows if the suite was crashed, it true if the suite was run but no
     * valid XML report was generated.
     * 
     * It is set to true when 'wasRun' field is set to true while parsing
     * output.txt, after that if the valid XML report is parsed then this
     * property is set to false.  
     */
    boolean isCrashed;

    /**
     * The crashed suite may be expected according to EFL.
     */
    boolean isCrashExpected;
 
    /** 
     * Shows if the suite is available for platform the program is run on.
     * Like org.eclipse.jdt.core.tests.eval.TestAll is available for Windows
     * only.
     */
    boolean isAvailable;

    /**
     * The tests number for the suite is taken from eut*.suite.properties file
     * or detected from XML report (configuration issue case).
     */
    int tests_total;

    /**
     * The number of passed tests in this suite - it is calculated based on
     * values from XML report.
     */
    int tests_reported_passed;

    /**
     * The number of expected failures / errors which really happen in this
     * suite - calculating while 'testsuite' tag processing in XML report. 
     * This value is used to get the total tests number for Relative Summary,
     * i.e. the total tests number must be decreased on this value.
     */
    int tests_expected_failures_errors;

    /**
     * The number of tests ended with error - the value is taken from
     * 'testsuite' tag in XML report.
     */
    int tests_reported_end_with_error;

    /**
     * The number of tests ended with failure - the value is taken from
     * 'testsuite' tag in XML report.
     */
    int tests_reported_end_with_failure;

    /**
     * The number of unexpected errors for this suite - it is derived from
     * error information ('error' tag) in XML report and corrresponded EFL
     * file.
     */
    int tests_unexpected_end_with_error;

    /**
     * The number of unexpected failures for this suite - it is derived from
     * failure information ('failure' tag) in XML report and corrresponded
     * EFL file.
     */
    int tests_unexpected_end_with_failure;

    /**
     * Keeps the list of tests ended with unexpected error/failure. 
     */
    ArrayList<EUTTestInfo> unexpectedErrorFailureTests;

    /**
     * Keeps the suite global error /failure message extract from 
     * 'error'/'failure' tag in XML report (like 'Can not find plugin" error.
     */
    String suiteIssueMessage;

    /**
     * Keeps the suite error/failure content extracted from 'error'/'failure'
     * tag in XML report.
     *
     * This field null value indicates one more state of suite (it was run, it
     * does not crash, still no testcases were run due to suite error).
     */
    StringBuffer suiteIssueContent;
} // end of class 'EUTSuiteInfo' definition

/**
 * Keeps the information about particular test of EUT test suite - it is
 * initialized during EUT status collecting, only tests whith unexpected
 * failure/error are stored and used while summary report generating.
 */
final class EUTTestInfo {
    static final int TEST_PASSED = 0;
    static final int TEST_FAILURE = 1;
    static final int TEST_ERROR = 2;

    /** 
     * Keeps the test status extracted from XML report - it is TEST_PASSED if
     * the 'testcase' tag does not have a child 'error' or 'failure' tag.
     */
    int testStatus = TEST_PASSED;

    /**
     * Keeps the class name of the test extracted from XML report.
     */
    String testClass;

    /**
     * Keeps the test method name extracted from XML report.
     */
    String testName;

    /**
     * Keeps the test error/failure message extract from 'error'/'failure' tag
     * in XML report.
     */
    String testIssueMessage;

    /**
     * Keeps the test error/failure content (stack trace in most of the cases)
     * extracted from 'error'/'failure' tag in XML report.
     */
    StringBuffer testIssueContent;

    /**
     * A helper method converting the testStatus to string.
     */
    String getStatusString() {
        switch (testStatus) {
            case TEST_PASSED: return "PASSED"; 
            case TEST_FAILURE: return "FAILURE"; 
            default: return "ERROR"; 
        }
    }
} // end of class 'EUTTestInfo' definition

/**
 * Keeps the pack of summary data to pass it to report emitters to avoid too
 * long list of method arguments.
 */
final class EUTSummaryInfo {
    EUTSuiteInfo ss; // Summary Suite
    int tests_run_total; // need for Relative Summary
    int tests_crashed_total;
    int tests_unexpected_crashed_total;
    ArrayList<EUTSuiteInfo> suites_unexpected_crashed;
    String eut_version;
    String os, ws, arch;
    String tested_vm_ags;
    File testedJavaVersionLog;
    File runningJavaVersionLog;
} // end of class 'EUTSummaryInfo' definition
