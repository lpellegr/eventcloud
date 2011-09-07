#!/bin/sh

. $(dirname $0)/configuration/set-environment.sh

java -Xms256m -Xmx1024m \
     -server \
     -Djava.security.policy=$PATH_TO_RESOURCES/proactive.security.policy \
     -Deventcloud.bundle.home=$PATH_TO_RESOURCES/../ \
     -Dlog4j.configuration=file:$PATH_TO_RESOURCES/log4j.properties \
     -Dlogback.configurationFile=file:$PATH_TO_RESOURCES/logback.xml \
     -cp $CLASSPATH fr.inria.eventcloud.deployment.cli.launchers.EventCloudLauncher $@

. $(dirname $0)/configuration/unset-environment.sh
