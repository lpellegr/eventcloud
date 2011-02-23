package fr.inria.eventcloud.kernel.operations.datastore;

import java.io.Serializable;

import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.datastore.SemanticDatastore;

/**
 * 
 * @author lpellegr 
 */
public abstract class DatastoreOperation implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected URI spaceURI;

    public DatastoreOperation(URI spaceURI) {
        this.spaceURI = spaceURI;
    }

    public abstract DatastoreResponseOperation<?> handle(SemanticDatastore dataStore);

}
