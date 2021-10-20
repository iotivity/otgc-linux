#!/bin/bash
set -x 
# build setup script to be used from otgc_linux folder
#
#   top_level
#       |
#       |---- iotivity_lite  (will be created)
#       |---- otgc_linux  (script is in this folder)


cd ..
# clone in top_level
rm -rf iotivity-lite
git clone https://github.com/iotivity/iotivity-lite.git
cd iotivity-lite
#git checkout bb53715d5e4dbe30360685690bac61f2e4546f6b
git checkout master
cd ..

#
# go to otgc-linux folder
#
cd otgc-linux







