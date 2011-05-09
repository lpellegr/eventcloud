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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.reasoner;

import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.rdf2go.impl.jena26.ModelImplJena26;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Statement;

import com.hp.hpl.jena.rdf.model.ModelFactory;

import fr.inria.eventcloud.messages.response.can.SparqlResponse;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;

/**
 * SparqlColander is used to filter the results from a set of
 * {@link SparqlResponse} by using the original SPARQL request. Indeed, due to
 * the infrastructure, we can route only a sub-set of SPARQL queries. This mean
 * that the {@link SparqlResponse} does not contains the final result. To get
 * the final result we have to use a {@link SparqlColander} to remove some extra
 * values.
 * 
 * @author lpellegr
 */
public class SparqlColander {

    // All models instantiated are in-memory models
    private transient Model[] models;

    public SparqlColander() {
        this.models = new Model[4];

        com.hp.hpl.jena.rdf.model.Model model;

        for (int i = 0; i < 4; i++) {
            model = ModelFactory.createDefaultModel();
            this.models[i] = new ModelImplJena26(model);
            this.models[i].open();
        }
    }

    /**
     * Filters a set of {@link SparqlResponse} for a given
     * {@code originalAskQuery}.
     * 
     * @param originalAskQuery
     *            the filter constraint.
     * @param responses
     *            the set of responses to filter.
     * 
     * @return {@code true} if there are some values matching the
     *         {@code originalAskQuery}, {@code false} otherwise.
     */
    public synchronized boolean filterSparqlAsk(String originalAskQuery,
                                                List<SparqlResponse> responses) {
        this.cleanAndFill(this.models[0], responses);
        return this.models[0].sparqlAsk(originalAskQuery);
    }

    /**
     * Filters a set of {@link SparqlResponse} for a given
     * {@code originalConstructQuery}.
     * 
     * @param originalConstructQuery
     *            the filter constraint.
     * @param responses
     *            the set of responses to filter.
     * 
     * @return the values matching the {@code originalConstructQuery}.
     */
    public synchronized ClosableIterable<Statement> filterSparqlConstruct(String originalConstructQuery,
                                                                          List<SparqlResponse> responses) {
        this.cleanAndFill(this.models[1], responses);

        return this.models[1].sparqlConstruct(originalConstructQuery);
    }

    /**
     * Filters a set of {@link SparqlResponse} for a given
     * {@code originalDescribeQuery}.
     * 
     * @param originalDescribeQuery
     *            the filter constraint.
     * @param responses
     *            the set of responses to filter.
     * 
     * @return the values matching the {@code originalDescribeQuery}.
     */
    public synchronized ClosableIterable<Statement> filterSparqlDescribe(String originalDescribeQuery,
                                                                         List<SparqlResponse> responses) {
        this.cleanAndFill(this.models[2], responses);
        return this.models[2].sparqlConstruct(originalDescribeQuery);
    }

    /**
     * Filters a set of {@link SparqlResponse} for a given
     * {@code originalSelectQuery}.
     * 
     * @param originalSelectQuery
     *            the filter constraint.
     * @param responses
     *            the set of responses to filter.
     * 
     * @return the values matching the {@code originalSelectQuery}.
     */
    public synchronized QueryResultTable filterSparqlSelect(String originalSelectQuery,
                                                            List<SparqlResponse> responses) {
        this.cleanAndFill(this.models[3], responses);
        return this.models[3].sparqlSelect(originalSelectQuery);
    }

    private void cleanAndFill(Model model, List<SparqlResponse> responses) {
        model.removeAll();

        for (Response<?> response : responses) {
            for (ClosableIterableWrapper ciw : ((SparqlResponse) response).getDeserializedResults()) {
                model.addAll(ciw.toRDF2Go().iterator());
            }
        }
        model.commit();
    }

}
