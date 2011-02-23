package fr.inria.eventcloud.initializers;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;
import fr.inria.eventcloud.api.SemanticFactory;
import fr.inria.eventcloud.api.messages.reply.SparqlAskResponse;
import fr.inria.eventcloud.api.messages.reply.SparqlConstructResponse;
import fr.inria.eventcloud.api.messages.reply.SparqlDescribeResponse;
import fr.inria.eventcloud.api.messages.reply.SparqlSelectResponse;
import fr.inria.eventcloud.api.messages.request.SparqlAskQuery;
import fr.inria.eventcloud.api.messages.request.SparqlConstructQuery;
import fr.inria.eventcloud.api.messages.request.SparqlDescribeQuery;
import fr.inria.eventcloud.api.messages.request.SparqlSelectQuery;
import fr.inria.eventcloud.deployment.NodeProvider;
import fr.inria.eventcloud.deployment.NodeProviderKey;
import fr.inria.eventcloud.kernel.SemanticSpaceOverlayKernel;
import fr.inria.eventcloud.messages.request.can.AddStatementRequest;
import fr.inria.eventcloud.messages.request.can.RemoveStatementRequest;
import fr.inria.eventcloud.messages.request.can.RemoveStatementsRequest;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.util.RDF2GoBuilder;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Initializes a space network on a local machine or by distributing the active
 * objects on several machines. A space network is represented by a CAN network.
 * 
 * @author lpellegr
 */
public class SpaceNetworkInitializer extends
		SemanticNetworkInitializer<Tracker> {

	private final static Logger logger = LoggerFactory
			.getLogger(SpaceNetworkInitializer.class);

	private ExecutorService threadsPool = Executors.newCachedThreadPool();

	private URI spaceURI;

	private NodeProvider nodeProvider;

	// very ugly but no other solution at this time!
	private FinalizeTrackersInitialization finalizeTrackerInitialization;

	public void setFinalizeTrackerInitialization(
			FinalizeTrackersInitialization task) {
		this.finalizeTrackerInitialization = task;
	}

	public SpaceNetworkInitializer(URI spaceURI) {
		this.spaceURI = spaceURI;
	}

	/**
	 * Setup a new space network composed of the specified number of peers of
	 * type CAN.
	 * 
	 * @param nbPeersCAN
	 *            the number of peers to add on the network.
	 */
	public void setUpNetworkOnLocalMachine(int nbPeersCAN) {
		if (super.initialized) {
			throw new IllegalStateException("Network already initialized");
		}

		try {
			Tracker tracker = 
				SemanticFactory.newActiveSemanticCANTracker(
							UUID.randomUUID().toString());
			super.trackers = new Tracker[] { tracker };

			if (this.finalizeTrackerInitialization != null) {
				this.finalizeTrackerInitialization.run(trackers);
			}

			SemanticSpaceOverlayKernel[] kernels = SemanticFactory
					.newActiveSemanticSpaceOverlayKernelsInParallel(nbPeersCAN,
							super.trackers, true);

			URI[] spaceURIs = new URI[nbPeersCAN];
			for (int i = 0; i < spaceURIs.length; i++) {
				spaceURIs[i] = this.spaceURI;
			}

			SemanticPeer[] peers = SemanticFactory
					.newActiveSemanticCANPeersInParallel(spaceURIs,
							Arrays.asList(super.trackers), kernels);

			this.addOnNetworkInParallel(peers);

			super.initialized = true;
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (NodeException e) {
			e.printStackTrace();
		}
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
        if (super.initialized) {
            throw new IllegalStateException("Network already initialized.");
        }

        this.nodeProvider = new NodeProvider(pathToGCMA);
        this.nodeProvider.deploy(1, 0, maxCANNodesToAcquire);

        try {
            super.trackers = new Tracker[] { 
            		SemanticFactory.newActiveSemanticCANTracker(
            				UUID.randomUUID().toString(),
            				UUID.randomUUID().toString(),
            				this.nodeProvider.getNextNode(
            						NodeProviderKey.TRACKERS)) };
            if (this.finalizeTrackerInitialization != null) {
            	this.finalizeTrackerInitialization.run(trackers);
            }
            
            // Kernels use the same nodes as peers because
            // they must be on the same JVM.
            List<Node> nodesCAN = this.nodeProvider.getAllNodes(
                    				NodeProviderKey.PEERS_CAN);;

            Map<Node, SemanticSpaceOverlayKernel> deployedKernels =
                       SemanticFactory.newActiveSemanticSpaceOverlayKernelsInParallel(
                               this.trackers, true, nodesCAN);

            SemanticPeer[] peers = SemanticFactory.newActiveSemanticCANPeersInParallel(
                                       this.spaceURI, Arrays.asList(super.trackers),
                                       deployedKernels);
           this.addOnNetworkInParallel(peers);

            super.initialized = true;
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

	private void addOnNetwork(SemanticPeer[] peers) {
		for (int i = 0; i < peers.length; i++) {
			this.getRandomTracker().addOnNetwork(peers[i]);
		}
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

	public void addStatement(String subject, String predicate, String object) {
		try {
			this.addStatement(RDF2GoBuilder.toStatement(subject, predicate,
					object));
		} catch (SemanticSpaceException e) {
			e.printStackTrace();
		}
	}

	public void addStatement(Statement stmt) {
		PAFuture.waitFor(super.getRandomTracker().getRandomPeer()
				.send(new AddStatementRequest(this.spaceURI, stmt)));

		if (logger.isInfoEnabled()) {
			StringBuffer buf = new StringBuffer();
			buf.append("Add operation has been performed ");
			buf.append("for triple <");
			buf.append(stmt.getSubject());
			buf.append(",");
			buf.append(stmt.getPredicate());
			buf.append(",");
			buf.append(stmt.getObject());
			buf.append(">");
			buf.append(".");
			logger.info(buf.toString());
		}
	}

	public void addRandomStatements(int nb) {
		for (int i = 0; i < nb; i++) {
			this.threadsPool.execute(new Runnable() {
				public void run() {
					addStatement(SemanticHelper.generateRandomStatement());
				}
			});
		}
	}

	public void removeStatement(String subject, String predicate, String object) {
		try {
			super.getRandomTracker()
					.getRandomPeer()
					.send(new RemoveStatementRequest(this.spaceURI,
							RDF2GoBuilder.toStatement(subject, predicate,
									object)));
		} catch (SemanticSpaceException e) {
			e.printStackTrace();
		}
	}

	public void removeStatements(String subject, String predicate, String object) {
		try {
			super.getRandomTracker()
					.getRandomPeer()
					.send(new RemoveStatementsRequest(this.spaceURI,
							RDF2GoBuilder.createStatement(
									subject == null ? null : RDF2GoBuilder
											.toSubject(subject),
									predicate == null ? null : RDF2GoBuilder
											.toPredicate(predicate),
									object == null ? null : RDF2GoBuilder
											.toObject(object))));
		} catch (SemanticSpaceException e) {
			e.printStackTrace();
		}
	}

	public boolean sparqlAsk(String askQuery) {
		Boolean response = false;
		try {
			response = ((SparqlAskResponse) PAFuture.getFutureValue(super
					.getRandomTracker().getRandomPeer()
					.send(new SparqlAskQuery(this.spaceURI, askQuery)))).getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}

	public Set<Statement> sparqlConstruct(String constructQuery) {
		Set<Statement> result = null;

		try {
			result = SemanticHelper.asSet(((SparqlConstructResponse) PAFuture
					.getFutureValue(super
							.getRandomTracker()
							.getRandomPeer()
							.send(new SparqlConstructQuery(this.spaceURI,
									constructQuery)))).getResult());

		} catch (DispatchException e) {
			logger.error("Error while dispatching construct query.", e);
		}

		return result;
	}

	public Set<Statement> sparqlDescribe(String describeQuery) {
		Set<Statement> result = null;

		try {
			result = SemanticHelper.asSet(((SparqlDescribeResponse) PAFuture
					.getFutureValue(super
							.getRandomTracker()
							.getRandomPeer()
							.send(new SparqlDescribeQuery(this.spaceURI,
									describeQuery)))).getResult());

		} catch (DispatchException e) {
			logger.error("Error while dispatching describe query.", e);
		}

		return result;
	}

	public QueryResultTable sparqlSelect(String selectQuery) {
		QueryResultTable result = null;

		try {
			result = ((SparqlSelectResponse) PAFuture.getFutureValue(super
					.getRandomTracker().getRandomPeer()
					.send(new SparqlSelectQuery(this.spaceURI, selectQuery))))
					.getResult();
		} catch (DispatchException e) {
			e.printStackTrace();
		}

		return result;
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
	}

	public URI getSpaceURI() {
		return this.spaceURI;
	}

}
