#!/bin/sh

. $(dirname $0)/configuration/set-environment.sh

PNP_PORT=$1
HTTP_PORT=$2
shift
shift

java -Xms64m -Xmx128m \
     -server \
     -Djava.security.policy=$PATH_TO_RESOURCES/proactive.security.policy \
     -Deventcloud.bundle.home=$PATH_TO_RESOURCES/../ \
     -Dlog4j.configuration=file:$PATH_TO_RESOURCES/log4j.properties \
     -Dlogback.configurationFile=file:$PATH_TO_RESOURCES/logback.xml \
     -Dproactive.communication.protocol=pnp \
     -Dproactive.pnp.port=$PNP_PORT \
     -Dproactive.http.port=$HTTP_PORT \
     -cp $CLASSPATH fr.inria.eventcloud.deployment.cli.launchers.WsPutGetProxyLauncher $@

. $(dirname $0)/configuration/unset-environment.sh
     