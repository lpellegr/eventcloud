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
package fr.inria.eventcloud.factories;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.factories.AbstractFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerAttributeController;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerImpl;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.tracker.TrackerAttributeController;
import org.objectweb.proactive.extensions.p2p.structured.tracker.TrackerImpl;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soceda.socialfilter.relationshipstrengthengine.RelationshipStrengthEngineManager;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.overlay.SemanticPeerImpl;
import fr.inria.eventcloud.tracker.SemanticTracker;
import fr.inria.eventcloud.tracker.SemanticTrackerImpl;

/**
 * SemanticFactory must be used to create new instances of Semantic components
 * like for example {@link SemanticTracker}s and {@link SemanticPeer}s.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public final class SemanticFactory extends AbstractFactory {

    private static final Logger log =
            LoggerFactory.getLogger(SemanticFactory.class);

    private SemanticFactory() {
    }

    /**
     * Creates a new semantic tracker component deployed on the local JVM and
     * associates it to the network named "default".
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker() {
        return SemanticFactory.createSemanticTracker(
                "default", new HashMap<String, Object>());
    }

    /**
     * Creates a new semantic tracker component deployed on the local JVM and
     * associates it to the specified {@code networkName}.
     * 
     * @param networkName
     *            the network name managed by the tracker.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(String networkName) {
        return SemanticFactory.createSemanticTracker(
                networkName, new HashMap<String, Object>());
    }

    /**
     * Creates a new semantic tracker component deployed on the specified
     * {@code node} and associates it to the network named "default".
     * 
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(Node node) {
        return SemanticFactory.newSemanticTracker("default", node);
    }

    /**
     * Creates a new semantic tracker component deployed on the specified
     * {@code GCM virtual node} and associates it to the network named
     * "default".
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(GCMVirtualNode vn) {
        return SemanticFactory.newSemanticTracker("default", vn);
    }

    /**
     * Creates a new semantic tracker component deployed on a node provided by
     * the specified {@code node provider} and associates it to the network
     * named "default".
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(NodeProvider nodeProvider) {
        return SemanticFactory.newSemanticTracker("default", nodeProvider);
    }

    /**
     * Creates a new semantic tracker component deployed on the specified
     * {@code node} and associates it to the given {@code networkName}.
     * 
     * @param networkName
     *            the network name managed by the tracker.
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(String networkName,
                                                     Node node) {
        return SemanticFactory.createSemanticTracker(
                networkName, ComponentUtils.createContext(node));
    }

    /**
     * Creates a new semantic tracker component deployed on the specified
     * {@code GCM virtual node} and associates it to the given
     * {@code networkName}.
     * 
     * @param networkName
     *            the network name managed by the tracker.
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(String networkName,
                                                     GCMVirtualNode vn) {
        return SemanticFactory.createSemanticTracker(
                networkName, ComponentUtils.createContext(vn));
    }

    /**
     * Creates a new semantic tracker component deployed on a node provided by
     * the specified {@code node provider} and associates it to the given
     * {@code networkName}.
     * 
     * @param networkName
     *            the network name managed by the tracker.
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(String networkName,
                                                     NodeProvider nodeProvider) {
        return SemanticFactory.createSemanticTracker(
                networkName, getContextFromNodeProvider(
                        nodeProvider, SemanticTrackerImpl.TRACKER_VN));
    }

    private static SemanticTracker createSemanticTracker(String networkName,
                                                         Map<String, Object> context) {
        try {
            SemanticTracker tracker =
                    ComponentUtils.createComponentAndGetInterface(
                            SemanticTrackerImpl.SEMANTIC_TRACKER_ADL, context,
                            TrackerImpl.TRACKER_SERVICES_ITF,
                            SemanticTracker.class, true);

            ((TrackerAttributeController) GCM.getAttributeController(((Interface) tracker).getFcItfOwner())).setAttributes(
                    tracker, networkName);

            log.info(
                    "SemanticTracker {} associated to network named '{}' created",
                    tracker.getId(), networkName);

            return tracker;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates a new semantic peer component deployed on the local JVM with the
     * specified overlay {@link Provider}.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(SerializableProvider<T> overlayProvider) {
        return SemanticFactory.createSemanticPeer(
                overlayProvider, new HashMap<String, Object>());
    }

    /**
     * Creates a new semantic peer component deployed on the specified
     * {@code node}.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(SerializableProvider<T> overlayProvider,
                                                                             Node node) {
        return SemanticFactory.createSemanticPeer(
                overlayProvider, ComponentUtils.createContext(node));
    }

    /**
     * Creates a new semantic peer component deployed on the specified
     * {@code GCM virtual node}.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(SerializableProvider<T> overlayProvider,
                                                                             GCMVirtualNode vn) {
        return SemanticFactory.createSemanticPeer(
                overlayProvider, ComponentUtils.createContext(vn));
    }

    /**
     * Creates a new semantic peer component deployed on a node provided by the
     * specified {@code node provider}.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(SerializableProvider<T> overlayProvider,
                                                                             NodeProvider nodeProvider) {
        return SemanticFactory.createSemanticPeer(
                overlayProvider, getContextFromNodeProvider(
                        nodeProvider, SemanticPeerImpl.PEER_VN));
    }

    private static <T extends StructuredOverlay> SemanticPeer createSemanticPeer(SerializableProvider<T> overlayProvider,
                                                                                 Map<String, Object> context) {
        try {
            SemanticPeer peer =
                    ComponentUtils.createComponentAndGetInterface(
                            SemanticPeerImpl.SEMANTIC_PEER_ADL, context,
                            PeerImpl.PEER_SERVICES_ITF, SemanticPeer.class,
                            true);

            ((PeerAttributeController) GCM.getAttributeController(((Interface) peer).getFcItfOwner())).setAttributes(
                    peer, overlayProvider);

            if (EventCloudProperties.SOCIAL_FILTER_URL.getValue() != null) {
                Component peerComponent = ((Interface) peer).getFcItfOwner();
                RelationshipStrengthEngineManager socialFilter =
                        ComponentUtils.lookupFcInterface(
                                EventCloudProperties.SOCIAL_FILTER_URL.getValue(),
                                SemanticPeerImpl.SOCIAL_FILTER_SERVICES_ITF,
                                RelationshipStrengthEngineManager.class);

                GCM.getGCMLifeCycleController(peerComponent).stopFc();
                GCM.getBindingController(peerComponent).bindFc(
                        SemanticPeerImpl.SOCIAL_FILTER_SERVICES_ITF,
                        socialFilter);
                GCM.getGCMLifeCycleController(peerComponent).startFc();

                log.info(
                        "SemanticPeer {} bound to social filter {}",
                        peer.getId(),
                        EventCloudProperties.SOCIAL_FILTER_URL.getValue());
            }

            log.info("SemanticPeer {} created", peer.getId());

            return peer;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (IllegalLifeCycleException e) {
            throw new IllegalStateException(e);
        } catch (IllegalBindingException e) {
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
