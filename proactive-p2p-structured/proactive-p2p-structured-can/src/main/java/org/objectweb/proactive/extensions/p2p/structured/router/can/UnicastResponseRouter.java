package org.objectweb.proactive.extensions.p2p.structured.router.can;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.p2p.structured.configuration.DefaultProperties;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lpellegr
 */
public class UnicastResponseRouter<T extends Response<Coordinate>> extends Router<T, Coordinate> {

    private static final Logger logger =
    	LoggerFactory.getLogger(UnicastResponseRouter.class);
    
    public UnicastResponseRouter() {
        super();
    }

    @Override
	public void makeDecision(StructuredOverlay overlay, T response) {
    	if (response.getHopCount() == 0) {
            overlay.getResponseEntries().put(
                    response.getId(), new ResponseEntry(1));
        }

    	if (response.validatesKeyConstraints(overlay)) {
            this.handle(overlay, response);
        } else {
        	this.doRoute(overlay, response);
        }
	}
    
    protected void doHandle(StructuredOverlay overlay, T response) {
        if (logger.isDebugEnabled()) {
            logger.debug("The peer " + overlay + " contains the key to reach " + response.getKey() + ".");
        }

        overlay.getRequestResponseManager().pushFinalResponse(response);
    }

    protected void doRoute(StructuredOverlay overlay, T response) {
        AbstractCanOverlay overlayCAN = ((AbstractCanOverlay) overlay);

        short dimension = 0;
        short direction = NeighborTable.ANY_DIRECTION;

        // finds the dimension on which the key to reach is not contained
        for (; dimension < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            direction = overlayCAN.contains(dimension, response.getKey().getElement(dimension));

            if (direction == -1) {
                direction = NeighborTable.INFERIOR_DIRECTION;
                break;
            } else if (direction == 1) {
                direction = NeighborTable.SUPERIOR_DIRECTION;
                break;
            }
        }
        
        // selects one neighbor in the dimension and the direction previously affected
        NeighborEntry neighborChosen = overlayCAN.nearestNeighbor(response.getKey(), dimension, direction);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "The message is routed to a neigbour because the current peer "
                    + "managing " + overlay + " does not contains the key to reach ("
                    + response.getKey() + "). Neighbor is selected from dimension " 
                    + dimension + " and direction " + direction + ": " + neighborChosen);
        }

        // sends the message to it
        try {
            response.incrementHopCount(1);
            neighborChosen.getStub().route(response);
        } catch (ProActiveRuntimeException e) {
            logger.error(
                    "Error while sending the message to the neighbor managing " 
                    + neighborChosen.getZone());
            e.printStackTrace();
        }
    }

}
