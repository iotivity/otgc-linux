#!/bin/bash
set -x #echo on
#
# build script to be used on linux
# oracle jdk installed on home dir (e.g. ~)
#
OTGC_VERSION=3.0.0

ls ~/jdk1.8.0_281/bin

mydir=$PWD


if [ -d ~/jdk1.8.0_281/bin ] 
then
    echo "JDK exist." 
    #
    # build otgc (in the otgc-linux folder)
    #
    export PATH=~/jdk1.8.0_281/bin:$PATH
    export LD_LIBRARY_PATH=./lib/jni
else
    echo "Error: Directory does not exist: installing Azul JDK."

    cd ~
    #wget https://www.azul.com/downloads/?version=java-8-lts&package=jdk-fx
    wget https://cdn.azul.com/zulu/bin/zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64.tar.gz
    tar -xvf zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64.tar.gz
    export PATH=~/zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64/bin:$PATH
    cd $mydir
fi

java -version

# architecture of the current device
uname -m


#
# build otgc (in the otgc-linux folder)
#
#export PATH=~/jdk1.8.0_281/bin:$PATH
export LD_LIBRARY_PATH=./lib/jni


#export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"

# install the create lib, so that maven can find it during the build
mvn install:install-file \
    -Dfile=lib/iotivity-lite.jar \
    -DgroupId=org.iotivity \
    -DartifactId=iotivity-lite \
    -Dversion=1.0 \
    -Dpackaging=jar \
    -DgeneratePom=true

# do the actual build
mvn jfx:jar

# build the debian package
cd ./build/debian
./otgc_native.sh ../../target/jfx/app amd64
cd ..
cd ..

#
# install the created debian package e.g. the otgc application
#
# remove the currently installed package
sudo dpkg -r otgc
# install the newly created package
sudo dpkg -i ./build/debian/out/otgc-${OTGC_VERSION}.deb

#choose the correct Java version
#sudo update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java






