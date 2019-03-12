#!/bin/bash

# Define parameters which are passed in
PROJECT_NAME=$1

# Define the template
cat << EOF
#!/bin/bash

# Constants
MY_PATH=/usr/lib/$PROJECT_NAME/lib/jni/

# Modify the LD_LIBRARY_PATH environment variable to remove the path where the OTGC shared library is located
export LD_LIBRARY_PATH="\${LD_LIBRARY_PATH/\${MY_PATH}:/}"
ldconfig

# Remove project script /usr/bin/otgc.sh
if [ -e /usr/bin/$PROJECT_NAME.sh ]
then
    rm /usr/bin/$PROJECT_NAME.sh
fi

# Remove library project folder /usr/lib/otgc
if [ -d /usr/lib/$PROJECT_NAME ]
then
    rm -r /usr/lib/$PROJECT_NAME
fi

# Remove data project folder	/usr/share/otgc
if [ -d /usr/share/$PROJECT_NAME ]
then
    rm -r /usr/share/$PROJECT_NAME
fi

EOF
