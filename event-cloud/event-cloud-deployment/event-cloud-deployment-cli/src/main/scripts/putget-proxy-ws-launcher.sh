#!/bin/sh

. $(dirname $0)/configuration/set-environment.sh

java -Xms64m -Xmx128m \
     -server \
     -Djava.security.policy=$PATH_TO_RESOURCES/proactive.security.policy \
     -Deventcloud.bundle.home=$PATH_TO_RESOURCES/../ \
     -Dlog4j.configuration=file:$PATH_TO_RESOURCES/log4j.properties \
     -Dlogback.configurationFile=file:$PATH_TO_RESOURCES/logback.xml \
     -Dproactive.communication.protocol=pnp \
     -Dproactive.pnp.port=8889 \
     -Dproactive.http.port=8892 \
     -cp $CLASSPATH fr.inria.eventcloud.deployment.cli.launchers.WsPutGetProxyLauncher $@

. $(dirname $0)/configuration/unset-environment.sh
     