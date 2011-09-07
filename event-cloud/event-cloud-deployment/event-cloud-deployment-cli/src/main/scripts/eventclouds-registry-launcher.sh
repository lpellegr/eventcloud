#!/bin/sh

. configuration/set-environment.sh

java -Xms128m -Xmx256m \
	 -server \
     -Dproactive.communication.protocol=pnp \
     -Dproactive.pnp.port=1100 \
     -Djava.security.policy=$PATH_TO_RESOURCES/proactive.security.policy \
     -Dlog4j.configuration=file:$PATH_TO_RESOURCES/log4j.properties \
     -Dlogback.configurationFile=file:$PATH_TO_RESOURCES/logback.xml \
     -cp $CLASSPATH fr.inria.eventcloud.deployment.cli.launchers.EventCloudsRegistryLauncher $@

. configuration/unset-environment.sh
