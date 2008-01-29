require LWP::UserAgent;
require HTTP::Request;

#################### User difuned variables ####################

#User defined variables
#Proxy server to be used
#$proxy = "proxy.com";

#testuites results locations
%test_results_links = (
                        "file:///home/smishura/public_html/r{SNAPSHOT}"
                        => "http://people.apache.org/~smishura/r{SNAPSHOT}",
                        );

#list of testsuites
%testsuites = (
               "hdk_by_hdk"      => "Ant Scenario (or Self-Hosting)",
               "axis2_app"       => "Axis Application",
               "classlib-test"   => "Classlib tests",
               "dacapo"          => "Dacapo",
               "ehwa-api"        => "Eclipse Hello World Application(API)",
               "eut33"           => "Eclipse 3.3 Unit Tests",
               "tptp"            => "Eclipse TPTP Tests",
               "func"            => "Functional Test Suite",
               "gut"             => "Geronimo Unit Tests",
               "jdktools-test"   => "JDKTools Tests",
               "JettyScenario"   => "Jetty scenario",
               "scimark"         => "Scimark",
               "struts_test"     => "Struts",
               "tomcat-scenario" => "Tomcat scenario",
               "stress"          => "Stress Test Suite",
               "reliability"     => "Reliability Test Suite",
               "jedit_test"      => "JEdit scenario",
               "ega"             => "Eclipse Geronimo Application (EGA) scenario x 48h",
               "drlvm-test"      => "DRLVM tests",
               "drlvm-reg-test"  => "DRLVM regression tests",
               "vtsvm"           => "VTS VM Test Suite"
);   

#list of platforms
%platforms = ("WIN32" => "Windows_x86",
              "LIN32" => "Linux_x86",
              "WIN64" => "Windows_x86_64",
              "LIN64" => "Linux_x86_64");

#verbose printing
#$verbose = 1;

#Hide strings from snapshot results if there no results obtained for the testsuite
#$hide_no_results = 1;

#Directiry for caching testing data
$cache_dir = "./cache";

#Time period in seconds when script will try to download test reults not contained
#in cache
$snapshot_exp_period = 1000000; #1000000 - is about 11 days

#Time period in seconds when script will try to (re)collect results even they are 
#in cache
$cache_lag = 300000; #300000 - is about 3 days

#snapshots list page
$snapshots_page = "http://people.apache.org/builds/harmony/snapshots";

#template for snapshots summary table
$snapshots_table_template = "snapshots_table.tpl";

#template for snapshots summary table row
$snapshot_row_template = "snapshots_row.tpl";

#template for snapshots summary table row with removed binaries
$snapshot_exp_row_template = "snapshots_exp_row.tpl";

#template for testsuites summary table
$testsuites_table_template = "testsuites_table.tpl";

#template for testsuites summary table row
$testsuites_row_template = "testsuites_row.tpl";


#snapshots summary output file
$snapshots_output_file = "snapshots_summary.html";

#generated results base link
$gen_results_base_link = ".";

#testsuites summary base dir
$test_results_output_path = ".";

#testsuites summary file
$test_results_output_file = "index.html";

# API completeness report location
$api_report_path="Linux_x86/japi/index.html";

#################### Subroutines ####################

sub get_result {
    my $platform = $_[0];
    my $testsuite = $_[1];
    my @test_results_links_list = keys(%test_results_links);
    my $i = 0;
    #if period for reults update hasn't finished try to load results from all 
    #the available places
    while (!exists($testsuites_results{$platform}{$testsuite}) &&  
                defined($test_results_links_list[$i]) &&
                ($cur_timestamp - $cache_timestamp) < $snapshot_exp_period) {

        #Generate link to the snapshot results view
        $snapshots_tests_results = "$test_results_links{$test_results_links_list[$i]}/$platforms{$platform}/$testsuite";
        $snapshots_tests_results =~ s/\{SNAPSHOT\}/$snapshot/g;
        $result_text = "<a href=\"$snapshots_tests_results\"><font color=\"{FONT_COLOR}\">{RESULT}</font></a>";

        #Generate link to the snapshot results download
        $snapshots_tests_results = "$test_results_links_list[$i]/$platforms{$platform}/$testsuite";
        $snapshots_tests_results =~ s/\{SNAPSHOT\}/$snapshot/g;


        #Try to load and parse JUnit results
        if (!exists($testsuites_results{$platform}{$testsuite})) {
            #Try to load testsuite status using overview-summary.html info
            $request = HTTP::Request->new(GET => "$snapshots_tests_results/overview-summary.html");
            $response = $ua->request($request);
            if ($response->is_success) {
                if (defined($verbose)) {
                    print "Found overview-summary.html r$snapshot, platform: $platforms{$platform}, testsuite: $testsuite\n";
                    print "Link: $snapshots_tests_results/overview-summary.html\n";
                }
                $result = $response->content;
                if ($result =~ /
                                 \<td\>\s*([0-9]+)\s*\<\/td\>\s*        #Tests
                                 \<td\>\s*([0-9]+)\s*\<\/td\>\s*        #Failures
                                 \<td\>\s*([0-9]+)\s*\<\/td\>\s*        #Errors
                                 \<td\>\s*([0-9]+\.[0-9]+)\%\s*\<\/td\> #Success rate
                               /xigsm) {

                    #gathered new data 
                    $data_updated = 1;

                    $result_data = "$4% ($1)";
                    $failures = $2 + $3;
                    $result_text =~ s/\{RESULT\}/$result_data/g;
                    if ($failures > 0) {
                        $result_text =~ s/\{FONT_COLOR\}/red/g;
                        $testsuites_results{$platform}{$testsuite} = $result_text;
                        $testsuites_statuses{$platform}{$testsuite} = -1;

                        if (defined($verbose)) {
                            print "STATUS: FAILED, $result_data!\n";
                        }
                    } else {
                        $result_text =~ s/\{FONT_COLOR\}/green/g;
                        $testsuites_results{$platform}{$testsuite} = $result_text;
                        $testsuites_statuses{$platform}{$testsuite} = 1;

                        if (defined($verbose)) {
                            print "STATUS: PASSED, $result_data!\n";
                        }
                    }
                    next;
                }
            } else {
                if (defined($verbose)) {
                    print "Can't download overview-summary.html for r$snapshot, platform: $platforms{$platform}, testsuite: $testsuite\n";
                    print "Link: $snapshots_tests_results/overview-summary.html\n";
                }
            }
        }

        #If JUnit results were not found try to load EUT results in special 
        #representation
        if (!exists($testsuites_results{$platform}{$testsuite})) {
            #Try to load testsuite status using index.html info
            $request = HTTP::Request->new(GET => "$snapshots_tests_results/index.html");
            $response = $ua->request($request);
            if ($response->is_success) {
                if (defined($verbose)) {
                    print "Found index.html r$snapshot, platform: $platforms{$platform}, testsuite: $testsuite\n";
                    print "Link: $snapshots_tests_results/index.html\n";
                }
                $result = $response->content;
                if ($result =~ /
                                \<td\>\s*[0-9]+\s*\<\/td\>\s*           #Tests
                                \<td\>\s*([0-9]+)\s*\<\/td\>\s*         #Sudden Failures -> $1
                                \<td\>\s*([0-9]+)\s*\<\/td\>\s*         #Sudden Errors   -> $2
                                \<td\>\s*([0-9]+)\s*\<\/td\>\s*         #Sudden Crashes  -> $3
                                \<td\>\s*[0-9]+\.[0-9]+\%\s*\<\/td\>    #Success rate
                                .*?                                     #Skip the data between 2 tables
                                \<td\>\s*([0-9]+)\s*\<\/td\>\s*         #Tests           -> $4
                                \<td\>\s*[0-9]+\s*\<\/td\>\s*           #Sudden Failures 
                                \<td\>\s*[0-9]+\s*\<\/td\>\s*           #Sudden Errors
                                \<td\>\s*[0-9]+\s*\<\/td\>\s*           #Sudden Crashes
                                \<td\>\s*([0-9]+\.[0-9]+)\%\s*\<\/td\>  #Success rate    -> $5
                               /xigsm) {

                    #gathered new data 
                    $data_updated = 1;

                    $result_data = "$5% ($4)";
                    $failures = $1 + $2 + $3;
                    $result_text =~ s/\{RESULT\}/$result_data/g;
                    if ($failures > 0) {
                        $result_text =~ s/\{FONT_COLOR\}/red/g;
                        $testsuites_results{$platform}{$testsuite} = $result_text;
                        $testsuites_statuses{$platform}{$testsuite} = -1;

                        if (defined($verbose)) {
                            print "STATUS: FAILED, $result_data!\n";
                        }
                    } else {
                        $result_text =~ s/\{FONT_COLOR\}/green/g;
                        $testsuites_results{$platform}{$testsuite} = $result_text;
                        $testsuites_statuses{$platform}{$testsuite} = 1;

                        if (defined($verbose)) {
                            print "STATUS: PASSED, $result_data!\n";
                        }
                    }
                    next;
                }
            } else {
                if (defined($verbose)) {
                    print "Can't download index.html for r$snapshot, platform: $platforms{$platform}, testsuite: $testsuite\n";
                    print "Link: $snapshots_tests_results/overview-summary.html\n";
                }
            }
        }

        #If none of above were found try to load testsuite status using status.txt info
        $request = HTTP::Request->new(GET => "$snapshots_tests_results/status.txt");
        $response = $ua->request($request);
        if ($response->is_success) {
            if (defined($verbose)) {
                print "Found status.txt for r$snapshot, platform: $platforms{$platform}, testsuite: $testsuite\n";
                print "Link: $snapshots_tests_results/status.txt\n";
            }
            $result = $response->content;

            #gathered new data 
            $data_updated = 1;

            if ($result =~ /passed/ig) {
                $result_text =~ s/\{FONT_COLOR\}/green/g;
                $result_text =~ s/\{RESULT\}/PASSED/g;
                $testsuites_results{$platform}{$testsuite} = $result_text;
                $testsuites_statuses{$platform}{$testsuite} = 1;

                if (defined($verbose)) {
                    print "STATUS: PASSED!\n";
                }
            } else {
                $result_text =~ s/\{FONT_COLOR\}/red/g;
                $result_text =~ s/\{RESULT\}/FAILED/g;
                $testsuites_results{$platform}{$testsuite} = $result_text;
                $testsuites_statuses{$platform}{$testsuite} = -1;

                if (defined($verbose)) {
                    print "STATUS: FAILED!\n";
                }
            }
        } else {
            if (defined($verbose)) {
                print "Can't download status.txt for r$snapshot, platform: $platforms{$platform}, testsuite: $testsuite\n";
                print "Link: $snapshots_tests_results/status.txt\n";
            }
        }
       
        $i++;
    }

}


sub get_api_report {

    # TODO: search reports on all platforms
    my $rev = $_[0];

    my $i = 0;
    my @test_results_links_list = keys(%test_results_links);
    while (defined($test_results_links_list[$i])) {
        $snapshot_api_report="$test_results_links_list[$i]/$api_report_path";
        $snapshot_api_report=~ s/\{SNAPSHOT\}/$rev/g;
        $request = HTTP::Request->new(GET => "$snapshot_api_report");
        $response = $ua->request($request);
        if ($response->is_success) {
            $api_report = $snapshot_api_report;
            $data_updated = 1;
            return;
        }
        $i++;
    }
}

#################### Main script ####################

# Undef variable to put file one variable.
undef $/;

$ua = LWP::UserAgent->new;
$ua->timeout(1000);
if (defined($proxy)) {
    $ua->proxy(["http", "ftp"], "$proxy");
}

$request = HTTP::Request->new(GET => "$snapshots_page");
$response = $ua->request($request);

%snapshots = ();
if ($response->is_success) {
    $data = $response->content;
    while ($data =~ /.*\[DIR\].*r(\d{6,8}).*(\d{2}\-\w+\-\d{4})/g) {
        $snapshots{$1} = $2;
    }
} else {
    die "Can't download snapshot page!";
}

#create directory for cache
if (!-e "$cache_dir") {
    mkdir ("$cache_dir", 0755) or die "Can't create folder $cache_dir, please check permissions.";
}

opendir (CACHE_DIR, $cache_dir) or die "Can't open cache dir: $cache_dir";
while (defined ($file = readdir(CACHE_DIR))) {
    if ($file =~ /([0-9]+)\.cache/ && !exists($snapshots{$1})) {
        $snapshots{$1} = "";
    }
}

$row_num = 1;
open (SNAPSHOTS_TABLE, "< $snapshots_table_template") or die "Can't open template file $snapshots_table_template.";
$table_template = <SNAPSHOTS_TABLE>;
close(SNAPSHOTS_TABLE);

foreach $snapshot (reverse(sort(keys %snapshots))) {
    if (($row_num % 2) > 0) {
        $row_type = "OddRowCell";
    } else {
        $row_type = "TableCell";
    }
    $row_num++;

    #This is the main loop for gathering testing results for each platform and 
    #each testsuite
    %testsuites_results = ();
    %testsuites_statuses = ();

    $cache_timestamp = 0;
    $data_updated = 0;

    #Check if snapshot binaries files were removed from snapshots page
    #in case if it is removed we don't add links to binaries in main table
    if ($snapshots{$snapshot}) {
        open (SNAPSHOTS_ROW, "< $snapshot_row_template") or die "Can't open template file $snapshot_row_template.";
    } else {
        open (SNAPSHOTS_ROW, "< $snapshot_exp_row_template") or die "Can't open template file $snapshot_row_template.";
    }
    $snapshots_row = <SNAPSHOTS_ROW>;
    close(SNAPSHOTS_ROW);

    $snapshot_cache_file = $cache_dir . "/" . $snapshot . "." . cache;
    open (CACHE_DATA, "< $snapshot_cache_file");
    $snapshot_cache_data = <CACHE_DATA>;
    close(CACHE_DATA);

    $cur_timestamp = time;

    undef $api_report;
    if ($snapshot_cache_data) {
        if (defined($verbose)) {
            print "Cache file loaded: $snapshot_cache_file\n";
        }
        #Parse cachefile timestamp
        if ($snapshot_cache_data =~ /TIMESTAMP\:\s*([0-9]*)/g) {
            $cache_timestamp = $1;
        }
        #Parse snapshot date
        if (($snapshot_cache_data =~ /SNAPSHOT_DATE\:\s*(.*?)\;/g) && $1) {
            $snapshots{$snapshot} = $1;
        }
        #Find API completeness report
        if (($snapshot_cache_data =~ /API_REPORT\:\s*(.*?)\;/g) && $1) {
            $api_report = $1;
        }
        #Parse cachefile data if cache is older than $cache_lag
        if ($cache_timestamp && (($cur_timestamp - $cache_timestamp) > $cache_lag)) {
            while ($snapshot_cache_data =~ /PLATFORM:(.*?);TESTSUITE:(.*?);STATUS:(.*?);TEXT:(.*?);/gsmi) {
                $testsuites_results{$1}{$2} = $4;
                $testsuites_statuses{$1}{$2} = $3;
            }
        }
    } else {
        if (defined($verbose)) {
            print "Cache file not found: $snapshot_cache_file\n";
        }
    }

    if (!$cache_timestamp) {
        $cache_timestamp = $cur_timestamp;
    }


    $snapshots_row =~ s/\{TABLE_CELL_TYPE\}/$row_type/g;
    $snapshots_row =~ s/\{SNAPSHOT_NUM\}/$snapshot/g;
    $snapshots_row =~ s/\{DATE\}/$snapshots{$snapshot}/g;
    $snapshots_row =~ s/\{RES_BASE_LINK\}/$gen_results_base_link/g;

    foreach $platform (keys %platforms) {
        $passed = 0;
        $failed = 0;

        foreach $testsuite (keys %testsuites) {
            get_result($platform, $testsuite);

            if ($testsuites_statuses{$platform}{$testsuite} > 0) {
                $passed++;
            } elsif ($testsuites_statuses{$platform}{$testsuite} < 0) {
                $failed++;
            }
        }
        $snapshots_row =~ s/\{$platform\_PASSED\}/$passed/g;
        $snapshots_row =~ s/\{$platform\_FAILED\}/$failed/g;
    }
    if (!defined($api_report)) {
        get_api_report($snapshot);
    }

    #We needd to update snapshot testing results table and cache file only if 
    #some new data was downloaded or if table or cachefile were removed
    if ($data_updated || 
            !-e "$test_results_output_path/r$snapshot/$test_results_output_file" ||
            !-e "$snapshot_cache_file") {
        #start generation of summary table of testing results for single snapshot
        #open table template
        open (TESTSUITES_TABLE, "< $testsuites_table_template") or die "Can't open template file $testsuites_table_template.";
        $testsuites_table = <TESTSUITES_TABLE>;
        close(TESTSUITES_TABLE);
    
        $noinfo_text = "N/A";
    
        #store tests data in table and cachefile
        $snapshot_cache_data =  "TIMESTAMP:$cache_timestamp\n";
        $snapshot_cache_data .= "SNAPSHOT_DATE:$snapshots{$snapshot};\n";
        $testsuites_table =~ s/\{SNAPSHOT_NUM\}/$snapshot/g;
        foreach $testsuite (sort({lc($testsuites{$a}) cmp lc($testsuites{$b}) } keys %testsuites)) {
            open (TESTSUITES_ROW, "< $testsuites_row_template") or die "Can't open template file $testsuites_row_template.";
            $testsuites_row = <TESTSUITES_ROW>;
            close(TESTSUITES_ROW);
            $testsuites_row =~ s/\{TESTSUITE_NAME\}/$testsuites{$testsuite}/g;
            $row_has_results = 0;
            foreach $platform (keys %platforms) {
                $status = $noinfo_text;
                if (exists($testsuites_results{$platform}{$testsuite})) {
                    $row_has_results = 1;
                    $status = $testsuites_results{$platform}{$testsuite};
                    $snapshot_cache_data .= "PLATFORM:$platform;TESTSUITE:$testsuite;STATUS:$testsuites_statuses{$platform}{$testsuite};TEXT:$status;\n";
                }
                $testsuites_row =~ s/\{$platform\}/$status/g;
            }
            if (!defined ($hide_no_results) || $row_has_results) {
                $testsuites_table =~ s/\{TABLE_ROW\}/$testsuites_row\n\{TABLE_ROW\}/g;
            }
        }
        if (defined($api_report)) {
            $testsuites_table =~ s/\{API_REPORT\}/<i><small><a href=\"$api_report\">API completeness report<a> (without endorsed packages)<small><i>/g;
            $snapshot_cache_data .= "API_REPORT:$api_report;\n";
        } else {
            $testsuites_table =~ s/\{API_REPORT\}//g;
        }
        $testsuites_table =~ s/\{TABLE_ROW\}//g;
        $time = gmtime;
        $testsuites_table =~ s/\{TIMESTAMP\}/$time/g;
        #end generation

        #store generated table and cache file
        #check if the directory exists and try to create it if not
        if (!-e "$test_results_output_path/r$snapshot") {
            mkdir ("$test_results_output_path/r$snapshot", 0755) or die "Can't create folder $test_results_output_path/$snapshot, please check permissions.";
        }
        #open file and store table
        open(TESTSUITES_RES, "> $test_results_output_path/r$snapshot/$test_results_output_file")
           or die "Can't open file $test_results_output_path/r$snapshot/$test_results_output_file to save tests results, please check path and permissions.";
        print TESTSUITES_RES $testsuites_table;
        close(TESTSUITES_RES);
        #end store generated table

        #open cache file and store data
        open (CACHE_DATA, "> $snapshot_cache_file")
           or die "Can't open file $snapshot_cache_file to save cache data, please check path and permissions.";
        print CACHE_DATA $snapshot_cache_data;
        close(CACHE_DATA);
        #end store cache data
    } else {
        if (defined($verbose)) {
            print "No new data downloaded for snapshot r$snapshot\n";
        }
    }

    #add row to snapshots summary table
    $table_template =~ s/\{TABLE_ROW\}/$snapshots_row\{TABLE_ROW\}/g;
}
$table_template =~ s/\{TABLE_ROW\}//g;

$time = gmtime;
$table_template =~ s/\{TIMESTAMP\}/$time/g;

#Get tests results
open(OUTPUT_FILE, "> $snapshots_output_file")  
    or die "Can't open file $snapshots_output_file to save output, please check path permissions.";
print OUTPUT_FILE $table_template;
close(OUTPUT_FILE);

