package fr.inria.eventcloud.api;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.deployment.NodeProvider;
import fr.inria.eventcloud.deployment.NodeProviderKey;
import fr.inria.eventcloud.kernel.SemanticSpaceOverlayKernel;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.overlay.SemanticStructuredOverlay;
import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;
import fr.inria.eventcloud.tracker.SemanticCanTracker;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * SemanticFactory can be used to create new instances of Semantic objects like
 * for example {@link SemanticTracker}, {@link SemanticSpaceCanOverlay},
 * {@link SemanticSpaceChordOverlay} or {@link SemanticPeer}.
 * 
 * @author lpellegr
 */
public class SemanticFactory {

    /**
     * Creates a new {@link SemanticSpaceOverlayKernel} active object.
     * 
     * @param trackers
     *            the trackers that the kernel can query in order to have an
     *            entry point in the network.
     * @param autoRemove
     *            indicates if the repository associated to the kernel is
     *            removed when the active object is terminated.
     * @return the new SemanticSpaceoverlayKernel created.
     * 
     * @throws ActiveObjectCreationException
     *             if a problem occurs during the creation of the active object.
     * @throws NodeException
     *             if a problem occurs during the deployment.
     */
    public static SemanticSpaceOverlayKernel newActiveSemanticSpaceOverlayKernel(
            Tracker[] trackers, boolean autoRemove) throws ActiveObjectCreationException,
            NodeException {
        return (SemanticSpaceOverlayKernel) PAActiveObject.newActive(
                SemanticSpaceOverlayKernel.class.getCanonicalName(),
                    new Object[] { trackers, autoRemove });
    }

    /**
     * Creates a new {@link SemanticSpaceOverlayKernel} active object.
     * 
     * @param trackers
     *            the trackers that the kernel can query in order to have an
     *            entry point in the network.
     * @param node
     *            the node to use for deployment.
     * @param autoRemove
     *            indicates if the repository associated to the kernel is
     *            removed when the active object is terminated.
     * @return the new SemanticSpaceoverlayKernel created.
     * 
     * @throws ActiveObjectCreationException
     *             if a problem occurs during the creation of the active object.
     * @throws NodeException
     *             if a problem occurs during the deployment.
     */
    public static SemanticSpaceOverlayKernel newActiveSemanticSpaceOverlayKernel(
            Tracker[] trackers, Node node, boolean autoRemove)
                throws ActiveObjectCreationException, NodeException {
        return (SemanticSpaceOverlayKernel) PAActiveObject.newActive(
                SemanticSpaceOverlayKernel.class.getCanonicalName(),
                    new Object[] { trackers, autoRemove }, node);
    }

    /**
     * Creates a new {@link SemanticSpaceOverlayKernel} active object.
     * 
     * @param trackers
     *            the trackers that the kernel can query in order to have an
     *            entry point in the network.
     * @param nodeProvider
     *            the {@link NodeProvider} to use for the deployment of the new
     *            {@link SemanticSpaceOverlayKernel}.
     * @param autoRemove
     *            indicates if the repository associated to the kernel is
     *            removed when the active object is terminated.
     * @return the new Peer object created.
     * 
     * @throws ActiveObjectCreationException
     *             if a problem occurs during the creation of the active object.
     * @throws NodeException
     *             if a problem occurs during the deployment.
     */
    public static SemanticSpaceOverlayKernel newActiveSemanticSpaceOverlayKernel(
            Tracker[] trackers, NodeProvider nodeProvider, boolean autoRemove)
            throws ActiveObjectCreationException, NodeException {
        return (SemanticSpaceOverlayKernel) PAActiveObject.newActive(
                    SemanticSpaceOverlayKernel.class.getCanonicalName(),
                    new Object[] { trackers, autoRemove },
                    nodeProvider == null ? null : nodeProvider.getNextNode(NodeProviderKey.PEERS_CAN));
    }

    /**
     * Creates the specified number of kernels in parallel.
     * 
     * @param nbKernels
     *            the number of {@link SemanticSpaceOverlayKernel} to create.
     * @param trackers
     *            the {@link Tracker}s used by each kernel.
     * @param autoRemove
     *            indicates if the kernels which are active objects must remove
     *            their repository when their activity is terminated.
     * @return the kernels created in parallel.
     */
    public static SemanticSpaceOverlayKernel[] newActiveSemanticSpaceOverlayKernelsInParallel(
            int nbKernels, final Tracker[] trackers, final boolean autoRemove) {
        final SemanticSpaceOverlayKernel[] kernels = new SemanticSpaceOverlayKernel[nbKernels];

        ExecutorService executor = Executors.newCachedThreadPool();
        final CountDownLatch doneSignal = new CountDownLatch(nbKernels);

        for (int i = 0; i < nbKernels; i++) {
            final int index = i;
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        kernels[index] = SemanticFactory.newActiveSemanticSpaceOverlayKernel(
                                trackers, autoRemove);
                    } catch (ActiveObjectCreationException e) {
                        e.printStackTrace();
                    } catch (NodeException e) {
                        e.printStackTrace();
                    } finally {
                    	doneSignal.countDown();
                    }
                }
            });
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        return kernels;
    }

    /**
     * Creates {@link SemanticSpaceOverlayKernel}s in parallel by using one node
     * by {@link SemanticSpaceOverlayKernel} object.
     * 
     * @param trackers
     *            the {@link Tracker}s used by each kernel.
     * @param autoRemove
     *            indicates if the kernels which are active objects must remove
     *            their repository when their activity is terminated.
     * @param nodes
     *            nodes to use for deploying each
     *            {@link SemanticSpaceOverlayKernel} created.
     * @return the {@link SemanticSpaceOverlayKernel}s created associated with
     *         their node.
     */
    public static Map<Node, SemanticSpaceOverlayKernel> newActiveSemanticSpaceOverlayKernelsInParallel(
            final Tracker[] trackers, final boolean autoRemove, List<Node> nodes) {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        final CountDownLatch doneSignal = new CountDownLatch(nodes.size());
        final Map<Node, SemanticSpaceOverlayKernel> result = 
                new ConcurrentHashMap<Node, SemanticSpaceOverlayKernel>();

        for (final Node node : nodes) {
            threadPool.execute(new Runnable() {
                public void run() {
                    try {
                        result.put(node, newActiveSemanticSpaceOverlayKernel(
                                                  trackers, node, autoRemove));
                    } catch (ActiveObjectCreationException e) {
                        e.printStackTrace();
                    } catch (NodeException e) {
                        e.printStackTrace();
                    } finally {
                    	doneSignal.countDown();
                    }
                }
            });
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }

        return result;
    }

    /**
     * Creates a new {@link SemanticCanTracker} active object.
     * 
     * @param associatedNetworkName
     *            the name of the network to which the tracker is associated.
     * @return the new Tracker object created.
     * 
     * @throws ActiveObjectCreationException
     *             if a problem occurs during the creation of the active object.
     * @throws NodeException
     *             if a problem occurs during the deployment.
     */
    public static SemanticCanTracker newActiveSemanticCANTracker(String associatedNetworkName)
            throws ActiveObjectCreationException, NodeException {
        return SemanticFactory.newActiveSemanticCANTracker(
        			associatedNetworkName, UUID.randomUUID().toString());
    }

    /**
     * Creates a new {@link SemanticCanTracker} active object.
     * 
     * @param trackerName
     *            the name of the tracker.
     * @param associatedNetworkName
     *            the name of the network to which the tracker is associated.
     * @return the new Tracker object created.
     * 
     * @throws ActiveObjectCreationException
     *             if a problem occurs during the creation of the active object.
     * @throws NodeException
     *             if a problem occurs during the deployment.
     */
    public static SemanticCanTracker newActiveSemanticCANTracker(String associatedNetworkName,
            String trackerName) throws ActiveObjectCreationException, NodeException {
        return SemanticFactory.newActiveSemanticCANTracker(
        			associatedNetworkName, trackerName, null);
    }

    /**
     * Creates a new {@link SemanticCanTracker} active object.
     * 
     * @param associatedNetworkName
     *            the name of the network to which the tracker is associated.
     * @param trackerName
     *            the name of the tracker.
     * @param node
     *            the node to use for deployment.
     * @return the new Tracker object created.
     * 
     * @throws ActiveObjectCreationException
     *             if a problem occurs during the creation of the active object.
     * @throws NodeException
     *             if a problem occurs during the deployment.
     */
    public static SemanticCanTracker newActiveSemanticCANTracker(String associatedNetworkName,
            String trackerName, Node node) throws ActiveObjectCreationException, NodeException {

        Object[] constructorParameters;
        if (trackerName == null) {
            constructorParameters = new Object[] { associatedNetworkName };
        } else {
            constructorParameters = new Object[] { associatedNetworkName, trackerName };
        }

        return (SemanticCanTracker) PAActiveObject.newActive(SemanticCanTracker.class,
                constructorParameters, node);
    }
    

    /**
     * Creates a new {@link SemanticPeer} active object by using the specified
     * overlay abstraction.
     * 
     * @param remoteTrackers
     *            the {@link Tracker}s which serve as entry point.
     * @param overlay
     *            the overlay to set.
     * @return the new Peer object created.
     * 
     * @throws ActiveObjectCreationException
     *             if a problem occurs during the creation of the active object.
     * @throws NodeException
     *             if a problem occurs during the deployment.
     */
    public static SemanticPeer newActiveSemanticPeer(List<Tracker> remoteTrackers,
            SemanticStructuredOverlay overlay) throws ActiveObjectCreationException, NodeException {
        return SemanticFactory.newActiveSemanticPeer(remoteTrackers, overlay, null);
    }

    /**
     * Creates a new {@link SemanticPeer} active object by using the specified
     * overlay abstraction and the given node which indicates where to deploy
     * the object.
     * 
     * @param remoteTrackers
     *            the {@link Tracker}s which serves as entry point.
     * @param overlay
     *            the overlay to set.
     * @param node
     *            the node used by the peer.
     * @return the new {@link SemanticPeer} stub object created.
     * 
     * @throws ActiveObjectCreationException
     *             if a problem occurs during the creation of the active object.
     * @throws NodeException
     *             if a problem occurs during the deployment.
     */
    public static SemanticPeer newActiveSemanticPeer(List<Tracker> remoteTrackers, 
            SemanticStructuredOverlay overlay, Node node) throws ActiveObjectCreationException, NodeException {
        return (SemanticPeer) PAActiveObject.turnActive(
                new SemanticPeer(remoteTrackers, overlay), node);
    }

    /**
     * Creates {@link SemanticPeer}s of type CAN in parallel by using one
     * {@link Node} by peer object.
     * 
     * @param spaceURIs
     *            {@link URI}s associated to each peer.
     * @param remoteTrackers
     *            the {@link Tracker}s which serve as entry point.
     * @param kernels
     *            {@link SemanticSpaceOverlayKernel}s (active objects)
     *            associated to each peer.
     * @return the {@link SemanticSpaceOverlayKernel}s created.
     */
    public static SemanticPeer[] newActiveSemanticCANPeersInParallel(
            final URI[] spaceURIs, final List<Tracker> remoteTrackers,
            final SemanticSpaceOverlayKernel[] kernels) {
        assert spaceURIs.length == kernels.length;

        final SemanticPeer[] peers = new SemanticPeer[spaceURIs.length];

        ExecutorService threadPool = Executors.newCachedThreadPool();
        final CountDownLatch doneSignal = new CountDownLatch(spaceURIs.length);

        for (int i = 0; i < spaceURIs.length; i++) {
            final int index = i;
            threadPool.execute(new Runnable() {
                public void run() {
                    try {
                        peers[index] = SemanticFactory
                                .newActiveSemanticPeer(remoteTrackers, new SemanticSpaceCanOverlay(
                                        spaceURIs[index], kernels[index]));
                    } catch (ActiveObjectCreationException e) {
                        e.printStackTrace();
                    } catch (NodeException e) {
                        e.printStackTrace();
                    } finally {
                    	doneSignal.countDown();
                    }
                }
            });
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }

        return peers;
    }

    /**
     * Creates {@link SemanticPeer}s of type CAN in parallel by using one
     * {@link Node} by peer object.
     * 
     * @param spaceURI
     *            {@link URI} associated to each peer.
     * @param remoteTrackers
     *            the {@link Tracker}s which serve as entry point.
     * @param deployedKernels
     *            the {@link SemanticSpaceCanOverlay}s that have already be
     *            deployed with their associated {@link Node} used for
     *            deployment.
     * @return the {@link SemanticPeer}s created.
     */
    public static SemanticPeer[] newActiveSemanticCANPeersInParallel(final URI spaceURI, 
            final List<Tracker> remoteTrackers, Map<Node, SemanticSpaceOverlayKernel> deployedKernels) {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        final CountDownLatch doneSignal = new CountDownLatch(deployedKernels.size());
        final SemanticPeer[] result = new SemanticPeer[deployedKernels.size()];

        int i = 0;
        for (final Entry<Node, SemanticSpaceOverlayKernel> entry : 
                                            deployedKernels.entrySet()) {
            final int index = i;
            threadPool.execute(new Runnable() {
                public void run() {
                    try {
                        SemanticPeer peer = new SemanticPeer(remoteTrackers,
                                new SemanticSpaceCanOverlay(spaceURI, entry.getValue()));
                        result[index] =  (SemanticPeer) PAActiveObject.turnActive(
                                                peer, entry.getKey());
                    } catch (ActiveObjectCreationException e) {
                        e.printStackTrace();
                    } catch (NodeException e) {
                        e.printStackTrace();
                    } finally {
                    	doneSignal.countDown();
                    }
                }
            });
            i++;
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }

        return result;
    }

}
