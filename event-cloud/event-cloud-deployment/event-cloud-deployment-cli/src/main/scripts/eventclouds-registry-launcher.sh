#!/bin/sh

. $(dirname $0)/configuration/set-environment.sh

REGISTRY_INSTANCE_FILE=$1
PORT=$2
shift 2

java -Xms128m -Xmx256m \
     -server \
     -Djava.security.policy=$PATH_TO_RESOURCES/proactive.security.policy \
     -Deventcloud.bundle.home=$BUNDLE_HOME \
     -Deventcloud.instance.file=$REGISTRY_INSTANCE_FILE \
     -Dlog4j.configuration=file:$PATH_TO_RESOURCES/log4j.properties \
     -Dlogback.configurationFile=file:$PATH_TO_RESOURCES/logback.xml \
     -Dlogging.output.filename=$(basename $REGISTRY_INSTANCE_FILE) \
     -Dproactive.communication.protocol=pnp \
     -Dproactive.pnp.port=$PORT \
     -cp $CLASSPATH fr.inria.eventcloud.deployment.cli.launchers.EventCloudsRegistryLauncher $@ &

echo $! > $REGISTRY_INSTANCE_FILE.pid

. $(dirname $0)/configuration/unset-environment.sh
