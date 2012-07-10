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
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

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

        CustomElementVisitor visitor = new CustomElementVisitor();

        // TODO: add support for multiple graph patterns
        ElementWalker.walk(query.getQueryPattern(), visitor);

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
                                                  CustomElementVisitor visitor) {
        List<AtomicQuery> result =
                new ArrayList<AtomicQuery>(visitor.elementPathBlocks.size());

        for (ElementPathBlock epb : visitor.elementPathBlocks) {
            Iterator<TriplePath> it = epb.patternElts();

            TriplePath triple;
            while (it.hasNext()) {
                triple = it.next();

                AtomicQuery atomicQuery =
                        this.createAtomicQuery(query, visitor, triple);

                result.add(atomicQuery);
            }
        }

        return result;
    }

    private AtomicQuery createAtomicQuery(Query query,
                                          CustomElementVisitor visitor,
                                          TriplePath triple) {
        AtomicQuery atomicQuery =
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

        return atomicQuery;
    }

    public static SparqlDecomposer getInstance() {
        return SparqlDecomposer.Singleton.INSTANCE;
    }

    private static class CustomElementVisitor extends ElementVisitorBase {

        private List<ElementPathBlock> elementPathBlocks;

        private Node graphNode;

        private int nbGraphPatterns;

        public CustomElementVisitor() {
            super();
            this.elementPathBlocks = new ArrayList<ElementPathBlock>(1);
        }

        @Override
        public void visit(ElementNamedGraph elt) {
            super.visit(elt);

            if (this.graphNode == null) {
                this.graphNode = elt.getGraphNameNode();
            }

            this.nbGraphPatterns++;
        }

        @Override
        public void visit(ElementPathBlock elt) {
            super.visit(elt);

            this.elementPathBlocks.add(elt);
        }

        @Override
        public void visit(ElementFilter el) {
            super.visit(el);

            // TODO: add support for filter constraints
        }

    }

}
