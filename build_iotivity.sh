#!/bin/bash
set -x 
# build setup script to be used from otgc_linux folder
#
#   top_level
#       |---- otgc_linux 
#       |        |- lib/jni  (will be created)
#       |
#       |---- iotivity_lite  (should be there)

# in otgc_linux
mkdir -p lib/jni

if [ -d ~/jdk1.8.0_281/bin ] 
then
    echo "JDK exist." 
    #
    # build otgc (in the otgc-linux folder)
    #
    export PATH=~/jdk1.8.0_281/bin:$PATH
    export LD_LIBRARY_PATH=./lib/jni
    export JAVA_HOME=~/jdk1.8.0_281
fi

# go to the folder of iotivity
cd ../iotivity-lite/

cd ./port/linux
make DEBUG=1 SECURE=1 IPV4=1 TCP=1 PKI=1 DYNAMIC=1 CLOUD=1 JAVA=1 IDD=1 
cd ..
cd ..
cd ..
# back at top_level

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







