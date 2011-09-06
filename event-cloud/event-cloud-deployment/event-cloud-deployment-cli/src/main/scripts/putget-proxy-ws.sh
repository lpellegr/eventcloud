#!/bin/sh

. $EVENTCLOUD_BUNDLE_HOME/scripts/configuration/set-environment.sh

java -Xms64m -Xmx128m \
	 -server \
     -Deventcloud.bundle.home=$EVENTCLOUD_BUNDLE_HOME \
     -Dproactive.communication.protocol=pnp \
     -Dproactive.pnp.port=8889 \
     -Dproactive.http.port=8889 \
     -Djava.security.policy=$PATH_TO_RESOURCES/proactive.security.policy \
     -Dlog4j.configuration=file:$PATH_TO_RESOURCES/log4j.properties \
     -Dlogback.configurationFile=file:$PATH_TO_RESOURCES/logback.xml \
     -cp $CLASSPATH fr.inria.eventcloud.deployment.cli.launchers.WsPutGetProxyLauncher $@

. $EVENTCLOUD_BUNDLE_HOME/scripts/configuration/unset-environment.sh
