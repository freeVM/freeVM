#!/bin/sh

set -x
set -e

SOURCE=${1:-http://people.apache.org/~hindessm/milestones/5.0M15/apache-harmony-5.0-src-r991518-snapshot.tar.gz}
DIST=${2:-lenny}
DEFARCH=`dpkg --print-architecture`
ARCH=${3:-$DEFARCH}
TARGET=${4:-target}/$DIST-$ARCH

# workaround for chicken/egg problem with debian/changelog files
# I check in the versions I ultimately use ASAP after release.
CLOGBASE=${5:-http://people.apache.org/~hindessm/rel}

BASE=$DIST-$ARCH.cow

test -d $BASE ||
  sudo cowbuilder --create --basepath $BASE --distribution $DIST \
                  --debootstrap debootstrap --debootstrapopts --arch=$ARCH

mkdir -p $TARGET
netcat -q60 -l 127.0.0.1 1234 |tar xvf - -C $TARGET &
NCPID=$!

ARCHCMD=
case "$ARCH" in
    i386)
        ARCHCMD=linux32
        ;;
    amd64)
        ARCHCMD=linux64
        ;;
esac

cat <<'EOF' >/tmp/bundle-deb-builder
#!/bin/sh

set -x
set -e

SOURCE=$1
CLOGBASE=$2
CLOG=
case "$SOURCE" in
  *-6.0-*)
    CLOG=${CLOGBASE}/changelog-6.0
    ;;
  *)
    CLOG=${CLOGBASE}/changelog-5.0
    ;;
esac

HOME=/tmp
export HOME
cd
DEBARCH=`dpkg --print-architecture`

echo deb http://www.apache.org/dist/harmony/milestones/6.0/debian/$DEBARCH/ ./ \
  >>/etc/apt/sources.list

apt-get update
apt-get -y --force-yes install harmony-6.0-jre
apt-get -y install ant subversion ant-optional liblcms1-dev libjpeg62-dev \
                   libpng-dev libxft-dev libxml2-dev libxtst-dev libxext-dev \
                   gnupg fakeroot debhelper libecj-java junit netcat-traditional

mkdir /tmp/build
useradd -m -d /tmp/build build
chown -R build:build /tmp/build

su -c "wget --no-cache -q -O- http://www.apache.org/dist/harmony/KEYS | \
         gpg --import; \
       wget $SOURCE && \
       wget --no-cache $SOURCE.asc && \
       gpg --verify ${SOURCE##*/}.asc && \
       tar xzf ${SOURCE##*/} && \
       cd apache-harmony-?.0-src-r*[0-9] && \
       wget --no-cache $CLOG && \
       cp -v ${CLOG##*/} debian/changelog && \
       perl -i -pne 's/-Werror//' classlib/depends/build/defines.mk && \
       ANT_OPTS=-Djava.net.preferIPv4Stack=true ant fetch-depends" - build

cat /tmp/build/*/common_resources/depends/jars/ecj_*/ecj-*.jar \
  >/usr/share/ant/lib/ecj-harmony.jar

su -c "cd apache-harmony-?.0-src-r*[0-9] && \
       dpkg-buildpackage -rfakeroot -us -uc" - build

( cd /tmp/build; \
  for f in *.deb ; do md5sum $f >$f.md5 ; sha1sum $f >$f.sha; done; \
  tar cvf - *.deb *.md5 *.sha ) | nc -q60 127.0.0.1 1234
EOF
chmod a+rx /tmp/bundle-deb-builder

$ARCHCMD sudo cowbuilder --execute --basepath $BASE -- \
  /tmp/bundle-deb-builder $SOURCE $CLOGBASE

wait $NCPID
