package fr.inria.eventcloud.reasoner;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;

import fr.inria.eventcloud.api.messages.request.SparqlQuery;
import fr.inria.eventcloud.messages.request.can.SemanticRequest;
import fr.inria.eventcloud.reasoner.visitor.QueryIdentifierVisitor;

/**
 * Try to identify Sparql query type by using Sesame Visitor pattern.
 * 
 * @author lpellegr
 */
public class SparqlQueryIdentifier extends QueryModelVisitorBase<RuntimeException> {

    public static SparqlQueryIdentifier instance;

	/**
	 * Creates a new {@link SemanticRequest} from a {@link SparqlQuery}.
	 * 
	 * @param query
	 *            the {@link SparqlQuery} from the public API to use.
	 * @return a new {@link SemanticRequest} from the private API.
	 * 
	 * @throws QueryParseException
	 *             if query is malformed or if it has an unknown type.
	 */
	public static SemanticRequest createQueryMessage(SparqlQuery query)
			throws MalformedQueryException {
		return SparqlQueryIdentifier.getInstance().newQueryMessageFrom(query);
	}

	private SemanticRequest newQueryMessageFrom(SparqlQuery query)
			throws MalformedQueryException {
		SPARQLParser parser = new SPARQLParser();
        ParsedQuery pq =
            parser.parseQuery(query.toString(), null);
        
        QueryIdentifierVisitor visitor = new QueryIdentifierVisitor();
        pq.getTupleExpr().visit(visitor);
        return visitor.processIdentificationAndCreateQueryMessage(query);
	}
	
	private static SparqlQueryIdentifier getInstance() {
		if (SparqlQueryIdentifier.instance == null) {
			SparqlQueryIdentifier.instance = new SparqlQueryIdentifier();
		}
		return SparqlQueryIdentifier.instance;
	}
    
}
