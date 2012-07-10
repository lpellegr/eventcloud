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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;

import fr.inria.eventcloud.exceptions.DecompositionException;

/**
 * Test cases associated to the {@link SparqlDecomposer}.
 * 
 * @author lpellegr
 */
public class SparqlDecomposerTest {

    private SparqlDecomposer decomposer;

    public SparqlDecomposerTest() {
        this.decomposer = SparqlDecomposer.getInstance();
    }

    @Test
    public void testLegalAskQuery() throws DecompositionException {
        List<AtomicQuery> atomicQueries =
                this.decomposer.decompose("ASK { GRAPH ?g { ?s ?p ?o } }");

        assertCorrectDecomposition(atomicQueries, 1, 4);
    }

    @Test
    public void testLegalConstructQuery() throws DecompositionException {
        List<AtomicQuery> atomicQueries =
                this.decomposer.decompose("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }");

        assertCorrectDecomposition(atomicQueries, 1, 4);
    }

    @Test
    public void testLegalDescribeQuery() throws DecompositionException {
        List<AtomicQuery> atomicQueries =
                this.decomposer.decompose("DESCRIBE ?s ?p ?o { GRAPH ?g { ?s ?p ?o } }");

        assertCorrectDecomposition(atomicQueries, 1, 4);
    }

    @Test
    public void testLegalSelectQuery1() throws DecompositionException {
        List<AtomicQuery> atomicQueries =
                this.decomposer.decompose("SELECT ?s ?p ?o { GRAPH ?g { ?s ?p ?o } }");

        assertCorrectDecomposition(atomicQueries, 1, 4);
    }

    @Test
    public void testLegalSelectQuery2() throws DecompositionException {
        List<AtomicQuery> atomicQueries =
                this.decomposer.decompose("SELECT ?s ?p ?o { GRAPH ?g { ?s ?p ?o . ?o ?p ?s } }");

        assertCorrectDecomposition(atomicQueries, 2, 4);
    }

    @Test
    public void testLegalSelectQuery3() throws DecompositionException {
        List<AtomicQuery> atomicQueries =
                this.decomposer.decompose("SELECT ?s ?p ?o { GRAPH ?g { ?s <urn:p:0> ?o . ?s <urn:p:1> ?p } }");

        assertCorrectDecomposition(atomicQueries, 2, 3);
    }

    @Test
    public void testLegalSelectQuery4() throws DecompositionException {
        List<AtomicQuery> atomicQueries =
                this.decomposer.decompose("SELECT ?s ?p ?o { GRAPH ?g { { ?s <urn:p:0> ?o } UNION { ?s <urn:p:1> ?p } } }");

        assertCorrectDecomposition(atomicQueries, 2, 3);
    }

    @Test
    public void testLegalSelectQuery5() throws DecompositionException {
        List<AtomicQuery> atomicQueries =
                this.decomposer.decompose("SELECT DISTINCT ?s ?p ?o { GRAPH ?g { ?s ?p ?o } } LIMIT 1000");

        assertCorrectDecomposition(atomicQueries, 1, 4);

        AtomicQuery atomicQuery = atomicQueries.get(0);

        assertTrue(atomicQuery.hasLimit());
        assertTrue(atomicQuery.isDistinct());
        assertFalse(atomicQuery.isReduced());
        assertEquals(1000, atomicQuery.getLimit());
    }

    @Test
    public void testLegalSelectQuery6() throws DecompositionException {
        List<AtomicQuery> atomicQueries =
                this.decomposer.decompose("SELECT REDUCED ?s ?p ?o { GRAPH ?g { ?s ?p ?o } } LIMIT 1000 OFFSET 200");

        assertCorrectDecomposition(atomicQueries, 1, 4);

        AtomicQuery atomicQuery = atomicQueries.get(0);

        assertTrue(atomicQuery.hasLimit());
        assertFalse(atomicQuery.isDistinct());
        assertTrue(atomicQuery.isReduced());
        assertEquals(1000, atomicQuery.getLimit());
    }

    @Test(expected = DecompositionException.class)
    public void testIllegalQueryWithTwoGraphPatterns()
            throws DecompositionException {
        this.decomposer.decompose("SELECT ?g1 ?g2 WHERE { GRAPH ?g1 { ?s ?p ?o } GRAPH ?g2 { ?o ?p ?s } }");
    }

    @Test(expected = DecompositionException.class)
    public void testIllegalQueryWithoutGraphPattern()
            throws DecompositionException {
        this.decomposer.decompose("SELECT ?g WHERE { ?s ?p ?o }");
    }

    private static void assertCorrectDecomposition(List<AtomicQuery> atomicQueries,
                                                   int nbAtomicQueries,
                                                   int nbVariablePerAtomicQuery) {
        assertEquals(nbAtomicQueries, atomicQueries.size());

        for (AtomicQuery atomicQuery : atomicQueries) {
            assertEquals(nbVariablePerAtomicQuery, atomicQuery.getNbVars());
        }
    }

}
