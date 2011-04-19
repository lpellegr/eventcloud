package fr.inria.eventcloud.messages.request.can;

import static com.google.common.base.Preconditions.checkNotNull;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.messages.response.can.RemoveStatementsResponse;
import fr.inria.eventcloud.overlay.can.SemanticCanOverlay;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Removes all statements that validates the specified constraints.
 * 
 * @author lpellegr
 */
public class RemoveStatementsRequest extends AnycastRequest {

    private static final long serialVersionUID = 1L;

    private URI context;

    private Statement statement;

    public RemoveStatementsRequest(final URI context, final Statement statement) {
        super(new DefaultAnycastConstraintsValidator(
                SemanticHelper.createCoordinateWithNullValues(statement)));
        this.context = checkNotNull(context);
        this.statement = statement;
    }

    public AnycastRequestRouter<AnycastRequest> getRouter() {
        return new AnycastRequestRouter<AnycastRequest>() {
            @Override
            public void onPeerValidatingKeyConstraints(AbstractCanOverlay overlay,
                                                       AnycastRequest msg) {
                ((SemanticCanOverlay) overlay).getDatastore().removeStatement(
                        context, statement);
            }
        };
    }

    public RemoveStatementsResponse createResponse() {
        return new RemoveStatementsResponse(this);
    }

}
