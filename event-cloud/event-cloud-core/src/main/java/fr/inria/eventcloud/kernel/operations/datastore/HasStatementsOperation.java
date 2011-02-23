package fr.inria.eventcloud.kernel.operations.datastore;

import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.datastore.SemanticDatastore;

/**
 * 
 * @author lpellegr
 */
public class HasStatementsOperation extends DatastoreOperation {

    private static final long serialVersionUID = 1L;

    public HasStatementsOperation(URI spaceURI) {
        super(spaceURI);
    }

    public DatastoreResponseOperation<Boolean> handle(SemanticDatastore dataStore) {
        try {
            return new DatastoreResponseOperation<Boolean>(
                        dataStore.sparqlAsk(super.spaceURI,
                                "ASK { GRAPH<" + super.spaceURI + "> { ?s ?p ?o } . }"));
        } catch (Exception e) {
            e.printStackTrace();
            return new DatastoreResponseOperation<Boolean>(false);
        }
    }

}
