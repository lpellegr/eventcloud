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
package org.ow2.proactive.scheduler.job.programming;

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
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;

/**
 * Job for {@link SchedulerNodeProvider}.
 * 
 * @author The ProActive Team
 */
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
            @Override
            public void run() {
                stopAllFileSystemServers();
            }
        });
    }

    /**
     * Constructs and submits a new {@link NodeProviderJob} for the specified
     * node request. <br>
     * It creates a job composed of a number of {@link NodeProviderTask tasks}
     * equals to the specified number of {@link Node nodes}. <br>
     * The {@link Node nodes} of the {@link NodeProviderTask tasks} are selected
     * by using the node source specified by the given name. <br>
     * The contents of the specified data folder is put in the input space of
     * the {@link NodeProviderTask tasks} and the jars contained in this folder
     * are used to enrich the classpath of the JVM of the
     * {@link NodeProviderTask tasks} on which the specified JVM arguments are
     * also set. <br>
     * The job is then submitted to the specified {@link Scheduler}.
     * 
     * @param nodeRequestID
     *            ID of the node request.
     * @param scheduler
     *            {@link Scheduler} on which the job must be submitted.
     * @param nbNodes
     *            Number of {@link Node nodes}.
     * @param dataFolder
     *            Folder containing the data.
     * @param jvmArguments
     *            JVM arguments.
     * @param registryURL
     *            URL of the {@link NodeProviderRegistry}.
     * @param nodeSourceName
     *            Name of the node source.
     * @throws NodeProviderException
     *             If any error occurs during the deployment of the job.
     */
    public NodeProviderJob(UniqueID nodeRequestID, Scheduler scheduler,
            int nbNodes, String dataFolder, List<String> jvmArguments,
            String registryURL, String nodeSourceName)
            throws NodeProviderException {
        try {
            this.nodeRequestID = nodeRequestID;
            this.scheduler = scheduler;
            this.jobID =
                    this.scheduler.submit(this.createJob(
                            nbNodes, dataFolder, jvmArguments, registryURL,
                            nodeSourceName));

            if (logger.isDebugEnabled()) {
                logger.debug("Job #" + this.jobID.getReadableName()
                        + " for node request #" + this.nodeRequestID
                        + " has been submitted to the scheduler");
            }
        } catch (IOException ioe) {
            throw new NodeProviderException(
                    "Cannot start ProActive dataserver for node request #"
                            + this.nodeRequestID + ": " + ioe.getMessage(), ioe);
        } catch (InvalidScriptException ise) {
            throw new NodeProviderException(
                    "Cannot create job for node request #" + this.nodeRequestID
                            + ": " + ise.getMessage(), ise);
        } catch (UserException ue) {
            throw new NodeProviderException(
                    "Cannot create job for node request #" + this.nodeRequestID
                            + ": " + ue.getMessage(), ue);
        } catch (NotConnectedException nce) {
            throw new NodeProviderException(
                    "Cannot submit job for node request #" + this.nodeRequestID
                            + ": " + nce.getMessage(), nce);
        } catch (PermissionException pe) {
            throw new NodeProviderException(
                    "Cannot submit job for node request #" + this.nodeRequestID
                            + ": " + pe.getMessage(), pe);
        } catch (SubmissionClosedException sce) {
            throw new NodeProviderException(
                    "Cannot submit job for node request #" + this.nodeRequestID
                            + ": " + sce.getMessage(), sce);
        } catch (JobCreationException jce) {
            throw new NodeProviderException(
                    "Cannot submit job for node request #" + this.nodeRequestID
                            + ": " + jce.getMessage(), jce);
        }
    }

    private TaskFlowJob createJob(int nbNodes, String dataFolder,
                                  List<String> jvmArguments,
                                  String registryURL, String nodeSourceName)
            throws IOException, InvalidScriptException, UserException {
        TaskFlowJob job = new TaskFlowJob();
        String inputSpaceURL = startFileSystemServer(dataFolder);
        String[] jarNames =
                this.getJarNames(new File(dataFolder)).toArray(new String[] {});

        job.setName("NodeProviderJob#" + this.nodeRequestID);
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
                    addClasspathScriptPath, jarNames));
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

    private List<String> getJarNames(File currentFile) {
        if (currentFile.isDirectory()) {
            List<String> jarNames = new ArrayList<String>();

            File[] folders = currentFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            for (File folder : folders) {
                List<String> innerJarNames = this.getJarNames(folder);
                for (int i = 0; i < innerJarNames.size(); i++) {
                    innerJarNames.set(i, folder.getName() + File.separator
                            + innerJarNames.get(i));
                }

                jarNames.addAll(innerJarNames);
            }

            File[] jarFiles = currentFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            });
            for (File jarFile : jarFiles) {
                jarNames.add(jarFile.getName());
            }

            return jarNames;
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

    /**
     * Kills the {@link NodeProviderJob}.
     */
    public void kill() {
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
                    + this.jobID.value() + " because the job is unknown", uje);
        } catch (PermissionException pe) {
            logger.error("No permission to kill the node provider job #"
                    + this.jobID.value(), pe);
        }
    }

    private static synchronized String startFileSystemServer(final String dataFolder)
            throws IOException {
        if (fileSystemServers.containsKey(dataFolder)) {
            return fileSystemServers.get(dataFolder).getVFSRootURL();
        } else {
            FileSystemServerDeployer fileSystemServer =
                    new FileSystemServerDeployer(dataFolder, true);
            String url = fileSystemServer.getVFSRootURL();
            fileSystemServers.put(dataFolder, fileSystemServer);

            if (logger.isDebugEnabled()) {
                logger.debug("ProActive dataserver successfully started. VFS URL of this provider: "
                        + url);
            }

            return url;
        }
    }

    private static synchronized void stopFileSystemServer(String dataFolder) {
        if (fileSystemServers.containsKey(dataFolder)) {
            FileSystemServerDeployer fileSystemServer =
                    fileSystemServers.get(dataFolder);
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
        for (String dataFolder : fileSystemServers.keySet()) {
            stopFileSystemServer(dataFolder);
        }
    }
}
