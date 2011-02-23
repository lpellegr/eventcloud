package fr.inria.eventcloud.api.messages.request;

import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.messages.reply.can.SparqlAskReply;
import fr.inria.eventcloud.messages.request.can.SemanticRequest;

/**
 * @author lpellegr
 */
public class SparqlAskQuery extends SparqlQuery {

    private static final long serialVersionUID = 1L;

    public SparqlAskQuery(URI spaceURI, String queryAsString) {
        super(spaceURI, queryAsString);
    }

    public SparqlAskReply createResponseMessage(
            SemanticRequest query) {
        return new SparqlAskReply(query, query.getKeyToReach());
    }

}
