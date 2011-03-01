package fr.inria.eventcloud.kernel.operations.datastore;

import java.io.Serializable;

import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.datastore.SemanticDatastore;

/**
 * 
 * @author lpellegr
 */
public class AddStatementOperation extends DatastoreOperation {

    private static final long serialVersionUID = 1L;
    
    private InnerAddStatementOperation operation;

    public AddStatementOperation(final URI space, final Statement statement) {
        super(space);
        this.operation = new InnerAddStatementOperation() {
            private static final long serialVersionUID = 1L;

            public DatastoreResponseOperation<Boolean> handle(SemanticDatastore dataStore) {
                dataStore.addStatement(space, statement);
                return new DatastoreResponseOperation<Boolean>(true);
            };
        };
    }

    public AddStatementOperation(final URI space, final Resource subject, final URI predicate,
            final String literal) {
        super(space);
        this.operation = new InnerAddStatementOperation() {
            private static final long serialVersionUID = 1L;

            public DatastoreResponseOperation<Boolean> handle(SemanticDatastore dataStore) {
                dataStore.addStatement(space, subject, predicate, literal);
                return new DatastoreResponseOperation<Boolean>(true);
            };
        };
    }

    public AddStatementOperation(final URI space, final Resource subject, final URI predicate,
            final String literal, final String languageTag) {
        super(space);
        this.operation = new InnerAddStatementOperation() {
            private static final long serialVersionUID = 1L;

            public DatastoreResponseOperation<Boolean> handle(SemanticDatastore dataStore) {
                dataStore.addStatement(
                        space, subject, predicate, literal, languageTag);
                return new DatastoreResponseOperation<Boolean>(true);
            };
        };
    }

    public AddStatementOperation(final URI space, final Resource subject, final URI predicate,
            final String literal, final URI datatypeURI) {
        super(space);
        this.operation = new InnerAddStatementOperation() {
            private static final long serialVersionUID = 1L;

            public DatastoreResponseOperation<Boolean> handle(SemanticDatastore dataStore) {
                dataStore.addStatement(
                        space, subject, predicate, literal, datatypeURI);
                return new DatastoreResponseOperation<Boolean>(true);
            };
        };
    }

    public AddStatementOperation(final URI space, final String subjectURIString,
            final URI predicate, final String literal) {
        super(space);
        this.operation = new InnerAddStatementOperation() {
            private static final long serialVersionUID = 1L;

            public DatastoreResponseOperation<Boolean> handle(SemanticDatastore dataStore) {
                dataStore.addStatement(
                        space, subjectURIString, predicate, literal);
                return new DatastoreResponseOperation<Boolean>(true);
            };
        };
    }

    public AddStatementOperation(final URI space, final String subjectURIString,
            final URI predicate, final String literal, final String languageTag) {
        super(space);
        this.operation = new InnerAddStatementOperation() {
            private static final long serialVersionUID = 1L;

            public DatastoreResponseOperation<Boolean> handle(SemanticDatastore dataStore) {
                dataStore.addStatement(
                        space, subjectURIString, predicate, literal, languageTag);
                return new DatastoreResponseOperation<Boolean>(true);
            };
        };
    }

    public AddStatementOperation(final URI space, final String subjectURIString,
            final URI predicate, final String literal, final URI datatypeURI) {
        super(space);
        this.operation = new InnerAddStatementOperation() {
            private static final long serialVersionUID = 1L;

            public DatastoreResponseOperation<Boolean> handle(SemanticDatastore dataStore) {
                dataStore.addStatement(
                        space, subjectURIString, predicate, literal, datatypeURI);
                return new DatastoreResponseOperation<Boolean>(true);
            };
        };
    }

    public DatastoreResponseOperation<Boolean> handle(SemanticDatastore dataStore) {
        return this.operation.handle(dataStore);
    }

    public abstract class InnerAddStatementOperation implements Serializable {

        private static final long serialVersionUID = 1L;

        public InnerAddStatementOperation() {

        }

        public abstract DatastoreResponseOperation<Boolean> handle(SemanticDatastore dataStore);

    }
}
