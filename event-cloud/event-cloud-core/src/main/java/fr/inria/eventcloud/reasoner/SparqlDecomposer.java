/**
 * Copyright (c) 2011 INRIA.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;

import fr.inria.eventcloud.reasoner.AtomicQuery.ParentQueryForm;

/**
 * A SPARQL decomposer is in charge of decomposing a SPARQL query into
 * sub-queries ( {@link AtomicQuery}s).
 * <p>
 * TODO: The field of applications of this decomposer is very limited. It would
 * be nice to create a parser that parses the SPARQL grammar and extracts
 * informations while the grammar is parsed.
 * 
 * @author lpellegr
 */
public final class SparqlDecomposer {

    private static final Logger log =
            LoggerFactory.getLogger(SparqlDecomposer.class);

    public List<AtomicQuery> decompose(String sparqlQuery) {
        Query query = QueryFactory.create(sparqlQuery);

        ParentQueryForm parentQueryForm;

        if (query.isAskType()) {
            parentQueryForm = ParentQueryForm.ASK;
        } else if (query.isConstructType()) {
            parentQueryForm = ParentQueryForm.CONSTRUCT;
        } else if (query.isDescribeType()) {
            parentQueryForm = ParentQueryForm.DESCRIBE;
        } else if (query.isSelectType()) {
            parentQueryForm = ParentQueryForm.SELECT;
        } else {
            throw new IllegalArgumentException("Unknown query type");
        }

        return this.parseQueryTree(
                parentQueryForm, (ElementGroup) query.getQueryPattern());
    }

    private List<AtomicQuery> parseQueryTree(ParentQueryForm parentQueryForm,
                                             ElementGroup group) {
        log.debug("SparqlDecomposer.parseQueryTree({}, {})", new Object[] {
                parentQueryForm, group.toString().replaceAll("\n", "")});

        List<AtomicQuery> atomicQueries = new ArrayList<AtomicQuery>();

        for (Element elt : group.getElements()) {
            this.processElement(parentQueryForm, elt, atomicQueries, null);
        }

        return atomicQueries;
    }

    private void processElement(ParentQueryForm parentQueryForm, Element elt,
                                List<AtomicQuery> atomicQueries, Node graph) {

        log.debug(
                "SparqlDecomposer.processElement({}, {}, {}, {})",
                new Object[] {
                        parentQueryForm, elt.toString().replaceAll("\n", ""),
                        atomicQueries, graph});
        if (elt instanceof ElementNamedGraph) {
            log.debug("    ElementNamedGraph");

            // parses the graph variable
            Node graphValue = ((ElementNamedGraph) elt).getGraphNameNode();

            for (Element e : ((ElementGroup) ((ElementNamedGraph) elt).getElement()).getElements()) {
                this.processElement(
                        parentQueryForm, e, atomicQueries, graphValue);
            }
        } else if (elt instanceof ElementPathBlock) {
            log.debug("    ElementpathBlock");
            // parses a Basic Graph Pattern
            this.parse(
                    parentQueryForm, (ElementPathBlock) elt, atomicQueries,
                    graph);
        } else if (elt instanceof ElementUnion) {
            log.debug("    ElementUnion");
            // parses an UNION keyword which forms a disjunction
            // of two graph patterns
            ElementUnion unionBlock = ((ElementUnion) elt);
            for (Element unionElt : unionBlock.getElements()) {
                for (Element graphPattern : ((ElementGroup) unionElt).getElements()) {
                    this.parse(
                            parentQueryForm, (ElementPathBlock) graphPattern,
                            atomicQueries, graph);
                }
            }
        } else {
            log.debug(" elt type " + elt.getClass());
        }
    }

    public void parse(ParentQueryForm parentQueryForm, ElementPathBlock elt,
                      List<AtomicQuery> atomicQueries, Node graph) {
        log.debug("SparqlDecomposer.parse({}, {}, {}, {})", new Object[] {
                parentQueryForm, elt.toString().replaceAll("\n", ""),
                atomicQueries, graph});
        ElementPathBlock block = elt;
        Iterator<TriplePath> it = block.patternElts();

        TriplePath triple;

        while (it.hasNext()) {
            triple = it.next();

            // TODO adds support for FilterElement
            atomicQueries.add(new AtomicQuery(
                    parentQueryForm, graph, triple.getSubject(),
                    triple.getPredicate(), triple.getObject()));
        }
    }

}
