#!/bin/sh

. $(dirname $0)/configuration/set-environment.sh

EVENTCLOUD_INSTANCE_FILE=$1
PORT=$2
shift 2

java -Xms256m -Xmx1024m \
     -server \
     -Djava.security.policy=$PATH_TO_RESOURCES/proactive.security.policy \
     -Deventcloud.bundle.home=$BUNDLE_HOME \
     -Deventcloud.instance.file=$EVENTCLOUD_INSTANCE_FILE \
     -Dlog4j.configuration=file:$PATH_TO_RESOURCES/log4j.properties \
     -Dlogback.configurationFile=file:$PATH_TO_RESOURCES/logback.xml \
     -Dlogging.output.filename=$(basename $EVENTCLOUD_INSTANCE_FILE) \
     -Dproactive.communication.protocol=pnp \
     -Dproactive.pnp.port=$PORT \
     -cp $CLASSPATH fr.inria.eventcloud.deployment.cli.launchers.EventCloudLauncher $@ &

echo $! > $EVENTCLOUD_INSTANCE_FILE.pid

. $(dirname $0)/configuration/unset-environment.sh
