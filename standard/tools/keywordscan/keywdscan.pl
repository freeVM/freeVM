#!/usr/bin/perl
use strict;
#use POSIX;
use POSIX qw/strftime/;
use File::Find;
#use Time::Local;
use Cwd;
use File::Spec::Functions;

=head1 NAME

keywdscan.pl - Perl script for keyword scanning

=head1 SYNOPSIS

  #Scan using the file "config_file"
  keywdscan.pl config_file
  
=head1 DESCRIPTION

Takes one command line parameter - the name of the configuration file. See C<KEYWD_CONFIG> for a sample. If no
file name is supplied it will look for a file called C<KEYWD_CONFIG> in the same directory as it's being run from.

The list of words to be scanned for are read from C< KeywordInputFileName>.

Scans source code files in directories under C<ScanRootDirectory> looking for occurrences of keywords.
Examined file types (C<FileTypes>) are specified by suffix - eg java, c, h, cpp....


Output is written to C<ReportFileName> as a CSV file in the form:

File name
   Line number, line, matched word
   Line number, line, matched word
   ....
   
Note that in "line" any commas are changed to : so as to preserve the format of the CSV file.

Case sensitive searching is optional.

=head1 FUNCTIONS

=cut

#
my $config_file = shift;

if ( !defined $config_file ) {
	my $config_path = getcwd();
	print "Looking for config file in local directory $config_path\n";
	$config_file = catfile( $config_path, "KEYWD_CONFIG" );
}

if ( !-e "$config_file" ) {
	die "Config file ($config_file)does not exist.\n";
}

#
#
# Read the config file
#
#
print "Using Config file $config_file \n";
open( CONF, "$config_file" ) || die "Can't open KEYWD_CONFIG file $!\n";

my $keywd_infile;
my $keywd_outfile;
my $scan_root;
my $case_ins = 0;
my @filetypes;

while (<CONF>) {
	if (/^KeywordInputFileName:(.*)/) { $keywd_infile  = $1; }
	if (/^ReportFileName:(.*)/)       { $keywd_outfile = $1; }
	if (/^ScanRootDirectory:(.*)/)    { $scan_root     = $1; }
	if (/^CaseInsensitive/)           { $case_ins      = 1; }
	if (/^FileTypes:(.*)/)            { @filetypes     = split /,/, $1; }
}

#
# Get date
#
#my ( $se, $mi, $ho, $md, $mo, $ye, $wd, $yd, $is ) = localtime;
my $date_str = strftime("%Y-%m-%d at %H:%M", localtime);

#
#my $month    = $mo + 1;
#my $year     = $ye + 1900;
#my $date_str = $year . "-" . $month . "-" . $md . " at " . $ho . ":" . $mi;

#
print "Starting scan at root directory: $scan_root\n";
print "Reading from: $keywd_infile\n";
print "Writing to: $keywd_outfile\n";

#
open( OUT, "> $keywd_outfile" ) || die "Can't open ReportFile  $!\n";
print OUT "Scan date $date_str\n";
if ($case_ins) { print OUT"Case insensitive search\n"; }
else { print OUT "Case sensitive search\n"; }
my $ft = join ";", @filetypes;
print OUT "Looking for file types $ft \n\n";

#
# Create lookup table of filetypes
#
my %sourcetypes = map { $_ => 1 } @filetypes;

#
# Read Keyword file
#
my @words;
my @matchwords;

open( KWD, "$keywd_infile" ) || die "Can't open KeywordInputFile  $!\n";
while (<KWD>) {
	next if (/^#/);
	my $word = rem_trail_whites($_);

	#
	push( @words, $word );

# Deal with ( . in the keyword file by back-slashing. Could deal with * here too?
	$word = quotemeta($word);
	push( @matchwords, $word );
}

#
# Get a list of source files
#
my %matchcount;
my @sourcefiles;
find( \&source, $scan_root );

#
# Check each file for keyword matches
#
my $totalmatch = 0;
foreach my $file (@sourcefiles) {
	print OUT "$file\n";
	print OUT ",Line number, Line, Match\n";
	$matchcount{$file} = 0;
	match($file);
	print OUT "Total matches found in $file = $matchcount{$file}\n\n";
	$totalmatch += $matchcount{$file};
}
print OUT "Total keywords matched is $totalmatch\n";
close OUT;
close KWD;
close CONF;

=head2 C<match (file_name)>

Scans a file for a keywords

=cut

sub match {
	my ($f) = @_;
	my $line_no = 0;
	open( SRC, "$f" ) || die "Can't open file $!\n";
	while (<SRC>) {
		$line_no++;
		my $line = rem_trail_whites($_);

		#Changs any commas to : so as not to interfere with the CSV file.
		$line =~ s/,/:/g;
		if ($case_ins) {
			for ( my $i = 0 ; $i <= $#words ; $i++ ) {
				if (/$matchwords[$i]/i) {
					$matchcount{$f}++;
					print OUT ",$line_no, $line, $words[$i]\n";
				}
			}
		}
		else {
			for ( my $i = 0 ; $i <= $#words ; $i++ ) {
				if (/$matchwords[$i]/) {
					$matchcount{$f}++;
					print OUT ",$line_no, $line, $words[$i]\n";
				}
			}
		}
	}
	close SRC;
	return;
}

=head2 C<source>

Required by File::Find - looks for files ending in .xxx

=cut

sub source {
	return unless ( $_ =~ /\.([^.]+)$/ );
	my $ext = $1;
	if ( exists $sourcetypes{$ext} ) {
		my $name = catfile( $File::Find::dir, $_ );
		push @sourcefiles, $name;
	}
	return;
}

=head2 C<rem_trail_whites>

Removes and trailing whitespace character. For some reason chomp has trouble with dos files.

=cut

sub rem_trail_whites {
	my ($thing) = @_;
	$thing =~ s/[\s\r\n]+$//;
	return $thing;
}

=head1 AUTHOR

Zoe Slattey, E<lt>zoe@uk.ibm.comE<gt>

=head1 COPYRIGHT AND LICENSE

/* Copyright 2004 The Apache Software Foundation or its licensors, as applicable
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
   
=cut
