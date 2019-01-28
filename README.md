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
### IoTivity Base API
To import the IoTivity Base API Binary into the OTGC Linux App project:

1. Create a new directory called `lib` in the project root.
2. Build the IoTivity binary in linux.

        scons BUILD_JAVA=1

3. Copy the `iotivity.jar` into `lib` directory. This file is found in:

        <iotivity>/out/linux/<your arch>/<release mode>/java/iotivity.jar

4. Add iotivity.jar to the libraries of the project.

5. Copy the libraries (only .so files) for IoTivity into the `lib` directory. These libraries is found in:

        <iotivity>/out/linux/<your arch>/<release mode>

6. Add the following command, to link the previous libraries with iotivity.jar, in the run/debug configuration:

        -Djava.library.path=<project directory>/lib/

## Build
 
## Testing
  
## Usage
### Debian package
The OTGC application requires the following packages:

- openjdk-8-jdk
- openjfx

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
  
## License

This library is released under Apache 2.0 license (http://www.apache.org/licenses/LICENSE-2.0.txt).

Please see the file 'LICENSE.md' in the root folder for further information.