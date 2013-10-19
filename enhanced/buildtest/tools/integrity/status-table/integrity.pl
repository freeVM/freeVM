#    Licensed to the Apache Software Foundation (ASF) under one or more
#    contributor license agreements.  See the NOTICE file distributed with
#    this work for additional information regarding copyright ownership.
#    The ASF licenses this file to You under the Apache License, Version 2.0
#    (the "License"); you may not use this file except in compliance with
#    the License.  You may obtain a copy of the License at
#     
#         http://www.apache.org/licenses/LICENSE-2.0
#     
#     Unless required by applicable law or agreed to in writing, software
#     distributed under the License is distributed on an "AS IS" BASIS,
#     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#     See the License for the specific language governing permissions and
#     limitations under the License.

require LWP::UserAgent;
require HTTP::Request;
use HTTP::Date;

# Undef variable to put file one variable.
undef $/;

#User defined variables
#Proxy server to be used
#$proxy = "proxy.com:8080";

#testuites results locations
%test_results_links = ("file:///home/varlax/public_html/harmony-integrity" => "http://people.apache.org/~varlax/harmony-integrity");
                       

#list of testsuites
%testsuites = (
               "hdk"               => " Federated HDK build",
               "drlvm"             => " DRLVM build",
               "classlib"          => " Classlib build",
               "classlib-test"     => "Classlib tests",
               "classlib-gui-test" => "Classlib Swing/AWT tests",
               "ehwa-api"          => " Eclipse Hello World Application",
               "jdktools-test"     => "JDKTools tests",
               "drlvm-test"        => "DRLVM tests",
               "drlvm-reg-test"    => "DRLVM regression tests"
);   

#list of platforms
%platforms = ("WIN32" => "windows_x86",
              "LIN32" => "linux_x86",
              "WIN64" => "windows_x86_64",
              "LIN64" => "linux_x86_64",
              "LIN_IPF" => "linux_ia64");

#verbose printing
#$verbose = 1;

#Hide strings from snapshot results if there no results obtained for the testsuite
$hide_no_results = 1;

#template for testsuites summary table
$testsuites_table_template = "testsuites_table.tpl";

#template for testsuites summary table row
$testsuites_row_template = "testsuites_row.tpl";

$gen_results_base_link = ".";

#testsuites summary base dir
$test_results_output_path = ".";

#testsuites summary file
$test_results_output_file = "index.html";

# testing data
$history_file = "./history2";

#################### Subroutines ####################

$ua = LWP::UserAgent->new;
$ua->timeout(1000);
if (defined($proxy)) {
    $ua->proxy(["http", "ftp"], "$proxy");
}

$cur_timestamp = time;

sub d2s
{
    my $time = str2time($_[0]);
    if (!defined($time)) {
        return $_[0];
    }
 
    my($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = gmtime($time);
    my $str;
    if ($cur_timestamp > $time + 12*60*60) {
        $str =  sprintf("%04d-%02d-%02d",
            $year+1900, $mon+1, $mday, $hour, $min);
    } else {
        $str =  sprintf("%02d:%02d", $hour, $min);
    }

    return $str;
}

sub dd {
    my $time = str2time($_[0]);
    if (!defined($time)) {
        return $_[0];
    }
    if ($cur_timestamp > $time + 24*60*60) {
        $str =  sprintf("%d days", ($cur_timestamp - $time)/(24*60*60));
    } elsif ($cur_timestamp > $time + 60*60) {
        $str =  sprintf("%d hours", ($cur_timestamp - $time)/(60*60));
    } else {
        $str =  sprintf("%02d min", ($cur_timestamp - $time)/60);
    }
    return $str;
}

sub get_revision {
    my $loc = $_[0];
    my $rev;
    if (defined $loc) {
        $request = HTTP::Request->new(GET => "$loc/execution_log.html");
        $response = $ua->request($request);
        if ($response->is_success) {
            $response->content =~ /svn = r(\d*)/;
            $rev = $1;
            if (!defined $1) {
                $response->content =~ /svn revision = (\d*)/;
                $rev = $1;
            }
        }
    }
    return $rev;
}

sub get_result {
    my $platform = $_[0];
    my $suite = $_[1];
    
    my $status = 0;
    my $date, $txt, $link, $web_link, $rev;
    
    my $i = 0;
    my @test_results_links_list = keys(%test_results_links);
    
    while ($status == 0 && defined($test_results_links_list[$i]) ) {

        #Generate link to the snapshot results
        $link = "$test_results_links_list[$i]/$platforms{$platform}/$suite";
        $web_link = "$test_results_links{$test_results_links_list[$i]}/$platforms{$platform}/$suite";
    
        #Try to load suite status using status.txt info
        $request = HTTP::Request->new(GET => "$link/status.txt");
        $response = $ua->request($request);
        if ($response->is_success) {
            $result = $response->content;
            if (defined($verbose)) {
                print "Found status.txt for platform: $platforms{$platform}, suite: $suite\n";
                print "Link: $link/status.txt\n";
                print "Content:\n-------------\n$result\n-------------\n";
            }
            $result =~ /.*\btimestamp=(.*)[\r\n;]*.*/ig;
            if (defined(str2time($1))) {
                $date = $1;
            } else {
                $date = $response->header("Last-Modified");
            }

            if ($result =~ /.*\bpassed\b.*/i) {
                $status = 1;
                $txt = "PASSED";
            } else {
                $status = -1;
                $txt = "FAILED";
            }
        } else {
            if (defined($verbose)) {
                print "Can't download status.txt for platform: $platforms{$platform}, testsuite: $suite\n";
                print "Link: $link/status.txt\n";
                print "HTTP Response ", $response->code, "\n";
            }
        }

        #If status.txt was not loaded try to load and parse JUnit results
        if ($status == 0) {
            #Try to load testsuite status using overview-summary.html info
            $request = HTTP::Request->new(GET => "$link/overview-summary.html");
            $response = $ua->request($request);
            if ($response->is_success) {
                if (defined($verbose)) {
                    print "Found overview-summary.html platform: $platforms{$platform}, testsuite: $suite\n";
                    print "Link: $link/overview-summary.html\n";
                }
                $date = $response->header("Last-Modified");
                $result = $response->content;
                if ($result =~ /\<td\>\s*([0-9]+)\s*\<\/td\>\s*\<td\>\s*([0-9]+)\s*\<\/td\>\s*\<td\>\s*([0-9]+)\s*\<\/td\>\s*\<td\>\s*([0-9]+\.[0-9]+)\%\s*\<\/td\>/igsm) 
                {
                    $txt = "$4% ($1)";
                    $failures = $2 + $3;
                    if ($failures > 0) {
                        $status = -1;
                    } else {
                        $status = 1;
                    }
                }
            } else {
                if (defined($verbose)) {
                    print "Can't download overview-summary.html for platform: $platforms{$platform}, testsuite: $suite\n";
                    print "Link: $link/overview-summary.html\n";
                }
            }
        }
        if ($status != 0) { 
            $rev = get_revision($link);
            if (defined($verbose)) { 
                print "Status = $status\nDate = $date\nText = $txt\nRev = $rev\n";
            }
        }
        $i++;
    }
    return ($status, $date, $txt, $web_link, $rev);
}

#################### Main loop ####################

{
    #gathering testing results for each platform and each testsuite
    %testsuites_results = ();
    %last_passed = ();
    %last_passed_rev = ();

    open (DATA, "< $history_file");
    $history_data = <DATA>;
    close(DATA);

    if ($history_data) {
        if (defined($verbose)) {
            print "History file loaded: $history_file\n";
        }
        #Parse data
        while ($history_data =~ /PLATFORM:(.*?);TESTSUITE:(.*?);LASTGOOD:(.*?);LASTGOOD_REV:(.*?);/gsmi) {
            $last_passed{$1}{$2} = $3;
            $last_passed_rev{$1}{$2} = $4;
            if (defined($verbose)) {
                print "$1 $2 last good = ",d2s($3)," $4\n";
            }
        }
    } else {
        if (defined($verbose)) {
            print "History file not found: $history_file\n";
        }
    }

    $data_updated = 0;
    
    foreach $platform (keys %platforms) {
        foreach $testsuite (keys %testsuites) {
            ($status, $date, $txt, $link, $rev) = get_result($platform, $testsuite);
            if ($status != 0) 
            {
                #gathered new data 
                $data_updated = 1;

                $ds = d2s($date);
                $result_text = "<a href=\"$link\"><font color=\"{FONT_COLOR}\">$ds</font></a>";
                if ($status == 1) {
                    $result_text =~ s/\{FONT_COLOR\}/green/g;
                    $last_passed{$platform}{$testsuite} = $date;
                    $last_passed_rev{$platform}{$testsuite} = $rev;
                } else {
                    $result_text =~ s/\{FONT_COLOR\}/red/g;
                    if (exists($last_passed{$platform}{$testsuite})) {
                        $since = d2s($last_passed{$platform}{$testsuite});
                        $result_text .= "<small><br>since $since</small>";
                    } else {
                        $result_text .="<small><br>ever</small>";
                    }
                }
                $last_rev = $last_passed_rev{$platform}{$testsuite};
                if ($last_rev ne "") {
                    $result_text .="<small><br>last passed r$last_rev</small>";
                }
                $testsuites_results{$platform}{$testsuite} = $result_text;
            }
        }
    }

    #We need to update snapshot testing results table file only if 
    #some new data was downloaded or if table was removed
    if ($data_updated || 
            !-e "$test_results_output_path/$test_results_output_file")
    {
        #start generation of summary table of testing results for single snapshot
        #open table template
        open (TESTSUITES_TABLE, "< $testsuites_table_template") or die "Can't open template file $testsuites_table_template.";
        $testsuites_table = <TESTSUITES_TABLE>;
        close(TESTSUITES_TABLE);
    
        $noinfo_text = "N/A";
        $history_data = "";
    
        foreach $testsuite (sort({lc($testsuites{$a}) cmp lc($testsuites{$b}) } keys %testsuites)) {
            open (TESTSUITES_ROW, "< $testsuites_row_template") or die "Can't open template file $testsuites_row_template.";
            $testsuites_row = <TESTSUITES_ROW>;
            close(TESTSUITES_ROW);
            $testsuites_row =~ s/\{TESTSUITE_NAME\}/$testsuites{$testsuite}/g;
            $row_has_results = 0;
            foreach $platform (keys %platforms) {
                $status = $noinfo_text;
                if (exists($testsuites_results{$platform}{$testsuite})) {
                    $status = $testsuites_results{$platform}{$testsuite};
                    $row_has_results = 1;
                }
                $testsuites_row =~ s/\{$platform\}/$status/g;
                if (exists($last_passed{$platform}{$testsuite})) {
                    $history_data .= "PLATFORM:$platform;TESTSUITE:$testsuite;LASTGOOD:$last_passed{$platform}{$testsuite};LASTGOOD_REV:$last_passed_rev{$platform}{$testsuite};\n";
                }
            }
            if (!defined ($hide_no_results) || $row_has_results) {
                $testsuites_table =~ s/\{TABLE_ROW\}/$testsuites_row\n\{TABLE_ROW\}/g;
            }
        }
        $testsuites_table =~ s/\{TABLE_ROW\}//g;
        $time = gmtime;
        $testsuites_table =~ s/\{TIMESTAMP\}/$time/g;
        #end generation

        #open file and store table
        open(TESTSUITES_RES, "> $test_results_output_path/$test_results_output_file")
           or die "Can't open file $test_results_output_path/$test_results_output_file to save tests results, please check path and permissions.";
        print TESTSUITES_RES $testsuites_table;
        close(TESTSUITES_RES);
        #end store generated table

        #open cache file and store data
        open (DATA, "> $history_file")
           or die "Can't open file $history_file to save data, please check path and permissions.";
        print DATA $history_data;
        close(DATA);
        #end store cache data

    } else {
        if (defined($verbose)) {
            print "No data downloaded\n";
        }
    }
}
