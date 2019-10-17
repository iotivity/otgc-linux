#!/bin/bash
set -x #echo on
# build setup script to be used with an curl command
#
OTGC_VERSION=2.2.0

#
# system update
#
sudo apt-get -y update
sudo apt-get -y upgrade
sudo apt-get -y update

# make sure that git is there, because the scripts are using git.
# nano is just good to have,
sudo apt-get -y install git nano automake make

# maven and swig are needed for building
sudo apt-get -y install maven
sudo apt-get -y install swig

#
# install dependend jdk/jfx packages
#
sudo apt-get -y install openjdk-8-jdk
# install java components, but later overwrite them with the downgraded versions if they exist on the system
sudo apt-get -y install openjfx
sudo apt-get -y install libopenjfx-jni
sudo apt-get -y install libopenjfx-java
# install downgraded java components
sudo apt-get -y install openjfx=8u161-b12-1ubuntu2 
sudo apt-get -y install libopenjfx-java=8u161-b12-1ubuntu2 
sudo apt-get -y install libopenjfx-jni=8u161-b12-1ubuntu2

rm -rf otgc-linux
git clone https://github.com/openconnectivity/otgc-linux.git
cd otgc-linux


mkdir lib
cd lib
mkdir jni
cd ..
cd ..

#build the lib
rm -rf iotivity-lite
git clone https://github.com/iotivity/iotivity-lite.git
cd iotivity-lite
git checkout swig
git checkout otgc_220


git apply --stat ../otgc-linux/extlibs/patchs/remove_cred_by_credid.patch
git apply ../otgc-linux/extlibs/patchs/remove_cred_by_credid.patch

# not needed anymore
#git apply --stat ../otgc-linux/extlibs/patchs/fix_oc_api.patch
#git apply ../otgc-linux/extlibs/patchs/fix_oc_api.patch

cd ./port/linux
make DEBUG=1 SECURE=1 IPV4=1 TCP=0 PKI=1 DYNAMIC=1 CLOUD=0 JAVA=1 IDD=1 
cd ..
cd ..
cd ..

#
# copying the result of the build
#
pwd
cp ./iotivity-lite/swig/iotivity-lite-java/libs/*.so ./otgc-linux/lib/jni/.
cp ./iotivity-lite/swig/iotivity-lite-java/libs/*.jar ./otgc-linux/lib/.

#
# build otgc (in the otgc-linux folder)
#
cd otgc-linux

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
cd ..
# back at the root level

#
# install the created debian package e.g. the otgc application
#
# remove the currently installed package
sudo dpkg -r otgc
# install the newly created package
sudo dpkg -i ./otgc-linux/build/debian/out/otgc-${OTGC_VERSION}.deb







