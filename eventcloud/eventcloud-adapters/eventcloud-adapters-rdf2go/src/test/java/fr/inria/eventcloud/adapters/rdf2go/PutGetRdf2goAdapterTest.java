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
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.impl.QuadPatternImpl;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.Variable;

import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;

/**
 * Provides test cases for the {@link PutGetRdf2goAdapter}. This class provides
 * tests in order to test whether the translation between RDF2Go objects and
 * eventcloud-api/Jena objects works.
 * 
 * @author lpellegr
 */
public class PutGetRdf2goAdapterTest extends
        Rdf2goAdapterTest<PutGetRdf2goAdapter> {

    @Before
    public void setUp() {
        super.adapter = new PutGetRdf2goAdapter(new MockPutGetProxy());
    }

    @Test
    public void testAddURIResourceURINode() {
        super.adapter.add(uri, uri, uri, uri);
        super.adapter.add(uri, uri, uri, literal);

        Assert.assertTrue(super.adapter.contains(uri, uri, uri, uri));
        Assert.assertTrue(super.adapter.contains(uri, uri, uri, literal));
    }

    @Test
    public void testAddStatement() {
        super.adapter.add(model.createStatement(uri, uri, uri));
        super.adapter.add(model.createStatement(uri, uri, literal));

        Assert.assertTrue(super.adapter.contains(uri, uri, uri, uri));
        Assert.assertTrue(super.adapter.contains(uri, uri, uri, literal));
    }

    @Test
    public void testAddIteratorOfQextendsStatement() {
        List<Statement> stmts = new ArrayList<Statement>();
        for (int i = 0; i < 10; i++) {
            stmts.add(model.createStatement(
                    uri, uri, model.createPlainLiteral(Integer.toString(i))));
        }

        super.adapter.add(stmts.iterator());

        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(super.adapter.contains(model.createStatement(
                    uri, uri, model.createPlainLiteral(Integer.toString(i)))));
        }
    }

    @Ignore
    public void testAddInputStreamSerializationFormat() {
        // we don't have to test it because there is no RDF2Go object that is
        // used as input or output
    }

    @Test
    public void testContainsURIResourceURINode() {
        Assert.assertFalse(super.adapter.contains(uri, uri, uri, uri));
        super.adapter.add(model.createStatement(uri, uri, uri));
        Assert.assertTrue(super.adapter.contains(uri, uri, uri, uri));
    }

    @Test
    public void testContainsStatement() {
        Assert.assertFalse(super.adapter.contains(uri, uri, uri, uri));
        super.adapter.add(model.createStatement(uri, uri, uri));
        Assert.assertTrue(super.adapter.contains(uri, uri, uri, uri));
    }

    @Test
    public void testDeleteURIResourceURINode() {
        Assert.assertFalse(super.adapter.contains(uri, uri, uri, uri));
        super.adapter.add(model.createStatement(uri, uri, uri));
        Assert.assertTrue(super.adapter.contains(uri, uri, uri, uri));
        super.adapter.delete(model.createStatement(uri, uri, uri));
        Assert.assertFalse(super.adapter.contains(uri, uri, uri, uri));
    }

    @Test
    public void testDeleteStatement() {
        Assert.assertFalse(super.adapter.contains(uri, uri, uri, uri));
        super.adapter.add(model.createStatement(uri, uri, uri));
        Assert.assertTrue(super.adapter.contains(uri, uri, uri, uri));
        super.adapter.delete(model.createStatement(uri, uri, uri));
        Assert.assertFalse(super.adapter.contains(uri, uri, uri, uri));
    }

    @Test
    public void testDeleteIteratorOfQextendsStatement() {
        List<Statement> stmts = new ArrayList<Statement>();
        for (int i = 0; i < 10; i++) {
            stmts.add(model.createStatement(
                    uri, uri, model.createPlainLiteral(Integer.toString(i))));
        }

        // add is assumed to work
        super.adapter.add(stmts.iterator());
        super.adapter.delete(stmts.iterator());

        for (int i = 0; i < 10; i++) {
            Assert.assertFalse(super.adapter.contains(model.createStatement(
                    uri, uri, model.createPlainLiteral(Integer.toString(i)))));
        }
    }

    @Test
    public void testDeleteQuadPattern() {
        URI urn1 = model.createURI("urn:predicate:1");
        URI urn2 = model.createURI("urn:predicate:2");

        for (int i = 0; i < 5; i++) {
            super.adapter.add(model.createStatement(
                    uri, urn1, model.createPlainLiteral(Integer.toString(i))));
        }
        for (int i = 0; i < 5; i++) {
            super.adapter.add(model.createStatement(
                    uri, urn2, model.createPlainLiteral(Integer.toString(i))));
        }

        super.adapter.delete(new QuadPatternImpl(
                Variable.ANY, Variable.ANY, urn1, Variable.ANY));

        for (int i = 0; i < 5; i++) {
            Assert.assertFalse(super.adapter.contains(model.createStatement(
                    uri, urn1, model.createPlainLiteral(Integer.toString(i)))));
        }
        for (int i = 0; i < 5; i++) {
            Assert.assertTrue(super.adapter.contains(model.createStatement(
                    uri, urn2, model.createPlainLiteral(Integer.toString(i)))));
        }

        super.adapter.delete(new QuadPatternImpl(uri, uri, urn2, Variable.ANY));

        for (int i = 0; i < 5; i++) {
            Assert.assertFalse(super.adapter.contains(model.createStatement(
                    uri, urn1, model.createPlainLiteral(Integer.toString(i)))));
        }
        for (int i = 0; i < 5; i++) {
            Assert.assertFalse(super.adapter.contains(model.createStatement(
                    uri, urn2, model.createPlainLiteral(Integer.toString(i)))));
        }
    }

    @Test
    public void testFind() {
        URI urn1 = model.createURI("urn:predicate:1");
        URI urn2 = model.createURI("urn:predicate:2");

        for (int i = 0; i < 5; i++) {
            super.adapter.add(model.createStatement(
                    uri, urn1, model.createPlainLiteral(Integer.toString(i))));
        }
        for (int i = 0; i < 5; i++) {
            super.adapter.add(model.createStatement(
                    uri, urn2, model.createPlainLiteral(Integer.toString(i))));
        }

        ClosableIterator<Statement> result =
                super.adapter.find(new QuadPatternImpl(
                        Variable.ANY, Variable.ANY, urn2, Variable.ANY));

        int count = 0;
        while (result.hasNext()) {
            result.next();
            count++;
        }

        Assert.assertEquals(5, count);
    }

    @Ignore
    public void testExecuteSparqlAsk() {
        // we don't have to test it because there is no RDF2Go object that is
        // used as input or output
    }

    @Test
    public void testExecuteSparqlConstruct()
            throws MalformedSparqlQueryException {
        for (int i = 0; i < 5; i++) {
            super.adapter.add(model.createStatement(
                    uri, uri, model.createPlainLiteral(Integer.toString(i))));
        }

        ClosableIterable<Statement> result =
                super.adapter.executeSparqlConstruct("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }");

        int count = 0;
        ClosableIterator<Statement> it = result.iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }

        Assert.assertEquals(5, count);
    }

    @Ignore
    public void testExecuteSparqlDescribe() {
        // we don't have to test it because the operation is not supported but
        // also because if the test with the Construct query pass then this one
        // will also pass
    }

    @Test
    public void testExecuteSparqlSelect() throws MalformedSparqlQueryException {
        for (int i = 0; i < 5; i++) {
            super.adapter.add(model.createStatement(
                    uri, uri, model.createPlainLiteral(Integer.toString(i))));
        }

        QueryResultTable result =
                super.adapter.executeSparqlSelect("SELECT ?s ?p ?o ?g WHERE { GRAPH ?g { ?s ?p ?o } }");

        int count = 0;
        ClosableIterator<QueryRow> it = result.iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }

        Assert.assertEquals(5, count);
    }

}
