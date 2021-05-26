#!/bin/bash
set -x #echo on
# build setup script to be used with an curl command
#

rm -rf otgc-linux
git clone https://github.com/openconnectivity/otgc-linux.git
cd otgc-linux

# build the iotivity library
./build_iotivity.sh
# build the linux otgc application (including .deb file)
./build_otgc.sh





