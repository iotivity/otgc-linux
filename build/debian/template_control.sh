#!/bin/bash

# Define parameters which are passed in
VERSION=$1
ARCH=$2

# Define the template
cat << EOF
Package: OTGC
Version: $VERSION
Section: custom
Priority: optional
Architecture: $ARCH
Pre-Depends: openjdk-8-jdk, openjfx
Maintainer: DEKRA Testing and Certification, S.A.U.
Description: Onboarding Tool and Generic Client.
EOF
