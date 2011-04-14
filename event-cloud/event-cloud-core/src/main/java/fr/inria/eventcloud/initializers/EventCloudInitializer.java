package fr.inria.eventcloud.initializers;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.api.SemanticFactory;
import fr.inria.eventcloud.deployment.NodeProvider;
import fr.inria.eventcloud.messages.request.can.ShutdownRequest;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.tracker.SemanticCanTracker;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Initializes a space network on a local machine or by distributing the active
 * objects on several machines. A space network is represented by a CAN network.
 * 
 * @author lpellegr
 */
public class EventCloudInitializer extends SemanticNetworkInitializer<SemanticCanTracker> {

	private ExecutorService threadsPool;

	private NodeProvider nodeProvider;

	// very ugly but no other solution at this time!
	private FinalizeTrackersInitialization finalizeTrackerInitialization;

	public void setFinalizeTrackerInitialization(
			FinalizeTrackersInitialization task) {
		this.finalizeTrackerInitialization = task;
	}

	public EventCloudInitializer() {
		this.threadsPool = Executors.newFixedThreadPool(50);
	}

	/**
	 * Setup a new space network composed of the specified number of peers of
	 * type CAN.
	 * 
	 * @param nbCanPeers
	 *            the number of peers to add on the network.
	 */
	public void setUpNetworkOnLocalMachine(int nbCanPeers) {
		if (super.initialized) {
			throw new IllegalStateException("Network already initialized");
		}

		SemanticCanTracker tracker = 
			SemanticFactory.newActiveSemanticCanTracker(
						UUID.randomUUID().toString());
		super.trackers = new SemanticCanTracker[] { tracker };

		if (this.finalizeTrackerInitialization != null) {
			this.finalizeTrackerInitialization.run(trackers);
		}
		
		SemanticPeer[] peers = 
			SemanticFactory.newActiveSemanticCanPeersInParallel(
				nbCanPeers, super.trackers);

		this.addOnNetworkInParallel(peers);

		super.initialized = true;
	}

	/**
	 * Setup a new space network using the specified GCMA descriptor.
	 * 
	 * @param pathToGCMA
	 *            the path to the descriptor to use.
	 * 
	 * @param maxCANNodesToAcquire
	 *            the maximum number of nodes for peers of type CAN to acquire.
	 */
	public void setUpNetworkOnMultipleMachine(File pathToGCMA, int maxCANNodesToAcquire) {
//        if (super.initialized) {
//            throw new IllegalStateException("Network already initialized.");
//        }
//
//        this.nodeProvider = new NodeProvider(pathToGCMA);
//        this.nodeProvider.deploy(1, 0, maxCANNodesToAcquire);
//
//        try {
//            super.trackers = new Tracker[] { 
//            		SemanticFactory.newActiveSemanticCanTracker(
//            				UUID.randomUUID().toString(),
//            				UUID.randomUUID().toString(),
//            				this.nodeProvider.getNextNode(
//            						NodeProviderKey.TRACKERS)) };
//            if (this.finalizeTrackerInitialization != null) {
//            	this.finalizeTrackerInitialization.run(trackers);
//            }
//            
//            // Kernels use the same nodes as peers because
//            // they must be on the same JVM.
//            List<Node> nodesCAN = this.nodeProvider.getAllNodes(
//                    				NodeProviderKey.PEERS_CAN);;
//
//            Map<Node, SemanticSpaceOverlayKernel> deployedKernels =
//                       SemanticFactory.newActiveSemanticSpaceOverlayKernelsInParallel(
//                               this.trackers, true, nodesCAN);
//
//            SemanticPeer[] peers = SemanticFactory.newActiveSemanticCanPeersInParallel(
//                                       this.spaceURI, Arrays.asList(super.trackers),
//                                       deployedKernels);
//           this.addOnNetworkInParallel(peers);
//
//            super.initialized = true;
//        } catch (ActiveObjectCreationException e) {
//            e.printStackTrace();
//        } catch (NodeException e) {
//            e.printStackTrace();
//        }
    }

	private void addOnNetworkInParallel(final SemanticPeer[] peers) {
		final CountDownLatch doneSignal = new CountDownLatch(peers.length);

		for (int i = 0; i < peers.length; i++) {
			final int index = i;
			this.threadsPool.execute(new Runnable() {
				public void run() {
					try {
						getRandomTracker().addOnNetwork(peers[index]);
					} finally {
						doneSignal.countDown();
					}
				}
			});
		}

		try {
			doneSignal.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public void addRandomStatements(final URI context, int nb) {
		final CountDownLatch doneSignal = new CountDownLatch(nb);
		
		for (int i = 0; i < nb; i++) {
			this.threadsPool.execute(new Runnable() {
				public void run() {
					getRandomPeer().addStatement(context, SemanticHelper.generateRandomStatement());
					doneSignal.countDown();
				}
			});
		}
		
		try {
			doneSignal.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public SemanticPeer getRandomPeer() {
		return super.getRandomTracker().getRandomPeer();
	}

	/**
	 * Returns <code>true</code> if the network has been deployed on the local
	 * machine without using the grid component model, <code>false</code>
	 * otherwise.
	 * 
	 * @return <code>true</code> if the network has been deployed on the local
	 *         machine without using the grid component model,
	 *         <code>false</code> otherwise.
	 */
	public boolean isLocalSetup() {
		return this.nodeProvider == null;
	}

	/**
	 * Returns the {@link NodeProvider} instance used to perform the network
	 * initialization or <code>null</code> if the network has been initialized
	 * on the local JVM without using grid component model.
	 * 
	 * @return the {@link NodeProvider} instance used to perform the network
	 *         initialization or <code>null</code> if the network has been
	 *         initialized on the local JVM without using grid component model.
	 */
	public NodeProvider getNodeProvider() {
		return this.nodeProvider;
	}

	public void tearDownNetwork() {
		this.threadsPool.shutdown();
		
		// shutdowns the datastores
		try {
			this.getRandomTracker().getRandomPeer().send(new ShutdownRequest());
		} catch (DispatchException e) {
			e.printStackTrace();
		}
	}

}
