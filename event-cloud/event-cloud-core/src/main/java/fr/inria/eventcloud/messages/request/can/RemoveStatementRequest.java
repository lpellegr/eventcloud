package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.ForwardRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastQueryRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.UnicastConstraintsValidator;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.kernel.operations.datastore.RemoveStatementOperation;
import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * 
 * @author lpellegr
 */
public class RemoveStatementRequest extends ForwardRequest {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory
            .getLogger(RemoveStatementRequest.class);

    private RemoveStatementOperation operation;

    public RemoveStatementRequest(final URI space, final Statement statement) {
        super(SemanticHelper.createCoordinateFrom(statement));
        this.operation = new RemoveStatementOperation(space, statement);
    }

    public RemoveStatementRequest(final URI space, final Resource subject,
            final URI predicate, final String literal) {
        super(SemanticHelper.createCoordinateArrayFrom(subject, predicate, literal));
        this.operation = new RemoveStatementOperation(space, subject, predicate, literal);
    }

    public RemoveStatementRequest(final URI space, final Resource subject,
            final URI predicate, final String literal, final String languageTag) {
        super(SemanticHelper.createCoordinateArrayFrom(subject, predicate, literal));
        this.operation = new RemoveStatementOperation(space, subject, predicate, literal,
                languageTag);
    }

    public RemoveStatementRequest(final URI space, final Resource subject,
            final URI predicate, final String literal, final URI datatypeURI) {
        super(SemanticHelper.createCoordinateArrayFrom(subject, predicate, literal));
        this.operation = new RemoveStatementOperation(space, subject, predicate, literal,
                datatypeURI);
    }

    public RemoveStatementRequest(final URI space, final String subjectURIString,
            final URI predicate, final String literal) {
        super(SemanticHelper.createCoordinateArrayFrom(subjectURIString, predicate, literal));
        this.operation = new RemoveStatementOperation(space, subjectURIString, predicate, literal);
    }

    public RemoveStatementRequest(final URI space, final String subjectURIString,
            final URI predicate, final String literal, final String languageTag) {
        super(SemanticHelper.createCoordinateArrayFrom(subjectURIString, predicate, literal));
        this.operation = new RemoveStatementOperation(space, subjectURIString, predicate, literal,
                languageTag);
    }

    public RemoveStatementRequest(final URI space, final String subjectURIString,
            final URI predicate, final String literal, final URI datatypeURI) {
        super(SemanticHelper.createCoordinateArrayFrom(subjectURIString, predicate, literal));
        this.operation = new RemoveStatementOperation(space, subjectURIString, predicate, literal,
                datatypeURI);
    }

    public RemoveStatementOperation getOperation() {
        return operation;
    }

    public Router<ForwardRequest, Coordinate> getRouter() {
        return new UnicastQueryRouter<ForwardRequest>(new UnicastConstraintsValidator()) {
            protected void onDestinationReached(StructuredOverlay overlay, ForwardRequest msg) {
                ((SemanticSpaceCanOverlay) overlay).
                    getLocalSemanticSpaceOverlayKernel().send(
                        ((RemoveStatementRequest)msg).getOperation());

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Destination {} reached and remove statement operation performed",
                            overlay);
                }
            };
        };
    }
    
}
