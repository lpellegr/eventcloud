package fr.inria.eventcloud.deployment.cli.launchers;

import com.beust.jcommander.Parameter;

import fr.inria.eventcloud.webservices.deployment.WebServiceDeployer;

public class EventCloudManagementWsLaucher extends Launcher {

    @Parameter(names = {"--registry-url", "-r"}, description = "Eventclouds registry URL", required = true)
    private String registryUrl;

    @Parameter(names = {"--port-lower-bound"}, description = "Port lower bound used to assign port to web services which are deployed", required = true)
    private int portLowerBound;

    @Parameter(names = {"--port", "-p"}, description = "Deploys the web service at the specified port", required = true)
    private int port;

    public static void main(String[] args) {
        EventCloudManagementWsLaucher launcher =
                new EventCloudManagementWsLaucher();
        launcher.parseArguments(launcher, args);
        launcher.launch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String run() {
        return WebServiceDeployer.deployEventCloudManagementWebService(
                this.registryUrl, this.portLowerBound,
                "proactive/services/EventCloud_management-webservices",
                this.port);
    }

}
