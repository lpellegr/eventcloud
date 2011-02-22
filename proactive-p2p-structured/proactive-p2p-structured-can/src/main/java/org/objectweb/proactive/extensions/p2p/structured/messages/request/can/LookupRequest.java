package org.objectweb.proactive.extensions.p2p.structured.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.reply.AbstractReply;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.can.LookupReply;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastQueryRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.UnicastConstraintsValidator;

/**
 * A <code>LookupRequest</code> is a query message which may be used in
 * order to find a peer which manages a specified coordinate on a CAN structured
 * peer-to-peer network.
 * 
 * @author Laurent Pellegrino
 */
public class LookupRequest extends ForwardRequest {

    private static final long serialVersionUID = 1L;

    protected Peer remotePeerReached;

    public LookupRequest(Coordinate coordinateToReach) {
        super(coordinateToReach);
    }

    public Peer getRemotePeerReached() {
        return this.remotePeerReached;
    }

    public void setRemotePeerReached(Peer remotePeerReached) {
        this.remotePeerReached = remotePeerReached;
    }

    /**
     * {@inheritDoc}
     */
    public Router<ForwardRequest, Coordinate> getRouter() {
        return new UnicastQueryRouter<ForwardRequest>(new UnicastConstraintsValidator()) {
            protected void onDestinationReached(StructuredOverlay overlay, ForwardRequest msg) {
                ((LookupRequest) msg).setRemotePeerReached(overlay.getRemotePeer());
            };
        };
    }

    /**
     * {@inheritDoc}
     */
    public AbstractReply<Coordinate> createResponseMessage() {
        return new LookupReply(this, this.remotePeerReached);
    }

}
