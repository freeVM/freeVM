#!/bin/sh
#
#!
# @file ./roster.sh
#
# @brief Boot JVM configuration file roster generator.
#
# Shell script to enumerate the files in the project for use by
# configuration script @link ./config.sh ./config.sh@endlink and
# for when files are added or deleted.  This information is used
# by all every @link ./Makefile Makefile@endlink and by the
# document compiler @c @b doxygen .
#
# Remember that if you add a Java source file, a C source or header
# file, or a shell script, or @e any other type of file that needs
# to either be compiled or incorporated into the documentation, you
# should run this script again so that the @b config directory roster
# locates the update.  This way, 'doxygen' will automatically
# incorporate it into the documentation suite.
#
# @todo  HARMONY-6-roster.sh-1 A Windows .BAT version of this
#        script needs to be written
#
#
# @section Control
#
# \$URL: https://svn.apache.org/repos/asf/incubator/harmony/enhanced/trunk/sandbox/contribs/bootjvm/bootJVM/roster.sh $
#
# \$Id: roster.sh 327202 2005-10-21 14:50:11Z dlydick $
#
# Copyright 2005 The Apache Software Foundation
# or its licensors, as applicable.
#
# Licensed under the Apache License, Version 2.0 ("the License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
# either express or implied.
#
# See the License for the specific language governing permissions
# and limitations under the License.
#
# @version \$LastChangedRevision: 327202 $
#
# @date \$LastChangedDate: 2005-10-21 09:50:11 -0500 (Fri, 21 Oct 2005) $
#
# @author \$LastChangedBy: dlydick $
#
# @section Reference
#
#/ /* 
# (Use  #! and #/ with dox-filter.sh to fool Doxygen into
# parsing this non-source text file for the documentation set.
# Use the above open comment to force termination of parsing
# since it is not a Doxygen-style 'C' comment.)
#
#
###################################################################
#
# Script setup
#

# `dirname $0` for shells without that utility
PGMDIR=`expr "${0:-.}/" : '\(/\)/*[^/]*//*$'  \| \
             "${0:-.}/" : '\(.*[^/]\)//*[^/][^/]*//*$' \| .`
PGMDIR=`cd $PGMDIR; pwd`

# `basename $0` for shells without that utility
PGMNAME=`expr "/${0:-.}" : '\(.*[^/]\)/*$' : '.*/\(..*\)' \|\
              "/${0:-.}" : '\(.*[^/]\)/*$' : '.*/\(..*\)' \| \
              "/${0:-.}" : '.*/\(..*\)'`

THISDATE=`date`

if test ! -d config
then
    echo "$PGMNAME:  Directory '$PGMDIR/config' not found."
    echo "$PGMNAME:  Please first run '$PGMDIR/config.sh'."
    exit 1
fi

###################################################################
#
# Create project roster files
#
USEDOX="for 'dox.sh' and 'gmake dox'"
USEDOXBLD="for 'dox.sh' and 'gmake all' and 'gmake clean'"

CRCD="config/config_roster_c.dox"
CRCM="config/config_roster_c.mak"
rm -f $CRCD $CRCM
(
    echo "#"
    echo "# Roster of source files"
    echo "# $USEDOXBLD"
    echo "#"
    echo "# for directory jvm/src"
    echo "#"
    echo "# Auto-generated by $PGMNAME"
    echo "# on $THISDATE:"
    echo "# DO NOT MODIFY!"
    echo "#"
    echo "INPUT=\\"
    for f in jvm/src/*.c
    do
        echo "    $f \\"
    done
    echo ""  # Make SURE to have a blank line after final '\' character
    echo "# EOF"

) | tee $CRCD | \
    grep -v "jvm/src/jvmmain.c" | \
    sed 's/^INPUT=/C_SOURCES:=/;s,jvm/src/,,' > $CRCM

chmod -w $CRCD $CRCM

CRHD="config/config_roster_h.dox"
CRHM="config/config_roster_h.mak"
rm -f $CRHD $CRHM
(
    echo "#"
    echo "# Roster of header files $USEDOX"
    echo "#"
    echo "# Auto-generated by $PGMNAME"
    echo "# on $THISDATE:"
    echo "# DO NOT MODIFY!"
    echo "#"
    echo "INPUT+=\\"
    echo "    config/config.h \\"
    for f in main/src/jvmmain.c jvm/src/*.h
    do
        echo "    $f \\"
    done

    for f in jvm/include/*.h
    do
        echo "    $f \\"
    done
    echo ""
    echo "# EOF"

) | tee $CRHD | sed 's/^INPUT+=/C_HEADERS:=/;s,jvm/src/,,'       | \
                grep -v main/src/jvmmain.c                       | \
                sed 's,    jvm/src/,    ,'         | \
                sed 's,    jvm/include/,    ../include/,' | \
                sed 's,config/config.h,../../config/config.h,' > $CRHM

chmod -w $CRHD $CRHM

CJCD="config/config_roster_jni_c.dox"
CJCM="config/config_roster_jni_c.mak"
rm -f $CJCD $CJCM
(
    echo "#"
    echo "# Roster of JNI sample C source files"
    echo "# $USEDOXBLD"
    echo "#"
    echo "# for directory jni/src/harmony/generic/0.0/src"
    echo "#"
    echo "# Auto-generated by $PGMNAME"
    echo "# on $THISDATE:"
    echo "# DO NOT MODIFY!"
    echo "#"
) | tee $CJCD > $CJCM

    echo "INPUT+=\\"     >> $CJCD
    echo "C_SOURCES:=\\" >> $CJCM

(
    for f in jni/src/harmony/generic/0.0/src/*.c
    do
        echo "    $f \\"
    done
    echo ""
    echo "# EOF"

) | tee -a $CJCD | sed 's,jni/src/harmony/generic/0.0/src/,,' >> $CJCM

chmod -w $CJCD $CJCM

CJHD="config/config_roster_jni_h.dox"
CJHM="config/config_roster_jni_h.mak"
rm -f $CJHD $CJHM
(
    echo "#"
    echo "# Roster of JNI sample C header files $USEDOX"
    echo "#"
    echo "# Auto-generated by $PGMNAME"
    echo "# on $THISDATE:"
    echo "# DO NOT MODIFY!"
    echo "#"
    echo "INPUT+=\\"
    for f in jni/src/harmony/generic/0.0/include/*.h
    do
        echo "    $f \\"
    done
    echo ""
    echo "# EOF"

) | tee $CJHD | \
    sed 's/^INPUT+=/C_HEADERS:=/' | \
    sed 's,jni/src/harmony/generic/0.0/include,../include,' \
  > $CJHM

chmod -w $CJHD $CJHM

CJJD="config/config_roster_jni_java.dox"
CJJM="config/config_roster_jni_java.mak"
rm -f $CJJD $CJJM
(
    echo "#"
    echo "# Roster of JNI sample Java source files"
    echo "# $USEDOXBLD"
    echo "#"
    echo "# for directory jni/src/harmony/generic/0.0/src"
    echo "#"
    echo "# Auto-generated by $PGMNAME"
    echo "# on $THISDATE:"
    echo "# DO NOT MODIFY!"
    echo "#"
) | tee $CJJD > $CJJM

    echo "INPUT+=\\"        >> $CJJD
    echo "JAVA_SOURCES:=\\" >> $CJJM

(
    for f in \
        `find jni/src/harmony/generic/0.0/src -name \*.java -print`
    do
        echo "    $f \\"
    done
    echo ""
    echo "# EOF"

) | tee -a $CJJD | sed 's/INPUT+=/JAVA_SOURCES:=/' | \
                   sed 's,jni/src/harmony/generic/0.0/src/,,' >> $CJJM

chmod -w $CJJD $CJJM

CTJD="config/config_roster_test_java.dox"
CTJM="config/config_roster_test_java.mak"
rm -f $CTJD $CTJM
(
    echo "#"
    echo "# Roster of test Java source files $USEDOXBLD"
    echo "#"
    echo "# Auto-generated by $PGMNAME"
    echo "# on $THISDATE:"
    echo "# DO NOT MODIFY!"
    echo "#"
    echo "INPUT+=\\"
    for f in `find test/src -name \*.java -print`
    do
        echo "    $f \\"
    done
    echo ""
    echo "# EOF"

) | tee $CTJD | \
    sed 's/INPUT+=/JAVA_SOURCES:=/;s,test/src/,,'> $CTJM

chmod -w $CTJD $CTJM

COJD="config/config_roster_org_java.dox"
rm -f $COJD
(
    echo "#"
    echo "# Roster of org.apache.harmony Java source files $USEDOX"
    echo "#"
    echo "# Auto-generated by $PGMNAME"
    echo "# on $THISDATE:"
    echo "# DO NOT MODIFY!"
    echo "#"
    echo "INPUT+=\\"
    for f in `find org/apache/harmony -name \*.java -print`
    do
        echo "    $f \\"
    done
    echo ""
    echo "# EOF"

) > $COJD

chmod -w $COJD

CUSD="config/config_roster_sh.dox"
rm -f $CUSD
(
    echo "#"
    echo "# Roster of shell scripts $USEDOX"
    echo "#"
    echo "# Auto-generated by $PGMNAME"
    echo "# on $THISDATE:"
    echo "# DO NOT MODIFY!"
    echo "#"
    echo "INPUT+=\\"
    # Have [A-Z]* listed _LAST_ due to Doxygen bug that points
    # reference to them off to last `basename X` in list.
    # Unfortunately, this bug also produces blank documentation,
    # but at least the annotations are present and point to the
    # right page. The bug has something to do with multiple files
    # named 'Makefile' et al and having one of these in the top-level
    # directory.  It wants more path name clarification, but does
    # not seem to like even absolute path names.  The other scripts
    # in the top-level directory are properly parsed.
    #
    # Notice that '_project_/src/Makefile' files have the relative path
    # prefix './' attached to the front of the @@file directive.  This
    # is to avoid an interesting sensitivity in Doxygen that got
    # confused between 'jvm/src/Makefile' and 'main/src/Makefile'
    # and failed to produce the "File List" entry for
    # 'jvm/src/Makefile'.  By marking them './jvm/src/Makefile' et al,
    # this behavior went away.
    #
    # Even with them listed last, the <b>@@file</b> directive still
    # must have an absolute path name for the documentation to
    # be properly parsed.  (See also comments in
    # @link ./INSTALL ./INSTALL@endlink).
    #
    for f in *.sh \
             support/*.sh \
             `find . -name Makefile -print | grep -v "^\./Makefile" | \
                                             sed 's,^\.\/,,' | \
                                             sort` \
             support/[A-Z]* \
             [A-Z]*
    do
        echo "    $f \\"
    done
    echo ""
    echo "# EOF"

) > $CUSD

chmod -w $CUSD

echo ""
echo "$PGMNAME:  Source files:                 $CRCD"
echo "                                          $CRCM"
echo "$PGMNAME:  Header files:                 $CRHD"
echo "                                          $CRHM"
echo "$PGMNAME:  Sample JNI C source files:    $CJCD"
echo "                                          $CJCM"
echo "$PGMNAME:  Sample JNI C header files:    $CJHD"
echo "                                          $CJHM"
echo "$PGMNAME:  Sample JNI Java source files: $CJJD"
echo "                                          $CJCM"
echo "$PGMNAME:  Test Java source files:       $CTJD"
echo "                                          $CTJM"
echo "$PGMNAME:  org.apache.harmony Java src:  $COJD"
echo "$PGMNAME:  Utility shell scripts:        $CUSD"
echo ""


###################################################################
#
# Done.
#
exit 0
#
# EOF
