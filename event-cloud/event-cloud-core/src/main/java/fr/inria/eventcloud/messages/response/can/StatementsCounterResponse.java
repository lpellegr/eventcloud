package fr.inria.eventcloud.messages.response.can;

import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastResponseRouter;

import fr.inria.eventcloud.messages.request.can.StatementsCounterRequest;

/**
 * Response associated to {@link StatementsCounterRequest}.
 * 
 * @author lpellegr
 */
public class StatementsCounterResponse extends AnycastResponse {

    private static final long serialVersionUID = 1L;

    private final Map<StatementsCounterRequest.Entry, Long> entries;

    public StatementsCounterResponse(StatementsCounterRequest request) {
        super(request);
        this.entries = request.getNbStatementsByPeer();
    }

    public Map<StatementsCounterRequest.Entry, Long> getEntries() {
        return this.entries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Router<? extends RequestResponseMessage<StringCoordinate>, StringCoordinate> getRouter() {
        return new AnycastResponseRouter<StatementsCounterResponse>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(AnycastResponse subResponse) {
        for (Entry<StatementsCounterRequest.Entry, Long> entry : ((StatementsCounterResponse) subResponse).getEntries()
                .entrySet()) {
            if (!this.entries.containsKey(entry.getKey())) {
                this.entries.put(entry.getKey(), entry.getValue());
            }
        }
    }

}
