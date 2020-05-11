<!---
  ~ //******************************************************************
  ~ //
  ~ // Copyright 2018 DEKRA Testing and Certification, S.A.U. All Rights Reserved.
  ~ //
  ~ //******************************************************************
  ~ //
  ~ // Licensed under the Apache License, Version 2.0 (the "License");
  ~ // you may not use this file except in compliance with the License.
  ~ // You may obtain a copy of the License at
  ~ //
  ~ //      http://www.apache.org/licenses/LICENSE-2.0
  ~ //
  ~ // Unless required by applicable law or agreed to in writing, software
  ~ // distributed under the License is distributed on an "AS IS" BASIS,
  ~ // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ // See the License for the specific language governing permissions and
  ~ // limitations under the License.
  ~ //
  ~ //******************************************************************
  --->
# Onboarding Tool and Generic Client: Linux App
  
## Overview

## Project Setup
### IoTivity-lite API
To import the IoTivity-lite API Binary into the OTGC Linux App project:

1. Create a new directory called `lib` in the project root.

2. Create a new directory called `jni` into &lt;otgc-linux>/lib

3. Copy **iotivity.jar** into &lt;otgc-linux>/lib.

4. Copy **libiotivity-lite.so** into &lt;otgc-linux>/lib/jni

5. Install iotivity-lite.jar into the local Maven repository
```
mvn install:install-file \
    -Dfile=lib/iotivity-lite.jar \
    -DgroupId=org.iotivity \
    -DartifactId=iotivity-lite \
    -Dversion=1.0 \
    -Dpackaging=jar \
    -DgeneratePom=true
```

To run or to debug the OTGC application in an IDE, the following command as to be added as VM argument to link iotivity-lite library to the project:
```
-Djava.library.path=<otgc-linux>/lib/jni
```



## Build
### IoTivity-lite Linux API

The steps required to build the binary of the IoTivity-lite Linux API are shown below:

1. Change to the swig branch.
```
git checkout swig
```
2. Apply all patchs of the OTGC in IoTivity-lite
```
git apply <otgc-linux>/extlibs/patchs/fix_swig_flags.patch
git apply <otgc-linux>/extlibs/patchs/ignore_cloud_discover_resources.patch
```
3. Go to the linux directory.
```
cd <iotivity-lite>/port/linux
```
4. Execute the command to build the library.
```
make DEBUG=1 SECURE=1 IPV4=1 TCP=0 PKI=1 DYNAMIC=1 CLOUD=0 JAVA=1 IDD=1
```

Once built, the library can be found at:
```
<iotivity-lite>/swig/iotivity-lite-java/libs
```

### OTGC

The steps to build the OTGC are shown below:

1. To build the project, execute the command:
```
mvn jfx:jar
```
2. When the project is built, go to the Debian directory
```
cd <otgc-linux>/build/debian
```
3. To create the Debian package, execute the command:
```
./otgc_native.sh <otgc-linux>/target/jfx/app
```

Once the Debian package is build, it can be found in:
```
<otgc-linux>/build/debian/out
```
 
## Testing
  
## Usage
### Debian package
The OTGC application requires the following packages:

- openjdk-8-jdk
- openjfx for Ubuntu 16
- openjfx=8u161-b12-1ubuntu2 libopenjfx-java=8u161-b12-1ubuntu2 libopenjfx-jni=8u161-b12-1ubuntu2 for Ubuntu 18

This packages can install through the next command:

    sudo apt-get install <package>
    
where package is the name of the package to install.

The installation of the OTGC won't be possible if the above packages are not installed. Once all dependencies are installed, to install the debian package, use the next command:

    sudo dpkg -i otgc-<version>.deb

where version is the target version of the package to install (e.g. 1.0.0).



To run the application when it has been installed:
1.  Click on the menu icon.
2.  Search OTGC in search box.
3.  Click on OTGC application.

The OTGC application will start in a few seconds.

To uninstall the aplication, use the next command:

    sudo dpkg -r otgc
  
## Script to build and install
The following command executes all steps indicated above for building and installing on Linux (ubuntu 18)

   curl https://openconnectivity.github.io/otgc-linux/setup.sh | bash
   
note: __when the executable does start, please reinstall jfx manually__ by entering on the commandline:

sudo apt-get install openjfx=8u161-b12-1ubuntu2 libopenjfx-java=8u161-b12-1ubuntu2 libopenjfx-jni=8u161-b12-1ubuntu2

and start otgc on the command line: /usr/bin/otgc.sh

 
 
For rasbian (stretch) use:

 curl https://openconnectivity.github.io/otgc-linux/setup-pi.sh | bash
   
 
Note for building on a raspberry pi the Archicture needs to be armhf and has to run a windowing system
(for example raspbian),
This change can be made in the file /build/debian/control.
  
## License

This library is released under Apache 2.0 license (http://www.apache.org/licenses/LICENSE-2.0.txt).

Please see the file 'LICENSE.md' in the root folder for further information.
