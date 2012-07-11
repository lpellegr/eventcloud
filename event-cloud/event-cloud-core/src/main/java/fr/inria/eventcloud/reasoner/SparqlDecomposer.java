/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.reasoner;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.ExprVisitorBase;
import com.hp.hpl.jena.sparql.expr.ExprWalker;

import fr.inria.eventcloud.exceptions.DecompositionException;

/**
 * This SPARQL decomposer is in charge of decomposing SPARQL queries into set of
 * atomic queries by parsing a SPARQL query as String. Currently this decomposer
 * is limited and does not support multiple graph patterns.
 * 
 * @author lpellegr
 */
public final class SparqlDecomposer {

    private static class Singleton {
        private static final SparqlDecomposer INSTANCE = new SparqlDecomposer();
    }

    private SparqlDecomposer() {

    }

    public List<AtomicQuery> decompose(String sparqlQuery)
            throws DecompositionException {
        Query query = QueryFactory.create(sparqlQuery);
        Op op = Algebra.compile(query);

        CustomOpVisitor visitor = new CustomOpVisitor();

        // TODO: add support for multiple graph patterns
        OpWalker.walk(op, visitor);

        if (visitor.nbGraphPatterns == 1) {
            return this.createAtomicQueries(query, visitor);
        } else {
            if (visitor.nbGraphPatterns == 0) {
                throw new DecompositionException(
                        "The specified SPARQL query does not contain any graph pattern: "
                                + sparqlQuery);
            } else {
                throw new DecompositionException(
                        "Multiple graph patterns are not yet supported");
            }
        }
    }

    private List<AtomicQuery> createAtomicQueries(Query query,
                                                  CustomOpVisitor visitor) {
        List<AtomicQuery> result =
                new ArrayList<AtomicQuery>(visitor.basicGraphPatterns.size());

        for (OpBGP bgp : visitor.basicGraphPatterns) {
            BasicPattern bp = bgp.getPattern();

            for (int i = 0; i < bp.size(); i++) {
                Triple triple = bp.get(i);

                AtomicQuery atomicQuery =
                        this.createAtomicQuery(query, visitor, triple);

                result.add(atomicQuery);
            }
        }

        return result;
    }

    private AtomicQuery createAtomicQuery(Query query, CustomOpVisitor visitor,
                                          Triple triple) {
        final AtomicQuery atomicQuery =
                new AtomicQuery(
                        visitor.graphNode, triple.getSubject(),
                        triple.getPredicate(), triple.getObject());

        // set sequence modifiers
        if (query.isDistinct()) {
            atomicQuery.setDistinct(true);
        }
        if (query.isReduced()) {
            atomicQuery.setReduced(true);
        }
        if (query.hasLimit()) {
            atomicQuery.setLimit(query.getLimit());
        }
        if (query.getOrderBy() != null) {
            atomicQuery.setOrderBy(this.filterSortConditions(
                    atomicQuery, query.getOrderBy()));
        }

        return atomicQuery;
    }

    /**
     * Filters the specified list of sortConditions to keep only the sort
     * conditions that use a variable declared inside the specified atomicQuery.
     * 
     * @param atomicQuery
     *            the atomic query containing the variables to look for.
     * @param sortConditions
     *            the sort conditions to filter.
     * 
     * @return a list of sortConditions that keeps only the sort conditions that
     *         use a variable declared inside the specified atomicQuery.
     */
    private List<SortCondition> filterSortConditions(AtomicQuery atomicQuery,
                                                     List<SortCondition> sortConditions) {
        SortConditionVisitor sortConditionVisitor = new SortConditionVisitor();
        List<SortCondition> result =
                new ArrayList<SortCondition>(sortConditions.size());

        for (SortCondition sortCondition : sortConditions) {
            ExprWalker.walk(sortConditionVisitor, sortCondition.getExpression());

            if (atomicQuery.containsVariable(sortConditionVisitor.var.getVarName())) {
                result.add(sortCondition);
            }
        }

        return result;
    }

    public static SparqlDecomposer getInstance() {
        return SparqlDecomposer.Singleton.INSTANCE;
    }

    private static class SortConditionVisitor extends ExprVisitorBase {

        private ExprVar var;

        @Override
        public void visit(ExprVar var) {
            super.visit(var);

            this.var = var;
        }

    }

    private static class CustomOpVisitor extends OpVisitorBase {

        private Node graphNode;

        private List<OpBGP> basicGraphPatterns;

        private int nbGraphPatterns;

        private List<ExprList> filterConstraints;

        public CustomOpVisitor() {
            super();

            this.basicGraphPatterns = new ArrayList<OpBGP>();
            this.filterConstraints = new ArrayList<ExprList>();
        }

        @Override
        public void visit(OpGraph opGraph) {
            super.visit(opGraph);

            if (this.graphNode == null) {
                this.graphNode = opGraph.getNode();
            }

            this.nbGraphPatterns++;
        }

        @Override
        public void visit(OpBGP opBGP) {
            super.visit(opBGP);

            this.basicGraphPatterns.add(opBGP);
        }

        @Override
        public void visit(OpFilter opFilter) {
            super.visit(opFilter);

            this.filterConstraints.add(opFilter.getExprs());
        }

    }

}
