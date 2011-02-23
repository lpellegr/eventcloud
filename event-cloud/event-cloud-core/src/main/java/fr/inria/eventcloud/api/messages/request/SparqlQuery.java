package fr.inria.eventcloud.api.messages.request;

import org.objectweb.proactive.extensions.p2p.structured.api.messages.Request;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.messages.reply.can.AnycastReply;
import fr.inria.eventcloud.messages.request.can.SemanticRequest;

/**
 * SPARQLQuery is an abstract class which contains information for all sparql
 * queries like {@link SparqlAskQuery}, {@link SparqlConstructQuery} and {@link SparqlSelectQuery}.
 * 
 * @author lpellegr
 */
public abstract class SparqlQuery implements Request {

    private static final long serialVersionUID = 1L;

    private String queryAsString;

    private URI spaceURI;

    public abstract AnycastReply<?> createResponseMessage(
            SemanticRequest query);

    public SparqlQuery(URI spaceURI, String queryAsString) {
        this.spaceURI = spaceURI;
        this.queryAsString = queryAsString;
    }

    public String toString() {
        return this.queryAsString;
    }

    public URI getSpaceURI() {
        return this.spaceURI;
    }

}
