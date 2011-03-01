package fr.inria.eventcloud.api.messages.reply;

import fr.inria.eventcloud.api.messages.request.SparqlAskQuery;
import fr.inria.eventcloud.messages.reply.can.SparqlAskReply;

/**
 * Associated response for {@link SparqlAskQuery}.
 * 
 * @author lpellegr
 */
public class SparqlAskResponse extends Sparqlresponse<Boolean> {

    private static final long serialVersionUID = 1L;

    public SparqlAskResponse(SparqlAskReply response) {
        super(response.mergeAndGetDataRetrieved(), 
        	  response.getOutboundHopCount(), 
        	  response.getInboundHopCount(), 
        	  response.getLatency(),
        	  response.getQueryDatastoreTime(),
        	  response.getFilterTime());
    }

}
