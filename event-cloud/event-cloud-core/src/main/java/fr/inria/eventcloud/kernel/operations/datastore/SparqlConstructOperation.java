package fr.inria.eventcloud.kernel.operations.datastore;

import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;

/**
 * 
 * @author lpellegr
 */
public class SparqlConstructOperation extends DatastoreOperation {

    private static final long serialVersionUID = 1L;
    
    private String query;

    public SparqlConstructOperation(URI spaceURI, String query) {
        super(spaceURI);
        this.query = query;
    }

    public DatastoreResponseOperation<ClosableIterableWrapper> handle(SemanticDatastore dataStore) {
        return new DatastoreResponseOperation<ClosableIterableWrapper>(
                    new ClosableIterableWrapper(
                            dataStore.sparqlConstruct(super.spaceURI, this.query)));
    }

}