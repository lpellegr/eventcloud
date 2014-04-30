/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

/**
 * Tests associated to {@link SparqlResultSerializer}.
 * 
 * @author lpellegr
 */
@RunWith(Parameterized.class)
public class SparqlResultSerializerTest {

    private DatasetGraph datastore;

    private final boolean compression;

    public SparqlResultSerializerTest(boolean compression) {
        this.compression = compression;
    }

    @Before
    public void setUp() {
        this.datastore = DatasetGraphFactory.createMem();
        // fills the datastore with some data
        for (int i = 0; i < 10; i++) {
            Node node = NodeFactory.createURI("http://abc" + i);
            this.datastore.add(node, node, node, node);
        }
    }

    @Test
    public void testEmptyBindingSerialization() {
        Binding binding = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SparqlResultSerializer.serialize(baos, binding, this.compression);

        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        Binding unserializedBinding =
                SparqlResultSerializer.deserializeBinding(
                        bais, this.compression);

        try {
            bais.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertNull(unserializedBinding);
    }

    @Test
    public void testBindingSerialization() {
        Node defaultNode = NodeFactory.createURI("http://www.inria.fr");

        BindingMap binding = BindingFactory.create();
        binding.add(Var.alloc("var1"), defaultNode);
        binding.add(Var.alloc("var2"), defaultNode);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SparqlResultSerializer.serialize(baos, binding, this.compression);

        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        Binding unserializedBinding =
                SparqlResultSerializer.deserializeBinding(
                        bais, this.compression);

        try {
            bais.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Set<String> vars = new HashSet<String>();
        Iterator<Var> varsIt = unserializedBinding.vars();
        while (varsIt.hasNext()) {
            vars.add(varsIt.next().getName());
        }

        Assert.assertTrue(vars.contains("var1"));
        Assert.assertTrue(vars.contains("var2"));
    }

    @Test
    public void testModelSerialization() {
        QueryExecution queryExec =
                QueryExecutionFactory.create(
                        QueryFactory.create("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }"),
                        DatasetFactory.create(this.datastore));
        Model model = queryExec.execConstruct();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SparqlResultSerializer.serialize(baos, model, this.compression);

        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        Model unserializedModel =
                SparqlResultSerializer.deserializeModel(bais, this.compression);

        try {
            bais.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(model.isIsomorphicWith(unserializedModel));

        queryExec.close();
    }

    @Test
    public void testResultSetSerialization() {
        QueryExecution queryExec =
                QueryExecutionFactory.create(
                        QueryFactory.create("SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }"),
                        DatasetFactory.create(this.datastore));
        ResultSet resultSet = queryExec.execSelect();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SparqlResultSerializer.serialize(baos, resultSet, this.compression);

        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        ResultSet unserializedResultSet =
                SparqlResultSerializer.deserializeResultSet(
                        bais, this.compression);

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

    @Parameterized.Parameters
    public static List<Boolean[]> parameters() {
        Boolean[][] data = new Boolean[][] { {false}, {true}};
        return Arrays.asList(data);
    }

}
