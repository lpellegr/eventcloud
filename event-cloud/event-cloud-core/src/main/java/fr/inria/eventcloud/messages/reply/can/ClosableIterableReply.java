package fr.inria.eventcloud.messages.reply.can;

import org.objectweb.proactive.extensions.p2p.structured.api.messages.Reply;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.messages.request.can.SemanticRequest;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;
import fr.inria.eventcloud.router.can.AnycastReplyRouter;

/**
 * Construct and Describe SPARQL queries share the same result type.
 * 
 * @see SparqlConstructReply
 * @see SparqlDescribeReply
 * 
 * @author lpellegr
 */
public abstract class ClosableIterableReply extends
        SemanticReply<ClosableIterableWrapper> {

    private static final long serialVersionUID = 1L;

    private static final transient Logger logger = 
        LoggerFactory.getLogger(ClosableIterableReply.class);

    public ClosableIterableReply(SemanticReply<?> response,
            							   Coordinate coordinateToReach, 
            							   ClosableIterableWrapper data) {
        super(response, coordinateToReach);
        this.storeData(data);
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "ClosableIterableResponseMessage created for query " +
                    response.getID() + " with some data retrieved.");
        }
    }

    public ClosableIterableReply(SemanticRequest query,
            							   Coordinate coordinateToReach) {
        super(query, coordinateToReach);
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "ClosableIterableResponseMessage created for query " +
                    query.getID() + " with no data retrieved."); 
        }
    }

    /**
     * Stores the result only if it contains some data.
     * 
     * @see AnycastReply#storeData(Object)
     */
    public void storeData(ClosableIterableWrapper data) {
        if (data.getData().size() > 0) {
            super.storeData(data);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public ClosableIterableWrapper merge(
            ClosableIterableWrapper data1, ClosableIterableWrapper data2) {
        if (data1 == null) {
            throw new NullPointerException();
        }
        
        if (data2 != null) {
            data1.addAll(data2.getData());
        }
        
        if (logger.isDebugEnabled()) {
            StringBuffer buf = new StringBuffer();
            buf.append("New data have been merged, response contains ");
            buf.append(data1.getData().size());
            buf.append(" data.");
            logger.debug(buf.toString());
        }
        
        return data1;
    }

    public abstract ClosableIterableWrapper performQueryDataStore(StructuredOverlay overlay);

    public abstract Reply createResponse();

    public AnycastReplyRouter<? extends 
            AnycastReply<ClosableIterableWrapper>, 
            ClosableIterableWrapper> getRouter() {
        return new AnycastReplyRouter
            <SemanticReply<ClosableIterableWrapper>, 
                ClosableIterableWrapper>(null);
    }

}
