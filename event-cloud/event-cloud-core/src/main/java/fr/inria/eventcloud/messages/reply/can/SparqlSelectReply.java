package fr.inria.eventcloud.messages.reply.can;

import org.objectweb.proactive.extensions.p2p.structured.api.messages.Reply;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;
import fr.inria.eventcloud.api.messages.reply.SparqlSelectResponse;
import fr.inria.eventcloud.messages.request.can.AnycastRequest;
import fr.inria.eventcloud.messages.request.can.SemanticRequest;
import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;
import fr.inria.eventcloud.rdf2go.wrappers.QueryResultTableWrapper;
import fr.inria.eventcloud.router.can.AnycastReplyRouter;

/**
 * @author lpellegr
 */
public class SparqlSelectReply extends
        SemanticReply<QueryResultTableWrapper> {

    private static final long serialVersionUID = 1L;
    
    private static final transient Logger logger = LoggerFactory
            .getLogger(SparqlSelectReply.class);

    public SparqlSelectReply(SemanticReply<?> response, 
            					 Coordinate coordinatesToReach,
            					 QueryResultTableWrapper queryResultTable) {
        super(response, coordinatesToReach);
        this.storeData(queryResultTable);
    }
    
    public SparqlSelectReply(AnycastRequest query,
    							 Coordinate coordinatesToReach,
    							 QueryResultTableWrapper queryResultTable) {
        super((SemanticRequest) query, coordinatesToReach);
        this.storeData(queryResultTable);
        if (logger.isDebugEnabled()) {
            logger.debug("SparqlSelectReply created with some results merged for query {}.", query.getId());
        }
    }

    public SparqlSelectReply(SemanticRequest query,
            Coordinate coordinatesToReach) {
        super(query, coordinatesToReach);
        if (logger.isDebugEnabled()) {
            logger.debug("SparqlSelectReply created for query {}.", query.getId());
        }
    }

    /**
     * Stores the result only if it contains some data.
     * 
     * @see AnycastReply#storeData(Object)
     */
    public void storeData(QueryResultTableWrapper data) {
        if (data.getRows().size() > 0) {
            super.storeData(data);
        }
    }
    
    public QueryResultTableWrapper merge(QueryResultTableWrapper data1,
    									 QueryResultTableWrapper data2) {
        if (data1 == null) {
            throw new NullPointerException();
        }
        
        if (data2 != null) {
            data1.getRows().addAll(data2.getRows());
        }
        
        return data1;
    }
    
    public QueryResultTableWrapper performQueryDataStore(StructuredOverlay overlay) {
        QueryResultTableWrapper res = null;
        try {
            res = new QueryResultTableWrapper(((SemanticSpaceCanOverlay) overlay)
                              .getLocalSemanticSpaceOverlayKernel().sparqlSelect(
                                      super.getSparqlQuery().getSpaceURI(), 
                                      super.getSparqlQuery().toString()));
        } catch (SemanticSpaceException e) {
            e.printStackTrace();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("QueryDataStore on {} for select query ({}).", overlay, res.getRows().size());
        }
        
        return res;
    }

    public AnycastReplyRouter<? extends 
                AnycastReply<QueryResultTableWrapper>, 
                    QueryResultTableWrapper> getRouter() {
        return new AnycastReplyRouter<
            SemanticReply<QueryResultTableWrapper>, 
            QueryResultTableWrapper>(null);
    }

    public Reply createResponse() {
        return new SparqlSelectResponse(this);
    }

}
