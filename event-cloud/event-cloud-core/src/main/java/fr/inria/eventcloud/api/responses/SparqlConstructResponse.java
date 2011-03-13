package fr.inria.eventcloud.api.responses;

import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;

/**
 * 
 * @author lpellegr
 */
public class SparqlConstructResponse extends SparqlResponse<ClosableIterableWrapper> {

	private static final long serialVersionUID = 1L;

	public SparqlConstructResponse(long inboundHopCount, long outboundHopCount, 
								   long latency, ClosableIterableWrapper result) {
		super(inboundHopCount, outboundHopCount, latency, result);
	}
	
}
