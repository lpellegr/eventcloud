package org.objectweb.proactive.extensions.p2p.structured.router.can;

import java.util.Iterator;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.p2p.structured.configuration.DefaultProperties;
import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanRequestResponseManager;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This router is used to route messages of type {@link AnycastRequest}.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the type of message to route
 */
public abstract class AnycastRequestRouter<T extends AnycastRequest> extends Router<AnycastRequest, Coordinate> {

    private static final Logger logger = LoggerFactory.getLogger(AnycastRequestRouter.class);

    public AnycastRequestRouter() {
        super();
    }
    
    /**
     * This method is called just before the next routing step when 
     * the request reach a peer which validates the routing constraints.
     * 
     * @param overlay
     *            the {@link AbstractCanOverlay} of the peer which validates
     *            constraints.
     * 
     * @param request the message which is handled.
     */
    public abstract void onPeerValidatingKeyConstraints(AbstractCanOverlay overlay, AnycastRequest request);

    /**
     * {@inheritDoc}
     */
    public void makeDecision(StructuredOverlay overlay, AnycastRequest request) {
    	AbstractCanOverlay canOverlay = ((AbstractCanOverlay) overlay);
    	CanRequestResponseManager messagingManager = (CanRequestResponseManager) canOverlay.getRequestResponseManager();
    	
		// the current overlay has already received the request
    	if (messagingManager.hasReceivedRequest(request.getId())) {
			request.getAnycastRoutingList().removeLast().getPeerStub().route(request.createResponse());
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Request " + request.getId() + " has reach peer " 
						+ canOverlay + " which has already received it");
			}
		} else {
			// the current overlay validates the constraints
			if (request.validatesKeyConstraints(canOverlay)) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"Request " + request.getId() + " is on peer " 
							+ overlay + " which validates constraints " + request.getKey());
				}
				
				this.onPeerValidatingKeyConstraints(canOverlay, request);
				
				messagingManager.markRequestAsReceived(request.getId());

				// sends the message to the other neighbors which validates the constraints
				this.doHandle(overlay, request);
			} else {
				this.route(overlay, request);
			}
		}
    }

	/**
	 * When this method is called we can be sure that the specified
	 * <code>overlay</code> validates the constraints. The next step is to
	 * propagate the request to the neighbors which validates the constraints.
	 */
    protected void doHandle(final StructuredOverlay overlay, final AnycastRequest request) {
    	AbstractCanOverlay canOverlay = ((AbstractCanOverlay) overlay);
    	
        // the current peer has no neighbor: this means that the query can 
        // only be handled by itself
        if (canOverlay.getNeighborTable().size() == 0) {
            super.onDestinationReached(overlay, request);
            overlay.getResponseEntries().put(request.getId(), new ResponseEntry(1));
            AnycastResponse response = request.createResponse();
            response.incrementHopCount(1);
            overlay.route(response);
        } else {
        	NeighborTable neighborsToSendTo = getNeighborsToSendTo(overlay, request);

        	// neighborsToSendTo equals 0 means that we don't have to route the
			// query anymore: we are on a leaf and the response must be returned;
			if (neighborsToSendTo.size() == 0) {
				super.onDestinationReached(overlay, request);
				AnycastResponse response = 
					(AnycastResponse) request.createResponse();
				response.incrementHopCount(1);
				overlay.getResponseEntries().put(response.getId(), new ResponseEntry(1));
				overlay.route(response);
			}
			// neighborsToSendTo > 0 means we have to perform many send
			// operation and the current peer must await for the number 
			// of responses sent.
			else {
				// adds entry containing the number of responses awaited
				final ResponseEntry entry = 
					new ResponseEntry(neighborsToSendTo.size());

				overlay.getResponseEntries().put(request.getId(), entry);

				// constructs the routing list used by responses for return trip
				request.getAnycastRoutingList().add(
						new AnycastRoutingEntry(overlay.getId(),
								overlay.getRemotePeer()));

				for (int dim = 0; dim < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
					for (int direction = 0; direction < 2; direction++) {
						Iterator<NeighborEntry> it = neighborsToSendTo.get(dim, direction).values().iterator();
						while (it.hasNext()) {
							it.next().getStub().route(request);
						}
					}
				}
			}
						
			if (logger.isDebugEnabled()) {
				logger.debug("Request " + request.getId() + " has been sent to " 
								+ neighborsToSendTo.size() + " neighbor(s) from " + overlay);
			}
        }
        
    }

    private NeighborTable getNeighborsToSendTo(final StructuredOverlay overlay,
                                               final AnycastRequest msg) {
        NeighborTable neighborsToSendTo = new NeighborTable();

        for (int dimension = 0; dimension < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (int direction = 0; direction < 2; direction++) {
                for (NeighborEntry entry : ((AbstractCanOverlay) overlay).getNeighborTable().get(dimension, direction).values()) {
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
							&& msg.validatesKeyConstraints(entry.getZone())) {
						neighborsToSendTo.add(entry, dimension, direction);
					}
				}
			}
		}
        
        return neighborsToSendTo;
    }

	/**
	 * Forward the request to the neighbors until a {@link Peer} which 
	 * validates the constraints is found.
	 * 
	 * @param overlay
	 *            the overlay used to route the message.
	 * 
	 * @param request
	 *            the message to route.
	 */
    protected void doRoute(StructuredOverlay overlay, AnycastRequest request) {
        AbstractCanOverlay overlayCAN = ((AbstractCanOverlay) overlay);

		short dimension = 0;
        short direction = NeighborTable.ANY_DIRECTION;

        // finds the dimension on which the key to reach is not contained
        for (; dimension < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            direction = overlayCAN.contains(dimension, request.getKey().getElement(dimension));

            if (direction == -1) {
                direction = NeighborTable.INFERIOR_DIRECTION;
                break;
            } else if (direction == 1) {
                direction = NeighborTable.SUPERIOR_DIRECTION;
                break;
            }
        }

        // selects one neighbor in the dimension and the direction previously affected
        NeighborEntry neighborChosen = overlayCAN.nearestNeighbor(request.getKey(), dimension, direction);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "The message is routed to a neigbour because the current peer "
                    + "managing " + overlay + " does not contains the key to reach ("
                    + request.getKey() + "). Neighbor is selected from dimension " 
                    + dimension + " and direction " + direction + ": " + neighborChosen);
        }

        // sends the message to it
        try {
            overlay.getResponseEntries().put(
            		request.getId(), new ResponseEntry(1));
			request.getAnycastRoutingList().add(
					new AnycastRoutingEntry(
							overlay.getId(),
							overlay.getRemotePeer()));
            neighborChosen.getStub().route(request);
        } catch (ProActiveRuntimeException e) {
            logger.error(
                    "Error while sending the message to the neighbor managing " 
                    + neighborChosen.getZone());
            e.printStackTrace();
        }
    }

}
