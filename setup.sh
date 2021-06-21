#!/bin/bash
set -x #echo on
# build setup script to be used with an curl command
#

rm -rf otgc-linux
git clone https://github.com/iotivity/otgc-linux.git
cd otgc-linux

# install the tools
sh ./install_tools.sh
# build the iotivity library
sh ./checkout_iotivity.sh
# build the iotivity library
sh ./build_iotivity.sh
# build the linux otgc application (including .deb file)
sh ./build_otgc.sh





