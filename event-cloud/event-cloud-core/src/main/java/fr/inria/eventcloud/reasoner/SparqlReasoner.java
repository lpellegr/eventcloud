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

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import fr.inria.eventcloud.exceptions.DecompositionException;
import fr.inria.eventcloud.messages.request.can.SparqlAtomicRequest;

/**
 * The aim of this SPARQL reasoner is to decompose a SPARQL query into several
 * independent queries that can be executed in parallel on the network.
 * 
 * @author lpellegr
 */
public class SparqlReasoner {

    private SparqlReasoner() {

    }

    public static List<SparqlAtomicRequest> parse(String sparqlQuery) {
        try {
            List<AtomicQuery> atomicQueries =
                    SparqlDecomposer.getInstance().decompose(sparqlQuery);

            return FluentIterable.from(atomicQueries).transform(
                    new Function<AtomicQuery, SparqlAtomicRequest>() {
                        @Override
                        public SparqlAtomicRequest apply(AtomicQuery input) {
                            return new SparqlAtomicRequest(input);
                        };
                    }).toImmutableList();
        } catch (DecompositionException e) {
            throw new IllegalArgumentException(sparqlQuery);
        }
    }

}
