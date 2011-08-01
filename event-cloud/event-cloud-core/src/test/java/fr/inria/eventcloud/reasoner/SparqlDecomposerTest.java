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
package fr.inria.eventcloud.reasoner;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests associated to the {@link SparqlDecomposer} class.
 * 
 * @author lpellegr
 */
public class SparqlDecomposerTest {

    private SparqlDecomposer decomposer;

    public SparqlDecomposerTest() {
        this.decomposer = new SparqlDecomposer();
    }

    @Test
    public void testDecomposition() {
        List<AtomicQuery> atomicQueries =
                this.decomposer.decompose("ASK { GRAPH ?g { ?s ?p ?o } }");

        Assert.assertEquals(1, atomicQueries.size());
        Assert.assertEquals(4, atomicQueries.get(0).getVariables().size());

        atomicQueries =
                this.decomposer.decompose("SELECT ?g WHERE { GRAPH ?g { ?s <http://www.inria.fr/eventcloud/predicate1> ?o . ?s <http://www.inria.fr/eventcloud/predicate2> ?o } }");
        Assert.assertEquals(2, atomicQueries.size());
    }

}
