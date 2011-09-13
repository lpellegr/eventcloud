#!/bin/sh

. $(dirname $0)/configuration/set-environment.sh

PORT=$1
shift

java -Xms128m -Xmx256m \
     -server \
     -Djava.security.policy=$PATH_TO_RESOURCES/proactive.security.policy \
     -Deventcloud.bundle.home=$PATH_TO_RESOURCES/../ \
     -Dlog4j.configuration=file:$PATH_TO_RESOURCES/log4j.properties \
     -Dlogback.configurationFile=file:$PATH_TO_RESOURCES/logback.xml \
     -Dproactive.communication.protocol=pnp \
     -Dproactive.pnp.port=$PORT \
     -cp $CLASSPATH fr.inria.eventcloud.deployment.cli.launchers.EventCloudsRegistryLauncher $@

. $(dirname $0)/configuration/unset-environment.sh
