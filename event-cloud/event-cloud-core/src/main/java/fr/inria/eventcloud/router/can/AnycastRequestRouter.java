package fr.inria.eventcloud.router.can;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.p2p.structured.configuration.DefaultProperties;
import org.objectweb.proactive.extensions.p2p.structured.messages.PendingReplyEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.messages.reply.can.AnycastReply;
import fr.inria.eventcloud.messages.request.can.AnycastRequest;
import fr.inria.eventcloud.messages.request.can.AnycastRoutingEntry;
import fr.inria.eventcloud.overlay.SemanticQueryManager;
import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;

/**
 * Router used to route {@link AnycastRequest}s.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the type of message to route
 */
public abstract class AnycastRequestRouter<T extends AnycastRequest> 
                extends Router<AnycastRequest, Coordinate> {

    private static final transient Logger logger = 
        LoggerFactory.getLogger(AnycastRequestRouter.class);

    public AnycastRequestRouter(ConstraintsValidator<Coordinate> validator) {
        super(validator);
    }
    
    /**
     * This method is called when a message reach a peer which validates
     * constraints of routing just before the next routing step.
     * 
     * @param overlay
     *            the {@link AbstractCanOverlay} of the peer which validates
     *            constraints.
     * 
     * @param msg the message which is handled.
     */
    public abstract void onPeerWhichValidatesKeyConstraints(AbstractCanOverlay overlay, AnycastRequest msg);

    /**
     * {@inheritDoc}
     */
    public void makeDecision(StructuredOverlay overlay, AnycastRequest msg) {
    	SemanticQueryManager queryManager = 
    		(SemanticQueryManager) ((SemanticSpaceCanOverlay) overlay).getQueryManager();
    	
		// the current overlay has already received the message
    	if (queryManager.getQueriesIdentifierMet().contains(msg.getId())) {
			msg.getAnycastRoutingList().removeLast().getPeerStub().route(msg.createResponseMessage());
			if (logger.isDebugEnabled()) {
				logger.debug(
						"On peer " + overlay + " which has already received the query " 
						+ msg.getId() + ", empty response sent back.");
			}
		} else {
			queryManager.getQueriesIdentifierMet().add(msg.getId());
			
			// the current overlay validates the constraints
			if (super.validatesKeyConstraints(
					((AbstractCanOverlay) overlay), msg.getKeyToReach())) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"Route on peer " + overlay + " which validates constraints.");
				}
				this.onPeerWhichValidatesKeyConstraints(((AbstractCanOverlay) overlay), msg);
				
				// we have to send the message to other neighbors which validates the constraints
				this.performHandle(overlay, msg);
			} else {
				this.route(overlay, msg);
			}
		}
    }

	/**
	 * When this method is called we can be sure that the specified
	 * <code>overlay</code> validates the constraints. The next step is to
	 * propagate the message <code>msg</code> to neighbors which validates the
	 * constraints too.
	 */
    protected void performHandle(final StructuredOverlay overlay, final AnycastRequest msg) {
        // the current peer has no neighbors: it means that the query can 
        // only be handled by himself;
        if (((SemanticSpaceCanOverlay) overlay).getNeighborTable().size() == 0) {
            super.onDestinationReached(overlay, msg);
            overlay.getRepliesReceived().put(
            		msg.getId(), new PendingReplyEntry(1));
            AnycastReply<?> response = msg.createResponseMessage();
            response.queryDataStoreAndStoreData(overlay);
            response.incrementHopCount(1);
            overlay.route(response);
        } else {
        	NeighborTable neighborsToSendTo = getNeighborsToSendTo(overlay, msg);
        	
			// neighborsToSendTo equals 0 means that we don't have to route the
			// query anymore: we are on a leaf and response must be returned;
			if (neighborsToSendTo.size() == 0) {
				super.onDestinationReached(overlay, msg);
				AnycastReply<?> response = 
					(AnycastReply<?>) msg.createResponseMessage();
				response.queryDataStoreAndStoreData(overlay);
				response.incrementHopCount(1);

				if (msg.getAnycastRoutingList().size() > 0) {
					response.getAnycastRoutingList().removeLast()
						.getPeerStub().route(response);
				} else {
					System.err.println("AnycastQueryRouter.performHandle() ICI???");
					overlay.getRepliesReceived().put(msg.getId(),
							new PendingReplyEntry(1));
					overlay.route(response);
				}
			}
			// neighborsToSendTo > 0 means we have to perform many send
			// operation and the current peer must await for the number 
			// of responses sent.
			else {
				// adds entry containing the number of responses awaited
				final PendingReplyEntry entry = 
					new PendingReplyEntry(neighborsToSendTo.size());

				overlay.getRepliesReceived().put(msg.getId(), entry);

				FutureTask<Object> task = new FutureTask<Object>(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						return msg.createResponseMessage().queryDataStore(overlay);
					}
				});
				
				// memorizes the task that query the datastore
				((SemanticQueryManager) overlay.getQueryManager())
					.getPendingQueries().put(msg.getId(), task);
				
				// performs query datastore while query is sent to neighbors in
				// order to overlap the network communications.
				new Thread(task).start();

				// constructs the routing list used by responses for return trip
				msg.getAnycastRoutingList().add(
						new AnycastRoutingEntry(overlay.getId(),
								overlay.getRemotePeer()));
				msg.incrementHopCount(1);

				for (int dim = 0; dim < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
					for (int direction = 0; direction < 2; direction++) {
						Iterator<NeighborEntry> it = neighborsToSendTo.get(dim, direction).values().iterator();
						while (it.hasNext()) {
							it.next().getStub().route(msg);
						}
					}
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("On " + overlay + ", query sent to "
						+ neighborsToSendTo.size() + " neighbor(s).");
			}
        }
        
    }

    private NeighborTable getNeighborsToSendTo(final StructuredOverlay overlay,
                                               final AnycastRequest msg) {
        NeighborTable neighborsToSendTo = new NeighborTable();

        for (int dimension = 0; dimension < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (int direction = 0; direction < 2; direction++) {
                for (NeighborEntry entry : ((SemanticSpaceCanOverlay) overlay).getNeighborTable().get(dimension, direction).values()) {
                    AnycastRoutingEntry entryCommingFromSender = 
                    	msg.getAnycastRoutingList()
                    		.getRoutingResponseEntryBy(entry.getId());
                    /*
                     * If message contains the neighbor identifier in its
                     * routing list, we know that this neighbor has
                     * already receive the query and we can remove it from the
                     * neighbors we need to send the query: it avoids sometimes a
                     * remote call.
                     */
					if (entryCommingFromSender == null
							&& this.validatesKeyConstraints(msg,
									entry.getZone())) {
						neighborsToSendTo.add(entry, dimension, direction);
					}
				}
			}
		}
        
        return neighborsToSendTo;
    }
    
    private boolean validatesKeyConstraints(AnycastRequest msg, Zone zone) {
        for (int i = 0; i < msg.getKeyToReach().size(); i++) {
            // if coordinate is null we skip the test
            if (msg.getKeyToReach().getElement(i) != null) {
                // the specified overlay does not contains the key
                if (zone.contains(i, msg.getKeyToReach().getElement(i)) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

	/**
	 * Forward the message to neighbors until a {@link Peer} which validates the
	 * constraints is found.
	 * 
	 * @param overlay
	 *            the overlay used to route the message.
	 * 
	 * @param msg
	 *            the message to route.
	 */
    protected void performRoute(StructuredOverlay overlay, AnycastRequest msg) {
        AbstractCanOverlay overlayCAN = ((AbstractCanOverlay) overlay);

		if (logger.isDebugEnabled()) {
			logger.debug("Route in order to find a peer which validates constraints.");
		}
		short dimension = 0;
        short direction = NeighborTable.ANY_DIRECTION;

        // finds the dimension on which the key to reach is not contained
        for (; dimension < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            direction = overlayCAN.contains(dimension, msg.getKeyToReach().getElement(dimension));

            if (direction == -1) {
                direction = NeighborTable.INFERIOR_DIRECTION;
                break;
            } else if (direction == 1) {
                direction = NeighborTable.SUPERIOR_DIRECTION;
                break;
            }
        }

        // selects one neighbor in the dimension and the direction previously affected
        NeighborEntry neighborChosen = overlayCAN.nearestNeighbor(msg.getKeyToReach(), dimension, direction);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "The message is routed to a neigbour because the current peer "
                    + "managing " + overlay + " does not contains the key to reach ("
                    + msg.getKeyToReach() + "). Neighbor is selected from dimension " 
                    + dimension + " and direction " + direction + ": " + neighborChosen);
        }

        // sends the message to it
        try {
            overlay.getRepliesReceived().put(
            		msg.getId(), new PendingReplyEntry(1));
			msg.getAnycastRoutingList().add(
					new AnycastRoutingEntry(
							overlay.getId(),
							overlay.getRemotePeer()));
            neighborChosen.getStub().route(msg);
        } catch (ProActiveRuntimeException e) {
            logger.error(
                    "Error while sending the message to the neighbor managing " 
                    + neighborChosen.getZone());
            e.printStackTrace();
        }
    }

}
