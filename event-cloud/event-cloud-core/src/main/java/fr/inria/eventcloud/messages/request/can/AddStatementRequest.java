package fr.inria.eventcloud.messages.request.can;

import java.io.IOException;

import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
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

import fr.inria.eventcloud.kernel.operations.datastore.DatastoreOperation;
import fr.inria.eventcloud.kernel.operations.datastore.AddStatementOperation;
import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * 
 * @author lpellegr
 */
public class AddStatementRequest extends ForwardRequest {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = 
            LoggerFactory.getLogger(AddStatementRequest.class);

    private byte[] operation;

    public AddStatementRequest(final URI space, final Statement statement) {
        super(SemanticHelper.createCoordinateFrom(statement));
        this.operation = 
            toByteArray(
                    new AddStatementOperation(space, statement));
    }

    public AddStatementRequest(final URI space, final Resource subject, final URI predicate,
            final String literal) {
        super(SemanticHelper.createCoordinateArrayFrom(subject, predicate, literal));
        this.operation = 
            toByteArray(
                    new AddStatementOperation(
                            space, subject, predicate, literal));
    }

    public AddStatementRequest(final URI space, final Resource subject, final URI predicate,
            final String literal, final String languageTag) {
        super(SemanticHelper.createCoordinateArrayFrom(subject, predicate, literal));
        this.operation = 
            toByteArray(
                    new AddStatementOperation(
                            space, subject, predicate, literal));
    }

    public AddStatementRequest(final URI space, final Resource subject, final URI predicate,
            final String literal, final URI datatypeURI) {
        super(SemanticHelper.createCoordinateArrayFrom(subject, predicate, literal));
        this.operation = 
            toByteArray(
                    new AddStatementOperation(
                            space, subject, predicate, literal, datatypeURI));
    }

    public AddStatementRequest(final URI space, final String subjectURIString,
            final URI predicate, final String literal) {
        super(SemanticHelper.createCoordinateArrayFrom(subjectURIString, predicate, literal));
        this.operation = 
            toByteArray(
                    new AddStatementOperation(
                            space, subjectURIString, predicate, literal));
    }

    public AddStatementRequest(final URI space, final String subjectURIString,
            final URI predicate, final String literal, final String languageTag) {
        super(SemanticHelper.createCoordinateArrayFrom(subjectURIString, predicate, literal));
        this.operation = 
            toByteArray(
                    new AddStatementOperation(
                            space, subjectURIString, predicate, literal, languageTag));
    }

    public AddStatementRequest(final URI space, final String subjectURIString,
            final URI predicate, final String literal, final URI datatypeURI) {
        super(SemanticHelper.createCoordinateArrayFrom(subjectURIString, predicate, literal));
        this.operation = 
            toByteArray(
                new AddStatementOperation(
                        space, subjectURIString, predicate, literal, datatypeURI));
    }

    public byte[] getOperation() {
        return this.operation;
    }

    public Router<ForwardRequest, Coordinate> getRouter() {
        return new UnicastQueryRouter<ForwardRequest>(new UnicastConstraintsValidator()) {
            protected void onDestinationReached(StructuredOverlay overlay, ForwardRequest msg) {
                try {
                    ((SemanticSpaceCanOverlay) overlay).getLocalSemanticSpaceOverlayKernel().send(
                            (DatastoreOperation) ByteToObjectConverter.ObjectStream
                                    .convert(((AddStatementRequest) msg).getOperation()));
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Destination reached " + overlay + " and triple added.");
                }
            };
        };
    }

    private static byte[] toByteArray(AddStatementOperation op) {
        byte[] result = null;
        try {
             result = ObjectToByteConverter.ObjectStream.convert(op);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    
}
