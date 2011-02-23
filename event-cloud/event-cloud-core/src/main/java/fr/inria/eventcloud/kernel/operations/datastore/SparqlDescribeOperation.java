package fr.inria.eventcloud.kernel.operations.datastore;

import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;

/**
 * 
 * @author lpellegr
 */
public class SparqlDescribeOperation extends DatastoreOperation {

    private static final long serialVersionUID = 1L;
    
    private String query;

    public SparqlDescribeOperation(URI spaceURI, String query) {
        super(spaceURI);
        this.query = query;
    }

    public DatastoreResponseOperation<ClosableIterableWrapper> handle(SemanticDatastore dataStore) {
        return new DatastoreResponseOperation<ClosableIterableWrapper>(
                    new ClosableIterableWrapper(
                            dataStore.sparqlDescribe(super.spaceURI, this.query)));
    }

}