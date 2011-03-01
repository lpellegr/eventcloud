package fr.inria.eventcloud.reasoner.visitor;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * Iterates on the algebra SPARQL query in order to check if the query have to
 * be decomposed for routing. If it is considered useful (there are one or
 * several conjunctive or disjunctive operators) to decompose the query then
 * each {@link StatementPattern} is extracted.
 * 
 * @author lpellegr
 */
public class StatementPatternsVisitor extends QueryModelVisitorBase<RuntimeException> {

	private boolean requireFiltration = false;

	private List<StatementPattern> statementPatterns;

	public StatementPatternsVisitor() {
		this.statementPatterns = new ArrayList<StatementPattern>();
	}

	/**
	 * Met a {@link Join} node (i.e conjunctive operator: AND).
	 * 
	 * @param node
	 *            the {@link Join} operator met.
	 */
	public void meet(Join node) throws RuntimeException {
		if (!this.requireFiltration) {
			this.requireFiltration = true;
		}

		super.meet(node);
	}

	/**
	 * Met an {@link Union} node (i.e disjunctive operator: OR).
	 * 
	 * @param node
	 *            the {@link Union} operator met.
	 */
	public void meet(Union node) throws RuntimeException {
		if (!this.requireFiltration) {
			this.requireFiltration = true;
		}

		super.meet(node);
	}

	/**
	 * Met an {@link Filter} node.
	 * 
	 * @param node
	 *            the {@link Filter} operator met.
	 */
	public void meet(Filter node) throws RuntimeException {
		if (!this.requireFiltration) {
			this.requireFiltration = true;
		}

		super.meet(node);
	}

	/**
	 * Stores each {@link StatementPattern}s met.
	 * 
	 * @param node
	 *            the {@link StatementPattern} met.
	 */
	public void meet(StatementPattern node) throws RuntimeException {
		this.statementPatterns.add(node);
		super.meet(node);
	}

	/**
	 * Returns a boolean indicating if the query have to be decomposed into
	 * sub-queries.
	 * 
	 * @return a boolean indicating if the query have to be decomposed into
	 *         sub-queries.
	 */
	public boolean requireFiltration() {
		return this.requireFiltration;
	}

	/**
	 * Returns the {@link StatementPattern}s met.
	 * 
	 * @return the {@link StatementPattern}s met or <code>null</code> if the
	 *         query is not decomposed.
	 */
	public List<StatementPattern> getStatementPatterns() {
		return this.statementPatterns;
	}

}
