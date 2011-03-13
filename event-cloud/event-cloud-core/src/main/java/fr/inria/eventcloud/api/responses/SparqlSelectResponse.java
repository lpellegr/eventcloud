package fr.inria.eventcloud.api.responses;

import fr.inria.eventcloud.rdf2go.wrappers.QueryResultTableWrapper;


/**
 * 
 * @author lpellegr
 */
public class SparqlSelectResponse extends SparqlResponse<QueryResultTableWrapper> {

	private static final long serialVersionUID = 1L;

	public SparqlSelectResponse(long inboundHopCount, long outboundHopCount, 
								   long latency, QueryResultTableWrapper result) {
		super(inboundHopCount, outboundHopCount, latency, result);
	}
	
}
