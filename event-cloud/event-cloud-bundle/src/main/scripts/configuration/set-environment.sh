#!/bin/sh

CURRENT_DIR=$PWD/$(dirname $0)
export BUNDLE_HOME=$CURRENT_DIR/../
export PATH_TO_RESOURCES=$CURRENT_DIR/../resources

unset CLASSPATH

# Cannot use wildcard because there is an issue when a
# jar file contains '.' character in its name
for jar in $(ls $CURRENT_DIR/../lib/);
do
    CLASSPATH="${CURRENT_DIR}/../lib/${jar}:${CLASSPATH}"
done;

export CLASSPATH

if [ $DEBUG ];
then
    echo "--> Using BUNDLE_HOME=${BUNDLE_HOME}"
    echo "--> Using PATH_TO_RESOURCES=${PATH_TO_RESOURCES}"
    echo "--> Using CLASSPATH=${CLASSPATH}"
fi
