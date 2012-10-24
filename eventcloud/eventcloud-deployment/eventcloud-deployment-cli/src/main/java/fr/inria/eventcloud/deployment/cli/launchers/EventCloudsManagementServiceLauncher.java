package fr.inria.eventcloud.deployment.cli.launchers;

import com.beust.jcommander.Parameter;

import fr.inria.eventcloud.webservices.deployment.WsDeployer;

public class EventCloudsManagementServiceLauncher extends Launcher {

    @Parameter(names = {"--registry-url", "-r"}, description = "EventClouds registry URL", required = true)
    private String registryUrl;

    @Parameter(names = {"--port", "-p"}, description = "Port on which to deploy the web service", required = true)
    private int port;

    public static void main(String[] args) {
        EventCloudsManagementServiceLauncher launcher =
                new EventCloudsManagementServiceLauncher();
        launcher.parseArguments(launcher, args);
        launcher.launch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String run() {
        return WsDeployer.getEndpointUrl(WsDeployer.deployEventCloudsManagementService(
                this.registryUrl,
                "proactive/services/eventclouds/management-webservices",
                this.port));
    }

}
