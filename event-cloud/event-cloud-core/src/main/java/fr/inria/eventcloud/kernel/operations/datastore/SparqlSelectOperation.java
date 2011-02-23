package fr.inria.eventcloud.kernel.operations.datastore;

import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.rdf2go.wrappers.QueryResultTableWrapper;

/**
 * 
 * @author lpellegr
 */
public class SparqlSelectOperation extends DatastoreOperation {

    private static final long serialVersionUID = 1L;
    
    private String query;

    public SparqlSelectOperation(URI spaceURI, String query) {
        super(spaceURI);
        this.query = query;
    }

    public DatastoreResponseOperation<QueryResultTableWrapper> handle(SemanticDatastore dataStore) {
        return new DatastoreResponseOperation<QueryResultTableWrapper>(
                    new QueryResultTableWrapper(
                            dataStore.sparqlSelect(super.spaceURI, this.query)));
    }

}