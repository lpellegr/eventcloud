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
package fr.inria.eventcloud.adapters.rdf2go;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QuadPattern;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;

/**
 * This class is used as an adapter for any object that implements the
 * {@link PutGetApi} interface. It provides methods with types which are
 * compatible with RDF2Go. These methods then delegate the calls to the
 * underlying object by using the {@link PutGetApi}.
 * 
 * @author lpellegr
 */
public final class PutGetRdf2goAdapter extends Rdf2goAdapter<PutGetApi> {

    /**
     * Constructs a new RDF2Go adapter for the given object.
     * 
     * @param obj
     *            the object to adapt.
     */
    public PutGetRdf2goAdapter(PutGetApi obj) {
        super(obj);
    }

    public boolean add(URI context, Resource subject, URI predicate, Node object) {
        return super.delegate.add(toQuadruple(
                context, subject, predicate, object));
    }

    public boolean add(Statement stmt) {
        return this.add(
                stmt.getContext(), stmt.getSubject(), stmt.getPredicate(),
                stmt.getObject());
    }

    public boolean add(java.util.Iterator<? extends Statement> statements) {
        List<Quadruple> collection = new ArrayList<Quadruple>();
        while (statements.hasNext()) {
            collection.add(toQuadruple(statements.next()));
        }

        return super.delegate.add(collection);
    }

    public boolean add(URL url, SerializationFormat format) {
        return super.delegate.add(url, format);
    }

    public boolean contains(URI context, Resource subject, URI predicate,
                            Node object) {
        return super.delegate.contains(toQuadruple(
                context, subject, predicate, object));
    }

    public boolean contains(Statement stmt) {
        return this.contains(
                stmt.getContext(), stmt.getSubject(), stmt.getPredicate(),
                stmt.getObject());
    }

    public boolean delete(URI context, Resource subject, URI predicate,
                          Node object) {
        return super.delegate.delete(toQuadruple(
                context, subject, predicate, object));
    }

    public boolean delete(Statement stmt) {
        return this.delete(
                stmt.getContext(), stmt.getSubject(), stmt.getPredicate(),
                stmt.getObject());
    }

    public boolean delete(java.util.Iterator<? extends Statement> statements) {
        List<Quadruple> collection = new ArrayList<Quadruple>();
        while (statements.hasNext()) {
            collection.add(toQuadruple(statements.next()));
        }

        return super.delegate.delete(collection);
    }

    public ClosableIterator<Statement> delete(QuadPattern quadPattern) {
        return toClosableIterator(super.delegate.delete(toQuadruplePattern(quadPattern)));
    }

    public ClosableIterator<Statement> find(QuadPattern quadPattern) {
        return toClosableIterator(super.delegate.find(toQuadruplePattern(quadPattern)));
    }

    public boolean executeSparqlAsk(String sparqlAskQuery) {
        return super.delegate.executeSparqlAsk(sparqlAskQuery).getResult();
    }

    public ClosableIterable<Statement> executeSparqlConstruct(String sparqlConstructQuery) {
        return toClosableIterable(super.delegate.executeSparqlConstruct(
                sparqlConstructQuery).getResult());
    }

    public ClosableIterable<Statement> executeSparqlDescribe(String sparqlDescribeQuery) {
        return toClosableIterable(super.delegate.executeSparqlDescribe(
                sparqlDescribeQuery).getResult());
    }

    public QueryResultTable executeSparqlSelect(String sparqlSelectQuery) {
        return toQueryResultTable(super.delegate.executeSparqlSelect(
                sparqlSelectQuery).getResult());
    }

}
