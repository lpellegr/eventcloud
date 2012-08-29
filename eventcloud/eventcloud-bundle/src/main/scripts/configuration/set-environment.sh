#!/bin/sh

CURRENT_DIR=$PWD/$(dirname $0)

function export_variables() {
    cd $CURRENT_DIR/..
    export BUNDLE_HOME=$PWD
    export PATH_TO_LIBRARIES=$BUNDLE_HOME/libs
    export PATH_TO_RESOURCES=$BUNDLE_HOME/resources
    cd $CURRENT_DIR
	
    unset CLASSPATH
    export CLASSPATH="$PATH_TO_LIBRARIES/*:${CLASSPATH}"
    
    debug_variables
}

function debug_variables() {
    if [ $DEBUG ];
    then
        echo "--> Using BUNDLE_HOME=${BUNDLE_HOME}"
        echo "--> Using PATH_TO_LIBRARIES=${PATH_TO_LIBRARIES}"
        echo "--> Using PATH_TO_RESOURCES=${PATH_TO_RESOURCES}"
        echo "--> Using CLASSPATH=${CLASSPATH}"
    fi
}

export_variables