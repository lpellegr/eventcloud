package fr.inria.eventcloud.api.messages.request;

import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.messages.reply.can.SparqlSelectReply;
import fr.inria.eventcloud.messages.reply.can.SemanticReply;
import fr.inria.eventcloud.messages.request.can.SemanticRequest;

/**
 * @author lpellegr
 */
public class SparqlSelectQuery extends SparqlQuery {

    private static final long serialVersionUID = 1L;

    public SparqlSelectQuery(URI spaceURI, String queryAsString) {
        super(spaceURI, queryAsString);
    }

    public SemanticReply<?> createResponseMessage(SemanticRequest query) {
        return new SparqlSelectReply(query, query.getKeyToReach());
    }

}
