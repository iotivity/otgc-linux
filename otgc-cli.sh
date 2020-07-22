#!/bin/bash 

#
# Constants
#
OTGC_VERSION=2.0.0
UBUNTU_VERSION=16
ARCHITECTURE=amd64

#
# Functions
#
usage() { 
	echo "This script allows to build, install and execute the OTGC application" 
	echo -e "Usage:\n ./otgc-cli.sh [options]"
    echo -e "Option:"
    echo -e "\t--build\t\t\t -> Install all dependecies and build the OTGC application for Ubuntu"
    echo -e "\t--build-pi\t\t -> Install all dependencies and build the OTGC application for Raspberry Pi"
    echo -e "\t--install <version>\t -> Install the OTGC application"
    echo -e "\t--run\t\t\t -> Execute the OTGC application"
    echo -e "\t--help\t\t\t -> Show information"
    echo -e "Examples:"
    echo -e "\t./otgc-cli.sh --build"
    echo -e "\t./otgc-cli.sh --build --install 2.9.0"
    echo -e "\t./otgc-cli.sh --build-pi"
    echo -e "\t./otgc-cli.sh --install 2.9.0"
    echo -e "\t./otgc-cli.sh --run"
}

downgrade_javafx() {
    sudo apt-get -y install openjfx=8u161-b12-1ubuntu2 --allow-downgrades
    sudo apt-get -y install libopenjfx-java=8u161-b12-1ubuntu2 --allow-downgrades
    sudo apt-get -y install libopenjfx-jni=8u161-b12-1ubuntu2 --allow-downgrades
}

build() {
    echo "Building OTGC"
    # make sure that git is there, because the scripts are using git.
    # nano is just good to have,
    sudo apt-get -y install git 
    sudo apt-get -y nano 
    sudo apt-get -y automake
    sudo apt-get -y install make 
    sudo apt-get -y install make-guile
    sudo apt-get -y install gcc

    # maven and swig are needed for building
    sudo apt-get -y install maven
    sudo apt-get -y install swig

    if [ ${UBUNTU_VERSION:0:2} -gt 16 ]
    then
        downgrade_javafx
    else
        # install java components
        sudo apt-get -y install openjfx
        sudo apt-get -y install libopenjfx-jni
        sudo apt-get -y install libopenjfx-java
    fi

    # clone OTGC Linux project with submodules
    rm -rf otgc-linux
    git clone --recurse-submodules https://github.com/openconnectivity/otgc-linux.git

    # create structure for IoTivity-lite library
    cd otgc-linux
    mkdir -p lib/jni

    # build the IoTivity-lite library
    cd ./extlibs/iotivity-lite

    # apply all patches in the patch folder for IoTivity-lite
    git apply --ignore-whitespace ../patchs/*.patch

    # compile the library for Linux
    cd ./port/linux
    make DEBUG=1 SECURE=1 IPV4=1 TCP=0 PKI=1 DYNAMIC=1 CLOUD=0 JAVA=1 IDD=1 
    cd ../../../..  # back at the root folder

    # copy the result of the build
    cp ./extlibs/iotivity-lite/swig/iotivity-lite-java/libs/*.so ./lib/jni/.
    cp ./extlibs/iotivity-lite/swig/iotivity-lite-java/libs/*.jar ./lib/.

    # build OTGC
    export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"

    #install the created library, so maven can find it during the build
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
    ./otgc_native.sh ../../target/jfx/app $ARCHITECTURE
    cd ../../../
}

install() {
    echo "Installing OTGC v$OTGC_VERSION"

    #
    # install the created debian package e.g. the otgc application
    #
    # remove the currently installed package
    sudo dpkg -r otgc
    # install the newly created package
    pwd
    sudo dpkg -i ./otgc-linux/build/debian/out/otgc-${OTGC_VERSION}.deb
}

run() {
    echo "Running OTGC"

    # downgrade JavaFX library if necessary
    if [ ${UBUNTU_VERSION:0:2} -gt 16 ]
    then
        downgrade_javafx
    fi

    # run the application
    /usr/bin/otgc.sh
}

#
# Main
#

# check version of ubuntu
UBUNTU_VERSION=$(lsb_release -sr)
echo "Ubuntu version $UBUNTU_VERSION"

# Loop until all parameters are used up
if [ $# -ge 1 ]
then
    while [ "$1" != "" ]; do
        case $1 in
            --build )       build
                            ;;
            --build-pi )    ARCHITECTURE=armhf
                            build
                            ;;
            --install )     shift
                            OTGC_VERSION=$1
                            install
                            ;;
            --run )         run
                            ;;
            --help )        usage
                            exit
                            ;;
            * )             usage
                            exit 1
        esac

        # Shift all the parameters down by one
        shift

    done
else
  usage
fi
