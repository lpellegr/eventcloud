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
package org.objectweb.proactive.extensions.p2p.structured.factories;

import java.util.HashMap;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerAttributeController;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerImpl;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerInterface;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides some static methods in order to ease the creation of peer
 * components.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public final class PeerFactory extends AbstractFactory {

    private static final Logger log =
            LoggerFactory.getLogger(PeerFactory.class);

    private PeerFactory() {
    }

    /**
     * Creates a new peer component deployed on the local JVM by using the
     * specified {@code overlay} abstraction.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link Peer} interface of the new peer
     *         component created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(SerializableProvider<T> overlayProvider) {
        return PeerFactory.createPeer(
                new HashMap<String, Object>(), null, overlayProvider);
    }

    /**
     * Creates a new peer component deployed on the local JVM by using the
     * specified deployment configuration and the specified {@code overlay}
     * abstraction.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link Peer} interface of the new peer
     *         component created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(DeploymentConfiguration deploymentConfiguration,
                                                             SerializableProvider<T> overlayProvider) {
        return PeerFactory.createPeer(
                new HashMap<String, Object>(), deploymentConfiguration,
                overlayProvider);
    }

    /**
     * Creates a new peer component deployed on the specified {@code node} by
     * using the specified {@code overlay} abstraction.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link Peer} interface of the new peer
     *         component created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(Node node,
                                                             SerializableProvider<T> overlayProvider) {
        return PeerFactory.newPeer(node, null, overlayProvider);
    }

    /**
     * Creates a new peer component deployed on the specified {@code node} by
     * using the specified deployment configuration and the specified
     * {@code overlay} abstraction.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link Peer} interface of the new peer
     *         component created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(Node node,
                                                             DeploymentConfiguration deploymentConfiguration,
                                                             SerializableProvider<T> overlayProvider) {
        return PeerFactory.createPeer(
                ComponentUtils.createContext(node), deploymentConfiguration,
                overlayProvider);
    }

    /**
     * Creates a new peer component deployed on the specified
     * {@code GCM virtual node} by using the specified {@code overlay}
     * abstraction.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link Peer} interface of the new peer
     *         component created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(GCMVirtualNode vn,
                                                             SerializableProvider<T> overlayProvider) {
        return PeerFactory.newPeer(vn, null, overlayProvider);
    }

    /**
     * Creates a new peer component deployed on the specified
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
     * @return the reference on the {@link Peer} interface of the new peer
     *         component created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(GCMVirtualNode vn,
                                                             DeploymentConfiguration deploymentConfiguration,
                                                             SerializableProvider<T> overlayProvider) {
        return PeerFactory.createPeer(
                ComponentUtils.createContext(vn), deploymentConfiguration,
                overlayProvider);
    }

    /**
     * Creates a new peer component deployed on a node provided by the specified
     * {@code node provider} by using the specified {@code overlay} abstraction.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link Peer} interface of the new peer
     *         component created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(NodeProvider nodeProvider,
                                                             SerializableProvider<T> overlayProvider) {
        return PeerFactory.newPeer(nodeProvider, null, overlayProvider);
    }

    /**
     * Creates a new peer component deployed on a node provided by the specified
     * {@code node provider} by using the specified deployment configuration and
     * the specified {@code overlay} abstraction.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link Peer} interface of the new peer
     *         component created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(NodeProvider nodeProvider,
                                                             DeploymentConfiguration deploymentConfiguration,
                                                             SerializableProvider<T> overlayProvider) {
        return PeerFactory.createPeer(
                AbstractFactory.getContextFromNodeProvider(
                        nodeProvider, PeerImpl.PEER_VN),
                deploymentConfiguration, overlayProvider);
    }

    private static <T extends StructuredOverlay> Peer createPeer(Map<String, Object> context,
                                                                 DeploymentConfiguration deploymentConfiguration,
                                                                 SerializableProvider<T> overlayProvider) {
        try {
            Peer peer =
                    ComponentUtils.createComponentAndGetInterface(
                            PeerImpl.PEER_ADL, context,
                            PeerImpl.PEER_SERVICES_ITF, PeerInterface.class,
                            true);

            PeerAttributeController peerAttributeController =
                    (PeerAttributeController) GCM.getAttributeController(((Interface) peer).getFcItfOwner());
            if (deploymentConfiguration != null) {
                peerAttributeController.setDeploymentConfiguration(deploymentConfiguration);
            }
            peerAttributeController.setAttributes(peer, overlayProvider);

            log.info("Peer {} created", peer.getId());

            return peer;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        }
    }

}
