#!/bin/sh

if [ -z $EVENTCLOUD_BUNDLE_HOME ];
then
	echo "EVENTCLOUD_BUNDLE_HOME not defined."
	exit 1;
fi

if [ ! -d $EVENTCLOUD_BUNDLE_HOME ];
then
	mkdir -p $EVENTCLOUD_BUNDLE_HOME
fi

export PATH_TO_RESOURCES=$EVENTCLOUD_BUNDLE_HOME/resources

unset CLASSPATH

# Cannot use wildcard because if jar name contains
# '.' character there are some issues
#CLASSPATH="${EVENTCLOUD_BUNDLE_HOME}/lib/*"
for jar in $(ls $EVENTCLOUD_BUNDLE_HOME/lib/);
do
    CLASSPATH="${EVENTCLOUD_BUNDLE_HOME}/lib/${jar}:${CLASSPATH}"
done;

export CLASSPATH

if [ $DEBUG ];
then
    echo "--> Using PATH_TO_RESOURCES=${PATH_TO_RESOURCES}"
    echo "--> Using EVENTCLOUD_BUNDLE_HOME=${EVENTCLOUD_BUNDLE_HOME}"
fi
