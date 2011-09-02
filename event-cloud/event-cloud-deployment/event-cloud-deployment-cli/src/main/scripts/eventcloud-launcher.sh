#!/bin/sh

. $EVENTCLOUD_BUNDLE_HOME/scripts/configuration/set-environment.sh

java -Xms256m -Xmx1024m \
	 -server \
	 -Deventcloud.bundle.home=$EVENTCLOUD_BUNDLE_HOME \
	 -Dproactive.communication.protocol=pnp \
     -Dproactive.pnp.port=1100 \
     -Djava.security.policy=$PATH_TO_RESOURCES/proactive.security.policy \
     -Dlog4j.configuration=file:$PATH_TO_RESOURCES/log4j.properties \
     -Dlogback.configurationFile=file:$PATH_TO_RESOURCES/logback.xml \
     -cp $CLASSPATH fr.inria.eventcloud.deployment.cli.launchers.EventCloudLauncher $@

. $EVENTCLOUD_BUNDLE_HOME/scripts/configuration/unset-environment.sh
