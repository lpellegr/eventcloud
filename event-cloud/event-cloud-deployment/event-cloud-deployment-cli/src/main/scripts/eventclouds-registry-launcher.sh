#!/bin/sh

. $EVENTCLOUD_BUNDLE_HOME/scripts/configuration/set-environment.sh

java -Xms128m -Xmx256m \
	 -server \
     -Deventcloud.bundle.home=$EVENTCLOUD_BUNDLE_HOME \
     -Djava.security.policy=$PATH_TO_RESOURCES/proactive.security.policy \
     -Dlog4j.configuration=file:$PATH_TO_RESOURCES/log4j.properties \
     -Dlogback.configurationFile=file:$PATH_TO_RESOURCES/logback.xml \
     -cp $CLASSPATH fr.inria.eventcloud.deployment.cli.launchers.EventCloudsRegistryLauncher $@

. $EVENTCLOUD_BUNDLE_HOME/scripts/configuration/unset-environment.sh
