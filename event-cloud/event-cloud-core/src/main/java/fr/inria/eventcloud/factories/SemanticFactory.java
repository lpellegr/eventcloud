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
package fr.inria.eventcloud.factories;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.adl.nodes.ADLNodeProvider;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.factories.AbstractFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerAttributeController;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.tracker.TrackerAttributeController;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * SemanticFactory must be used to create new instances of Semantic components
 * like for example {@link SemanticTracker}s and {@link SemanticPeer}s.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public final class SemanticFactory extends AbstractFactory {

    private static final Logger logger =
            LoggerFactory.getLogger(SemanticFactory.class);

    private static Factory factory;

    static {
        CentralPAPropertyRepository.GCM_PROVIDER.setValue(P2PStructuredProperties.GCM_PROVIDER.getValue());
        try {
            factory = FactoryFactory.getFactory();
        } catch (ADLException e) {
            e.printStackTrace();
        }
    }

    private SemanticFactory() {
    }

    /**
     * Creates a new semantic tracker component on the local JVM and associates
     * it to the network named "default".
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         tracker component created.
     */
    public static SemanticTracker newSemanticTracker() {
        return SemanticFactory.createSemanticTracker(
                "default", new HashMap<String, Object>());
    }

    /**
     * Creates a new semantic tracker component on the local JVM and associates
     * it to the specified {@code networkName}.
     * 
     * @param networkName
     *            the network name managed by the tracker.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         tracker component created.
     */
    public static SemanticTracker newSemanticTracker(String networkName) {
        return SemanticFactory.createSemanticTracker(
                networkName, new HashMap<String, Object>());
    }

    /**
     * Creates a new semantic tracker component on the specified {@code node}
     * and associates it to the given {@code networkName}.
     * 
     * @param networkName
     *            the network name managed by the tracker.
     * @param node
     *            the node to use for the deployment.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         tracker component created.
     */
    public static SemanticTracker newSemanticTracker(String networkName,
                                                     Node node) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (node != null) {
            List<Node> nodeList = new ArrayList<Node>(1);
            nodeList.add(node);
            context.put(ADLNodeProvider.NODES_ID, nodeList);
        }
        return SemanticFactory.createSemanticTracker(networkName, context);
    }

    /**
     * Creates a new semantic tracker component on the specified
     * {@code GCM virtual node} and associates it to the given
     * {@code networkName}.
     * 
     * @param networkName
     *            the network name managed by the tracker.
     * @param vn
     *            the GCM virtual node to use for the deployment.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         tracker component created.
     */
    public static SemanticTracker newSemanticTracker(String networkName,
                                                     GCMVirtualNode vn) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (vn != null) {
            context.put(vn.getName(), vn);
        }
        return SemanticFactory.createSemanticTracker(networkName, context);
    }

    /**
     * Creates a new semantic tracker component on {@code node} provided by the
     * specified GCM application and associates it to the given
     * {@code networkName}.
     * 
     * @param networkName
     *            the network name managed by the tracker.
     * @param gcma
     *            the GCM application to use for the deployment.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         tracker component created.
     */
    public static SemanticTracker newSemanticTracker(String networkName,
                                                     GCMApplication gcma) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (gcma != null) {
            context.put("deployment-descriptor", gcma);
        }
        return SemanticFactory.createSemanticTracker(networkName, context);
    }

    private static SemanticTracker createSemanticTracker(String networkName,
                                                         Map<String, Object> context) {
        try {
            Component tracker =
                    (Component) factory.newComponent(
                            EventCloudProperties.SEMANTIC_TRACKER_ADL.getValue(),
                            context);
            SemanticTracker stub =
                    (SemanticTracker) tracker.getFcInterface(P2PStructuredProperties.TRACKER_SERVICES_ITF.getValue());

            ((TrackerAttributeController) GCM.getAttributeController(tracker)).setAttributes(
                    stub, networkName);

            GCM.getGCMLifeCycleController(tracker).startFc();

            logger.info(
                    "SemanticTracker {} associated to network named '{}' created",
                    stub.getId(), networkName);

            return stub;
        } catch (ADLException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        } catch (IllegalLifeCycleException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates a new {@link SemanticPeer} component on the local machine.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(SerializableProvider<T> overlayProvider) {
        return SemanticFactory.createSemanticPeer(
                overlayProvider, new HashMap<String, Object>());
    }

    /**
     * Creates a new {@link SemanticPeer} component deployed on the specified
     * {@code node}.
     * 
     * @param node
     *            the node used to deploy the peer.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(SerializableProvider<T> overlayProvider,
                                                                             Node node) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (node != null) {
            List<Node> nodeList = new ArrayList<Node>(1);
            nodeList.add(node);
            context.put(ADLNodeProvider.NODES_ID, nodeList);
        }

        return SemanticFactory.createSemanticPeer(overlayProvider, context);
    }

    /**
     * Creates a new {@link SemanticPeer} component deployed on the specified
     * {@code GCM virtual node}.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * @param vn
     *            the GCM virtual node used to deploy the peer.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(SerializableProvider<T> overlayProvider,
                                                                             GCMVirtualNode vn) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (vn != null) {
            context.put(vn.getName(), vn);
        }

        return SemanticFactory.createSemanticPeer(overlayProvider, context);
    }

    /**
     * Creates a new {@link SemanticPeer} component deployed on {@code node}
     * provided by the specified GCM application.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * @param gcma
     *            the GCM application used to deploy the peer.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(SerializableProvider<T> overlayProvider,
                                                                             GCMApplication gcma) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (gcma != null) {
            context.put("deployment-descriptor", gcma);
        }
        return SemanticFactory.createSemanticPeer(overlayProvider, context);
    }

    private static <T extends StructuredOverlay> SemanticPeer createSemanticPeer(SerializableProvider<T> overlayProvider,
                                                                                 Map<String, Object> context) {
        try {
            Component peer =
                    (Component) factory.newComponent(
                            EventCloudProperties.SEMANTIC_PEER_ADL.getValue(),
                            context);
            SemanticPeer stub =
                    (SemanticPeer) peer.getFcInterface(P2PStructuredProperties.PEER_SERVICES_ITF.getValue());

            ((PeerAttributeController) GCM.getAttributeController(peer)).setAttributes(
                    stub, overlayProvider);

            GCM.getGCMLifeCycleController(peer).startFc();

            logger.info("SemanticPeer {} has been created", stub.getId());

            return stub;
        } catch (ADLException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        } catch (IllegalLifeCycleException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates the specified {@code number} of SemanticPeer components in
     * parallel.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * @param number
     *            the number of {@link SemanticPeer} components to create.
     * 
     * @return the SemanticPeer components created.
     */
    public static <T extends StructuredOverlay> SemanticPeer[] newSemanticPeersInParallel(SerializableProvider<T> overlayProvider,
                                                                                          int number) {
        return SemanticFactory.newSemanticPeersInParallel(
                overlayProvider, new Node[number]);
    }

    /**
     * Creates a number of SemanticPeer components that is equals to the number
     * {@code nodes} specified. Each new SemanticPeer is deployed on a node from
     * the nodes array specified in parameter.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * @param nodes
     *            the nodes to use for the deployment.
     * 
     * @return the SemanticPeer components created.
     */
    /*
     * TODO better implementation of newComponentInParallel
     */
    public static <T extends StructuredOverlay> SemanticPeer[] newSemanticPeersInParallel(SerializableProvider<T> overlayProvider,
                                                                                          Node[] nodes) {
        checkNotNull(nodes);

        SemanticPeer[] peers = new SemanticPeer[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            peers[i] =
                    SemanticFactory.newSemanticPeer(overlayProvider, nodes[i]);
        }
        return peers;
    }

}
