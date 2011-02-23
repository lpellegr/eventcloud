package fr.inria.eventcloud.kernel.operations.datastore;

import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.datastore.SemanticDatastore;

/**
 * 
 * @author lpellegr
 */
public class SparqlAskOperation extends DatastoreOperation {

    private static final long serialVersionUID = 1L;

    private String query;

    public SparqlAskOperation(URI spaceURI, String query) {
        super(spaceURI);
        this.query = query;
    }

    public DatastoreResponseOperation<Boolean> handle(SemanticDatastore dataStore) {
        return new DatastoreResponseOperation<Boolean>(
        		dataStore.sparqlAsk(super.spaceURI, this.query));
    }

}