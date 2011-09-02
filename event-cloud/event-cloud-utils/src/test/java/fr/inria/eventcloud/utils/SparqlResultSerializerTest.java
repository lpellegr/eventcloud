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
package fr.inria.eventcloud.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;

/**
 * Tests associated to {@link SparqlResultSerializerTest}.
 * 
 * @author lpellegr
 */
public class SparqlResultSerializerTest {

    private DatasetGraph datastore;

    @Before
    public void setUp() {
        this.datastore = DatasetGraphFactory.createMem();
        // fills the datastore with some data
        for (int i = 0; i < 10; i++) {
            Node node = Node.createURI("http://abc" + i);
            this.datastore.add(node, node, node, node);
        }
    }

    @Test
    public void testBindingSerializationWithCompressionDisabled() {
        testBindingSerialization(false);
    }

    @Test
    public void testBindingSerializationWithCompressionEnabled() {
        testBindingSerialization(true);
    }

    @Test
    public void testModelSerializationWithCompressionDisabled() {
        testModelSerialization(false);
    }

    @Test
    public void testModelSerializationWithCompressionEnabled() {
        testModelSerialization(true);
    }

    @Test
    public void testResultSetSerializationWithCompressionDisabled() {
        testResultSetSerialization(false);
    }

    @Test
    public void testResultSetserializationWithCompressionEnabled() {
        testResultSetSerialization(true);
    }

    private void testBindingSerialization(boolean gzipped) {
        Node defaultNode = Node.createURI("http://www.inria.fr");

        Binding parentBinding = BindingFactory.create();
        parentBinding.add(Var.alloc("parent"), defaultNode);
        Binding binding = BindingFactory.create(parentBinding);
        binding.add(Var.alloc("var1"), defaultNode);
        binding.add(Var.alloc("var2"), defaultNode);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SparqlResultSerializer.serialize(baos, binding, gzipped);

        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        Binding unserializedBinding =
                SparqlResultSerializer.deserializeBinding(bais, gzipped);

        try {
            bais.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Binding unserializedParentBinding = unserializedBinding.getParent();
        Assert.assertEquals(
                unserializedParentBinding.vars().next().getName(), "parent");

        Set<String> vars = new HashSet<String>();
        Iterator<Var> varsIt = unserializedBinding.vars();
        while (varsIt.hasNext()) {
            vars.add(varsIt.next().getName());
        }

        Assert.assertTrue(vars.contains("var1"));
        Assert.assertTrue(vars.contains("var2"));
    }

    private void testModelSerialization(boolean gzipped) {
        QueryExecution queryExec =
                QueryExecutionFactory.create(
                        QueryFactory.create("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }"),
                        new DatasetImpl(this.datastore));
        Model model = queryExec.execConstruct();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SparqlResultSerializer.serialize(baos, model, gzipped);

        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        Model unserializedModel =
                SparqlResultSerializer.deserializeModel(bais, gzipped);

        try {
            bais.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(model.isIsomorphicWith(unserializedModel));

        queryExec.close();
    }

    private void testResultSetSerialization(boolean gzipped) {
        QueryExecution queryExec =
                QueryExecutionFactory.create(
                        QueryFactory.create("SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }"),
                        new DatasetImpl(this.datastore));
        ResultSet resultSet = queryExec.execSelect();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SparqlResultSerializer.serialize(baos, resultSet, gzipped);

        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        ResultSet unserializedResultSet =
                SparqlResultSerializer.deserializeResultSet(bais, gzipped);

        try {
            bais.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(resultSet.getResultVars().equals(
                unserializedResultSet.getResultVars()));

        queryExec.close();
    }

    @After
    public void tearDown() {
        this.datastore.close();
    }

}
