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
package org.objectweb.proactive.extensions.p2p.structured.factories;

import java.util.HashMap;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerAttributeController;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerImpl;
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
     * specified overlay abstraction.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link Peer} interface of the new peer
     *         component created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(SerializableProvider<T> overlayProvider) {
        return PeerFactory.createPeer(
                overlayProvider, new HashMap<String, Object>());
    }

    /**
     * Creates a new peer component deployed on the specified {@code node} by
     * using the given {@code overlay} abstraction.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the {@link Peer} interface of the new peer
     *         component created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(SerializableProvider<T> overlayProvider,
                                                             Node node) {
        return PeerFactory.createPeer(
                overlayProvider, ComponentUtils.createContext(node));
    }

    /**
     * Creates a new peer component deployed on the specified
     * {@code GCM virtual node} by using the given {@code overlay} abstraction.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link Peer} interface of the new peer
     *         component created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(SerializableProvider<T> overlayProvider,
                                                             GCMVirtualNode vn) {
        return PeerFactory.createPeer(
                overlayProvider, ComponentUtils.createContext(vn));
    }

    private static <T extends StructuredOverlay> Peer createPeer(SerializableProvider<T> overlayProvider,
                                                                 Map<String, Object> context) {
        try {
            Peer peer =
                    ComponentUtils.createComponentAndGetInterface(
                            PeerImpl.PEER_ADL, context,
                            PeerImpl.PEER_SERVICES_ITF, Peer.class, true);

            ((PeerAttributeController) GCM.getAttributeController(((Interface) peer).getFcItfOwner())).setAttributes(
                    peer, overlayProvider);

            log.info("Peer {} created", peer.getId());

            return peer;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        }
    }

}
