package fr.inria.eventcloud.messages.reply.can;

import org.objectweb.proactive.extensions.p2p.structured.api.messages.Reply;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;
import fr.inria.eventcloud.api.messages.reply.SparqlDescribeResponse;
import fr.inria.eventcloud.api.messages.request.SparqlConstructQuery;
import fr.inria.eventcloud.messages.request.can.SemanticRequest;
import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;

/**
 * Response message for SPARQL query of type Construct.
 * 
 * @see AnycastReply
 * @see SparqlConstructQuery
 * 
 * @author lpellegr
 */
public class SparqlDescribeReply extends
                    ClosableIterableReply {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = 
        LoggerFactory.getLogger(SparqlDescribeReply.class);
    
    public SparqlDescribeReply(SemanticReply<?> response,
            					   Coordinate coordinateToReach, 
            					   ClosableIterableWrapper data) {
        super(response, coordinateToReach, data);
    }

    public SparqlDescribeReply(SemanticRequest query,
            Coordinate coordinateToReach) {
        super(query, coordinateToReach);
    }

    public Reply createResponse() {
        return new SparqlDescribeResponse(this);
    }
    
    public ClosableIterableWrapper performQueryDataStore(StructuredOverlay overlay) {
        if (logger.isDebugEnabled()) {
            logger.debug("QueryDataStore on {} for describe query.", overlay);
        }

        try {
            return new ClosableIterableWrapper(((SemanticSpaceCanOverlay) overlay).
                    getLocalSemanticSpaceOverlayKernel().sparqlDescribe(
                           super.getSparqlQuery().getSpaceURI(), 
                           super.getSparqlQuery().toString()));
        } catch (SemanticSpaceException e) {
            e.printStackTrace();
        }

        return null;
    }

}
