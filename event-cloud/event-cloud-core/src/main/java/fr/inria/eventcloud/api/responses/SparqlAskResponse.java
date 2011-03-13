package fr.inria.eventcloud.api.responses;


/**
 * 
 * @author lpellegr
 */
public class SparqlAskResponse extends SparqlResponse<Boolean> {

	private static final long serialVersionUID = 1L;

	public SparqlAskResponse(long inboundHopCount, long outboundHopCount, 
								   long latency, Boolean result) {
		super(inboundHopCount, outboundHopCount, latency, result);
	}
	
}
