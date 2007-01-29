#!/usr/bin/perl -Ilib

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

use CGI;
use Chart::Base;
use Chart::Lines;
use Chart::Mountain;
use Chart::HorizontalBars;
use Date::Manip;
use Date::Parse;
use POSIX qw(strftime);
use strict;


my $q = new CGI;

#$ENV{'http_proxy'} = "write proxy name here, if required";
$ENV{'TZ'} = `date +%z`;
$ENV{'SCRIPT_NAME'} = "HarmonyBugs.cgi";
my $DAY = 24*60*60;
my $WEEK = 7*$DAY;
my $YEAR = 365*$DAY;
my $MONTH = $YEAR/12;
my @DAYS = (31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
my $PERIOD_PARAM = $q->param('period') || '6m';
my $INTERVAL_PARAM = $q->param('int') || '1w';
my $COMP_PARAM = $q->param('comp') || 'none';
my $METHOD_PARAM = $q->param('method') || 'offline';
my $CHECK_PARAM = $q->param('docheck') || 'no';
my $TYPE_PARAM = $q->param('type') || 0;
$TYPE_PARAM = 0 if ($TYPE_PARAM > 7 || $TYPE_PARAM < 0);
my $CUR_TIME = time;
my $CACHE_LIFE_TIME = 60*60; # 1 hour

my %periods = ('1m' => $MONTH,
               '3m' => $YEAR/4,
               '6m' => $YEAR/2,
               '1y' => $YEAR,
               '2y' => $YEAR*2,
               '3y' => $YEAR*3,
               '5y' => $YEAR*5
              );
my %period_names = ('1m' => '1 month',
                    '3m' => '3 months',
                    '6m' => '6 months',
                    '1y' => '1 year',
                    '2y' => '2 years',
                    '3y' => '3 years',
                    '5y' => '5 years'
                   );
my %intervals = ('1d' => $DAY,
                 '1w' => $WEEK,
                 '1m' => $MONTH
                );
my %interval_names = ('1d' => 'Day',
                      '1w' => 'Week',
                      '1m' => 'Month'
                     );

my $PERIOD = $periods{'6m'};
if ($periods{$PERIOD_PARAM}) {
    $PERIOD = $periods{$PERIOD_PARAM};
} else {
    $PERIOD_PARAM='6m';
}
my $INTERVAL = $intervals{'1w'};
if ($intervals{$INTERVAL_PARAM}) {
    $INTERVAL = $intervals{$INTERVAL_PARAM};
} else {
    $INTERVAL_PARAM='1w';
}

if ($PERIOD < $INTERVAL) {
    $PERIOD = $periods{'6m'};
    $PERIOD_PARAM='6m';
    $INTERVAL = $intervals{'1w'};
    $INTERVAL_PARAM='1w';
}

$PERIOD = $PERIOD - $PERIOD % $INTERVAL if ($INTERVAL_PARAM ne '1m');

my ($SEC_SH, $MIN_SH, $HOUR_SH, $MDAY_SH, $MON_SH, $YEAR_SH, $WDAY_SH, $YDAY_SH, $ISDST_SH)=gmtime($CUR_TIME);
my $TIME_SHIFT = $HOUR_SH*60*60 + $MIN_SH*60 + $SEC_SH;

$TIME_SHIFT = $TIME_SHIFT + $WDAY_SH*$DAY if ($INTERVAL_PARAM eq '1w');
$TIME_SHIFT = $TIME_SHIFT + ($MDAY_SH-1)*$DAY if ($INTERVAL_PARAM eq '1m');

my @components = ('Classlib', 'DRLVM', 'VM', 'Contributions', 'Build-test-ci');
my %types = (0 => 'Any', 1 => 'Bug', 2 => 'New Feature', 3 => 'Task', 4 => 'Improvement', 5 => 'Wish', 6 => 'Test', 7 => 'Sub-task');
my $component_id = 0;
my @priorities = ('Blocker', 'Critical', 'Major', 'Minor', 'Trivial');
if ($COMP_PARAM ne 'none') {
    my $ok = 'no';
    for (my $i = 0; $i <= $#components; $i++) {
        if ($COMP_PARAM eq $components[$i]) {
            $ok = 'ok';
        }
    }
    if ($ok eq 'no') {
        $COMP_PARAM = 'none';
    }
}

my $TEMP    = "./tmp";
my $URLS    = "./url.txt";
my $LOG     = "./log";
my $RESULTS = "./results";
my $OFFLINE = "./HarmonyBugs.out";
my $ONLINE  = "wget -i $URLS -o $LOG -O - |";
my $GET     = ($METHOD_PARAM eq 'online' || !(-f $OFFLINE)) ? $ONLINE : $OFFLINE;

#    Types
#        <type id="1">Bug</type>
#        <type id="2">New Feature</type>
#        <type id="3">Task</type>
#        <type id="4">Improvement</type>
#        <type id="5">Wish</type>
#        <type id="6">Test</type>
#        <type id="7">Sub-task</type>
#    Statuses
#        <status id="1">Open</status>
#        <status id="3">In Progress</status>
#        <status id="4">Reopened</status>
#        <status id="5">Resolved</status>
#        <status id="6">Closed</status>
#    Resolutions
#        <resolution id="-1">Unresolved</resolution>
#        <resolution id="1">Fixed</resolution>
#        <resolution id="2">Won&apos;t Fix</resolution>
#        <resolution id="3">Duplicate</resolution>
#        <resolution id="5">Cannot Reproduce</resolution>
#        <resolution id="6">Invalid</resolution>
#        <resolution id="7">Later</resolution>

my @TYPE;
my @CREATED;
my @STATUS;
my @PRIORITY;
my @RESOLUTION;
my @COMPONENT;
my @MODULE;
my @TITLE;
my @KEY;
my @PATCH;
my @RESOLVED;
my @ticks;
my %unresolved;

if ($INTERVAL_PARAM ne '1w') {
    # Recalculate period:
    my $sm = $MON_SH - 1;
    my $sy = $YEAR_SH;
    if ($sm < 0) {
        $sm = 11;
        $sy--;
    }
    my $months = int($periods{$PERIOD_PARAM} * 12 / $YEAR);
    $PERIOD = 0;
    while ($months-- > 0) {
        $PERIOD = $PERIOD + $DAYS[$sm] * $DAY;
        if ($sy % 4 == 0 && $sm == 1) {
            $PERIOD = $PERIOD + $DAY;
        }
        $sm--;
        if ($sm < 0) {
            $sm = 11;
            $sy--;
        }
    }
}

if ($INTERVAL_PARAM eq '1m') {
    #calculate ticks
    my $tm = $CUR_TIME - $TIME_SHIFT;
    my @ticks_backwards;
    push @ticks_backwards, $CUR_TIME;
    do {
        push @ticks_backwards, $tm--;
        my ($T_SEC, $T_MIN, $T_HOUR, $T_MDAY, $T_MON, $T_YEAR, $T_WDAY, $T_YDAY, $T_ISDST)=gmtime($tm);
        my $days_to_drop = $DAYS[$T_MON];
        if ($T_YEAR % 4 == 0 && $T_MON == 1) {
            $days_to_drop = 29;
        }
        $tm = $tm - $days_to_drop * $DAY + 1;
    } while ($tm >= $CUR_TIME - $PERIOD - $TIME_SHIFT);
    for (my $i = ($#ticks_backwards - 1); $i >= 0; $i--) {
        push @ticks, $ticks_backwards[$i];
        # unresolved array init
        if ($COMP_PARAM ne 'none') {
            for (my $j = 0; $j <= $#priorities; $j++) {
                push @{$unresolved{$priorities[$j]}}, 0;
            }
        } else {
            for (my $j = 0; $j <= $#components; $j++) {
                push @{$unresolved{$components[$j]}}, 0;
            }
        }
    }
} else {
    for (my $i = ($CUR_TIME - $PERIOD - $TIME_SHIFT); $i <= $CUR_TIME; $i += $INTERVAL) {
        push @ticks, $i;
        # unresolved array init
        if ($COMP_PARAM ne 'none') {
            for (my $j = 0; $j <= $#priorities; $j++) {
                push @{$unresolved{$priorities[$j]}}, 0;
            }
        } else {
            for (my $j = 0; $j <= $#components; $j++) {
                push @{$unresolved{$components[$j]}}, 0;
            }
        }
    }
    if ($ticks[$#ticks] != $CUR_TIME) {
        push @ticks, $CUR_TIME;
        if ($COMP_PARAM ne 'none') {
            for (my $j = 0; $j <= $#priorities; $j++) {
                push @{$unresolved{$priorities[$j]}}, 0;
            }
        } else {
            for (my $j = 0; $j <= $#components; $j++) {
                push @{$unresolved{$components[$j]}}, 0;
            }
        }
    }
}

#Remove outdated data from $TEMP directory and getting latest cache, if any
my $cache_tm = 0;
my $cache;
my @tmp = `ls -r $TEMP`;
foreach my $t (@tmp) {
    my $tm = 0;
    $tm = $1 if ($t =~ /[\_]*([0-9]*)[lmp]\.png/);
    $tm = $1 if ($t =~ /[\_]*([0-9]*)\.tmp/);
    if ($tm != 0) {
        if ($CUR_TIME - $tm > $CACHE_LIFE_TIME) {
            my $ignore = `rm $TEMP/$t`;
        } else {
            if ($t =~ /[\_0-9]*\.tmp/ && $tm > $cache_tm) {
                $cache_tm = $tm;
                $cache = $t;
            }
        }
    }
}

#For reading cached data, if exist
my $new_cache = "";
my $cf;
$GET = "$TEMP/$cache" if ($GET eq $ONLINE && $cache_tm > 0);

if ($GET eq $ONLINE && $cache_tm == 0) {
    $new_cache = tmp_name("\.tmp");
    open $cf, "> $new_cache";
}

#Reading data and writing cache, if needed
open GET, $GET;
while (<GET>) {
    my $string = $_;
    my $doWrite = 0;

    if ($string =~ /\s*<type id="(\d+)">[-\ A-Za-z0-9,:()]*<\/type>/) {
        push @TYPE, $1;
        $doWrite++;
    }

    if ($string =~ /\s*<created>([-\ A-Za-z0-9,:()]*)<\/created>/) {
        push @CREATED, str2time($1);
        $doWrite++;
    }

    if ($string =~ /\s*<status id="(\d+)">([-\ A-Za-z0-9,:()]*)<\/status>/) {
        push @STATUS, $2;
        $doWrite++;
    }

    if ($string =~ /\s*<priority id="(\d+)">([-\ A-Za-z0-9,:()&;]*)<\/priority>/) {
        push @PRIORITY, $priorities[$1 - 1];
        $doWrite++;
    }

    if ($string =~ /\s*<resolution id="(\d+)">([-\ A-Za-z0-9,:()&;]*)<\/resolution>/) {
        push @RESOLUTION, $2;
        $doWrite++;
    }

    if ($string =~ /\s*<resolution id="-1">([-\ A-Za-z0-9,:()]*)<\/resolution>/) {
        push @RESOLUTION, $1;
        $doWrite++;
    }

    if ($string =~ /\s*<resolution>([-\ A-Za-z0-9,:()]*)<\/resolution>/) {
        push @RESOLUTION, $1;
        $doWrite++;
    }

    if ($string =~ /\s*<created>([-\ A-Za-z0-9,:()]*)<\/created>/) {
        push @COMPONENT, $components[$component_id];
        push @MODULE, -1;
        push @PATCH, 0;
        push @RESOLVED, 0;
        $doWrite++;
    }

    if ($string =~ /\s*<customfieldvalue><!\[CDATA\[Patch Available\]\]><\/customfieldvalue>/) {
        $PATCH[$#PATCH] = 1;
        $doWrite++;
    }

    if ($string =~ /\s*<summary>(.+)<\/summary>/) {
        push @TITLE, $1;
        $doWrite++;
    }

    if ($string =~ /\s*<key id="(\d+)">(.+)<\/key>/) {
        push @KEY, $2;
        $doWrite++;
    }

    if ($string =~ /<\/rss>/) {
        $component_id++;
        $doWrite++;
    }

    print $cf $string if ($new_cache ne "" && $doWrite > 0);
}

close $cf if ($new_cache ne "");

# To be sure that we received consistent information
my $total = $#CREATED;
if ($total != $#STATUS || $total != $#RESOLUTION || $total != $#PRIORITY || $total != $#TITLE || $total != $#MODULE ||
    $total != $#KEY    || $total != $#RESOLVED   || $total != $#TYPE     || $total != $#PATCH) {
    die "error: array sizes inconsistency: $#CREATED $#STATUS $#RESOLUTION $#TITLE $#MODULE $#KEY $#RESOLVED $#TYPE $#PATCH, should be equal. ",
        "Possibly data was broken. Please try again. \n";
}

&check_issues_resolution_date();

exit if ($CHECK_PARAM eq 'yes');

my %patch_available;

if ($COMP_PARAM eq 'none') {
    for (my $i = 0; $i <= $#components; $i++) {
        $patch_available{$components[$i]} = 0;
    }
} else {
    for (my $i = 0; $i <= $#priorities; $i++) {
        $patch_available{$priorities[$i]} = 0;
    }
}

for (my $i = 0; $i <= $total; $i++) {
    if ($TYPE_PARAM == 0 || $TYPE[$i] == $TYPE_PARAM) {
        my $j = $#ticks;
        if ($RESOLUTION[$i] ne 'Unresolved') {
            while ($ticks[$j] > $RESOLVED[$i] && $j >= 0) {
                $j--;
            }
        }

        while ($ticks[$j] > $CREATED[$i] && $j >= 0) {
            if ($COMP_PARAM ne 'none') {
                $unresolved{$PRIORITY[$i]}->[$j]++ if ($COMP_PARAM eq $COMPONENT[$i]);
            } else {
                $unresolved{$COMPONENT[$i]}->[$j]++;
            }
            $j--;
        }

        if ($RESOLUTION[$i] eq 'Unresolved') {
            if ($COMP_PARAM ne 'none') {
                $patch_available{$PRIORITY[$i]}++ if ($PATCH[$i] > 0 && $COMP_PARAM eq $COMPONENT[$i]);
            } else {
                $patch_available{$COMPONENT[$i]}++ if ($PATCH[$i] > 0);
            }
        }
    }
}

my @WRONG_TITLE;
my @MODULES;

if ($COMP_PARAM ne 'none') {
    for (my $i = 0; $i <= $total; $i++) {
        if ($COMP_PARAM eq $COMPONENT[$i] && $RESOLUTION[$i] eq 'Unresolved' && ($TYPE_PARAM == 0 || $TYPE[$i] == $TYPE_PARAM)) {
            my $t = $TITLE[$i];
            if ($t =~ /\s*\[(.+?)\]\s*\[(.+?)\].*/) {
                my $cmp = $1;
                my $mod = $2;
                if ($cmp =~ /\s*(?i)$COMP_PARAM\s*/) {
                    my $idx = -1;
                    for (my $j = 0; $j <= $#MODULES; $j++) {
                        my $m = $MODULES[$j];
                        if ($mod =~ /\s*(?i)$m\s*/) {
                            $idx = $j;
                        }
                    }
                    if ($idx < 0) {
                        push @MODULES, $mod;
                        $MODULE[$i] = $#MODULES;
                    } else {
                        $MODULE[$i] = $idx;
                    }
                } else {
                    push @WRONG_TITLE, $i;
                    $MODULE[$i] = -1;
                }
            } else {
                push @WRONG_TITLE, $i;
                $MODULE[$i] = -1;
            }
        }
    }
}

&generate_html_with_graph();

sub generate_html_with_graph () {
    # Generate HTML with Graph
    my $title = "Harmony unresolved issues history";
    my $description = "This tables and graphs show how many issues filed against Harmony\n".
        "  components were remaining in the unresolved status during specified time frames.\n".
        "<br></br>\n  The original statistics can be found on\n".
             "  <a href=\"http://issues.apache.org/jira/secure/IssueNavigator.jspa\">\n".
        "    Apache Harmony Issue Tracker.\n  </a>";

    print $q->header ('text/html; charset=koi8');
    print $q->start_html (-title => $title);


    print "<center>\n  <h2>\n    $title\n  </h2>\n";
    print "  <h3>\n    $COMP_PARAM component\n  </h3>\n" if ($COMP_PARAM ne 'none');
    print "</center>\n<p>\n  $description\n</p>\n";

    print "<p>\nPeriod:\n";
    foreach my $i (sort {$a<=>$b} values (%periods)) {
        foreach my $j (keys %periods) {
            if ($periods{$j} == $i) {
                if ($j ne $PERIOD_PARAM) {
                    print "<a href=\"$ENV{SCRIPT_NAME}?period=$j&int=$INTERVAL_PARAM&comp=$COMP_PARAM&method=$METHOD_PARAM&type=$TYPE_PARAM\">",
                          $period_names{$j}, "</a>&nbsp;\n";
                } else {
                    print $period_names{$j}, "&nbsp;\n";
                }
            }
        }
    }
    print "</p>\n";

    print "<p>\nInterval:\n";
    foreach my $i (sort {$a<=>$b} values (%intervals)) {
        foreach my $j (keys %intervals) {
            if ($intervals{$j} == $i) {
                if ($j ne $INTERVAL_PARAM) {
                    print "<a href=\"$ENV{SCRIPT_NAME}?period=$PERIOD_PARAM&int=$j&comp=$COMP_PARAM&method=$METHOD_PARAM&type=$TYPE_PARAM\">",
                          $interval_names{$j}, "</a>&nbsp;\n";
                } else {
                    print $interval_names{$j}, "&nbsp;\n";
                }
            }
        }
    }
    print "</p>\n";

    print "<p>\nBy component:\n";
    for (my $i = 0; $i <= $#components; $i++) {
        if ($components[$i] ne $COMP_PARAM) {
            print "<a href=\"$ENV{SCRIPT_NAME}?period=$PERIOD_PARAM&int=$INTERVAL_PARAM&comp=$components[$i]&method=$METHOD_PARAM&type=$TYPE_PARAM\">",
                  $components[$i], "</a>&nbsp;\n";
        } else {
            print $components[$i], "&nbsp;\n";
        }
    }
    print "</p>\n";

    print "<p>\nBy issue type:\n";
    foreach my $i (sort keys %types) {
        if ($i != $TYPE_PARAM) {
            print "<a href=\"$ENV{SCRIPT_NAME}?period=$PERIOD_PARAM&int=$INTERVAL_PARAM&comp=$COMP_PARAM&method=$METHOD_PARAM&type=$i\">",
                  $types{$i}, "</a>&nbsp;\n";
        } else {
            print $types{$i}, "&nbsp;\n";
        }
    }
    print "</p>\n";

    print "<center>\n<table border=\"1\">\n  <tr>\n    <th>$interval_names{$INTERVAL_PARAM} \\";

    if ($COMP_PARAM ne 'none') {
        print " Priority</th>\n";
        for (my $j = 0; $j <= $#priorities; $j++) {
            print "    <th width=100>", $priorities[$j], "</th>\n";
        }
    } else {
        print " Component</th>\n";
        for (my $j = 0; $j <= $#components; $j++) {
            print "    <th width=100>", $components[$j], "</th>\n";
        }
    }

    print "  </tr>\n";

    for (my $i = 0; $i <= $#ticks; $i++) {

        my $tick;
        $tick = strftime "%b'%y", gmtime($ticks[$i]) if ($INTERVAL_PARAM eq '1m');
        $tick = strftime "%e, %b'%y", gmtime($ticks[$i] + $DAY) if ($INTERVAL_PARAM eq '1w');
        $tick = strftime "%e, %b'%y", gmtime($ticks[$i]) if ($INTERVAL_PARAM eq '1d');
        $tick = "Now (patch available)" if ($ticks[$i] == $CUR_TIME);
        print "  <tr>\n    <th align=\"right\">", $tick, "</th>\n";

        if ($COMP_PARAM ne 'none') {
            for (my $j = 0; $j <= $#priorities; $j++) {
                if ($i == $#ticks) {
                    print "    <td align=\"right\">", $unresolved{$priorities[$j]}->[$i], "($patch_available{$priorities[$j]})</td>\n";
                } else {
                    print "    <td align=\"right\">", $unresolved{$priorities[$j]}->[$i], "</td>\n";
                }
            }
        } else {
            for (my $j = 0; $j <= $#components; $j++) {
                if ($i == $#ticks) {
                    print "    <td align=\"right\">", $unresolved{$components[$j]}->[$i], "($patch_available{$components[$j]})</td>\n";
                } else {
                    print "    <td align=\"right\">", $unresolved{$components[$j]}->[$i], "</td>\n";
                }
            }
        }
        print "  </tr>\n";
    }
    print "</table>\n";

    my @pics = draw_graph();

    for my $p(@pics){
        print "<img src=\"$p\"/>\n";
    }
    print "</center>\n";

    if ($COMP_PARAM ne 'none' && $COMP_PARAM ne 'Contributions' && $COMP_PARAM ne 'Build-test-ci' && $#WRONG_TITLE > 0) {
        print "<h4>Issues, summary of which does not conform to \"[component][module] summary...\" format:</h4>\n";
        print "<p>\n";
        for (my $i = 0; $i <= $#WRONG_TITLE; $i++) {
            my $key = $KEY[$WRONG_TITLE[$i]];
            print "<a href=\"http://issues.apache.org/jira/browse/$key\">$key</a>: $TITLE[$WRONG_TITLE[$i]]<br>\n";
        }
        print "</p>\n";
    }

    my $last_updated = "";
    if (-f $OFFLINE) {
        $last_updated = `ls -l --time-style="+%m-%d-%y %H:%M" $OFFLINE | awk '{print \$6 " " \$7}'`;
    } else {
        $last_updated = "never";
    }
    print "<p>\n";
    if ($GET eq $OFFLINE) {
        print "This version uses cached data that were last updated ", $last_updated, ".\n";
        print "<a href=\"$ENV{SCRIPT_NAME}?period=$PERIOD_PARAM&int=$INTERVAL_PARAM&comp=$COMP_PARAM&method=online&type=$TYPE_PARAM\">";
        print "Refresh</a>\n(may take 2-5 minutes depending on you internet connection type).\n";
    } else {
        print "This version uses actual data from Apache Harmony Issue Tracker.\n";
        print "<a href=\"$ENV{SCRIPT_NAME}?period=$PERIOD_PARAM&int=$INTERVAL_PARAM&comp=$COMP_PARAM&method=offline&type=$TYPE_PARAM\">";
        print "Return</a>\nto the cached version (last updated: $last_updated).\n";
    }
    print "</p>\n";

    # Generate weekly report
    if ($COMP_PARAM eq 'none') {
        my @submitted;
        my @fixed;
        my @totalu;
        my @unfixed;
        my @ufx_title = ('less than 1 week',
                         '1 week',
                         '2 weeks',
                         '3 weeks',
                         '4 weeks',
                         '5 weeks',
                         '6 weeks or longer'
        );
        my $tick = $CUR_TIME - ($WDAY_SH*$DAY + $HOUR_SH*60*60 + $MIN_SH*60 + $SEC_SH);
        for (my $i = 0; $i <= $#components; $i++) {
            my $sub = 0;
            my $fix = 0;
            my $ttu = 0;
            my @ufx = (0, 0, 0, 0, 0, 0, 0);
            for (my $j = 0; $j <= $total; $j++) {
                if ($COMPONENT[$j] eq $components[$i] && ($TYPE_PARAM == 0 || $TYPE[$j] == $TYPE_PARAM)) {
                    $sub++ if ($CREATED[$j] >= $tick - $WEEK && $CREATED[$j] < $tick);
                    $fix++ if ($RESOLVED[$j] >= $tick - $WEEK && $RESOLVED[$j] < $tick && $RESOLVED[$j] > 0);
                    $ttu++ if (($RESOLUTION[$j] eq 'Unresolved' || $RESOLVED[$j] >= $tick) && $CREATED[$j] < $tick);

                    my $tk = $tick;
                    for (my $t = 0; $t < 6; $t++) {
                        $ufx[$t]++ if (($RESOLUTION[$j] eq 'Unresolved' || $RESOLVED[$j] >= $tick) && $CREATED[$j] >= $tk - $WEEK && $CREATED[$j] < $tk);
                        $tk -= $WEEK;
                    }
                    $ufx[6]++ if (($RESOLUTION[$j] eq 'Unresolved' || $RESOLVED[$j] >= $tick) && $CREATED[$j] < $tk);
                }
            }
            push @submitted, $sub;
            push @fixed, $fix;
            push @totalu, $ttu;
            push @unfixed, \ @ufx;
        }
        my $tick1 = strftime "%B %e", gmtime($tick - $WEEK + $DAY);
        my $tick2 = strftime "%B %e, %Y", gmtime($tick);
        print "<table border=\"1\">\n  <tr>\n    <th colspan=", $#components+3, ">Weekly status ($tick1 - $tick2)</th>\n  </tr>\n  <tr>\n    <th>&nbsp;</th>\n";
        for (my $i = 0; $i <= $#components; $i++) {
            print "    <th>$components[$i]</th>\n";
        }
        print "    <th>Total</th>\n  </tr>\n";

        my $count = 0;
        print "  <tr>\n    <td>Issues submitted last week</td>\n";
        for (my $i = 0; $i <= $#components; $i++) {
            print "    <td>$submitted[$i]</td>\n";
            $count += $submitted[$i];
        }
        print "    <td>$count</td>\n  </tr>\n";

        $count = 0;
        print "  <tr>\n    <td>Issues resolved last week</td>\n";
        for (my $i = 0; $i <= $#components; $i++) {
            print "    <td>$fixed[$i]</td>\n";
            $count += $fixed[$i];
        }
        print "    <td>$count</td>\n  </tr>\n";

        $count = 0;
        print "  <tr>\n    <td>Total number of unresolved issues</td>\n";
        for (my $i = 0; $i <= $#components; $i++) {
            print "    <td>$totalu[$i]</td>\n";
            $count += $totalu[$i];
        }
        print "    <td>$count</td>\n  </tr>\n";

        print "  <tr>\n    <th colspan=", $#components+3, ">How long issues stay unresolved</th>\n  </tr>\n";

        for (my $j = 0; $j <= 6; $j++) {
            $count = 0;
            print "  <tr>\n    <td>$ufx_title[$j]</td>\n";
            for (my $i = 0; $i <= $#components; $i++) {
                print "    <td>${$unfixed[$i]}[$j]</td>\n";
                $count += ${$unfixed[$i]}[$j];
            }
            print "    <td>$count</td>\n  </tr>\n";
        }

        print "</table>\n";
    }

    print "<ADDRESS>\n  Any questions and suggestions please address to\n";
    print "  <a href=\"mailto:dev\@harmony.apache.org\">dev\@harmony.apache.org</a>.\n";
    print "  Thanks!\n";
    print "</ADDRESS>\n";
    print $q->end_html;
}

sub draw_graph () {
    my $skip = 0;
    my $direction = 'normal';
    my $max_ticks = 26;

    if ($INTERVAL_PARAM eq '1m') {
        if (10 < $#ticks && $#ticks <= 20) {
            $direction = 'staggered';
        }
        if (20 < $#ticks && $#ticks <= $max_ticks) {
            $direction = 'vertical';
        }
        if ($max_ticks < $#ticks) {
            $skip = int($#ticks/$max_ticks);
            $direction = 'vertical';
        }
    } else {
        if (6 < $#ticks && $#ticks <= 12) {
            $direction = 'staggered';
        }
        if (12 < $#ticks && $#ticks <= $max_ticks) {
            $direction = 'vertical';
        }
        if ($max_ticks < $#ticks) {
            $skip = int($#ticks/$max_ticks);
            $direction = 'vertical';
        }
    }

    my %grid_color=('grid_lines' => [220,220,220]);
    my %graph_settings = ('y_axes' => 'both',
                          'brush_size' => 4,
                          'legend_example_size' => 20,
                          'grid_lines' => 'true',
                          'legend_labels' => ($COMP_PARAM eq 'none') ? \@components : \@priorities,
                          'y_label' => 'number of unresolved issues',
                          'x_label' => $interval_names{$INTERVAL_PARAM},
                          'legend' => 'bottom',
                          'precision' => 0,
                          'skip_x_ticks' => $skip,
                          'grey_background' => 'false',
                          'colors' => \%grid_color,
                          'x_ticks' => $direction,
                         );

    my $lines=Chart::Lines->new(660,330);
    my $monts=Chart::Mountain->new(660,330);

    my @dates;
    for my $d (@ticks) {
        my $tick;
        $tick = strftime "%b'%y", gmtime($d) if ($INTERVAL_PARAM eq '1m');
        $tick = strftime "%e, %b'%y", gmtime($d + $DAY) if ($INTERVAL_PARAM eq '1w');
        $tick = strftime "%e, %b'%y", gmtime($d) if ($INTERVAL_PARAM eq '1d');
        $tick = "Now" if ($d == $CUR_TIME);
        push @dates, $tick;
    }

    $lines->add_dataset(@dates);
    $monts->add_dataset(@dates);

    if ($COMP_PARAM ne 'none') {
        for my $priority(@priorities){
            $lines->add_dataset( @{$unresolved{$priority}} );
            $monts->add_dataset( @{$unresolved{$priority}} );
        }
    } else {
        for my $comp(@components){
            $lines->add_dataset( @{$unresolved{$comp}} );
            $monts->add_dataset( @{$unresolved{$comp}} );
        }
    }

    $lines->set(%graph_settings);
    $monts->set(%graph_settings);

    my @toRet;
    my $fd;

    my $gname=tmp_name("l.png");
    open $fd,"> $gname";
    $lines->png($fd);
    close $fd;
    push @toRet,$gname;

    $gname=tmp_name("m.png");
    open $fd,"> $gname";
    $monts->png($fd);
    close $fd;
    push @toRet,$gname;

    if ($COMP_PARAM ne 'none' && $COMP_PARAM ne 'Contributions' && $COMP_PARAM ne 'Build-test-ci') {
        push @MODULES, 'unspecified module';

        my $bar=Chart::HorizontalBars->new(660, 120 + 20*$#MODULES);

        my @mu;
        my @mu_names;
        for (my $i = 0; $i <= $#MODULES; $i++) {
            push @mu, 0;
            push @mu_names, $MODULES[$i];
        }

        for (my $i = 0; $i <= $total; $i++) {
            if ($COMP_PARAM eq $COMPONENT[$i] && $RESOLUTION[$i] eq 'Unresolved' && ($TYPE_PARAM == 0 || $TYPE[$i] == $TYPE_PARAM)) {
                if ($MODULE[$i] < 0) {
                    $mu[$#MODULES]++;
                } else {
                    $mu[$MODULE[$i]]++;
                }
            }
        }

        # sorting numbers of modules issues in ascending order
        for (my $i = 0; $i < $#mu - 1; $i++) {
            for (my $j = $i+1; $j < $#mu; $j++) {
                if ($mu[$i] > $mu[$j]) {
                    my $ex1 = $mu[$i];
                    my $ex2 = $mu_names[$i];
                    $mu[$i] = $mu[$j];
                    $mu_names[$i] = $mu_names[$j];
                    $mu[$j] = $ex1;
                    $mu_names[$j] = $ex2;
                }
            }
        }

        my $tot_bugs = 0;
        for (my $i = 0; $i <= $#mu; $i++) {
            $tot_bugs = $tot_bugs + $mu[$i];
        }

        if ($tot_bugs <= 0) {
            return @toRet;
        }

        for (my $i = 0; $i <= $#mu; $i++) {
            my $p = int($mu[$i]*10000/$tot_bugs) / 100;
            $mu_names[$i] = "$mu_names[$i] - $p\% ($mu[$i])";
        }

        $bar->add_dataset(@mu_names);
        $bar->add_dataset(@mu);

        my %bar_settings = ('title' => 'Unresolved issues by module',
                            'grid_lines' => 'true',
                            'x_label' => 'number of issues',
                            'y_label' => 'module',
                            'include_zero' => 'true',
                            'spaced_bars' => 'true',
                            'legend' => 'none',
                            'y_axes' => 'left',
                            'grey_background' => 'false',
                           );

        $bar->set(%bar_settings);

        $gname=tmp_name("p.png");
        open $fd,"> $gname";
        $bar->png($fd);
        close $fd;
        push @toRet,$gname;
    }

    return @toRet;
}

sub tmp_name{
    my $ext = @_[0];
    my $toRet = "$TEMP/$CUR_TIME$ext";
    while (-f $toRet) {
        $toRet = "_$toRet";
    }

    return $toRet;
}

sub check_issues_resolution_date {
    my $ha = (-f "resolution.out") ? 1 : 0;
    my $res;
    my %res_hash;
    my $changed = 0;
    if ($ha > 0) {
        open GET, "resolution.out";
        while (<GET>) {
            my $s = $_;
            if ($s =~ /<bug>(.+)<\/bug><resolved>(\d+)<\/resolved>/) {
                $res_hash{$1} = $2;
            }
        }
    }
    if ($CHECK_PARAM eq 'yes') {
        print "Checking issues   0.0%";
    }
    for (my $i = 0; $i <= $total; $i++) {
        if ($RESOLUTION[$i] ne 'Unresolved' && ($TYPE_PARAM == 0 || $TYPE[$i] == $TYPE_PARAM)) {
            my $rd = $res_hash{$KEY[$i]};
            if ($rd > 0) {
                $RESOLVED[$i] = $rd;
            } else {
                if ($GET eq $ONLINE || $CHECK_PARAM eq 'yes') {
                    if ($CHECK_PARAM eq 'yes') {
                        my $prc = int($i * 1000 / $total) / 10;
                        my $str = "$prc";
                        while (length($str) < 6) {
                            $str = " $str";
                        }
                        print "\b\b\b\b\b\b$str%";
                    }
                    my $GET_HISTORY = "wget -q \"http://issues.apache.org/jira/browse/$KEY[$i]?page=com.atlassian.jira.plugin.system.issuetabpanels:changehistory-tabpanel\" -O - |";
                    open GET, $GET_HISTORY;
                    my $tm = 0;
                    while (<GET>) {
                        my $s = $_;
                        if ($s =~ /\s* Change by <a href=.*\[<font.*>(.*)<\/font>].*/) {
                            my $stp = "$1 -0800 (PST)";
                            $rd = str2time($stp);
                        }
                        if ($s =~ /\s* Resolution/) {
                            last;
                        }
                    }
                    $res_hash{$KEY[$i]} = $rd;
                    $RESOLVED[$i] = $rd;
                    $changed = 1;
                } else {
                    $RESOLVED[$i] = 0;
                }
            }
        }
    }
    if ($CHECK_PARAM eq 'yes') {
        print "\b\b\b\b\b\b100.0%\n";
    }

    if (($ha == 0 && $GET eq $ONLINE) || $changed > 0) {
        open $res, "> resolution.out";
        foreach my $i (keys %res_hash) {
            print $res "<bug>$i</bug><resolved>$res_hash{$i}</resolved>\n";
        }
        close $res;
    }
}
