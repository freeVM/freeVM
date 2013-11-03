#!/bin/sh

set -x
set -e

SOURCE=${1:-http://people.apache.org/~hindessm/milestones/5.0M15/apache-harmony-5.0-src-r991518-snapshot.tar.gz}
DIST=${2:-lenny}
DEFARCH=`dpkg --print-architecture`
ARCH=${3:-$DEFARCH}
TARGET=${4:-target}/$DIST-$ARCH
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

cat <<'EOF' >/tmp/bundle-hdk-builder
#!/bin/sh

set -x
set -e

SOURCE=$1

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
                   gnupg netcat-traditional

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
       ANT_OPTS=-Djava.net.preferIPv4Stack=true ant fetch-depends" - build

cat /tmp/build/*/common_resources/depends/jars/ecj_*/ecj-*.jar \
  >/usr/share/ant/lib/ecj-harmony.jar

su -c "cd apache-harmony-?.0-src-r*[0-9] && \
       ant build bundle-hdk bundle-jdk bundle-jre" - build

tar cvf - -C /tmp/build/*/target --exclude=hdk . | nc -q60 127.0.0.1 1234
EOF
chmod a+rx /tmp/bundle-hdk-builder

$ARCHCMD sudo cowbuilder --execute --basepath $BASE -- \
  /tmp/bundle-hdk-builder $SOURCE

wait $NCPID
