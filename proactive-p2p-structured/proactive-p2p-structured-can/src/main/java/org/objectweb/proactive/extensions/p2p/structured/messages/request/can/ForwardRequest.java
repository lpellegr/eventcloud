package org.objectweb.proactive.extensions.p2p.structured.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.reply.Reply;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.can.ForwardReply;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastQueryRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.UnicastConstraintsValidator;

/**
 * A <code>ForwardRequest</code> is a query message which may be in
 * order to find a peer which manages a specified coordinate on a CAN 
 * structured peer-to-peer network.
 * 
 * @author lpellegr
 */
public class ForwardRequest extends Request<Coordinate> {

    private static final long serialVersionUID = 1L;

    /**
     * The zone which is managed by the sender. It is used in order to send the
     * response when the keyToReach has been reached.
     */
    private Coordinate senderCoordinate;

    public ForwardRequest(Coordinate coordinateToReach) {
        super(coordinateToReach);
    }

    /**
     * Returns the key which is managed by the sender in order to send the
     * response when the keyToReach has been reached.
     * 
     * @return the key which is managed by the sender in order to send the
     *         response when the keyToReach has been reached.
     */
    public Coordinate getSenderCoordinate() {
        return this.senderCoordinate;
    }

    /**
     * {@inheritDoc}
     */
    public Router<ForwardRequest, Coordinate> getRouter() {
        return new UnicastQueryRouter<ForwardRequest>(
                    new UnicastConstraintsValidator());
    }

    /**
     * {@inheritDoc}
     */
    public void route(StructuredOverlay overlay) {
        if (this.senderCoordinate == null) {
            this.senderCoordinate = ((AbstractCanOverlay) overlay).getZone().getLowerBound();
        }
        super.route(overlay);
    }

    /**
     * {@inheritDoc}
     */
    public Reply<Coordinate> createResponseMessage() {
        return new ForwardReply(this);
    }

}
