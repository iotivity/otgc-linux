#!/bin/bash

# Define parameters which are passed in
PROJECT_NAME=$1

# Define the template
cat << EOF
[Desktop Entry]
Encoding=UTF-8
Name=OTGC
Comment=Onboarding Tool and Generic Client
Exec=/usr/bin/$PROJECT_NAME.sh
Icon=/usr/share/$PROJECT_NAME/data/ic_launcher_round.png
Terminal=false
Type=Application
EOF
