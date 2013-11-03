# !/bin/bash

# (c) 2013, Adaptive Alchemy, LLC
# Licensed under the Apache Software License 2.0 without warranty

# The intention of this script is to get all subdirectory names
# in the SVN repository under ANY revision

CAT_TO="$1"
URL="$2"
REV=$3

TEMP='/tmp/svnls.'$$
TEMP2='/tmp/svnls.2.'$$

function get_revs {
  _URL=$1
  _REV=$2
  if [ "$REV" != "" ] ; then
    _URL="$_URL@$_REV"
  fi
  svn log -v --stop-on-copy "$_URL"  | grep -E '^r[0-9]+\s+\| ' | awk '{print $1}' | sed s/^r// | sort | uniq
}



function cat_svnls {
  _CAT_TO=$1
  _URL=$2
  _REV=$3
  if [ "$_REV" != "" ] ; then
    _URL="$_URL@$_REV"
  fi
  svn ls "$_URL" >$TEMP 
  if [ ! $? ]; then
    return 1
  fi
  cat $_CAT_TO $TEMP | sort | uniq
}

>$CAT_TO

echo >&2
echo Getting revisions >&2
echo >&2

REV_SET=`get_revs $URL $REV`

for I in $REV_SET; do
  echo REVISION $I >&2
  cat_svnls $CAT_TO $URL $I >$TEMP2
  if [ $? ]; then
    cp -f $TEMP2 $CAT_TO
  fi
done

