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
package fr.inria.eventcloud.datastore;

import java.util.Iterator;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.TriplePattern;
import org.ontoware.rdf2go.model.node.NodeOrVariable;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.ResourceOrVariable;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.UriOrVariable;

/**
 * @author lpellegr
 */
public interface SemanticDatastoreOperations {

    // public void addAll(URI space, Iterator<? extends Statement> other)
    // throws SemanticSpaceException;

    public void addStatement(URI space, Statement statement);

    public void addStatement(URI space, Resource subject, URI predicate,
                             String literal);

    public void addStatement(URI space, Resource subject, URI predicate,
                             String literal, String languageTag);

    public void addStatement(URI space, Resource subject, URI predicate,
                             String literal, URI datatypeURI);

    public void addStatement(URI space, String subjectURIString, URI predicate,
                             String literal);

    public void addStatement(URI space, String subjectURIString, URI predicate,
                             String literal, String languageTag);

    public void addStatement(URI space, String subjectURIString, URI predicate,
                             String literal, URI datatypeURI);

    // query

    public boolean sparqlAsk(URI space, String query);

    public ClosableIterable<Statement> sparqlConstruct(URI space, String query);

    public ClosableIterable<Statement> sparqlDescribe(URI space, String query);

    public QueryResultTable sparqlSelect(URI space, String queryString);

    public ClosableIterator<Statement> findStatements(URI space,
                                                      TriplePattern triplepattern);

    // remove

    public void removeAll(URI space);

    public void removeAll(URI space, Iterator<? extends Statement> statements);

    public void removeStatement(URI space, Statement statement);

    public void removeStatement(URI space, Resource subject, URI predicate,
                                String literal);

    public void removeStatement(URI space, Resource subject, URI predicate,
                                String literal, String languageTag);

    public void removeStatement(URI space, Resource subject, URI predicate,
                                String literal, URI datatypeURI);

    public void removeStatement(URI space, String subjectURIString,
                                URI predicate, String literal);

    public void removeStatement(URI space, String subjectURIString,
                                URI predicate, String literal,
                                String languageTag);

    public void removeStatement(URI space, String subjectURIString,
                                URI predicate, String literal, URI datatypeURI);

    public void removeStatements(URI space, ResourceOrVariable subject,
                                 UriOrVariable predicate, NodeOrVariable object);

    public void removeStatements(URI space, TriplePattern triplePattern);

    // contain

    public boolean contains(URI space, Statement s);

    public boolean contains(URI space, ResourceOrVariable subject,
                            UriOrVariable predicate, NodeOrVariable object);

    public boolean contains(URI space, ResourceOrVariable subject,
                            UriOrVariable predicate, String plainLiteral);

}
