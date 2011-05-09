package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.datastore.SemanticDatastore;

/**
 * Used to remove a statement from the overlay network.
 * 
 * @author lpellegr
 */
public class RemoveStatementRequest extends StatementRequest {

    private static final long serialVersionUID = 1L;

    private final static Logger logger =
            LoggerFactory.getLogger(RemoveStatementRequest.class);

    public RemoveStatementRequest(URI context, Statement stmt) {
        super(context, stmt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestinationReached(StructuredOverlay overlay, URI context,
                                     Statement stmt) {
        ((SemanticDatastore) overlay.getDatastore()).removeStatement(
                context, stmt);

        if (logger.isDebugEnabled()) {
            logger.debug("Statement (" + context + ", " + stmt
                    + ") removed from " + overlay);
        }
    }

}
