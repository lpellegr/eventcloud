package org.objectweb.proactive.extensions.p2p.structured.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.LookupResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastRequestRouter;

/**
 * A <code>LookupRequest</code> is a query message which may be used in
 * order to find a peer which manages a specified coordinate on a CAN structured
 * peer-to-peer network.
 * 
 * @author lpellegr
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
        return new UnicastRequestRouter<ForwardRequest>() {
            protected void onDestinationReached(StructuredOverlay overlay, ForwardRequest msg) {
                ((LookupRequest) msg).setRemotePeerReached(overlay.getRemotePeer());
            };
        };
    }

    /**
     * {@inheritDoc}
     */
    public Response<Coordinate> createResponse() {
        return new LookupResponse(this, this.remotePeerReached);
    }

}
