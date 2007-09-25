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
import java.io.PrintWriter;

/**
 * Emits the summary information about EUT status in txt and hml form. 
 */
final class EUTHTMLSummaryEmitter {

    /** Keeps the output stream to write the index.htm data into. */
    private static PrintWriter out;

    /** Emits JUnit like html report with EUT specific fields w/o frames. */
    static void emitHTMLReport(PrintWriter out, EUTSummaryInfo esi) {
        EUTHTMLSummaryEmitter.out = out;

        emitiHTMLheaderAndBodyStart(esi.eut_version);
        emitRelativeSummary(esi.ss, esi.tests_run_total,
                esi.tests_unexpected_crashed_total);
        emitAbsoluteSummary(esi.ss, esi.tests_crashed_total);
        emitNoteOfErrorFailures();
        emitNoteOfSummary();
        emitSuitesStatictics();
        emitHTMLEnd();
    }

    private static void emitStylesheet() {
        out.println("<style type=\"text/css\">");
        out.println(
                "    BODY { FONT: 68% verdana,arial,helvetica; "
                + "COLOR: #000000}");
        out.println("    TABLE TR TD, TABLE TR TH { FONT-SIZE: 68% }");
        out.println("    TABLE.details TR TH { FONT-WEIGHT: bold; "
                + "BACKGROUND: #a6caf0; TEXT-ALIGN: left }");
        out.println("    TABLE.details TR TD { BACKGROUND: #eeeee0 }");
        out.println("    P { MARGIN-TOP: 0.5em; MARGIN-BOTTOM: 1em; "
                + "LINE-HEIGHT: 1.5em }");
        out.println("    H1 { MARGIN: 0px 0px 5px; "
                + "FONT: 165% verdana,arial,helvetica }");
        out.println("    H2 { MARGIN-TOP: 1em; MARGIN-BOTTOM: 0.5em; "
                + "FONT: bold 125% verdana,arial,helvetica }");
        out.println("    .Error { FONT-WEIGHT: bold; COLOR: red }");
        out.println("    .Failure { FONT-WEIGHT: bold; COLOR: purple }");
        out.println("    .Properties { TEXT-ALIGN: right }");
        out.println("</style>");
    }

    private static void emitiHTMLheaderAndBodyStart(String eut_version) {
        out.println("<HTML><HEAD><TITLE>Eclipse Unit Test " + eut_version
                + " Results: Summary</TITLE>");
        out.println("<META http-equiv=Content-Type content=\"text/html; "
                + "charset=windows-1252\">");
        emitStylesheet();
        out.println("</HEAD>");
        out.println();
        out.println("<BODY>");
        out.println("<H1>Eclipse Unit Test " + eut_version + " Results</H1>");
        out.println("<TABLE width=\"100%\">");
        out.println("  <TBODY>");
        out.println("  <TR><TD align=left></TD><TD align=right>"
                + "Designed for use with");
        out.println("      <A href=\"http://www.junit.org/\">JUnit</A> and");
        out.println("      <A href=\"http://jakarta.apache.org/\">Ant</A>."
                + "</TD></TR></TBODY></TABLE>");
        out.println();
        out.println("<HR SIZE=1>");
        out.println();
    }

    private static void emitSummary(String header, String ctPref,
            int total, int passes, int failures, int errors, int crashes) {
        out.println("<H2>" + header + "</H2>");
        out.println("<TABLE class=details cellSpacing=2 cellPadding=5 "
                + "width=\"95%\" border=0>");
        out.println("  <TBODY>");
        out.println("  <TR vAlign=top>");
        out.println("    <TH width=\"20%\">Tests</TH>");
        out.println("    <TH width=\"20%\">" + ctPref + " Failures</TH>");
        out.println("    <TH width=\"20%\">" + ctPref + " Errors</TH>");
        out.println("    <TH width=\"20%\">" + ctPref + " Crashes</TH>");
        out.println("    <TH>Success rate</TH>");

        // set the results class style depends on results value
        String tClass = "\"\"";

        if (crashes != 0) {
            tClass = "Error";
        } else if (errors != 0) {
            tClass = "Failure";
        } else if (failures != 0) {
            tClass = "Failure";
        }
        out.println("  <TR class=" + tClass + " vAlign=top>");
        out.println("    <TD>" + total + "</TD>");
        out.println("    <TD>" + failures + "</TD>");
        out.println("    <TD>" + errors + "</TD>");
        out.println("    <TD>" + crashes + "</TD>");
        out.println("    <TD>" + EUTReporter.makePassrateString(passes, total)
                + "</TD></TR></TBODY></TABLE>");
    }

    private static void emitRelativeSummary(EUTSuiteInfo ss,
            int tests_run_total, int tests_unexpected_crashed_total) {
        emitSummary("Relative Summary", "Sudden",
                tests_run_total, ss.tests_reported_passed, 
                ss.tests_unexpected_end_with_failure, 
                ss.tests_unexpected_end_with_error,
                tests_unexpected_crashed_total);
    }

    private static void emitAbsoluteSummary(EUTSuiteInfo ss,
            int tests_crashed_total) {
        emitSummary("Absolute Summary", "Total",
                ss.tests_total, ss.tests_reported_passed, 
                ss.tests_reported_end_with_failure, 
                ss.tests_reported_end_with_error,
                tests_crashed_total);
    }

    private static void emitNoteOfErrorFailures() {
        out.println("<TABLE width=\"95%\" border=0>");
        out.println("  <TBODY><TR>");
        out.println("  <TD style=\"TEXT-ALIGN: justify\">"
                + "Note: <EM>failures</EM> are anticipated and checked for "
                + "with assertions while <EM>errors</EM> are unanticipated.");
        out.println("  </TD></TR></TBODY></TABLE>");
    }

    private static void emitNoteOfSummary() {
        out.println("<TABLE width=\"95%\" border=0>");
        out.println("  <TBODY><TR>");
        out.println("  <TD style=\"TEXT-ALIGN: justify\">"
                + "Note: In <EM>Relative Summary</EM> one considers Expected "
                + "Failures List, i.e. the total tests number may be reduced, "
                + "actual failures / error can be ommitted if they are "
                + "expected, and pass rate may be 100%, in <EM>Absolute "
                + "Summary</EM> the real tests status statistics is given.");
        out.println("  </TD></TR></TBODY></TABLE>");
    }

    private static void emitSuitesStatictics() {
        out.println("<H2>Suites Detailes</H2>");
        out.println("<TABLE class=details cellSpacing=2 cellPadding=5 "
                + "width=\"95%\" border=0>");
        out.println("  <TBODY>");
        out.println("  <TR vAlign=top>");
        out.println("    <TH width=\"80%\">Name</TH>");
        out.println("    <TH>Total Tests</TH>");
        out.println("    <TH>Sudden Errors</TH>");
        out.println("    <TH>Sudden Failures</TH>");
        out.println("    <TH>Sudden Crashes</TH></TR>");

        for (int i = 0; i < EUTReporter.suiteList.size(); i++) {
            EUTSuiteInfo si = EUTReporter.suiteList.get(i);

            if (si.wasRun) {
                emitOneSuiteStatictics(si);
            } else {
                out.println("  <TR class=\"\" vAlign=top>");
                out.println("    <TD>" + si.name + "</TD>");
                out.println("    <TD>" + si.tests_total + "</TD>");
                out.println("    <TD colspan=3 align=center><EM>not run</EM></TD>");
            }
        }
        out.println("  </TBODY></TABLE>");
    }

    private static void emitOneSuiteStatictics(EUTSuiteInfo si) {
        String tClass = "\"\"";
        String strError = Integer.toString(si.tests_unexpected_end_with_error);
        String strFailure = Integer.toString(
                si.tests_unexpected_end_with_failure);
        String strCrash = "&nbsp;";

        // detect report style based on issue type
        if (si.isCrashed && !si.isCrashExpected) {
            tClass = "Error";
            strError = "&nbsp;";
            strFailure = "&nbsp;";
            strCrash = "crashed";
        } else if (si.tests_unexpected_end_with_error != 0) {
            tClass = "Failure";
        }  else if (si.tests_unexpected_end_with_failure != 0) {
            tClass = "Failure";
        }

        if (si.isCrashed && si.isCrashExpected) {
            strCrash = "expected crash";
        }

        // emit html code finally
        out.println("  <TR class=" + tClass + " vAlign=top>");

        // no link to html report in case of crash (because there is no html)
        String html_ref_prefix = "";
        String html_ref_suffix = "";

        if (!si.isCrashed && si.html_report_file_name != null) {
            String html_name = EUTReporter.path_to_html_prefix 
                    + si.html_report_file_name;
            html_ref_prefix = "<A href=\"" + html_name + "\">";
            html_ref_suffix = "</A>";
        }
        out.println("    <TD>" + html_ref_prefix);
        out.println("        " + si.name + html_ref_suffix + "</TD>");
        out.println("    <TD>" + si.tests_total + "</TD>");
        out.println("    <TD>" + strError + "</TD>");
        out.println("    <TD>" + strFailure + "</TD>");
        out.println("    <TD>" + strCrash + "</TD></TR>");
    }

    private static void emitHTMLEnd() {
        out.println("</BODY></HTML>");
    }
} // end of class 'EUTHTMLSummaryEmitter' definition
