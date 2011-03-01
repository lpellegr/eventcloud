package fr.inria.eventcloud.messages.reply.can;

import org.objectweb.proactive.extensions.p2p.structured.api.messages.Reply;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;

import fr.inria.eventcloud.messages.request.can.RemoveStatementsRequest;
import fr.inria.eventcloud.router.can.AnycastReplyRouter;

/**
 * Indicates if a {@link RemoveStatementsRequest} has succeeded or not.
 * 
 * @author lpellegr
 */
public class RemoveStatementsReply extends AnycastReply<Boolean> {

    private static final long serialVersionUID = 1L;

    public RemoveStatementsReply(RemoveStatementsRequest query,
    									   Coordinate coordinateToReach, 
    									   Boolean result) {
        super(query, coordinateToReach);
        super.storeData(result);
    }

    public RemoveStatementsReply(RemoveStatementsRequest query,
            							   Coordinate coordinateToReach) {
        super(query, coordinateToReach);
    }

    public Boolean merge(Boolean data1, Boolean data2) {
        return data1 || data2;
    }

    public Boolean queryDataStore(StructuredOverlay overlay) {
        return null;
    }

    public AnycastReplyRouter<? extends AnycastReply<Boolean>, Boolean> getRouter() {
        return new AnycastReplyRouter<AnycastReply<Boolean>, Boolean>(null);
    }

    /**
     * Not used because this message is never called from the public API.
     */
    public Reply createResponse() {
        return null;
    }

}
