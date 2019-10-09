#!/bin/bash
# build setup script to be used with an curl command
#

#
# system update
#
sudo apt-get -y update
sudo apt-get -y upgrade
sudo apt-get -y update

# make sure that git is there, because the scripts are using git.
# nano is just good to have,
sudo apt-get -y install git nano automake 

# maven and swig are needed for building
sudo apt-get -y install maven
sudo apt-get -y install swig
# install dependend jdk/jfx packages
sudo apt-get -y install openjdk-8-jdk
sudo apt-get -y install openjfx
sudo apt-get -y install libopenjfx-jni
sudo apt-get -y install libopenjfx-java

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
git checkout d301b81dc3ce39f477318be219ede1d9ab835940


git apply --stat ../otgc-linux/extlibs/patchs/remove_cred_by_credid.patch
git apply ../otgc-linux/extlibs/patchs/remove_cred_by_credid.patch

git apply --stat ../otgc-linux/extlibs/patchs/fix_oc_api.patch
git apply ../otgc-linux/extlibs/patchs/fix_oc_api.patch

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


# build otgc
cd otgc-linux
# in the otgc-linux folder
mvn jfx:jar

cd ./build/debian
./otgc_native.sh ../../target/jfx/app
cd ..
cd ..
cd ..
# back at the root level


# install otgc
sudo dpkg -r otgc
sudo dpkg -i ./otgc-linux/build/debian/out/otgc-2.1.0.deb








