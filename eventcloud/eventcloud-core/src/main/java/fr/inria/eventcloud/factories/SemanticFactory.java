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

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentConfiguration;
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
     * Creates a new generic semantic tracker component deployed on the local
     * JVM. <br>
     * The tracker is not initialized and not started.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         generic semantic tracker component created.
     */
    public static SemanticTracker newGenericSemanticTracker() {
        return SemanticFactory.createGenericSemanticTracker(new HashMap<String, Object>());
    }

    /**
     * Creates a new generic semantic tracker component deployed on the
     * specified {@code node}. <br>
     * The tracker is not initialized and not started.
     * 
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         generic semantic tracker component created.
     */
    public static SemanticTracker newGenericSemanticTracker(Node node) {
        return SemanticFactory.createGenericSemanticTracker(ComponentUtils.createContext(node));
    }

    /**
     * Creates a new generic semantic tracker component deployed on the
     * specified {@code GCM virtual node}. <br>
     * The tracker is not initialized and not started.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         generic semantic tracker component created.
     */
    public static SemanticTracker newGenericSemanticTracker(GCMVirtualNode vn) {
        return SemanticFactory.createGenericSemanticTracker(ComponentUtils.createContext(vn));
    }

    /**
     * Creates a new generic semantic tracker component deployed on a node
     * provided by the specified {@code node provider}. <br>
     * The tracker is not initialized and not started.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         generic semantic tracker component created.
     */
    public static SemanticTracker newGenericSemanticTracker(NodeProvider nodeProvider) {
        return SemanticFactory.createGenericSemanticTracker(AbstractFactory.getContextFromNodeProvider(
                nodeProvider, SemanticTrackerImpl.TRACKER_VN));
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
                new HashMap<String, Object>(), null, "default");
    }

    /**
     * Creates a new semantic tracker component deployed on the local JVM, by
     * using the specified deployment configuration and associates it to the
     * network named "default".
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(DeploymentConfiguration deploymentConfiguration) {
        return SemanticFactory.createSemanticTracker(
                new HashMap<String, Object>(), deploymentConfiguration,
                "default");
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
                new HashMap<String, Object>(), null, networkName);
    }

    /**
     * Creates a new semantic tracker component deployed on the local JVM, by
     * using the specified deployment configuration and associates it to the
     * specified {@code networkName}.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param networkName
     *            the network name managed by the tracker.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(DeploymentConfiguration deploymentConfiguration,
                                                     String networkName) {
        return SemanticFactory.createSemanticTracker(
                new HashMap<String, Object>(), deploymentConfiguration,
                networkName);
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
        return SemanticFactory.newSemanticTracker(node, null, "default");
    }

    /**
     * Creates a new semantic tracker component deployed on the specified
     * {@code node}, by using the specified deployment configuration and
     * associates it to the network named "default".
     * 
     * @param node
     *            the node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(Node node,
                                                     DeploymentConfiguration deploymentConfiguration) {
        return SemanticFactory.newSemanticTracker(
                node, deploymentConfiguration, "default");
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
        return SemanticFactory.newSemanticTracker(vn, null, "default");
    }

    /**
     * Creates a new semantic tracker component deployed on the specified
     * {@code GCM virtual node}, by using the specified deployment configuration
     * and associates it to the network named "default".
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(GCMVirtualNode vn,
                                                     DeploymentConfiguration deploymentConfiguration) {
        return SemanticFactory.newSemanticTracker(
                vn, deploymentConfiguration, "default");
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
        return SemanticFactory.newSemanticTracker(nodeProvider, null, "default");
    }

    /**
     * Creates a new semantic tracker component deployed on a node provided by
     * the specified {@code node provider}, by using the specified deployment
     * configuration and associates it to the network named "default".
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(NodeProvider nodeProvider,
                                                     DeploymentConfiguration deploymentConfiguration) {
        return SemanticFactory.newSemanticTracker(
                nodeProvider, deploymentConfiguration, "default");
    }

    /**
     * Creates a new semantic tracker component deployed on the specified
     * {@code node} and associates it to the specified {@code networkName}.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param networkName
     *            the network name managed by the tracker.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(Node node,
                                                     String networkName) {
        return SemanticFactory.newSemanticTracker(node, null, networkName);
    }

    /**
     * Creates a new semantic tracker component deployed on the specified
     * {@code node}, by using the specified deployment configuration and
     * associates it to the specified {@code networkName}.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param networkName
     *            the network name managed by the tracker.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(Node node,
                                                     DeploymentConfiguration deploymentConfiguration,
                                                     String networkName) {
        return SemanticFactory.createSemanticTracker(
                ComponentUtils.createContext(node), deploymentConfiguration,
                networkName);
    }

    /**
     * Creates a new semantic tracker component deployed on the specified
     * {@code GCM virtual node} and associates it to the specified
     * {@code networkName}.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param networkName
     *            the network name managed by the tracker.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(GCMVirtualNode vn,
                                                     String networkName) {
        return SemanticFactory.newSemanticTracker(vn, null, networkName);
    }

    /**
     * Creates a new semantic tracker component deployed on the specified
     * {@code GCM virtual node}, by using the specified deployment configuration
     * and associates it to the specified {@code networkName}.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param networkName
     *            the network name managed by the tracker.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(GCMVirtualNode vn,
                                                     DeploymentConfiguration deploymentConfiguration,
                                                     String networkName) {
        return SemanticFactory.createSemanticTracker(
                ComponentUtils.createContext(vn), deploymentConfiguration,
                networkName);
    }

    /**
     * Creates a new semantic tracker component deployed on a node provided by
     * the specified {@code node provider} and associates it to the specified
     * {@code networkName}.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param networkName
     *            the network name managed by the tracker.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(NodeProvider nodeProvider,
                                                     String networkName) {
        return SemanticFactory.newSemanticTracker(
                nodeProvider, null, networkName);
    }

    /**
     * Creates a new semantic tracker component deployed on a node provided by
     * the specified {@code node provider}, by using the specified deployment
     * configuration and associates it to the specified {@code networkName}.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param networkName
     *            the network name managed by the tracker.
     * 
     * @return the reference on the {@link SemanticTracker} interface of the new
     *         semantic tracker component created.
     */
    public static SemanticTracker newSemanticTracker(NodeProvider nodeProvider,
                                                     DeploymentConfiguration deploymentConfiguration,
                                                     String networkName) {
        return SemanticFactory.createSemanticTracker(
                AbstractFactory.getContextFromNodeProvider(
                        nodeProvider, SemanticTrackerImpl.TRACKER_VN),
                deploymentConfiguration, networkName);
    }

    private static SemanticTracker createSemanticTracker(Map<String, Object> context,
                                                         DeploymentConfiguration deploymentConfiguration,
                                                         String networkName) {
        SemanticTracker tracker =
                SemanticFactory.createGenericSemanticTracker(context);
        SemanticFactory.initGenericSemanticTracker(
                tracker, deploymentConfiguration, networkName);

        log.info(
                "SemanticTracker {} associated to network named '{}' created",
                tracker.getId(), networkName);

        return tracker;
    }

    private static SemanticTracker createGenericSemanticTracker(Map<String, Object> context) {
        return ComponentUtils.createComponentAndGetInterface(
                SemanticTrackerImpl.SEMANTIC_TRACKER_ADL, context,
                TrackerImpl.TRACKER_SERVICES_ITF, SemanticTracker.class, false);
    }

    /**
     * Initializes and starts the specified generic semantic tracker by using
     * the specified deployment configuration and associates it to the specified
     * {@code networkName}.
     * 
     * @param tracker
     *            the reference on the {@link SemanticTracker} interface of the
     *            generic semantic tracker component to initialize.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param networkName
     *            the network name managed by the tracker.
     */
    public static void initGenericSemanticTracker(SemanticTracker tracker,
                                                  DeploymentConfiguration deploymentConfiguration,
                                                  String networkName) {
        try {
            Component trackerComponent = ((Interface) tracker).getFcItfOwner();

            TrackerAttributeController trackerAttributeController =
                    (TrackerAttributeController) GCM.getAttributeController(trackerComponent);
            if (deploymentConfiguration != null) {
                trackerAttributeController.setDeploymentConfiguration(deploymentConfiguration);
            }
            trackerAttributeController.setAttributes(tracker, networkName);

            GCM.getGCMLifeCycleController(trackerComponent).startFc();
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        } catch (IllegalLifeCycleException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates a new generic semantic peer component deployed on the local JVM. <br>
     * The peer is not initialized and not started.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         generic semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newGenericSemanticPeer() {
        return SemanticFactory.createGenericSemanticPeer(new HashMap<String, Object>());
    }

    /**
     * Creates a new generic semantic peer component deployed on the specified
     * {@code node}. <br>
     * The peer is not initialized and not started.
     * 
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         generic semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newGenericSemanticPeer(Node node) {
        return SemanticFactory.createGenericSemanticPeer(ComponentUtils.createContext(node));
    }

    /**
     * Creates a new generic semantic peer component deployed on the specified
     * {@code GCM virtual node}. <br>
     * The peer is not initialized and not started.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         generic semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newGenericSemanticPeer(GCMVirtualNode vn) {
        return SemanticFactory.createGenericSemanticPeer(ComponentUtils.createContext(vn));
    }

    /**
     * Creates a new generic semantic peer component deployed on a node provided
     * by the specified {@code node provider}. <br>
     * The peer is not initialized and not started.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         generic semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newGenericSemanticPeer(NodeProvider nodeProvider) {
        return SemanticFactory.createGenericSemanticPeer(AbstractFactory.getContextFromNodeProvider(
                nodeProvider, SemanticPeerImpl.PEER_VN));
    }

    /**
     * Creates a new semantic peer component deployed on the local JVM by using
     * the specified {@code overlay} abstraction.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(SerializableProvider<T> overlayProvider) {
        return SemanticFactory.createSemanticPeer(
                new HashMap<String, Object>(), null, overlayProvider);
    }

    /**
     * Creates a new semantic peer component deployed on the local JVM by using
     * the specified deployment configuration and the specified {@code overlay}
     * abstraction.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(DeploymentConfiguration deploymentConfiguration,
                                                                             SerializableProvider<T> overlayProvider) {
        return SemanticFactory.createSemanticPeer(
                new HashMap<String, Object>(), deploymentConfiguration,
                overlayProvider);
    }

    /**
     * Creates a new semantic peer component deployed on the specified
     * {@code node} by using the specified {@code overlay} abstraction.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(Node node,
                                                                             SerializableProvider<T> overlayProvider) {
        return SemanticFactory.newSemanticPeer(node, null, overlayProvider);
    }

    /**
     * Creates a new semantic peer component deployed on the specified
     * {@code node} by using the specified deployment configuration and the
     * specified {@code overlay} abstraction.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(Node node,
                                                                             DeploymentConfiguration deploymentConfiguration,
                                                                             SerializableProvider<T> overlayProvider) {
        return SemanticFactory.createSemanticPeer(
                ComponentUtils.createContext(node), deploymentConfiguration,
                overlayProvider);
    }

    /**
     * Creates a new semantic peer component deployed on the specified
     * {@code GCM virtual node} by using the specified {@code overlay}
     * abstraction.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(GCMVirtualNode vn,
                                                                             SerializableProvider<T> overlayProvider) {
        return SemanticFactory.newSemanticPeer(vn, null, overlayProvider);
    }

    /**
     * Creates a new semantic peer component deployed on the specified
     * {@code GCM virtual node} by using the specified deployment configuration
     * and the specified {@code overlay} abstraction.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(GCMVirtualNode vn,
                                                                             DeploymentConfiguration deploymentConfiguration,
                                                                             SerializableProvider<T> overlayProvider) {
        return SemanticFactory.createSemanticPeer(
                ComponentUtils.createContext(vn), deploymentConfiguration,
                overlayProvider);
    }

    /**
     * Creates a new semantic peer component deployed on a node provided by the
     * specified {@code node provider} by using the specified {@code overlay}
     * abstraction.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(NodeProvider nodeProvider,
                                                                             SerializableProvider<T> overlayProvider) {
        return SemanticFactory.newSemanticPeer(
                nodeProvider, null, overlayProvider);
    }

    /**
     * Creates a new semantic peer component deployed on a node provided by the
     * specified {@code node provider} by using the specified deployment
     * configuration and the specified {@code overlay} abstraction.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link SemanticPeer} interface of the new
     *         semantic peer component created.
     */
    public static <T extends StructuredOverlay> SemanticPeer newSemanticPeer(NodeProvider nodeProvider,
                                                                             DeploymentConfiguration deploymentConfiguration,
                                                                             SerializableProvider<T> overlayProvider) {
        return SemanticFactory.createSemanticPeer(
                AbstractFactory.getContextFromNodeProvider(
                        nodeProvider, SemanticPeerImpl.PEER_VN),
                deploymentConfiguration, overlayProvider);
    }

    private static <T extends StructuredOverlay> SemanticPeer createSemanticPeer(Map<String, Object> context,
                                                                                 DeploymentConfiguration deploymentConfiguration,
                                                                                 SerializableProvider<T> overlayProvider) {
        SemanticPeer peer = SemanticFactory.createGenericSemanticPeer(context);
        SemanticFactory.initGenericSemanticPeer(
                peer, deploymentConfiguration, overlayProvider);

        log.info("SemanticPeer {} created", peer.getId());

        return peer;
    }

    private synchronized static <T extends StructuredOverlay> SemanticPeer createGenericSemanticPeer(Map<String, Object> context) {
        try {
            SemanticPeer peer =
                    ComponentUtils.createComponentAndGetInterface(
                            SemanticPeerImpl.SEMANTIC_PEER_ADL, context,
                            PeerImpl.PEER_SERVICES_ITF, SemanticPeer.class,
                            false);

            if (EventCloudProperties.SOCIAL_FILTER_URL.getValue() != null) {
                RelationshipStrengthEngineManager socialFilter =
                        ComponentUtils.lookupFcInterface(
                                EventCloudProperties.SOCIAL_FILTER_URL.getValue(),
                                SemanticPeerImpl.SOCIAL_FILTER_SERVICES_ITF,
                                RelationshipStrengthEngineManager.class);

                GCM.getBindingController(((Interface) peer).getFcItfOwner())
                        .bindFc(
                                SemanticPeerImpl.SOCIAL_FILTER_SERVICES_ITF,
                                socialFilter);

                log.info(
                        "SemanticPeer {} bound to social filter {}",
                        peer.getId(),
                        EventCloudProperties.SOCIAL_FILTER_URL.getValue());
            }

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
     * Initializes and starts the specified generic semantic peer by using the
     * specified deployment configuration and the specified {@code overlay}
     * abstraction.
     * 
     * @param peer
     *            the reference on the {@link SemanticPeer} interface of the
     *            generic semantic peer component to initialize.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param overlayProvider
     *            the overlay provider to use.
     */
    public static <T extends StructuredOverlay> void initGenericSemanticPeer(SemanticPeer peer,
                                                                             DeploymentConfiguration deploymentConfiguration,
                                                                             SerializableProvider<T> overlayProvider) {
        try {
            Component peerComponent = ((Interface) peer).getFcItfOwner();

            PeerAttributeController peerAttributeController =
                    (PeerAttributeController) GCM.getAttributeController(peerComponent);
            if (deploymentConfiguration != null) {
                peerAttributeController.setDeploymentConfiguration(deploymentConfiguration);
            }
            peerAttributeController.setAttributes(peer, overlayProvider);

            GCM.getGCMLifeCycleController(peerComponent).startFc();
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
                new Node[number], overlayProvider);
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
    public static <T extends StructuredOverlay> SemanticPeer[] newSemanticPeersInParallel(Node[] nodes,
                                                                                          SerializableProvider<T> overlayProvider) {
        checkNotNull(nodes);

        SemanticPeer[] peers = new SemanticPeer[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            peers[i] =
                    SemanticFactory.newSemanticPeer(nodes[i], overlayProvider);
        }
        return peers;
    }

}
