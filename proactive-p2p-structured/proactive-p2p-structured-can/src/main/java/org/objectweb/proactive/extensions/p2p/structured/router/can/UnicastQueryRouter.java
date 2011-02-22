package org.objectweb.proactive.extensions.p2p.structured.router.can;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.p2p.structured.configuration.DefaultProperties;
import org.objectweb.proactive.extensions.p2p.structured.messages.PendingReplyEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestReplyMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.AbstractRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCANOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Router used to route {@link RequestReplyMessage}s from a {@link Peer} to an
 * another {@link Peer}.
 * 
 * @author Laurent Pellegrino
 */
public class UnicastQueryRouter<T extends AbstractRequest<Coordinate>> 
                                              extends Router<T, Coordinate> {

    private static final Logger logger = 
    	 LoggerFactory.getLogger(UnicastQueryRouter.class);

    public UnicastQueryRouter(ConstraintsValidator<Coordinate> validator) {
        super(validator);
    }

	@Override
	public void makeDecision(StructuredOverlay overlay, T msg) {
		if (msg.getHopCount() == 0) {
			overlay.getRepliesReceived().put(
					msg.getID(), new PendingReplyEntry(1));
		}

		if (((AbstractCANOverlay) overlay).contains(msg.getKeyToReach())) {
			this.handle(overlay, msg);
		} else {
			this.performRoute(overlay, msg);
		}
	}
    
    protected void performHandle(StructuredOverlay overlay, T msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                        "Peer " + overlay + " contains the key to reach " 
                        + msg.getKeyToReach() + " for message ID " + msg.getID() + ".");
        }
        this.onDestinationReached(overlay, msg);
        msg.createResponseMessage().route(overlay);
    }

    protected void performRoute(StructuredOverlay overlay, T msg) {
        AbstractCANOverlay overlayCAN = ((AbstractCANOverlay) overlay);

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
            msg.incrementHopCount(1);
            neighborChosen.getStub().route(msg);
        } catch (ProActiveRuntimeException e) {
            logger.error(
                    "Error while sending the message to the neighbor managing " 
                    + neighborChosen.getZone());
            e.printStackTrace();
        }
    }

}
