# !/bin/bash

# (c) 2013, Adaptive Alchemy, LLC
#
# Licensed under the Apache Software License 2.0 without warranty

# This is simply a convenience wrapper script around svn-all-fast-import

DIR=`echo $0 | sed -e 's/git-import.sh//'`
RULES="$1"
REPO="$2"
REV=$3
RESUME=
MAX=1210000
if [ $REV -gt 0 ]; then
  RESUME='--resume-from'
fi

echo "BEGIN git-import.sh $RULES from $REPO"
date

# Run KDE svn2git import-harmony.rules
# svn-all-fast-export needs to be on the PATH
# authors_email.txt in the current directory
svn-all-fast-export --rules "${DIR}$RULES" --identity-map "${DIR}authors-map-email.txt" --add-metadata --stats --max-rev $MAX "$REPO" $RESUME $REV
RET=$?

if [ ! $RET ]; then
  echo error code $RET
fi

echo "END git-import.sh $RULES from $REPO"
date
echo

exit $RET

