package fr.inria.eventcloud.reasoner;

import java.util.List;

import fr.inria.eventcloud.api.messages.request.SparqlQuery;

/**
 * A {@link ParsedSparqlQuery} contains some extra information about a SPARQL
 * query which has been analyzed. It contains several sub-queries to dispatch if
 * it has been considered useful to decompose the original query for routing.
 * 
 * @author lpellegr
 */
public class ParsedSparqlQuery {

	private boolean requireFiltration = false;

	private SparqlQuery query;

	private List<SparqlQuery> subQueries;

	public ParsedSparqlQuery(boolean requireFilter, SparqlQuery originalQuery,
			List<SparqlQuery> subQueries) {
		this.requireFiltration = requireFilter;
		this.query = originalQuery;
		this.subQueries = subQueries;
	}

	public boolean requireFiltration() {
		return this.requireFiltration;
	}

	public SparqlQuery getQuery() {
		return this.query;
	}

	public List<SparqlQuery> getSubQueries() {
		return this.subQueries;
	}

}
