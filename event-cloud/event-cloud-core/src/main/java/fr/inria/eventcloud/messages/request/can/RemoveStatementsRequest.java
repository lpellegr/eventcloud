package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.kernel.operations.datastore.RemoveStatementOperation;
import fr.inria.eventcloud.messages.reply.can.RemoveStatementsReply;
import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;
import fr.inria.eventcloud.router.can.AnycastRequestRouter;
import fr.inria.eventcloud.util.SemanticHelper;
import fr.inria.eventcloud.validator.can.RemoveStatementsConstraintsValidator;

/**
 * Removes all statements that validates the specified constraints.
 * 
 * @author lpellegr
 */
public class RemoveStatementsRequest extends AnycastRequest {

    private static final long serialVersionUID = 1L;

    private static final transient Logger logger = LoggerFactory
            .getLogger(RemoveStatementRequest.class);

    private URI spaceURI;

    private Statement statement;

    public RemoveStatementsRequest(final URI space, final Statement statement) {
        super(SemanticHelper.createCoordinateFrom(statement));
        this.spaceURI = space;
        this.statement = statement;
    }

    public AnycastRequestRouter<AnycastRequest> getRouter() {
        return new AnycastRequestRouter<AnycastRequest>(new RemoveStatementsConstraintsValidator()) {
            public void onPeerWhichValidatesKeyConstraints(AbstractCanOverlay overlay, AnycastRequest msg) {
                ((SemanticSpaceCanOverlay) overlay).getLocalSemanticSpaceOverlayKernel().send(
                        new RemoveStatementOperation(
                                ((RemoveStatementsRequest)msg).getSpaceURI(),
                                ((RemoveStatementsRequest)msg).getStatement()));
                
                if (logger.isDebugEnabled()) {
                    logger.debug("Remove statements performed on {}", overlay);
                }
            }
        };
    }

    public URI getSpaceURI() {
        return this.spaceURI;
    }

    public Statement getStatement() {
        return this.statement;
    }

    public RemoveStatementsReply createResponseMessage() {
        return new RemoveStatementsReply(this, this.getKeyToReach(), true);
    }

}
