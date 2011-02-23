package fr.inria.eventcloud.messages.reply.can;

import java.io.IOException;

import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.extensions.p2p.structured.api.messages.Reply;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;
import fr.inria.eventcloud.api.messages.reply.SparqlAskResponse;
import fr.inria.eventcloud.messages.request.can.SemanticRequest;
import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;
import fr.inria.eventcloud.router.can.AnycastReplyRouter;

/**
 * @author lpellegr
 */
public class SparqlAskReply extends SemanticReply<Boolean> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(SparqlAskReply.class);
    
    public SparqlAskReply(SemanticRequest query,
    						  Coordinate coordinatesToReach,
    						  Boolean result) {
        super(query, coordinatesToReach);
        this.storeData(result);
        if (logger.isDebugEnabled()) {
            logger.debug("SparqlAskReply created for query {} with value set to {}", 
                    query.getID(), result);
        }
    }

    public SparqlAskReply(SemanticRequest query, Coordinate coordinateToReach) {
        super(query, coordinateToReach);
        if (logger.isDebugEnabled()) {
            logger.debug("SparqlAskReply created for query {} with default value (false)",
                    query.getID());
        }
    }

    public SparqlAskReply(SemanticReply<?> response,
    						  Coordinate keyToReach, Boolean result) {
	super (response, keyToReach);
	this.storeData(result);
    }

    public Boolean merge(Boolean data1, Boolean data2) {
        return data1 || data2;
    }

    public Boolean performQueryDataStore(StructuredOverlay overlay) {
        if (logger.isDebugEnabled()) {
            logger.debug("QueryDataStore on {} for ask query.", overlay);
        }
        try {
            return ((SemanticSpaceCanOverlay) overlay).getLocalSemanticSpaceOverlayKernel()
                    .sparqlAsk(super.getSparqlQuery().getSpaceURI(),
                            super.getSparqlQuery().toString());
        } catch (SemanticSpaceException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Redefined in order to merge data instead of storing intermediate results.
     * Indeed, an ask query response message returns one and only one result
     * which is a boolean. So the intermediate information can be merged in
     * order to avoid to have to carry some useless information.
     * 
     * @see AnycastReply#storeData(Object)
     */
    public void storeData(Boolean data) {
        if (super.dataRetrieved.size() == 0) {
            super.storeData(data);
        } else {
            try {
                super.storeData(
                        this.merge(
                                (Boolean) ByteToObjectConverter.ObjectStream.convert(
                                        super.dataRetrieved.remove(0).getBytes()), data));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public AnycastReplyRouter<? extends AnycastReply<Boolean>, Boolean> getRouter() {
        return new AnycastReplyRouter<SemanticReply<Boolean>, Boolean>(null);
    }

    public Reply createResponse() {
        return new SparqlAskResponse(this);
    }

}
