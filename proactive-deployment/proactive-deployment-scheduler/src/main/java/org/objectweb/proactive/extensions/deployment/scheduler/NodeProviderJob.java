/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.deployment.scheduler;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;

public class NodeProviderJob {
    private static final Logger logger =
            ProActiveLogger.getLogger(SchedulerLoggers.SCHEDULE);

    private static final Map<String, FileSystemServerDeployer> fileSystemServers;

    private final UniqueID nodeRequestID;

    private final Scheduler scheduler;

    private final JobId jobID;

    static {
        fileSystemServers = new HashMap<String, FileSystemServerDeployer>();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                stopAllFileSystemServers();
            }
        });
    }

    public NodeProviderJob(UniqueID nodeRequestID, Scheduler scheduler,
            int nbNodes, String userJarsLocation, List<String> jvmArguments,
            String registryURL, String nodeSourceName) throws Exception {
        this.nodeRequestID = nodeRequestID;
        this.scheduler = scheduler;
        this.jobID =
                this.scheduler.submit(this.createJob(
                        nbNodes, userJarsLocation, jvmArguments, registryURL,
                        nodeSourceName));

        if (logger.isDebugEnabled()) {
            logger.debug("Job #" + this.jobID.getReadableName()
                    + " for node request #" + this.nodeRequestID
                    + " has been submitted to the scheduler");
        }
    }

    private TaskFlowJob createJob(int nbNodes, String userJarsLocation,
                                  List<String> jvmArguments,
                                  String registryURL, String nodeSourceName)
            throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        String inputSpaceURL = startFileSystemServer(userJarsLocation);
        String[] userJars =
                this.getUserJarsList(new File(userJarsLocation)).toArray(
                        new String[] {});

        job.setName("NodeProviderJob");
        job.setInputSpace(inputSpaceURL);

        for (int i = 0; i < nbNodes; i++) {
            JavaTask task = new JavaTask();
            task.setName("NodeProviderTask " + (i + 1));
            task.setExecutableClassName(NodeProviderTask.class.getName());
            task.addArgument("registryURL", registryURL);
            task.addArgument("nodeRequestID", this.nodeRequestID);
            task.setRunAsMe(false);
            if (nodeSourceName != null) {
                task.setSelectionScript(this.createSelectionScript(nodeSourceName));
            }

            File addClasspathScriptPath =
                    new File(System.getProperty("scheduler.home")
                            + "/samples/scripts/misc/add_classpath.js");
            ForkEnvironment forkEnvironment = new ForkEnvironment();
            forkEnvironment.setWorkingDir(".");
            forkEnvironment.setEnvScript(new SimpleScript(
                    addClasspathScriptPath, userJars));
            if (jvmArguments != null) {
                for (String jvmArgument : jvmArguments) {
                    forkEnvironment.addJVMArgument(jvmArgument);
                }
            }
            task.setForkEnvironment(forkEnvironment);

            job.addTask(task);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Job for node request #" + this.nodeRequestID
                    + " has been created");
        }

        return job;
    }

    private List<String> getUserJarsList(File currentFile) {
        if (currentFile.isDirectory()) {
            List<String> userJars = new ArrayList<String>();

            File[] folders = currentFile.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            for (File folder : folders) {
                List<String> innerUserJars = this.getUserJarsList(folder);
                for (int i = 0; i < innerUserJars.size(); i++) {
                    innerUserJars.set(i, folder.getName()
                            + System.getProperty("file.separator")
                            + innerUserJars.get(i));
                }

                userJars.addAll(innerUserJars);
            }

            File[] jarFiles = currentFile.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            });
            for (File jarFile : jarFiles) {
                userJars.add(jarFile.getName());
            }

            return userJars;
        } else {
            return new ArrayList<String>();
        }
    }

    private SelectionScript createSelectionScript(String nodeSourceName)
            throws InvalidScriptException {
        String script =
                "try {        if (java.lang.System.getProperty('proactive.node.nodesource').equals('"
                        + nodeSourceName
                        + "')){                selected = true;} else {                selected = false;}} catch (error) {        selected = false;}";

        return new SelectionScript(script, "JavaScript");
    }

    public UniqueID getNodeRequestID() {
        return this.nodeRequestID;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public JobId getJobID() {
        return this.jobID;
    }

    public void killNodeProviderJob() {
        try {
            try {
                this.scheduler.killJob(this.jobID);
            } catch (NotConnectedException nce) {
                this.scheduler.renewSession();
                this.scheduler.killJob(this.jobID);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Node provider job #" + this.jobID.value()
                        + " for node request #" + this.nodeRequestID
                        + " has been terminated");
            }
        } catch (NotConnectedException nce) {
            logger.error("Cannot kill the node provider job #"
                    + this.jobID.value()
                    + " because connection to the scheduler has been lost", nce);
        } catch (UnknownJobException uje) {
            logger.error("Cannot kill the node provider job #"
                    + this.jobID.value() + " because job is unknown", uje);
        } catch (PermissionException pe) {
            logger.error("No permission to kill the node provider job #"
                    + jobID.value(), pe);
        }
    }

    private static String startFileSystemServer(final String userJarsLocation)
            throws IOException {
        if (fileSystemServers.containsKey(userJarsLocation)) {
            return fileSystemServers.get(userJarsLocation).getVFSRootURL();
        } else {
            try {
                FileSystemServerDeployer fileSystemServer =
                        new FileSystemServerDeployer(userJarsLocation, true);
                String url = fileSystemServer.getVFSRootURL();
                fileSystemServers.put(userJarsLocation, fileSystemServer);

                if (logger.isDebugEnabled()) {
                    logger.debug("ProActive dataserver successfully started. VFS URL of this provider: "
                            + url);
                }

                return url;
            } catch (IOException ioe) {
                logger.error("Cannot start ProActive dataserver: "
                        + ioe.getMessage(), ioe);
                throw ioe;
            }
        }
    }

    private static void stopFileSystemServer(String userJarsLocation) {
        if (fileSystemServers.containsKey(userJarsLocation)) {
            FileSystemServerDeployer fileSystemServer =
                    fileSystemServers.get(userJarsLocation);
            try {
                fileSystemServer.terminate();

                if (logger.isDebugEnabled()) {
                    logger.debug("ProActive dataserver at "
                            + fileSystemServer.getVFSRootURL()
                            + " has been terminated");
                }
            } catch (ProActiveException pae) {
                logger.error(
                        "Cannot properly terminate ProActive dataserver at "
                                + fileSystemServer.getVFSRootURL() + ": "
                                + pae.getMessage(), pae);
            }
        }
    }

    private static void stopAllFileSystemServers() {
        for (String userJarsLocation : fileSystemServers.keySet()) {
            stopFileSystemServer(userJarsLocation);
        }
    }
}
