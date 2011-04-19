package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.overlay.can.SemanticCanOverlay;

/**
 * Used to insert a statement over the overlay network.
 * 
 * @author lpellegr
 */
public class AddStatementRequest extends StatementRequest {

    private static final long serialVersionUID = 1L;

    public AddStatementRequest(final URI context, final Statement stmt) {
        super(context, stmt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestinationReached(StructuredOverlay overlay, URI context,
                                     Statement stmt) {
        ((SemanticCanOverlay) overlay).getDatastore().addStatement(
                context, stmt);
    }

}
