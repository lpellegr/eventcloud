package fr.inria.eventcloud.messages.reply.can;

import java.util.Map;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.api.messages.Reply;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;

import fr.inria.eventcloud.messages.request.can.StatementsCounterRequest;
import fr.inria.eventcloud.router.can.AnycastReplyRouter;

/**
 * Response associated to {@link StatementsCounterRequest}.
 * 
 * @author lpellegr
 */
public class StatementsCounterReply extends AnycastReply<Map<UUID, Integer>> {

    private static final long serialVersionUID = 1L;

    public StatementsCounterReply(StatementsCounterRequest query,
    										Coordinate coordinateToReach, 
    										Map<UUID, Integer> result) {
        super(query, coordinateToReach);
        super.storeData(result);
    }

    public StatementsCounterReply(StatementsCounterRequest query,
    										Coordinate coordinateToReach) {
        super(query, coordinateToReach);
    }

    public Map<UUID, Integer> merge(Map<UUID, Integer> data1, Map<UUID, Integer> data2) {
        if (data2 == null) {
        	return data1;
        } else {
            data1.putAll(data2);
            return data1;
        }
    }

    public Map<UUID, Integer> queryDataStore(StructuredOverlay overlay) {
        return null;
    }

    public AnycastReplyRouter<? extends AnycastReply<Map<UUID, Integer>>, Map<UUID, Integer>> getRouter() {
        return new AnycastReplyRouter<AnycastReply<Map<UUID, Integer>>, Map<UUID, Integer>>(null);
    }

    /**
     * Not used because this message is never called from the public API.
     */
    public Reply createResponse() {
        return null;
    }

}
