#!/bin/bash

# Create a Debian package
# DEKRA Testing and Certification, S.A.U.
# 24/09/2018

# Requirements
# 	- build-essential
#	- devscripts
#	- debhelper

# Constants
PROJECT_NAME="otgc"
VERSION="2.0.3"

program=$0

function usage()
{
    echo "usage: $program app_path"
    echo "  path      Path to assets, libraries and application jar"
    exit 1
}

if [ $# -ne 1 ] ; then
    usage
else
    path=$1
    
    mkdir $(pwd)/out

    # Prepare the package
    mkdir $(pwd)/out/$PROJECT_NAME-$VERSION
    mkdir $(pwd)/out/$PROJECT_NAME-$VERSION/DEBIAN
    mkdir -p $(pwd)/out/$PROJECT_NAME-$VERSION/usr/bin                          # otgc.sh
    mkdir -p $(pwd)/out/$PROJECT_NAME-$VERSION/usr/lib/$PROJECT_NAME            # otgc-1.0.0-jfx.jar
    mkdir -p $(pwd)/out/$PROJECT_NAME-$VERSION/usr/lib/$PROJECT_NAME/lib        # third party libraries
    mkdir -p $(pwd)/out/$PROJECT_NAME-$VERSION/usr/lib/$PROJECT_NAME/lib/jni    # dynamic library
    mkdir -p $(pwd)/out/$PROJECT_NAME-$VERSION/usr/share/$PROJECT_NAME/data     # assets
    mkdir -p $(pwd)/out/$PROJECT_NAME-$VERSION/usr/share/applications           # item desktop

    # Copy configuration and script files to DEBIAN folder
    if [ -e "$(pwd)/control" ]
    then
        cp $(pwd)/control $(pwd)/out/$PROJECT_NAME-$VERSION/DEBIAN
    else
        echo "Control file does not exist."
        exit 1
    fi
    # Pre-Installation script
    if [ -e "$(pwd)/template_preinst.sh" ]
    then
        chmod 755 $(pwd)/template_preinst.sh
        if [ -e "$(pwd)/out/preinst" ]
        then
            rm $(pwd)/out/preinst
        fi
        $(pwd)/template_preinst.sh "$PROJECT_NAME" > $(pwd)/out/preinst
	    chmod 755 $(pwd)/out/preinst
        cp $(pwd)/out/preinst $(pwd)/out/$PROJECT_NAME-$VERSION/DEBIAN
        rm $(pwd)/out/preinst
    fi
    # Post-Installation script
    if [ -e "$(pwd)/template_postinst.sh" ]
    then
        chmod 755 $(pwd)/template_postinst.sh
        if [ -e "$(pwd)/out/postinst" ]
        then
	        rm $(pwd)/out/postinst
        fi
        $(pwd)/template_postinst.sh "$PROJECT_NAME" > $(pwd)/out/postinst
	    chmod 755 $(pwd)/out/postinst
        cp $(pwd)/out/postinst $(pwd)/out/$PROJECT_NAME-$VERSION/DEBIAN
        rm $(pwd)/out/postinst
    fi
    # Pre-Remove script
    if [ -e "$(pwd)/template_prerm.sh" ]
    then
        chmod 755 $(pwd)/template_prerm.sh
        if [ -e "$(pwd)/out/prerm" ]
        then
	        rm $(pwd)/out/prerm
        fi
        $(pwd)/template_prerm.sh "$PROJECT_NAME" > $(pwd)/out/prerm
	    chmod 755 $(pwd)/out/prerm
        cp $(pwd)/out/prerm $(pwd)/out/$PROJECT_NAME-$VERSION/DEBIAN
        rm $(pwd)/out/prerm
    fi
    # Post-Remove script
    if [ -e "$(pwd)/template_postrm.sh" ]
    then
        chmod 755 $(pwd)/template_postrm.sh
        if [ -e "$(pwd)/out/postrm" ]
        then
	        rm $(pwd)/out/postrm
        fi
        $(pwd)/template_postrm.sh "$PROJECT_NAME" > $(pwd)/out/postrm
	    chmod 755 $(pwd)/out/postrm
        cp $(pwd)/out/postrm $(pwd)/out/$PROJECT_NAME-$VERSION/DEBIAN
        rm $(pwd)/out/postrm
    fi

    # Create executable script and copy to /usr/bin
    if [ -e "$(pwd)/out/$PROJECT_NAME.sh" ]
    then
        rm $(pwd)/out/$PROJECT_NAME.sh
    fi
    echo "#!/bin/bash"$'\n' >> "$(pwd)/out/$PROJECT_NAME.sh"
    echo "export LD_LIBRARY_PATH=/usr/lib/$PROJECT_NAME/lib/jni/:\${LD_LIBRARY_PATH}"$'\n' >> "$(pwd)/out/$PROJECT_NAME.sh"
    echo "cd /usr/share/$PROJECT_NAME" >> "$(pwd)/out/$PROJECT_NAME.sh"
    echo "mkdir logs"$'\n'  >> "$(pwd)/out/$PROJECT_NAME.sh"
    echo "DATE=\$(date +\"%Y%m%d%H%M\")"$'\n'  >> "$(pwd)/out/$PROJECT_NAME.sh"
    echo "java -jar /usr/lib/$PROJECT_NAME/$PROJECT_NAME-$VERSION-jfx.jar > logs/otgc_\$DATE.log"$'\n' >> "$(pwd)/out/$PROJECT_NAME.sh"
    chmod 755 "$(pwd)/out/$PROJECT_NAME.sh"
    cp "$(pwd)/out/$PROJECT_NAME.sh" $(pwd)/out/$PROJECT_NAME-$VERSION/usr/bin
    rm $(pwd)/out/$PROJECT_NAME.sh

    # Copy otgc.jar to /usr/lib/otgc
    if [ -e "$path/$PROJECT_NAME-$VERSION-jfx.jar" ]
    then
        cp "$path/$PROJECT_NAME-$VERSION-jfx.jar" $(pwd)/out/$PROJECT_NAME-$VERSION/usr/lib/$PROJECT_NAME
    else
        echo "$path/$PROJECT_NAME-$VERSION-jfx.jar does not exist."
        exit 1
    fi

    # Copy third party libraries and dynamic libraries to /usr/lib/otgc
    cp -r $path/lib/ $(pwd)/out/$PROJECT_NAME-$VERSION/usr/lib/$PROJECT_NAME/

    # Copy data to /usr/share/otgc
    for filename in $path/data/*; do
        cp $filename $(pwd)/out/$PROJECT_NAME-$VERSION/usr/share/$PROJECT_NAME/data
    done

    # Create a item desktop and copy to /usr/share/applications
    if [ -e "$(pwd)/template_desktop.sh" ]
    then
        chmod 755 $(pwd)/template_desktop.sh
        if [ -e "$(pwd)/out/$PROJECT_NAME.desktop" ]
        then
            rm $(pwd)/out/$PROJECT_NAME.desktop
        fi
        $(pwd)/template_desktop.sh "$PROJECT_NAME" > $(pwd)/out/$PROJECT_NAME.desktop
        cp $(pwd)/out/$PROJECT_NAME.desktop $(pwd)/out/$PROJECT_NAME-$VERSION/usr/share/applications
        rm $(pwd)/out/$PROJECT_NAME.desktop
    fi

    # Build native
    dpkg-deb --build $(pwd)/out/$PROJECT_NAME-$VERSION

    # Remove temporal files and folders
    rm -r $(pwd)/out/$PROJECT_NAME-$VERSION
fi

