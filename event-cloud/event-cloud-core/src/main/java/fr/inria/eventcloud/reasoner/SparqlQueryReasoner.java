package fr.inria.eventcloud.reasoner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParserUtil;

import fr.inria.eventcloud.api.messages.request.SparqlConstructQuery;
import fr.inria.eventcloud.api.messages.request.SparqlQuery;
import fr.inria.eventcloud.reasoner.visitor.StatementPatternsVisitor;

/**
 * 
 * @author lpellegr
 */
public class SparqlQueryReasoner implements Serializable {

    private static final long serialVersionUID = 1L;

    private static class LazyInitializer {
		
		public static final SparqlQueryReasoner instance = new SparqlQueryReasoner(); 
		
	}
	
	public ParsedSparqlQuery decompose(SparqlQuery query)
			throws MalformedQueryException {
		return  this.decomposeIfNecessary(
				query,
				QueryParserUtil.parseQuery(
						QueryLanguage.SPARQL,
						query.toString(), null));
	}

	private ParsedSparqlQuery decomposeIfNecessary(SparqlQuery query,
			ParsedQuery parsedQuery) {
		StatementPatternsVisitor visitor = new StatementPatternsVisitor();
		parsedQuery.getTupleExpr().visit(visitor);
		List<SparqlQuery> subQueries;

		// The query has been decomposed into one or several construct
		// sub-queries
		if (visitor.requireFiltration()) {
			subQueries = new ArrayList<SparqlQuery>(visitor
					.getStatementPatterns().size());
			for (StatementPattern pattern : visitor.getStatementPatterns()) {
				subQueries.add(new SparqlConstructQuery(query.getSpaceURI(), this
						.createConstructQueryFrom(pattern)));
			}
		} else {
			subQueries = new ArrayList<SparqlQuery>(1);
			subQueries.add(query);
		}
		return new ParsedSparqlQuery(
						visitor.requireFiltration(), query, subQueries);
	}

	public String createConstructQueryFrom(StatementPattern pattern) {
		String subject = asString(pattern.getSubjectVar());
		String predicate = asString(pattern.getPredicateVar());
		String object = asString(pattern.getObjectVar());

		StringBuffer graphQuery = new StringBuffer(64);
		graphQuery.append("CONSTRUCT { ");
		graphQuery.append(subject);
		graphQuery.append(" ");
		graphQuery.append(predicate);
		graphQuery.append(" ");
		graphQuery.append(object);
		graphQuery.append(" } WHERE { ");
		graphQuery.append(subject);
		graphQuery.append(" ");
		graphQuery.append(predicate);
		graphQuery.append(" ");
		graphQuery.append(object);
		graphQuery.append(" }");

		return graphQuery.toString();
	}

	private static String asString(Var var) {
		if (var.getName().startsWith("-descr-")) {
			// Variable created for describe query
			return "?" + var.getName().replaceAll("-", "");
		} else if (var.getName().startsWith("-anon-")) {
			// Blank node
			return "_:" + var.getName().replaceAll("-", "");
		} else if (var.getValue() == null) {
			// Variable
			return "?" + var.getName();
		} else if (var.getValue().toString().startsWith("\"")) {
			// Literal
			return var.getValue().toString();
		} else {
			// IRI
			return "<" + var.getValue().stringValue() + ">";
		}
	}
	
	public static SparqlQueryReasoner getInstance() {
		return LazyInitializer.instance;
	}

}
