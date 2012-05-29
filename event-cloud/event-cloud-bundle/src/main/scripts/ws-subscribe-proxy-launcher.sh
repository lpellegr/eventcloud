#!/bin/sh

if [ ! "$PATH_TO_RESOURCES" ];
then
    LOAD_ENVIRONMENT=1
fi;

if [ "$LOAD_ENVIRONMENT" ];
then
    . $(dirname $0)/configuration/set-environment.sh
fi;

WS_INSTANCE_FILE=$1
PNP_PORT=$2
HTTP_PORT=$3
shift 3

java -Xms256m -Xmx512m \
     -server \
     -Djava.security.policy=$PATH_TO_RESOURCES/proactive.security.policy \
     -Deventcloud.bundle.home=$BUNDLE_HOME \
     -Deventcloud.configuration=$PATH_TO_RESOURCES/eventcloud.properties \
     -Deventcloud.instance.file=$WS_INSTANCE_FILE \
     -Dlog4j.configuration=file:$PATH_TO_RESOURCES/log4j.properties \
     -Dlogback.configurationFile=file:$PATH_TO_RESOURCES/logback.xml \
     -Dlogging.output.filename=$(basename $WS_INSTANCE_FILE) \
     -Dproactive.communication.protocol=pnp \
     -Dproactive.pnp.port=$PNP_PORT \
     -cp $CLASSPATH fr.inria.eventcloud.deployment.cli.launchers.SubscribeWsLauncher $@ &

echo $! > $WS_INSTANCE_FILE.pid

if [ "$LOAD_ENVIRONMENT" ];
then
    . $(dirname $0)/configuration/unset-environment.sh
fi;
