/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
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
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprTransformCopy;
import com.hp.hpl.jena.sparql.expr.ExprTransformer;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.ExprVisitorBase;
import com.hp.hpl.jena.sparql.expr.ExprWalker;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.sparql.util.ExprUtils;

import fr.inria.eventcloud.exceptions.DecompositionException;

/**
 * This SPARQL decomposer is in charge of decomposing SPARQL queries into set of
 * atomic queries by parsing a SPARQL query as String. Currently this decomposer
 * is limited and does not support multiple graph patterns.
 * 
 * @author lpellegr
 * @author mantoine
 */
public final class SparqlDecomposer {

    private static class Singleton {
        private static final SparqlDecomposer INSTANCE = new SparqlDecomposer();
    }

    private static final String FUNCTION_META_GRAPH_IRI =
            "http://eventcloud.inria.fr/function#removeMetadata";

    private SparqlDecomposer() {
        FunctionRegistry.get().put(
                FUNCTION_META_GRAPH_IRI, RemoveMetadataFunction.class);
    }

    public SparqlDecompositionResult decompose(String sparqlQuery)
            throws DecompositionException {
        Query query = QueryFactory.create(sparqlQuery);
        Op op = Algebra.compile(query);

        CustomOpVisitor visitor = new CustomOpVisitor();

        // TODO: add support for multiple graph patterns
        OpWalker.walk(op, visitor);

        if (visitor.nbGraphPatterns == 1) {
            return new SparqlDecompositionResult(this.createAtomicQueries(
                    query, visitor));
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
        // to avoid wrong results limit must be applied if and only
        // if the number of triple patterns is equals to 1
        if (visitor.basicGraphPatterns.size() == 1 && query.hasLimit()) {
            atomicQuery.setLimit(query.getLimit());
        }
        // it is unnecessary to order results if no limit is applied
        if (visitor.basicGraphPatterns.size() == 1 && query.hasLimit()
                && query.getOrderBy() != null) {
            atomicQuery.setOrderBy(this.filterSortConditions(
                    atomicQuery, query.getOrderBy()));
        }
        if (!(visitor.getFilterConstraints().isEmpty())) {
            FilterTransformer transformer = new FilterTransformer(atomicQuery);
            List<ExprList> filterConstraints = new ArrayList<ExprList>();
            // visitor.getFilterConstraints().size() is always equals to 1
            ExprList el = visitor.getFilterConstraints().get(0);
            ExprList exprList = ExprTransformer.transform(transformer, el);

            if (!(ExprUtils.fmtSPARQL(exprList).equals("null()"))) {
                String expList = ExprUtils.fmtSPARQL(exprList);
                if (expList.contains(" , ")) {
                    // if several FILTER clauses, exprList will look like :
                    // filter1 , filter2 , etc
                    // so we have to split it, remove null() if the atomic query
                    // doesn't match all of the filters but only some of them,
                    // and put each different filter into Expr variables
                    String[] tabExpList = expList.split(" , ");
                    for (int i = 0; i < tabExpList.length; i++) {
                        if (!tabExpList[i].contains("null()")) {
                            Expr newExpr = ExprUtils.parse(tabExpList[i]);
                            filterConstraints.add(new ExprList(newExpr));
                        }
                    }
                } else {
                    filterConstraints.add(exprList);
                }
            }
            atomicQuery.setFilterConstraints(filterConstraints);
        } else {
            atomicQuery.setFilterConstraints(new ArrayList<ExprList>(0));
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

        public List<ExprList> getFilterConstraints() {
            return this.filterConstraints;
        }

    }

    private static class FilterTransformer extends ExprTransformCopy {

        private AtomicQuery query;

        public FilterTransformer(AtomicQuery query) {
            super();
            this.query = query;
        }

        @Override
        public Expr transform(ExprVar exprVar) {
            // if atomic query contains a variable that is in the filter clause
            if (this.query.containsVariable(exprVar.getVarName())) {
                return exprVar;
            }
            // else we don't add filter condition to this atomic query
            return NodeValue.nvNothing;
        }

        @Override
        public Expr transform(ExprFunction2 func, Expr expr1, Expr expr2) {
            if (expr1 == null) {
                return new E_Null();
            }

            if (expr2 == null) {
                return new E_Null();
            }

            if (expr1 instanceof E_Null) {
                return expr2;
            }

            if (expr2 instanceof E_Null) {
                return expr1;
            }

            return super.transform(func, expr1, expr2);
        }

    }

    private static class E_Null extends ExprFunction0 {
        private static final String symbol = "null";

        public E_Null() {
            super(symbol);
        }

        @Override
        public NodeValue eval(FunctionEnv env) {
            return NodeValue.nvNothing;
        }

        @Override
        public Expr copy() {
            return new E_Null();
        }

    }

}
