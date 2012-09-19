#!/bin/sh

. $(dirname $0)/configuration/set-environment.sh

# Paths to output files
LOGS_DIR=$BUNDLE_HOME/logs
REGISTRY_OUTPUT_FILE=$LOGS_DIR/eventclouds-registry.output
WS_EVENTCLOUDS_MANAGEMENT_OUTPUT_FILE=$LOGS_DIR/ws-eventclouds-management.output

# Paths to instance files
INSTANCES_DIR=$BUNDLE_HOME/instances
REGISTRY_INSTANCE_FILE=$INSTANCES_DIR/eventclouds-registry
WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE=$INSTANCES_DIR/ws-eventclouds-management

# Ports assignation
EVENTCLOUDS_REGISTRY_PORT=8081
EVENTCLOUDS_PORTS_LOWER_BOUND=1100
PROXIES_PORTS_LOWER_BOUND=9000
WS_EVENTCLOUDS_MANAGEMENT_HTTP_PORT=8082
WS_EVENTCLOUDS_MANAGEMENT_PNP_PORT=8083

trap clear 1 2 15

function main() {
    cd $BUNDLE_HOME

    check_requirements
   
    if [[ $# -eq 1 ]]
    then
        if [[ $1 = "-k" ]]
        then
            undeploy
            return 0;
        elif [[ $1 = "-kc" ]]
        then
            undeploy
            remove_logs
            return 0
        else
            print_usage
        fi
    fi;

    if [[ -d $INSTANCES_DIR ]];
    then
	    echo "Eventclouds registry already running. Use -k or -kc option to kill all existing instances."
	    return 1;
    fi;

    mkdir -p $INSTANCES_DIR &> /dev/null
    mkdir -p $LOGS_DIR &> /dev/null
    
    deploy
}

function check_requirements() {
    if ! command_exists java
    then
        print_java_requirements
        exit 1
    fi
	if ! command_exists python
    then
        print_python_requirements
        exit 1
    fi
    if ! command_exists watchmedo
    then
        print_watchdog_requirements
        exit 1
    fi
}

function print_java_requirements() {
    echo "Java is required!"
    echo "Download from http://www.oracle.com/technetwork/java/javase/downloads/index.html and install it"
}

function print_python_requirements() {
    echo "Python is required!"
    echo "Download from http://www.python.org/download/ and install it"
}

function print_watchdog_requirements() {
    echo "Please install the watchdog library by using *one* of the following commands:"
    echo "  sudo easy_install watchdog"
    echo "  sudo pip install watchdog"
}

function print_usage() {
    echo "usage: infrastructure-launcher.sh [-k|-kc]"
    echo "       -k  kills the running instance"
    echo "       -kc kills the running instance and removes the log files"
    exit 1
}

function command_exists() {
    return $(type $1 &> /dev/null)
}

function deploy() {
    # deploy the eventclouds registry
    deploy_eventclouds_registry

    # deploy eventclouds management web service
    deploy_ws_management_proxy
    
    echo "Logs available at:"
    echo "    $LOGS_DIR"
}

function deploy_eventclouds_registry() {
    java -Xms128m -Xmx256m \
     -server \
     -Djava.security.policy=$PATH_TO_RESOURCES/proactive.security.policy \
     -Deventcloud.bundle.home=$BUNDLE_HOME \
     -Deventcloud.configuration=$PATH_TO_RESOURCES/eventcloud.properties \
     -Deventcloud.instance.file=$REGISTRY_INSTANCE_FILE \
     -Dlog4j.configuration=file:$PATH_TO_RESOURCES/log4j.properties \
     -Dlogback.configurationFile=file:$PATH_TO_RESOURCES/logback.xml \
     -Dlogging.output.filename=$(basename $REGISTRY_INSTANCE_FILE) \
     -Dproactive.communication.protocol=pnp \
     -Dproactive.pnp.port=$EVENTCLOUDS_REGISTRY_PORT \
     -cp $CLASSPATH \
     fr.inria.eventcloud.deployment.cli.launchers.EventCloudsRegistryLauncher &> $REGISTRY_OUTPUT_FILE & 
     
    echo $! > $REGISTRY_INSTANCE_FILE.pid

    wait_file_creation $INSTANCES_DIR $(basename $REGISTRY_INSTANCE_FILE)

    EVENTCLOUDS_REGISTRY_URL=$(cat $REGISTRY_INSTANCE_FILE)

    echo "Eventclouds registry deployed at:"
    echo "    $EVENTCLOUDS_REGISTRY_URL"
}

function deploy_ws_management_proxy() {
    java -Xms256m -Xmx10240m \
     -server \
     -Djava.security.policy=$PATH_TO_RESOURCES/proactive.security.policy \
     -Deventcloud.bundle.home=$BUNDLE_HOME \
     -Deventcloud.configuration=$PATH_TO_RESOURCES/eventcloud.properties \
     -Deventcloud.instance.file=$WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE \
     -Dlog4j.configuration=file:$PATH_TO_RESOURCES/log4j.properties \
     -Dlogback.configurationFile=file:$PATH_TO_RESOURCES/logback.xml \
     -Dlogging.output.filename=$(basename $WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE) \
     -Dproactive.communication.protocol=pnp \
     -Dproactive.pnp.port=$WS_EVENTCLOUDS_MANAGEMENT_PNP_PORT \
     -cp $CLASSPATH fr.inria.eventcloud.deployment.cli.launchers.EventCloudManagementWsLaucher \
     --registry-url $EVENTCLOUDS_REGISTRY_URL --port-lower-bound $PROXIES_PORTS_LOWER_BOUND \
     -p $WS_EVENTCLOUDS_MANAGEMENT_HTTP_PORT &> $WS_EVENTCLOUDS_MANAGEMENT_OUTPUT_FILE &

    echo $! > $WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE.pid
        
    wait_file_creation $INSTANCES_DIR $(basename $WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE)
    
    echo "Eventclouds management web service deployed at:"
    echo "    $(cat $WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE)"
}

# $1 absolute path to directory where file creation is expected
# $2 expected filename
function wait_file_creation() {
    if [[ ! -f $1/$2 ]]
    then
    	python $BUNDLE_HOME/scripts/filename-watchdog.py $1 $2 &> /dev/null
    fi
}

function clear() {
    undeploy
    remove_logs
    exit 2
}

function undeploy() {
    kill_process $REGISTRY_INSTANCE_FILE
    kill_process $INSTANCES_DIR/ws-eventclouds-management

    rm -rf $INSTANCES_DIR &> /dev/null
}

function remove_logs() {
    rm -rf $LOGS_DIR &> /dev/null
}

# $1 an absolute path to an instance file
function kill_process() {
    if [[ -f $1 ]]
    then
        kill -9 $(cat $1.pid) &> /dev/null
    fi
}

main $@

. $BUNDLE_HOME/scripts/configuration/unset-environment.sh
