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

import java.security.KeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.common.exception.ConnectionException;

/**
 * This class provides methods to easily acquire {@link Node nodes} or
 * {@link GCMVirtualNode GCMVirtualNodes} from a ProActive Scheduler in order to
 * deploy a ProActive Programming application. <br>
 * To acquire {@link Node nodes} from a ProActive Scheduler, a
 * {@link NodeProviderJob} is created and submitted to the ProActive Scheduler
 * with some specifics configurations and options (which allow for instance to
 * enrich the classpath of the {@link Node nodes} with some jars specific to the
 * ProActive Programming application the user wants to deploy). <br>
 * Once the deployment of the {@link NodeProviderJob} is finished, the provided
 * list of {@link Node nodes} or {@link GCMVirtualNode} can be then used to
 * deploy a ProActive Programming application with the usual API.
 * 
 * @author The ProActive Team
 */
@PublicAPI
public class SchedulerNodeProvider {
    private static final Logger logger =
            ProActiveLogger.getLogger(SchedulerNodeProvider.class);

    private static final String NODES_AQUISITION_TIMEOUT_PROPERTY =
            "scheduler.node.provider.timeout";

    private static final int DEFAULT_NODES_AQUISITION_TIMEOUT = 600000;

    private final NodeProviderRegistry registry;

    private final Map<String, Scheduler> schedulers;

    private final Map<UniqueID, NodeProviderJob> nodeProviderJobs;

    private final int nodesAquisitionTimeout;

    /**
     * Constructs a new {@link SchedulerNodeProvider}.
     * 
     * @throws ProActiveException
     *             If an error occurs during the instantiation of the
     *             {@link NodeProviderRegistry}.
     */
    public SchedulerNodeProvider() throws ProActiveException {
        this.registry =
                (NodeProviderRegistry) PAActiveObject.newActive(
                        NodeProviderRegistry.class.getName(), new Object[0]);
        PAActiveObject.registerByName(this.registry, "NODE_PROVIDER_REGISTRY_"
                + (new UniqueID()).getCanonString());
        this.schedulers = new HashMap<String, Scheduler>();
        this.nodeProviderJobs = new HashMap<UniqueID, NodeProviderJob>();
        this.nodesAquisitionTimeout =
                (System.getProperty(NODES_AQUISITION_TIMEOUT_PROPERTY) != null)
                        ? Integer.parseInt(System.getProperty(NODES_AQUISITION_TIMEOUT_PROPERTY))
                        : DEFAULT_NODES_AQUISITION_TIMEOUT;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                SchedulerNodeProvider.this.releaseAllNodes();
            }
        });
    }

    /**
     * Submits a node request to acquire the specified number of {@link Node
     * nodes} by using the specified scheduler information (URL, username and
     * password). <br>
     * The contents of the specified data folder will be put in the input space
     * of the {@link Node nodes} and the jars contained in this folder will be
     * used to enrich the classpath of the {@link Node nodes}.
     * 
     * @param schedulerURL
     *            URL of the scheduler.
     * @param username
     *            Username to connect to the scheduler.
     * @param password
     *            Password to connect to the scheduler.
     * @param numberNodes
     *            Number of {@link Node nodes}.
     * @param dataFolder
     *            Folder containing the data to transfer to the {@link Node
     *            nodes}.
     * @return ID of the node request.
     * @throws NodeProviderException
     *             If an error occurs during the deployment.
     */
    public UniqueID submitNodeRequest(String schedulerURL, String username,
                                      String password, int numberNodes,
                                      String dataFolder)
            throws NodeProviderException {
        return this.submitNodeRequest(
                schedulerURL, username, password, numberNodes, dataFolder,
                null, (String[]) null);
    }

    /**
     * Submits a node request to acquire the specified number of {@link Node
     * nodes} by using the specified scheduler information (URL, username and
     * password). <br>
     * The contents of the specified data folder will be put in the input space
     * of the {@link Node nodes} and the jars contained in this folder will be
     * used to enrich the classpath of the {@link Node nodes}. <br>
     * The specified JVM arguments will be set to the {@link Node nodes}.
     * 
     * @param schedulerURL
     *            URL of the scheduler.
     * @param username
     *            Username to connect to the scheduler.
     * @param password
     *            Password to connect to the scheduler.
     * @param numberNodes
     *            Number of {@link Node nodes}.
     * @param dataFolder
     *            Folder containing the data to transfer to the {@link Node
     *            nodes}.
     * @param jvmArguments
     *            JVM arguments.
     * @return ID of the node request.
     * @throws NodeProviderException
     *             If an error occurs during the deployment.
     */
    public UniqueID submitNodeRequest(String schedulerURL, String username,
                                      String password, int numberNodes,
                                      String dataFolder,
                                      List<String> jvmArguments)
            throws NodeProviderException {
        return this.submitNodeRequest(
                this.connectToScheduler(schedulerURL, username, password),
                numberNodes, dataFolder, jvmArguments);
    }

    /**
     * Submits a node request to acquire the specified number of {@link Node
     * nodes} coming from the specified node sources by using the specified
     * scheduler information (URL, username and password). <br>
     * The contents of the specified data folder will be put in the input space
     * of the {@link Node nodes} and the jars contained in this folder will be
     * used to enrich the classpath of the {@link Node nodes}.
     * 
     * @param schedulerURL
     *            URL of the scheduler.
     * @param username
     *            Username to connect to the scheduler.
     * @param password
     *            Password to connect to the scheduler.
     * @param numberNodes
     *            Number of {@link Node nodes}.
     * @param dataFolder
     *            Folder containing the data to transfer to the {@link Node
     *            nodes}.
     * @param nodeSourceNames
     *            Names of the node sources.
     * @return ID of the node request.
     * @throws NodeProviderException
     *             If an error occurs during the deployment.
     */
    public UniqueID submitNodeRequest(String schedulerURL, String username,
                                      String password, int numberNodes,
                                      String dataFolder,
                                      String... nodeSourceNames)
            throws NodeProviderException {
        return this.submitNodeRequest(
                this.connectToScheduler(schedulerURL, username, password),
                numberNodes, dataFolder, null, nodeSourceNames);
    }

    /**
     * Submits a node request to acquire the specified number of {@link Node
     * nodes} coming from the specified node sources by using the specified
     * scheduler information (URL, username and password). <br>
     * The contents of the specified data folder will be put in the input space
     * of the {@link Node nodes} and the jars contained in this folder will be
     * used to enrich the classpath of the {@link Node nodes}. <br>
     * The specified JVM arguments will be set to the {@link Node nodes}.
     * 
     * @param schedulerURL
     *            URL of the scheduler.
     * @param username
     *            Username to connect to the scheduler.
     * @param password
     *            Password to connect to the scheduler.
     * @param numberNodes
     *            Number of {@link Node nodes}.
     * @param dataFolder
     *            Folder containing the data to transfer to the {@link Node
     *            nodes}.
     * @param jvmArguments
     *            JVM arguments.
     * @param nodeSourceNames
     *            Names of the node sources.
     * @return ID of the node request.
     * @throws NodeProviderException
     *             If an error occurs during the deployment.
     */
    public UniqueID submitNodeRequest(String schedulerURL, String username,
                                      String password, int numberNodes,
                                      String dataFolder,
                                      List<String> jvmArguments,
                                      String... nodeSourceNames)
            throws NodeProviderException {
        return this.submitNodeRequest(
                this.connectToScheduler(schedulerURL, username, password),
                numberNodes, dataFolder, jvmArguments, nodeSourceNames);
    }

    /**
     * Submits a node request to acquire the specified number of {@link Node
     * nodes} by using the specified scheduler information (URL, credentials). <br>
     * The contents of the specified data folder will be put in the input space
     * of the {@link Node nodes} and the jars contained in this folder will be
     * used to enrich the classpath of the {@link Node nodes}.
     * 
     * @param schedulerURL
     *            URL of the scheduler.
     * @param credentialsPath
     *            Path of the credentials to connect to the scheduler.
     * @param numberNodes
     *            Number of {@link Node nodes}.
     * @param dataFolder
     *            Folder containing the data to transfer to the {@link Node
     *            nodes}.
     * @return ID of the node request.
     * @throws NodeProviderException
     *             If an error occurs during the deployment.
     */
    public UniqueID submitNodeRequest(String schedulerURL,
                                      String credentialsPath, int numberNodes,
                                      String dataFolder)
            throws NodeProviderException {
        return this.submitNodeRequest(
                schedulerURL, credentialsPath, numberNodes, dataFolder, null,
                (String[]) null);
    }

    /**
     * Submits a node request to acquire the specified number of {@link Node
     * nodes} by using the specified scheduler information (URL, credentials). <br>
     * The contents of the specified data folder will be put in the input space
     * of the {@link Node nodes} and the jars contained in this folder will be
     * used to enrich the classpath of the {@link Node nodes}. <br>
     * The specified JVM arguments will be set to the {@link Node nodes}.
     * 
     * @param schedulerURL
     *            URL of the scheduler.
     * @param credentialsPath
     *            Path of the credentials to connect to the scheduler.
     * @param numberNodes
     *            Number of {@link Node nodes}.
     * @param dataFolder
     *            Folder containing the data to transfer to the {@link Node
     *            nodes}.
     * @param jvmArguments
     *            JVM arguments.
     * @return ID of the node request.
     * @throws NodeProviderException
     *             If an error occurs during the deployment.
     */
    public UniqueID submitNodeRequest(String schedulerURL,
                                      String credentialsPath, int numberNodes,
                                      String dataFolder,
                                      List<String> jvmArguments)
            throws NodeProviderException {
        return this.submitNodeRequest(
                schedulerURL, credentialsPath, numberNodes, dataFolder,
                jvmArguments, new String[0]);
    }

    /**
     * Submits a node request to acquire the specified number of {@link Node
     * nodes} coming from the specified node sources by using the specified
     * scheduler information (URL, credentials). <br>
     * The contents of the specified data folder will be put in the input space
     * of the {@link Node nodes} and the jars contained in this folder will be
     * used to enrich the classpath of the {@link Node nodes}.
     * 
     * @param schedulerURL
     *            URL of the scheduler.
     * @param credentialsPath
     *            Path of the credentials to connect to the scheduler.
     * @param numberNodes
     *            Number of {@link Node nodes}.
     * @param dataFolder
     *            Folder containing the data to transfer to the {@link Node
     *            nodes}.
     * @param nodeSourceNames
     *            Names of the node sources.
     * @return ID of the node request.
     * @throws NodeProviderException
     *             If an error occurs during the deployment.
     */
    public UniqueID submitNodeRequest(String schedulerURL,
                                      String credentialsPath, int numberNodes,
                                      String dataFolder,
                                      String... nodeSourceNames)
            throws NodeProviderException {
        return this.submitNodeRequest(
                schedulerURL, credentialsPath, numberNodes, dataFolder, null,
                nodeSourceNames);
    }

    /**
     * Submits a node request to acquire the specified number of {@link Node
     * nodes} coming from the specified node sources by using the specified
     * scheduler information (URL, credentials). <br>
     * The contents of the specified data folder will be put in the input space
     * of the {@link Node nodes} and the jars contained in this folder will be
     * used to enrich the classpath of the {@link Node nodes}. <br>
     * The specified JVM arguments will be set to the {@link Node nodes}.
     * 
     * @param schedulerURL
     *            URL of the scheduler.
     * @param credentialsPath
     *            Path of the credentials to connect to the scheduler.
     * @param numberNodes
     *            Number of {@link Node nodes}.
     * @param dataFolder
     *            Folder containing the data to transfer to the {@link Node
     *            nodes}.
     * @param jvmArguments
     *            JVM arguments.
     * @param nodeSourceNames
     *            Names of the node sources.
     * @return ID of the node request.
     * @throws NodeProviderException
     *             If an error occurs during the deployment.
     */
    public UniqueID submitNodeRequest(String schedulerURL,
                                      String credentialsPath, int numberNodes,
                                      String dataFolder,
                                      List<String> jvmArguments,
                                      String... nodeSourceNames)
            throws NodeProviderException {
        return this.submitNodeRequest(
                this.connectToScheduler(schedulerURL, credentialsPath),
                numberNodes, dataFolder, jvmArguments, nodeSourceNames);
    }

    private synchronized Scheduler connectToScheduler(String schedulerURL,
                                                      String username,
                                                      String password)
            throws NodeProviderException {
        if (this.schedulers.containsKey(schedulerURL)) {
            return this.schedulers.get(schedulerURL);
        }

        try {
            SchedulerAuthenticationInterface sai =
                    SchedulerConnection.join(schedulerURL);
            PublicKey pk = sai.getPublicKey();
            Scheduler scheduler = null;

            if (pk == null) {
                pk = Credentials.getPublicKey(Credentials.getPubKeyPath());
            }
            if (pk != null) {
                scheduler =
                        sai.login(Credentials.createCredentials(new CredData(
                                username, password), pk));
            } else {
                throw new KeyException(
                        "No public key found, cannot connect to Scheduler located at "
                                + schedulerURL);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Connected to Scheduler " + schedulerURL);
            }

            this.schedulers.put(schedulerURL, scheduler);

            return scheduler;
        } catch (ConnectionException ce) {
            throw new NodeProviderException(ce);
        } catch (LoginException le) {
            throw new NodeProviderException(le);
        } catch (AlreadyConnectedException ace) {
            throw new NodeProviderException(ace);
        } catch (KeyException ke) {
            throw new NodeProviderException(ke);
        }
    }

    private Scheduler connectToScheduler(String schedulerURL,
                                         String credentialsPath)
            throws NodeProviderException {
        if (this.schedulers.containsKey(schedulerURL)) {
            return this.schedulers.get(schedulerURL);
        }

        try {
            SchedulerAuthenticationInterface sai;
            sai = SchedulerConnection.join(schedulerURL);
            Scheduler scheduler =
                    sai.login(Credentials.getCredentials(credentialsPath));

            if (logger.isDebugEnabled()) {
                logger.debug("Connected to Scheduler " + schedulerURL);
            }

            this.schedulers.put(schedulerURL, scheduler);

            return scheduler;
        } catch (ConnectionException ce) {
            throw new NodeProviderException(ce);
        } catch (LoginException le) {
            throw new NodeProviderException(le);
        } catch (AlreadyConnectedException ace) {
            throw new NodeProviderException(ace);
        } catch (KeyException ke) {
            throw new NodeProviderException(ke);
        }
    }

    private UniqueID submitNodeRequest(Scheduler scheduler, int nbNodes,
                                       String dataFolder,
                                       List<String> jvmArguments,
                                       String... nodeSourceNames)
            throws NodeProviderException {
        UniqueID nodeRequestID = new UniqueID();

        this.registry.addNodeRequest(nodeRequestID, nbNodes);

        NodeProviderJob nodeProviderJob =
                new NodeProviderJob(
                        nodeRequestID, scheduler, nbNodes, dataFolder,
                        jvmArguments, this.registry.getURL(), nodeSourceNames);

        this.nodeProviderJobs.put(nodeRequestID, nodeProviderJob);

        return nodeRequestID;
    }

    private List<Node> getNodes(UniqueID nodeRequestID)
            throws NodeProviderException {
        if (this.nodeProviderJobs.containsKey(nodeRequestID)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Waiting for the acquisition of nodes for request #"
                        + nodeRequestID);
            }

            long startTime = System.currentTimeMillis();
            while (!this.registry.isDeploymentFinished(nodeRequestID)) {
                try {
                    if (System.currentTimeMillis() > (startTime + this.nodesAquisitionTimeout)) {
                        logger.error("Unsuccessful acquisition of nodes for node request #"
                                + nodeRequestID
                                + " after "
                                + this.nodesAquisitionTimeout + " ms");

                        this.releaseNodes(nodeRequestID);

                        return new ArrayList<Node>();
                    }

                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    this.releaseNodes(nodeRequestID);
                    throw new NodeProviderException(ie);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Acquisition of nodes for node request #"
                        + nodeRequestID + " completed");
            }

            return this.registry.getNodes(nodeRequestID);
        } else {
            logger.error("No such node request ID: #" + nodeRequestID);

            return new ArrayList<Node>();
        }
    }

    /**
     * Returns the {@link Node nodes} of the specified list of node requests. <br>
     * This method is blocking until all the {@link Node nodes} are available or
     * if the deployment fails.
     * 
     * @param nodeRequestIDs
     *            IDs of the Node requests.
     * @return The {@link Node nodes} of the specified node requests.
     * @throws NodeProviderException
     *             If an error occurs when getting the {@link Node nodes}.
     */
    public List<Node> getNodes(UniqueID... nodeRequestIDs)
            throws NodeProviderException {
        List<Node> nodes = new ArrayList<Node>();

        for (UniqueID nodeRequestID : nodeRequestIDs) {
            nodes.addAll(this.getNodes(nodeRequestID));
        }

        return nodes;
    }

    /**
     * Returns a {@link GCMVirtualNode} with the specified name and containing
     * the {@link Node nodes} coming from the specified list of node requests. <br>
     * This method is blocking until all the {@link Node nodes} are available or
     * if the deployment fails.
     * 
     * @param vnName
     *            Name of the {@link GCMVirtualNode}.
     * @param nodeRequestIDs
     *            IDs of the Node requests.
     * @return A {@link GCMVirtualNode} with the specified name and containing
     *         the {@link Node nodes} coming from the specified list of node
     *         requests.
     * @throws NodeProviderException
     *             If an error occurs when getting the {@link Node nodes}.
     */
    public GCMVirtualNode getGCMVirtualNode(String vnName,
                                            UniqueID... nodeRequestIDs)
            throws NodeProviderException {
        return new GCMVirtualNodeImpl(vnName, this.getNodes(nodeRequestIDs));
    }

    /**
     * Releases the {@link Node nodes} of the specified node request.
     * 
     * @param nodeRequestID
     *            Node request ID.
     */
    public void releaseNodes(UniqueID nodeRequestID) {
        this.registry.releaseNodes(nodeRequestID);
        this.nodeProviderJobs.remove(nodeRequestID);
    }

    /**
     * Releases all the {@link Node nodes} of all the node requests.
     */
    public void releaseAllNodes() {
        List<UniqueID> nodeRequestIDs =
                new ArrayList<UniqueID>(this.nodeProviderJobs.keySet());

        for (UniqueID nodeRequestID : nodeRequestIDs) {
            this.releaseNodes(nodeRequestID);
        }
    }
}
