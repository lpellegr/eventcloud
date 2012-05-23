/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
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
import org.apache.cxf.endpoint.Server;
import org.objectweb.proactive.api.PAActiveObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.webservices.deployment.WebServiceDeployer;

public class EventCloudManagementWsDeployer {

    private static final String LIB_DIR_URL =
            "http://eventcloud.inria.fr/binaries/libs/";

    private static final String RESOURCES_DIR_URL =
            "http://eventcloud.inria.fr/binaries/resources/";

    private static final String EVENTCLOUD_WS_START_PORT_PROPERTY_NAME =
            "eventcloud.ws.start.port";

    private static final String EVENTCLOUD_MANAGEMENT_WS_NAME =
            "EventCloudManagementWs";

    private static Process eventCloudManagementWsProcess = null;

    private static String libDirPath;

    private static String resourcesDirPath;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                EventCloudManagementWsDeployer.destroy();
            }
        });
    }

    public static String deploy(int eventCloudWsStartPort) throws IOException {
        return deploy(eventCloudWsStartPort, true);
    }

    public static String deploy(int eventCloudWsStartPort,
                                boolean activateLoggers) throws IOException {
        if (eventCloudManagementWsProcess == null) {
            List<String> cmd = new ArrayList<String>();

            if (System.getProperty("os.name").startsWith("Windows")) {
                cmd.add(System.getProperty("java.home") + File.separator
                        + "bin" + File.separator + "java.exe");
            } else {
                cmd.add(System.getProperty("java.home") + File.separator
                        + "bin" + File.separator + "java");
            }

            cmd.add("-cp");
            cmd.add(addClassPath());

            cmd.addAll(addProperties(activateLoggers));

            cmd.add("-D" + EVENTCLOUD_WS_START_PORT_PROPERTY_NAME + "="
                    + eventCloudWsStartPort);

            cmd.add(EventCloudManagementWsDeployer.class.getCanonicalName());

            final ProcessBuilder processBuilder =
                    new ProcessBuilder(cmd.toArray(new String[cmd.size()]));
            processBuilder.redirectErrorStream(true);
            eventCloudManagementWsProcess = processBuilder.start();

            final BufferedReader reader =
                    new BufferedReader(new InputStreamReader(
                            eventCloudManagementWsProcess.getInputStream()));
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    String line = null;
                    try {
                        while ((line = reader.readLine()) != null) {
                            System.out.println("ECManagement " + line);
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            });
            t.setDaemon(true);
            t.start();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            StringBuilder eventCloudManagementWebServiceEndpoint =
                    new StringBuilder("http://");
            try {
                eventCloudManagementWebServiceEndpoint.append(InetAddress.getLocalHost()
                        .getHostAddress());
            } catch (UnknownHostException uhe) {
                uhe.printStackTrace();
            }
            eventCloudManagementWebServiceEndpoint.append(":");
            eventCloudManagementWebServiceEndpoint.append(eventCloudWsStartPort);
            eventCloudManagementWebServiceEndpoint.append("/");
            eventCloudManagementWebServiceEndpoint.append(EVENTCLOUD_MANAGEMENT_WS_NAME);
            return eventCloudManagementWebServiceEndpoint.toString();
        } else {
            throw new IllegalStateException(
                    "Event Cloud management process already deployed");
        }
    }

    private static String addClassPath() throws IOException {
        downloadLibs();

        File libDir = new File(libDirPath);
        String[] libNames = libDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".jar")) {
                    return true;
                } else {
                    return false;
                }
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

    private static void downloadLibs() throws IOException {
        libDirPath = System.getProperty("java.io.tmpdir") + "eventcloud-libs";
        File tmpLibDir = new File(libDirPath);
        tmpLibDir.mkdir();

        File readme = new File(tmpLibDir, "README");
        FileUtils.copyURLToFile(new URL(LIB_DIR_URL + "README"), readme);

        String[] libNames = FileUtils.readFileToString(readme).split("\n");
        for (String libName : libNames) {
            FileUtils.copyURLToFile(new URL(LIB_DIR_URL + libName), new File(
                    tmpLibDir, libName));
        }
    }

    private static List<String> addProperties(boolean activateLoggers)
            throws IOException {
        downloadResources(activateLoggers);

        List<String> properties = new ArrayList<String>();

        properties.add("-Djava.security.policy=" + resourcesDirPath
                + File.separator + "proactive.java.policy");

        if (activateLoggers) {
            properties.add("-Dlog4j.configuration=file:" + resourcesDirPath
                    + File.separator + "log4j.properties");

            properties.add("-Dlogback.configurationFile=file:"
                    + resourcesDirPath + File.separator + "logback.xml");
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

    private static void downloadResources(boolean activateLoggers)
            throws IOException {
        resourcesDirPath =
                System.getProperty("java.io.tmpdir") + "eventcloud-resources";
        File tmpResourcesDir = new File(resourcesDirPath);
        tmpResourcesDir.mkdir();

        FileUtils.copyURLToFile(new URL(RESOURCES_DIR_URL
                + "proactive.java.policy"), new File(
                tmpResourcesDir, "proactive.java.policy"));

        if (activateLoggers) {
            FileUtils.copyURLToFile(new URL(RESOURCES_DIR_URL
                    + "log4j.properties"), new File(
                    tmpResourcesDir, "log4j.properties"));

            FileUtils.copyURLToFile(
                    new URL(RESOURCES_DIR_URL + "logback.xml"), new File(
                            tmpResourcesDir, "logback.xml"));
        } else {
            FileUtils.copyURLToFile(new URL(RESOURCES_DIR_URL
                    + "log4j-inactive.properties"), new File(
                    tmpResourcesDir, "log4j-inactive.properties"));

            FileUtils.copyURLToFile(new URL(RESOURCES_DIR_URL
                    + "logback-inactive.xml"), new File(
                    tmpResourcesDir, "logback-inactive.xml"));
        }

        FileUtils.copyURLToFile(new URL(RESOURCES_DIR_URL
                + "eventcloud.properties"), new File(
                tmpResourcesDir, "eventcloud.properties"));
    }

    public static void destroy() {
        if (eventCloudManagementWsProcess != null) {
            eventCloudManagementWsProcess.destroy();
            eventCloudManagementWsProcess = null;
            try {
                FileUtils.deleteDirectory(new File(libDirPath));
                FileUtils.deleteDirectory(new File(resourcesDirPath));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Logger log =
                LoggerFactory.getLogger(EventCloudManagementWsDeployer.class);

        EventCloudsRegistry eventCloudsRegistry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();

        int startPort =
                Integer.parseInt(System.getProperty(EVENTCLOUD_WS_START_PORT_PROPERTY_NAME));
        Server eventCloudManagementWebService =
                WebServiceDeployer.deployEventCloudManagementWebService(
                        PAActiveObject.getUrl(eventCloudsRegistry),
                        startPort + 1, EVENTCLOUD_MANAGEMENT_WS_NAME, startPort);

        log.info("Event Cloud management web service deployed at "
                + eventCloudManagementWebService.getEndpoint()
                        .getEndpointInfo()
                        .getAddress());
    }

}
