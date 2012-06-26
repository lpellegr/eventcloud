#!/bin/bash

. $(dirname $0)/configuration/set-environment.sh

# Paths to output files
OUTPUTS_DIR=$BUNDLE_HOME/logs
REGISTRY_OUTPUT_FILE=$OUTPUTS_DIR/eventclouds-registry.output

# Paths to instance files
INSTANCES_DIR=$BUNDLE_HOME/instances
REGISTRY_INSTANCE_FILE=$INSTANCES_DIR/eventclouds-registry
EVENTCLOUDS_INSTANCE_FILE=$INSTANCES_DIR/eventclouds

# Ports assignation
EVENTCLOUDS_REGISTRY_PORT=8081
EVENTCLOUDS_PORTS_LOWER_BOUND=1100
PROXIES_PORTS_LOWER_BOUND=9000
WS_EVENTCLOUDS_MANAGEMENT_HTTP_PORT=8082
WS_EVENTCLOUDS_MANAGEMENT_PNP_PORT=8083

# Infrastructure scale
NB_EVENTCLOUDS=1
NB_PEERS_BY_EVENTCLOUD=1
NB_TRACKERS_BY_EVENTCLOUD=1

trap undeploy 1 2 15

function main() {
	cd $BUNDLE_HOME

    if [[ "$(whereis inotifywait)" == "inotifywait:" ]];
        then echo "Library 'inotify-tools' is required, please install it.";
    fi;
   
    if [[ $# -ne 2 && $1 = "-k" ]];
    then
        undeploy
	return 0;
    fi;

    if [[ -d $INSTANCES_DIR ]];
    then
	echo "Eventclouds registry already running. Use -k option to kill all existing instances."
	return 1;
    fi;

    mkdir -p $INSTANCES_DIR;
    mkdir -p $OUTPUTS_DIR;

    # deploy the eventclouds registry
    deploy_eventclouds_registry

    # deploy eventclouds management web service
    deploy_ws_management_proxy
    
    # deploy the event clouds
    #deploy_eventclouds
    
    # deploy the ws proxies
    #deploy_ws_proxies
}

function deploy_eventclouds_registry() {
    . $BUNDLE_HOME/scripts/eventclouds-registry-launcher.sh \
        $REGISTRY_INSTANCE_FILE $EVENTCLOUDS_REGISTRY_PORT &> $REGISTRY_OUTPUT_FILE &

    wait_file_creation $INSTANCES_DIR $(basename $REGISTRY_INSTANCE_FILE)

    EVENTCLOUDS_REGISTRY_URL=$(cat $REGISTRY_INSTANCE_FILE)

    echo "Eventclouds registry deployed at:"
    echo $EVENTCLOUDS_REGISTRY_URL
    echo
}

function deploy_eventclouds() {
    PNP_PORT=$EVENTCLOUDS_PORTS_LOWER_BOUND

    for ((i=1; i<=$NB_EVENTCLOUDS; i++));
    do
	EVENTCLOUD_INSTANCE_FILE=$(eventcloud_instance_file $i)

	. $BUNDLE_HOME/scripts/eventcloud-launcher.sh $EVENTCLOUD_INSTANCE_FILE $PNP_PORT \
            --registry-url $EVENTCLOUDS_REGISTRY_URL --nb-trackers $NB_TRACKERS_BY_EVENTCLOUD \
            --nb-peers $NB_PEERS_BY_EVENTCLOUD &> $(eventcloud_output_file $i) &
	
	wait_file_creation $INSTANCES_DIR $(basename $EVENTCLOUD_INSTANCE_FILE)

	PNP_PORT=$(expr $PNP_PORT + 1)
	
	echo "Eventcloud with $NB_TRACKERS_BY_EVENTCLOUD tracker(s) and $NB_PEERS_BY_EVENTCLOUD peer(s) is running under url:"
	echo $(cut -d " " -f 3 $EVENTCLOUD_INSTANCE_FILE)
    done;
    echo
}

function deploy_ws_management_proxy() {
   WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE=$INSTANCES_DIR/ws-eventclouds-management

    . $BUNDLE_HOME/scripts/ws-eventclouds-management-launcher.sh \
        $WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE $WS_EVENTCLOUDS_MANAGEMENT_PNP_PORT \
        --registry-url $EVENTCLOUDS_REGISTRY_URL --port-lower-bound $PROXIES_PORTS_LOWER_BOUND \
        -p $WS_EVENTCLOUDS_MANAGEMENT_HTTP_PORT &> $OUTPUTS_DIR/ws-eventclouds-management.output &
        
    wait_file_creation $INSTANCES_DIR $(basename $INSTANCES_DIR/ws-eventclouds-management)
    
    echo "Eventclouds management web service deployed at:"
    echo $(cat $INSTANCES_DIR/ws-eventclouds-management)
}

function deploy_ws_proxies() {
    PORT=$PROXIES_PORTS_LOWER_BOUND

    echo "Proxies deployed are:"
    for ((i=1; i<=NB_EVENTCLOUDS; i++))
    do 
	ID=$(cut -d " " -f 3 $(eventcloud_instance_file $i))

	echo "  . for eventcloud $ID:"
	
	WS_SUBSCRIBE_INSTANCE_FILE=$(ws_instance_file ws-subscribe-proxy $i)
	deploy_ws_proxy ws-subscribe-proxy $PORT $i $ID
	PORT=$(expr $PORT + 1)
	wait_file_creation $INSTANCES_DIR $(basename $WS_SUBSCRIBE_INSTANCE_FILE)
	echo "      $(cat $WS_SUBSCRIBE_INSTANCE_FILE)"

	WS_PUBLISH_INSTANCE_FILE=$(ws_instance_file ws-publish-proxy $i)
	deploy_ws_proxy ws-publish-proxy $PORT $i $ID
	PORT=$(expr $PORT + 1)
	wait_file_creation $INSTANCES_DIR $(basename $WS_PUBLISH_INSTANCE_FILE)
	echo "      $(cat $WS_PUBLISH_INSTANCE_FILE)"

	WS_PUTGET_INSTANCE_FILE=$(ws_instance_file ws-putget-proxy $i)
	deploy_ws_proxy ws-putget-proxy $PORT $i $ID
	PORT=$(expr $PORT + 4)
	wait_file_creation $INSTANCES_DIR $(basename $WS_PUTGET_INSTANCE_FILE)
	echo "      $(cat $WS_PUTGET_INSTANCE_FILE)"
    done;
}

# $1 webservice name
# $2 webservice port number
# $3 eventcloud index associated
# $4 eventcloud url
function deploy_ws_proxy() {
    HTTP_PORT=$2
    PNP_PORT=$(expr $2 + 3)
    WS_INSTANCE_FILE=$(ws_instance_file $1 $3)

    . $BUNDLE_HOME/scripts/$1-launcher.sh $WS_INSTANCE_FILE $PNP_PORT $HTTP_PORT \
        --registry-url $EVENTCLOUDS_REGISTRY_URL --eventcloud-url $4 -p $HTTP_PORT &> $(ws_output_file $1 $3) &
}

# $1 is eventcloud index (1, 2, ...)
function eventcloud_instance_file() {
    echo "$INSTANCES_DIR/eventcloud$1"
}

# $1 is eventcloud index (1, 2, ...) 
function eventcloud_output_file() {
    echo "$OUTPUTS_DIR/eventcloud$1.output"
}

# $1 is webservice name 
# $2 is eventcloud index associated to webservice (1, 2, ...)
function ws_instance_file() {
    echo "$INSTANCES_DIR/$1$2"
}

# $1 is webservice name 
# $2 is eventcloud index associated to webservice (1, 2, ...)
function ws_output_file() {
    echo "$OUTPUTS_DIR/$1$2.output"
}

# $1 is an eventcloud URL
function remove_stream_prefix() {
    #echo $1 | sed s#http://streams.play-project.eu/#id#
    echo $(basename $1)
}

# $1 absolute path to directory where file creation is expected
# $2 expected filename
function wait_file_creation() {
    while [ 1 ]; 
    do 
	FILENAME=$(inotifywait --event create --format '%f' $1 2> /dev/null);
	if [[ $FILENAME = $2 ]]; 
	then 
	    break; 
	fi; 
    done 
}

function undeploy() {
    rm -rf $BUNDLE_HOME/repositories

    kill_process $REGISTRY_INSTANCE_FILE
    kill_process $INSTANCES_DIR/ws-eventclouds-management
  
    for ((i=1; i<=$NB_EVENTCLOUDS; i++));
    do
        kill_process $(eventcloud_instance_file $i)
        kill_process $(ws_instance_file ws-subscribe-proxy $i)
        kill_process $(ws_instance_file ws-publish-proxy $i)
        kill_process $(ws_instance_file ws-putget-proxy $i)
    done;

    rm -rf $INSTANCES_DIR
    rm -rf $OUTPUTS_DIR
}

# $1 an absolute path to an instance file
function kill_process() {
    if [[ -f $1 ]]
    then
	kill -9 $(cat $1.pid)
    fi
}

main $@

. $BUNDLE_HOME/scripts/configuration/unset-environment.sh