#!/bin/bash
set -x #echo on


#
# build script to be used on linux
# oracle jdk installed on home dir (e.g. ~)
#
OTGC_VERSION=3.0.0


# architecture of the current device
uname -m

# make sure that the zulu compiler is being used
export PATH=~/jdk1.8.0_281/bin:$PATH
java -version
env

#
# copying the result of the build to the created lib/jni folder in otgc_linux
# (from top_level)
pwd
mkdir -p lib/jni
cp ../iotivity-lite/swig/iotivity-lite-java/libs/*.so ./lib/jni/.
cp ../iotivity-lite/swig/iotivity-lite-java/libs/*.jar ./lib/.


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



echo "# script to start otgc_linux" > start.sh
echo "# create the data folder" >> start.sh
echo "mkdir data" >> start.sh
echo "# start the application" >> start.sh
echo "${homedir}/zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64/bin/java -Djava.library.path=./lib/jni -jar target/jfx/app/otgc-3.0.0-jfx.jar" >> start.sh

cp src/main/resources/data .

#${homedir}/zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64/bin/java -Djava.library.path=./lib/jni -jar target/jfx/app/otgc-3.0.0-jfx.jar &

# build the debian package
cd ./build/debian
#./otgc_native.sh ../../target/jfx/app amd64
cd ..
cd ..

#
# install the created debian package e.g. the otgc application
#
# remove the currently installed package
#sudo dpkg -r otgc
# install the newly created package
#sudo dpkg -i ./build/debian/out/otgc-${OTGC_VERSION}.deb

#choose the correct Java version
#sudo update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java







