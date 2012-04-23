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
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerAttributeController;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
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
public final class PeerFactory {

    private static final Logger logger =
            LoggerFactory.getLogger(PeerFactory.class);

    private static Factory factory;

    static {
        CentralPAPropertyRepository.GCM_PROVIDER.setValue(P2PStructuredProperties.GCM_PROVIDER.getValue());
        try {
            factory = FactoryFactory.getFactory();
        } catch (ADLException e) {
            e.printStackTrace();
        }
    }

    private PeerFactory() {
    }

    /**
     * Creates a new peer component on the local JVM by using the specified
     * overlay abstraction.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * 
     * @return the reference on the {@link Peer} interface of the new component
     *         created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(SerializableProvider<T> overlayProvider) {
        return PeerFactory.createPeer(
                overlayProvider, new HashMap<String, Object>());
    }

    /**
     * Creates a new peer component on the specified {@code node} by using the
     * given {@code overlay} abstraction.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * @param node
     *            the node used by the peer.
     * 
     * @return the reference on the {@link Peer} interface of the new component
     *         created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(SerializableProvider<T> overlayProvider,
                                                             Node node) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (node != null) {
            List<Node> nodeList = new ArrayList<Node>(1);
            nodeList.add(node);
            context.put(ADLNodeProvider.NODES_ID, nodeList);
        }
        return PeerFactory.createPeer(overlayProvider, context);
    }

    /**
     * Creates a new peer component on the specified {@code GCM virtual node} by
     * using the given {@code overlay} abstraction.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * @param vn
     *            the GCM virtual node used by the peer.
     * 
     * @return the reference on the {@link Peer} interface of the new component
     *         created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(SerializableProvider<T> overlayProvider,
                                                             GCMVirtualNode vn) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (vn != null) {
            context.put(vn.getName(), vn);
        }
        return PeerFactory.createPeer(overlayProvider, context);
    }

    /**
     * Creates a new peer component on {@code node} provided by the specified
     * GCM application and by using the given {@code overlay} abstraction.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     * @param gcma
     *            the GCM application used by the peer.
     * 
     * @return the reference on the {@link Peer} interface of the new component
     *         created.
     */
    public static <T extends StructuredOverlay> Peer newPeer(SerializableProvider<T> overlayProvider,
                                                             GCMApplication gcma) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (gcma != null) {
            context.put("deployment-descriptor", gcma);
        }
        return PeerFactory.createPeer(overlayProvider, context);
    }

    private static <T extends StructuredOverlay> Peer createPeer(SerializableProvider<T> overlayProvider,
                                                                 Map<String, Object> context) {
        try {
            Component peer =
                    (Component) factory.newComponent(
                            P2PStructuredProperties.PEER_ADL.getValue(),
                            context);
            Peer stub =
                    (Peer) peer.getFcInterface(P2PStructuredProperties.PEER_SERVICES_ITF.getValue());

            ((PeerAttributeController) GCM.getAttributeController(peer)).setAttributes(
                    stub, overlayProvider);

            GCM.getGCMLifeCycleController(peer).startFc();

            logger.info("Peer {} has been created", stub.getId());

            return stub;
        } catch (ADLException e) {
            e.printStackTrace();
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        } catch (IllegalLifeCycleException e) {
            e.printStackTrace();
        }

        return null;
    }

}
