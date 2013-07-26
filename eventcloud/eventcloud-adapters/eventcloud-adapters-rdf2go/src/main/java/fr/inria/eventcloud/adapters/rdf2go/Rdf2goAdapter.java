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
package fr.inria.eventcloud.adapters.rdf2go;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.impl.jena.TypeConversion;
import org.ontoware.rdf2go.model.QuadPattern;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.impl.QueryRowImpl;
import org.ontoware.rdf2go.model.impl.StatementImpl;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.wrappers.ModelWrapper;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;

/**
 * Stores the delegate and provide some convenient methods to convert some Jena
 * objects to RDF2Go objects.
 * 
 * @author lpellegr
 */
public abstract class Rdf2goAdapter<T> {

    protected T delegate;

    protected Rdf2goAdapter(T delegate) {
        this.delegate = delegate;
    }

    protected static final Quadruple toQuadruple(URI context, Resource subject,
                                                 URI predicate, Node object) {
        return new Quadruple(
                TypeConversion.toJenaNode(context),
                TypeConversion.toJenaNode(subject),
                TypeConversion.toJenaNode(predicate),
                TypeConversion.toJenaNode(object));
    }

    protected static final Quadruple toQuadruple(Statement stmt) {
        return toQuadruple(
                stmt.getContext(), stmt.getSubject(), stmt.getPredicate(),
                stmt.getObject());
    }

    protected static final QuadruplePattern toQuadruplePattern(QuadPattern quadPattern) {
        return new QuadruplePattern(
                TypeConversion.toJenaNode(quadPattern.getContext()),
                TypeConversion.toJenaNode(quadPattern.getSubject()),
                TypeConversion.toJenaNode(quadPattern.getPredicate()),
                TypeConversion.toJenaNode(quadPattern.getObject()));
    }

    protected static final ClosableIterator<Statement> toClosableIterator(List<Quadruple> collection) {
        final List<Statement> stmts = new ArrayList<Statement>();
        for (Quadruple quad : collection) {
            stmts.add(new StatementImpl(
                    (URI) TypeConversion.toRDF2Go(quad.getGraph()),
                    (Resource) TypeConversion.toRDF2Go(quad.getSubject()),
                    (URI) TypeConversion.toRDF2Go(quad.getPredicate()),
                    TypeConversion.toRDF2Go(quad.getObject())));
        }

        return generateClosableIterator(stmts);
    }

    public static final ClosableIterable<Statement> toClosableIterable(ModelWrapper model) {
        StmtIterator stmts = model.listStatements();
        final List<Statement> list = new ArrayList<Statement>();

        while (stmts.hasNext()) {
            com.hp.hpl.jena.rdf.model.Statement stmt = stmts.next();
            list.add(new StatementImpl(
                    null, // TODO check if null is allowed
                    (Resource) TypeConversion.toRDF2Go(stmt.getSubject()
                            .asNode()),
                    (URI) TypeConversion.toRDF2Go(stmt.getPredicate().asNode()),
                    TypeConversion.toRDF2Go(stmt.getObject().asNode())));
        }

        return generateClosableIterable(list);
    }

    protected static QueryResultTable toQueryResultTable(final ResultSetWrapper resultSet) {
        return new QueryResultTable() {
            private static final long serialVersionUID = 160L;

            @Override
            public ClosableIterator<QueryRow> iterator() {
                List<QueryRow> rows = new ArrayList<QueryRow>();

                while (resultSet.hasNext()) {
                    QuerySolution solution = resultSet.next();
                    QueryRowImpl row = new QueryRowImpl();
                    for (String var : this.getVariables()) {
                        RDFNode node = solution.get(var);
                        row.put(var, TypeConversion.toRDF2Go((node == null
                                ? null : node.asNode())));
                    }
                    rows.add(row);
                }

                return generateClosableIterator(rows);
            }

            @Override
            public List<String> getVariables() {
                return resultSet.getResultVars();
            }
        };
    }

    private static final <T> ClosableIterable<T> generateClosableIterable(final List<T> statements) {
        return new ClosableIterable<T>() {
            private static final long serialVersionUID = 160L;

            @Override
            public ClosableIterator<T> iterator() {
                return generateClosableIterator(statements);
            }
        };
    }

    private static <T> ClosableIterator<T> generateClosableIterator(final List<T> rows) {
        return new ClosableIterator<T>() {
            private Iterator<T> iterator = rows.iterator();

            @Override
            public void close() {
                // cannot really close
            }

            @Override
            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            @Override
            public T next() {
                return this.iterator.next();
            }

            @Override
            public void remove() {
                this.iterator.remove();
            }
        };
    }

}
