#!/bin/bash
set -x 
# build setup script to be used from otgc_linux folder
#
#   top_level
#       |---- otgc_linux 
#       |        |- lib/jni  (will be created)
#       |
#       |---- iotivity_lite  (will be created)

# in otgc_linux
mkdir lib
cd lib
mkdir jni
cd ..
cd ..

# build the lib 
# clone in top_level
rm -rf iotivity-lite
git clone https://github.com/iotivity/iotivity-lite.git
cd iotivity-lite
#git checkout bb53715d5e4dbe30360685690bac61f2e4546f6b
git checkout master


swigExists=`swig -version`
if [ -z "$swigExists" ]; then
sudo apt-get update
sudo apt-get -y install swig
fi

cd ./port/linux
make DEBUG=1 SECURE=1 IPV4=1 TCP=1 PKI=1 DYNAMIC=1 CLOUD=1 JAVA=1 IDD=1 
cd ..
cd ..
cd ..

#
# copying the result of the build to the created lib/jni folder in otgc_linux
# (from top_level)
pwd
cp ./iotivity-lite/swig/iotivity-lite-java/libs/*.so ./otgc-linux/lib/jni/.
cp ./iotivity-lite/swig/iotivity-lite-java/libs/*.jar ./otgc-linux/lib/.

#
# go to otgc-linux folder
#
cd otgc-linux







