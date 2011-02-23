package fr.inria.eventcloud.messages.request.can;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.kernel.SemanticSpaceOverlayKernel;
import fr.inria.eventcloud.kernel.operations.datastore.DatastoreResponseOperation;
import fr.inria.eventcloud.kernel.operations.datastore.SparqlSelectOperation;
import fr.inria.eventcloud.messages.reply.can.StatementsCounterReply;
import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;
import fr.inria.eventcloud.rdf2go.wrappers.QueryResultTableWrapper;
import fr.inria.eventcloud.router.can.AnycastRequestRouter;
import fr.inria.eventcloud.validator.can.RemoveStatementsConstraintsValidator;


/**
 * Count the number of statements by {@link Peer} by supposing that each
 * {@link Peer} as an unique {@link SemanticSpaceOverlayKernel}.
 * 
 * @author lpellegr
 */
public class StatementsCounterRequest extends AnycastRequest {

    private static final long serialVersionUID = 1L;

    private URI spaceURI;

    private Map<UUID, Integer> nbStatementsByPeer = new HashMap<UUID, Integer>();

    public StatementsCounterRequest(final URI space) {
        super(new Coordinate(null, null, null));
        this.spaceURI = space;
    }

    public Integer put(UUID key, Integer value) {
        return this.nbStatementsByPeer.put(key, value);
    }
    
    public AnycastRequestRouter<AnycastRequest> getRouter() {
        return new AnycastRequestRouter<AnycastRequest>( 
                new RemoveStatementsConstraintsValidator()) {
            @SuppressWarnings("unchecked")
            public void onPeerWhichValidatesKeyConstraints(AbstractCanOverlay overlay, AnycastRequest msg) {
                ((StatementsCounterRequest) msg).put(
                        overlay.getId(),
                        ((DatastoreResponseOperation<QueryResultTableWrapper>) 
                                ((SemanticSpaceCanOverlay) overlay)
                                    .getLocalSemanticSpaceOverlayKernel().send(
                                            new SparqlSelectOperation(
                                                    spaceURI, "SELECT ?s WHERE { ?s ?p ?o }")))
                                                        .getValue().getRows().size());
            }
        };
    }

    public StatementsCounterReply createResponseMessage() {
        return new StatementsCounterReply(this, this.getKeyToReach(), this.nbStatementsByPeer);
    }

}
