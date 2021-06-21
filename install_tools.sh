#!/bin/bash
set -x 


#
# install swig
#
swigExists=`swig -version`
if [ -z "$swigExists" ]; then
sudo apt-get update
sudo apt-get -y install swig
fi


ls ~/jdk1.8.0_281/bin
mydir=$PWD

#
# install java (zulu)
#
if [ -d ~/jdk1.8.0_281/bin ] 
then
    echo "JDK exist." 
    #
    # build otgc (in the otgc-linux folder)
    #
    export PATH=~/jdk1.8.0_281/bin:$PATH
    export LD_LIBRARY_PATH=./lib/jni
    export JAVA_HOME=~/jdk1.8.0_281
else
    echo "Error: Directory does not exist: installing Azul JDK."

    cd ~
    homedir=$PWD
    echo ${homedir}

    FILE=${homedir}/zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64.tar.gz
    if [ -f "$FILE" ]; then
        echo "$FILE exists."
    else
        echo "downloading $FILE"
        wget https://cdn.azul.com/zulu/bin/zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64.tar.gz
        tar -xvf zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64.tar.gz

    fi
    #wget https://www.azul.com/downloads/?version=java-8-lts&package=jdk-fx
    #wget https://cdn.azul.com/zulu/bin/zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64.tar.gz
    #tar -xvf zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64.tar.gz
    export PATH=${homedir}/zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64/bin:$PATH
    cd $mydir
    ${homedir}/zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64/bin/java -version

    #export JAVA_HOME=~/zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64/bin
    export JAVA_HOME=${homedir}/zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64
    # overwrite the existing JAVA environment

    ls $JAVA_HOME
    ls $JAVA_HOME_11_X64
   
    export JAVA_HOME_11_X64=$JAVA_HOME
fi

export PATH=${homedir}/zulu8.54.0.21-ca-fx-jdk8.0.292-linux_x64/bin:$PATH
export JAVA_HOME_11_X64=$JAVA_HOME
