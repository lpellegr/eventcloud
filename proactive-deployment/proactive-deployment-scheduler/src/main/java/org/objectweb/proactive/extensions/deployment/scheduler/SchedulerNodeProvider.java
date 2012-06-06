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

import java.security.KeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;

public class SchedulerNodeProvider {
    private static final Logger logger =
            ProActiveLogger.getLogger(SchedulerLoggers.SCHEDULE);

    private static final int NODES_AQUISITION_TIMEOUT = 60000;

    private final NodeProviderRegistry registry;

    private final Map<String, Scheduler> schedulers;

    private final Map<UniqueID, NodeProviderJob> nodeProviderJobs;

    public SchedulerNodeProvider() throws ProActiveException {
        this.registry =
                (NodeProviderRegistry) PAActiveObject.newActive(
                        NodeProviderRegistry.class.getName(), new Object[0]);
        PAActiveObject.registerByName(this.registry, "NODE_PROVIDER_REGISTRY_"
                + (new UniqueID()).getCanonString());
        this.schedulers = new HashMap<String, Scheduler>();
        this.nodeProviderJobs = new HashMap<UniqueID, NodeProviderJob>();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                SchedulerNodeProvider.this.releaseAllNodes();
            }
        });
    }

    public UniqueID deployNodes(String schedulerURL, String username,
                                String password, int numberNodes,
                                String userJarsLocation) throws Exception {
        return this.deployNodes(
                schedulerURL, username, password, numberNodes,
                userJarsLocation, null, null);
    }

    public UniqueID deployNodes(String schedulerURL, String username,
                                String password, int numberNodes,
                                String userJarsLocation,
                                List<String> jvmArguments) throws Exception {
        return this.deployNodes(
                this.connectToScheduler(schedulerURL, username, password),
                numberNodes, userJarsLocation, jvmArguments, null);
    }

    public UniqueID deployNodes(String schedulerURL, String username,
                                String password, int numberNodes,
                                String userJarsLocation, String nodeSourceName)
            throws Exception {
        return this.deployNodes(
                this.connectToScheduler(schedulerURL, username, password),
                numberNodes, userJarsLocation, null, nodeSourceName);
    }

    public UniqueID deployNodes(String schedulerURL, String username,
                                String password, int numberNodes,
                                String userJarsLocation,
                                List<String> jvmArguments, String nodeSourceName)
            throws Exception {
        return this.deployNodes(
                this.connectToScheduler(schedulerURL, username, password),
                numberNodes, userJarsLocation, jvmArguments, nodeSourceName);
    }

    public UniqueID deployNodes(String schedulerURL, String credentialsPath,
                                int numberNodes, String userJarsLocation)
            throws Exception {
        return this.deployNodes(
                schedulerURL, credentialsPath, numberNodes, userJarsLocation,
                null, null);
    }

    public UniqueID deployNodes(String schedulerURL, String credentialsPath,
                                int numberNodes, String userJarsLocation,
                                List<String> jvmArguments) throws Exception {
        return this.deployNodes(
                schedulerURL, credentialsPath, numberNodes, userJarsLocation,
                jvmArguments, null);
    }

    public UniqueID deployNodes(String schedulerURL, String credentialsPath,
                                int numberNodes, String userJarsLocation,
                                String nodeSourceName) throws Exception {
        return this.deployNodes(
                this.connectToScheduler(schedulerURL, credentialsPath),
                numberNodes, userJarsLocation, null, nodeSourceName);
    }

    public UniqueID deployNodes(String schedulerURL, String credentialsPath,
                                int numberNodes, String userJarsLocation,
                                List<String> jvmArguments, String nodeSourceName)
            throws Exception {
        return this.deployNodes(
                this.connectToScheduler(schedulerURL, credentialsPath),
                numberNodes, userJarsLocation, jvmArguments, nodeSourceName);
    }

    private synchronized Scheduler connectToScheduler(String schedulerURL,
                                                      String username,
                                                      String password)
            throws Exception {
        if (this.schedulers.containsKey(schedulerURL)) {
            return this.schedulers.get(schedulerURL);
        }

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
    }

    private Scheduler connectToScheduler(String schedulerURL,
                                         String credentialsPath)
            throws Exception {
        if (this.schedulers.containsKey(schedulerURL)) {
            return this.schedulers.get(schedulerURL);
        }

        SchedulerAuthenticationInterface sai =
                SchedulerConnection.join(schedulerURL);
        Scheduler scheduler =
                sai.login(Credentials.getCredentials(credentialsPath));

        if (logger.isDebugEnabled()) {
            logger.debug("Connected to Scheduler " + schedulerURL);
        }

        this.schedulers.put(schedulerURL, scheduler);

        return scheduler;
    }

    private UniqueID deployNodes(Scheduler scheduler, int nbNodes,
                                 String userJarsLocation,
                                 List<String> jvmArguments,
                                 String nodeSourceName) throws Exception {
        UniqueID nodeRequestID = new UniqueID();

        this.registry.addNodeRequest(nodeRequestID, nbNodes);

        NodeProviderJob nodeProviderJob =
                new NodeProviderJob(
                        nodeRequestID, scheduler, nbNodes, userJarsLocation,
                        jvmArguments, this.registry.getURL(), nodeSourceName);

        this.nodeProviderJobs.put(nodeRequestID, nodeProviderJob);

        return nodeRequestID;
    }

    public List<Node> getNodes(UniqueID nodeRequestID) throws Exception {
        if (this.nodeProviderJobs.containsKey(nodeRequestID)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Waiting for the acquisition of nodes for request #"
                        + nodeRequestID);
            }

            long startTime = System.currentTimeMillis();
            while (!this.registry.isDeploymentFinished(nodeRequestID)) {
                try {
                    if (System.currentTimeMillis() > (startTime + NODES_AQUISITION_TIMEOUT)) {
                        logger.error("Unsuccessful acquisition of nodes for node request #"
                                + nodeRequestID
                                + " after "
                                + NODES_AQUISITION_TIMEOUT + " ms");

                        this.releaseNodes(nodeRequestID);

                        return new ArrayList<Node>();
                    }

                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    this.releaseNodes(nodeRequestID);
                    throw ie;
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Acquisition of nodes for node request #"
                        + nodeRequestID + " completed");
            }

            return this.registry.getNodes(nodeRequestID);
        } else {
            return new ArrayList<Node>();
        }
    }

    public List<Node> getNodes(List<UniqueID> nodeRequestIDs) throws Exception {
        List<Node> nodes = new ArrayList<Node>();

        for (UniqueID nodeRequestID : nodeRequestIDs) {
            nodes.addAll(this.getNodes(nodeRequestID));
        }

        return nodes;
    }

    public GCMVirtualNode getGCMVirtualNode(String vnName,
                                            UniqueID nodeRequestID)
            throws Exception {
        return new GCMVirtualNodeImpl(vnName, this.getNodes(nodeRequestID));
    }

    public GCMVirtualNode getGCMVirtualNode(String vnName,
                                            List<UniqueID> nodeRequestIDs)
            throws Exception {
        return new GCMVirtualNodeImpl(vnName, this.getNodes(nodeRequestIDs));
    }

    public void releaseNodes(UniqueID nodeRequestID) {
        this.registry.releaseNodes(nodeRequestID);

        this.nodeProviderJobs.remove(nodeRequestID).killNodeProviderJob();
    }

    public void releaseAllNodes() {
        List<UniqueID> nodeRequestIDs =
                new ArrayList<UniqueID>(this.nodeProviderJobs.keySet());

        for (UniqueID nodeRequestID : nodeRequestIDs) {
            this.releaseNodes(nodeRequestID);
        }
    }
}
