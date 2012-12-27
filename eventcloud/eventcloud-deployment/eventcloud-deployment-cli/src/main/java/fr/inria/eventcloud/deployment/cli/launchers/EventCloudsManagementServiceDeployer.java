/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.deployment.cli.launchers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.cxf.endpoint.Server;
import org.objectweb.proactive.api.PAActiveObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.webservices.deployment.WsDeployer;

public class EventCloudsManagementServiceDeployer {

    private static final String EVENTCLOUD_BINARIES_URL =
            "http://eventcloud.inria.fr/binaries/";

    private static final String LOG_MANAGEMENT_WS_DEPLOYED =
            "EventCloud management web service deployed at ";

    private static MutableBoolean servicesDeployed = new MutableBoolean(false);

    private static Process eventCloudsManagementServiceProcess = null;

    private static String libDirPath;

    private static String resourcesDirPath;

    private static String repositoriesDirPath;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                EventCloudsManagementServiceDeployer.destroy();
            }
        });
    }

    public static String deploy(boolean onRelease, int port, String urlSuffix,
                                boolean activateLoggers) throws IOException {
        return deploy(
                onRelease, port, urlSuffix, null,
                EventCloudProperties.SOCIAL_FILTER_THRESHOLD.getValue(),
                activateLoggers);
    }

    public synchronized static String deploy(boolean onRelease, int port,
                                             String urlSuffix,
                                             String socialFilterUrl,
                                             double socialFilterThreshold,
                                             boolean activateLoggers)
            throws IOException {
        if (eventCloudsManagementServiceProcess == null) {
            String binariesBaseUrl = EVENTCLOUD_BINARIES_URL;

            if (onRelease) {
                binariesBaseUrl += "releases/latest/";
            } else {
                binariesBaseUrl += "snapshots/latest/";
            }

            List<String> cmd = new ArrayList<String>();

            String javaBinaryPath =
                    System.getProperty("java.home") + File.separator + "bin"
                            + File.separator + "java";

            if (System.getProperty("os.name").startsWith("Windows")) {
                javaBinaryPath = javaBinaryPath + ".exe";
            }

            cmd.add(javaBinaryPath);

            cmd.add("-cp");
            cmd.add(addClassPath(binariesBaseUrl + "libs/"));

            cmd.addAll(addProperties(
                    binariesBaseUrl + "resources/", activateLoggers));

            cmd.add(EventCloudsManagementServiceDeployer.class.getCanonicalName());
            cmd.add(Integer.toString(port));
            cmd.add(urlSuffix);
            cmd.add(Double.toString(socialFilterThreshold));

            if (socialFilterUrl != null && !socialFilterUrl.isEmpty()) {
                cmd.add(socialFilterUrl);
            }

            final ProcessBuilder processBuilder =
                    new ProcessBuilder(cmd.toArray(new String[cmd.size()]));
            processBuilder.redirectErrorStream(true);
            eventCloudsManagementServiceProcess = processBuilder.start();

            final BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    eventCloudsManagementServiceProcess.getInputStream()));
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    String line = null;
                    try {
                        while ((line = reader.readLine()) != null) {
                            if (!servicesDeployed.getValue()
                                    && line.contains(LOG_MANAGEMENT_WS_DEPLOYED)) {
                                servicesDeployed.setValue(true);
                                synchronized (servicesDeployed) {
                                    servicesDeployed.notifyAll();
                                }
                            }
                            System.out.println("ECManagement " + line);
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            });
            t.setDaemon(true);
            t.start();

            synchronized (servicesDeployed) {
                while (!servicesDeployed.getValue()) {
                    try {
                        servicesDeployed.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            StringBuilder eventCloudsManagementWsEndpoint =
                    new StringBuilder("http://");
            try {
                eventCloudsManagementWsEndpoint.append(InetAddress.getLocalHost()
                        .getHostAddress());
            } catch (UnknownHostException uhe) {
                uhe.printStackTrace();
            }
            eventCloudsManagementWsEndpoint.append(':');
            eventCloudsManagementWsEndpoint.append(port);
            eventCloudsManagementWsEndpoint.append('/');
            eventCloudsManagementWsEndpoint.append(urlSuffix);
            return eventCloudsManagementWsEndpoint.toString();
        } else {
            throw new IllegalStateException(
                    "EventCloud management process already deployed");
        }
    }

    private static String addClassPath(String libsUrl) throws IOException {
        downloadLibs(libsUrl);

        File libDir = new File(libDirPath);
        String[] libNames = libDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        StringBuilder classPath = new StringBuilder();
        for (String libName : libNames) {
            classPath.append(libDirPath + File.separator + libName
                    + File.pathSeparator);
        }
        classPath.delete(classPath.length() - 2, classPath.length() - 1);

        return classPath.toString();
    }

    private static void downloadLibs(String libsUrl) throws IOException {
        libDirPath =
                System.getProperty("java.io.tmpdir") + File.separator
                        + "eventcloud-libs";
        File tmpLibDir = new File(libDirPath);
        tmpLibDir.mkdir();

        File readme = new File(tmpLibDir, "README");
        FileUtils.copyURLToFile(new URL(libsUrl + "README"), readme);

        String[] libNames = FileUtils.readFileToString(readme).split("\n");
        for (String libName : libNames) {
            FileUtils.copyURLToFile(new URL(libsUrl + libName), new File(
                    tmpLibDir, libName));
        }
    }

    private static List<String> addProperties(String resourcesUrl,
                                              boolean activateLoggers)
            throws IOException {
        downloadResources(resourcesUrl, activateLoggers);

        List<String> properties = new ArrayList<String>();

        properties.add("-Djava.security.policy=" + resourcesDirPath
                + File.separator + "proactive.security.policy");

        if (activateLoggers) {
            properties.add("-Dlog4j.configuration=file:" + resourcesDirPath
                    + File.separator + "log4j-console.properties");

            properties.add("-Dlogback.configurationFile=file:"
                    + resourcesDirPath + File.separator + "logback-console.xml");
        } else {
            properties.add("-Dlog4j.configuration=file:" + resourcesDirPath
                    + File.separator + "log4j-inactive.properties");

            properties.add("-Dlogback.configurationFile=file:"
                    + resourcesDirPath + File.separator
                    + "logback-inactive.xml");
        }

        properties.add("-Deventcloud.configuration=" + resourcesDirPath
                + File.separator + "eventcloud.properties");

        return properties;
    }

    private static void downloadResources(String resourcesUrl,
                                          boolean activateLoggers)
            throws IOException {
        resourcesDirPath =
                System.getProperty("java.io.tmpdir") + File.separator
                        + "eventcloud-resources";

        File tmpResourcesDir = new File(resourcesDirPath);
        tmpResourcesDir.mkdir();

        FileUtils.copyURLToFile(new URL(resourcesUrl
                + "proactive.security.policy"), new File(
                tmpResourcesDir, "proactive.security.policy"));

        if (activateLoggers) {
            FileUtils.copyURLToFile(new URL(resourcesUrl
                    + "log4j-console.properties"), new File(
                    tmpResourcesDir, "log4j-console.properties"));

            FileUtils.copyURLToFile(new URL(resourcesUrl
                    + "logback-console.xml"), new File(
                    tmpResourcesDir, "logback-console.xml"));
        } else {
            FileUtils.copyURLToFile(new URL(resourcesUrl
                    + "log4j-inactive.properties"), new File(
                    tmpResourcesDir, "log4j-inactive.properties"));

            FileUtils.copyURLToFile(new URL(resourcesUrl
                    + "logback-inactive.xml"), new File(
                    tmpResourcesDir, "logback-inactive.xml"));
        }

        FileUtils.copyURLToFile(
                new URL(resourcesUrl + "eventcloud.properties"), new File(
                        tmpResourcesDir, "eventcloud.properties"));
        List<String> eventCloudProperties =
                FileUtils.readLines(new File(resourcesDirPath + File.separator
                        + "eventcloud.properties"));
        for (String property : eventCloudProperties) {
            if (property.startsWith(EventCloudProperties.REPOSITORIES_PATH.getName()
                    + "=")) {
                repositoriesDirPath =
                        property.substring(property.indexOf('=') + 1);
            }
        }
    }

    public synchronized static void destroy() {
        if (eventCloudsManagementServiceProcess != null) {
            eventCloudsManagementServiceProcess.destroy();
            eventCloudsManagementServiceProcess = null;

            try {
                FileUtils.deleteDirectory(new File(libDirPath));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            libDirPath = null;

            try {
                FileUtils.deleteDirectory(new File(resourcesDirPath));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            resourcesDirPath = null;

            try {
                FileUtils.deleteDirectory(new File(repositoriesDirPath));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            repositoriesDirPath = null;
        }
    }

    public static void main(String[] args) {
        Logger log =
                LoggerFactory.getLogger(EventCloudsManagementServiceDeployer.class);

        if (args.length < 3 || args.length > 4) {
            log.error("Usage: main port url_suffix social_filter_threshold [social_filter_url]");
            System.exit(1);
        }

        EventCloudProperties.SOCIAL_FILTER_THRESHOLD.setValue(Double.parseDouble(args[2]));

        if (args.length == 4) {
            EventCloudProperties.SOCIAL_FILTER_URL.setValue(args[3]);
            log.info(
                    "Property 'eventcloud.socialfilter.url' set to value '{}'",
                    args[2]);
        }

        EventCloudsRegistry registry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();

        int port = Integer.parseInt(args[0]);
        Server eventCloudsManagementService =
                WsDeployer.deployEventCloudsManagementService(
                        PAActiveObject.getUrl(registry), args[1], port);

        log.info(LOG_MANAGEMENT_WS_DEPLOYED
                + eventCloudsManagementService.getEndpoint()
                        .getEndpointInfo()
                        .getAddress());
    }

}
