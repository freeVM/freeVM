#!/bin/sh

#  Copyright 2001,2004-2006 The Apache Software Foundation
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# ----------------------------------------------------------------------------
# SimpleAxis2Server Script
#
# Environment Variable Prequisites
#
#   AXIS2_HOME   Home of Axis2 installation. If not set I will  try
#                   to figure it out.
#
#   JAVA_HOME       Must point at your Java Development Kit installation.
#
#   JAVA_OPTS       (Optional) Java runtime options.
#
# -----------------------------------------------------------------------------

# Get the context and from that find the location of setenv.sh
. `dirname $0`/setenv.sh

echo JAVA_HOME=$JAVA_HOME >>$1
echo JAVA_OPTS=$JAVA_OPTS >>$1
echo AXIS2_HOME=$AXIS2_HOME >>$1

$JAVA_HOME/bin/java $JAVA_OPTS -classpath $AXIS2_HOME:$AXIS2_CLASSPATH \
org.apache.axis2.transport.SimpleAxis2Server \
-repo $AXIS2_HOME/repository -conf $AXIS2_HOME/conf/axis2.xml >>$1 2>&1
