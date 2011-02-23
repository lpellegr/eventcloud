package fr.inria.eventcloud.api.messages.request;

import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.messages.reply.can.AnycastReply;
import fr.inria.eventcloud.messages.reply.can.SparqlConstructReply;
import fr.inria.eventcloud.messages.request.can.SemanticRequest;

/**
 * @author lpellegr
 */
public class SparqlConstructQuery extends SparqlQuery {

    private static final long serialVersionUID = 1L;

    public SparqlConstructQuery(URI spaceURI, String queryAsString) {
        super(spaceURI, queryAsString);
    }

    public AnycastReply<?> createResponseMessage(SemanticRequest query) {
        return new SparqlConstructReply(query, query.getKeyToReach());
    }

}
