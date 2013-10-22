#!/bin/sh

set -x
set -e

SOURCE=${1:-https://svn.apache.org/repos/asf/harmony/enhanced/java/trunk@945584}
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

cat <<'EOF' >/tmp/bundle-source-builder
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
apt-get -y install ant subversion ant-optional netcat-traditional

mkdir /tmp/build
useradd -m -d /tmp/build build
mkdir -p /tmp/build/.subversion/auth/svn.ssl.server
(cd /tmp/build/.subversion/auth/svn.ssl.server; \
  wget http://people.apache.org/~hindessm/rel/a19143e091784ae02c4c8a4d5b2cb30e )
chown -R build:build /tmp/build

su -c "svn co --non-interactive $SOURCE federated && \
       cd federated && ant setup copy-src bundle-src-tgz" - build

tar cvf - -C /tmp/build/*/target --exclude=src . | nc -q10 127.0.0.1 1234
EOF
chmod a+rx /tmp/bundle-source-builder

$ARCHCMD sudo cowbuilder --execute --basepath $BASE -- \
  /tmp/bundle-source-builder $SOURCE

wait $NCPID
