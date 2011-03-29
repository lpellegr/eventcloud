package fr.inria.eventcloud.reasoner;

import java.util.ArrayList;
import java.util.List;

import fr.inria.eventcloud.messages.request.can.SparqlConstructRequest;

/**
 * 
 * @author lpellegr
 */
public class SparqlReasoner {

	private final SparqlDecomposer decomposer;
	
	public SparqlReasoner() {
		this.decomposer = new SparqlDecomposer();
	}
	
	public List<SparqlConstructRequest> parseSparql(String sparqlQuery) {
		List<SparqlConstructRequest> subRequests;
		List<AtomicSparqlQuery> subQueries = this.decomposer.decompose(sparqlQuery);
		subRequests = new ArrayList<SparqlConstructRequest>(subQueries.size());
		
		for (AtomicSparqlQuery query : subQueries) {
			subRequests.add(new SparqlConstructRequest(query));
		}
		
		return subRequests;
	}
	
}
