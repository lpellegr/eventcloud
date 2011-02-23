package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.AbstractRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;

import fr.inria.eventcloud.messages.reply.can.AnycastReply;

/**
 * Message used to dispatch a query to all peers validating the specified
 * constraints which are the coordinates to reach.
 * 
 * @author lpellegr
 */
public abstract class AnycastRequest extends AbstractRequest<Coordinate> {

    private static final long serialVersionUID = 1L;
    
    private AnycastRoutingList anycastRoutingList = new AnycastRoutingList();

    /**
     * Constructs a new message with the specified coordinates to reach.
     * 
     * @param coordinatesToReach
     *            the coordinates to reach.
     */
    public AnycastRequest(Coordinate coordinatesToReach) {
        super(coordinatesToReach);
    }

    public abstract AnycastReply<?> createResponseMessage();

    /**
     * Returns the {@link AnycastRoutingList} containing the
     * {@link AnycastRoutingEntry} to use in order to route the response.
     * 
     * @return the {@link AnycastRoutingList} containing the
     *         {@link AnycastRoutingEntry} to use in order to route the
     *         response.
     */
    public AnycastRoutingList getAnycastRoutingList() {
        return this.anycastRoutingList;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("AnycastQueryMessage ID=");
        buf.append(this.getId());

        buf.append("\nStack: \n");
        for (AnycastRoutingEntry entry : this.anycastRoutingList) {
            buf.append("  - ");
            buf.append(entry.getPeerStub());
            buf.append("\n");
        }

        return buf.toString();
    }

}
