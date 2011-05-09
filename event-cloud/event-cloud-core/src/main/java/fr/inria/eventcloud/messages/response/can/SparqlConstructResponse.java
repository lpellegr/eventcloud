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
package fr.inria.eventcloud.messages.response.can;

import java.util.HashSet;
import java.util.Set;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Statement;

import fr.inria.eventcloud.messages.request.can.SparqlConstructRequest;
import fr.inria.eventcloud.messages.request.can.SparqlRequest;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Response associated to {@link SparqlConstructRequest}.
 * 
 * @author lpellegr
 */
public class SparqlConstructResponse extends SparqlResponse {

    private static final long serialVersionUID = 1L;

    public SparqlConstructResponse(SparqlRequest request) {
        super(request);
    }

    public ClosableIterable<Statement> getResults() {
        Set<Statement> stmts = new HashSet<Statement>();

        for (ClosableIterableWrapper ciw : super.getDeserializedResults()) {
            ClosableIterator<Statement> it = ciw.toRDF2Go().iterator();
            while (it.hasNext()) {
                stmts.add(it.next());
            }
        }

        return SemanticHelper.generateClosableIterable(stmts);
    }

}
