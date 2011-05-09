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
package fr.inria.eventcloud.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;

/**
 * Operation used to execute a SPARQL Construct query directly on a given peer.
 * 
 * @author lpellegr
 */
public class SparqlConstructOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    private URI context;

    private String sparqlConstructQuery;

    public SparqlConstructOperation(URI context, String sparqlConstructQuery) {
        this.context = context;
        this.sparqlConstructQuery = sparqlConstructQuery;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperation handle(StructuredOverlay overlay) {
        return new SparqlConstructResponseOperation(
                new ClosableIterableWrapper(
                        ((SemanticDatastore) overlay.getDatastore()).sparqlConstruct(
                                context, this.sparqlConstructQuery)));
    }

}
