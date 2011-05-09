package org.objectweb.proactive.extensions.p2p.structured.api;

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
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.adl.nodes.ADLNodeProvider;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerImpl;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

/**
 * Provides some static methods in order to ease the creation of peer objects.
 * 
 * @author lpellegr
 */
public class PeerFactory {

    private static Factory factory;

    static {
        CentralPAPropertyRepository.GCM_PROVIDER.setValue(P2PStructuredProperties.GCM_PROVIDER.getValue());
        try {
            factory = FactoryFactory.getFactory();
        } catch (ADLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new peer active object on the local JVM by using the specified
     * overlay abstraction.
     * 
     * @param overlay
     *            the overlay to set.
     * 
     * @return the new active object created.
     */
    public static Peer newActivePeer(StructuredOverlay overlay) {
        return PeerFactory.newActivePeer(overlay, null);
    }

    /**
     * Creates a new peer active object on the specified {@code node} by using
     * the given {@code overlay} abstraction.
     * 
     * @param overlay
     *            the overlay to set.
     * @param node
     *            the node used by the peer.
     * 
     * @return the new active object created.
     */
    public static Peer newActivePeer(StructuredOverlay overlay, Node node) {
        try {
            return PAActiveObject.newActive(
                    PeerImpl.class, new Object[] {overlay}, node);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a new peer component on the local JVM by using the specified
     * overlay abstraction.
     * 
     * @param overlay
     *            the overlay to set.
     * 
     * @return the reference on the {@link Peer} interface of the new component
     *         created.
     */
    public static Peer newComponentPeer(StructuredOverlay overlay) {
        return PeerFactory.createComponentPeer(
                overlay, new HashMap<String, Object>());
    }

    /**
     * Creates a new peer component on the specified {@code node} by using the
     * given {@code overlay} abstraction.
     * 
     * @param overlay
     *            the overlay to set.
     * @param node
     *            the node used by the peer.
     * 
     * @return the reference on the {@link Peer} interface of the new component
     *         created.
     */
    public static Peer newComponentPeer(StructuredOverlay overlay, Node node) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (node != null) {
            List<Node> nodeList = new ArrayList<Node>(1);
            nodeList.add(node);
            context.put(ADLNodeProvider.NODES_ID, nodeList);
        }
        return createComponentPeer(overlay, context);
    }

    /**
     * Creates a new peer component on the specified {@code GCM virtual node} by
     * using the given {@code overlay} abstraction.
     * 
     * @param overlay
     *            the overlay to set.
     * @param vn
     *            the GCM virtual node used by the peer.
     * 
     * @return the reference on the {@link Peer} interface of the new component
     *         created.
     */
    public static Peer newComponentPeer(StructuredOverlay overlay,
                                        GCMVirtualNode vn) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (vn != null) {
            context.put(vn.getName(), vn);
        }
        return createComponentPeer(overlay, context);
    }

    /**
     * Creates a new peer component on {@code node} provided by the specified
     * GCM application and by using the given {@code overlay} abstraction.
     * 
     * @param overlay
     *            the overlay to set.
     * @param gcma
     *            the GCM application used by the peer.
     * 
     * @return the reference on the {@link Peer} interface of the new component
     *         created.
     */
    public static Peer newComponentPeer(StructuredOverlay overlay,
                                        GCMApplication gcma) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (gcma != null) {
            context.put("deployment-descriptor", gcma);
        }
        return createComponentPeer(overlay, context);
    }

    private static Peer createComponentPeer(StructuredOverlay overlay,
                                            Map<String, Object> context) {
        try {
            Component peer =
                    (Component) factory.newComponent(
                            P2PStructuredProperties.PEER_ADL.getValue(),
                            context);
            Peer stub =
                    (Peer) peer.getFcInterface(P2PStructuredProperties.PEER_SERVICES_ITF.getValue());
            stub.init(stub, overlay);
            GCM.getGCMLifeCycleController(peer).startFc();

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
