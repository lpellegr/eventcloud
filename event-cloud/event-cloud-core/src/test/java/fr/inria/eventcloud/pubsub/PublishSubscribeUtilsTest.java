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
package fr.inria.eventcloud.pubsub;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

/**
 * Test cases associated to {@link PublishSubscribeUtils}.
 * 
 * @author lpellegr
 */
public class PublishSubscribeUtilsTest {

    @Test
    public void testRemoveResultVarsExceptGraphVar() {
        String[] subscriptions =
                {
                        "SELECT ?oddName ?s ?p ?o WHERE { GRAPH ?oddName { ?s ?p ?o }}",
                        "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o }}",
                        "SELECT ?g ?p ?o WHERE { GRAPH ?g { <http://www.inria.fr> ?p ?o }}",
                        "SELECT ?g ?s ?o WHERE { GRAPH ?g { ?s <http://www.inria.fr> ?o }}",
                        "SELECT ?g ?p ?o WHERE { GRAPH ?g { ?s ?p <http://www.inria.fr> }}",
                        "SELECT ?g ?s WHERE { GRAPH ?g { ?s <http://www.inria.fr> <http://www.inria.fr> }}",
                        "SELECT ?g ?p WHERE { GRAPH ?g { <http://www.inria.fr> ?p <http://www.inria.fr> }}",
                        "SELECT ?g ?o WHERE { GRAPH ?g { <http://www.inria.fr> <http://www.inria.fr> ?o }}",
                        "SELECT ?g WHERE { GRAPH ?g { <http://www.inria.fr> <http://www.inria.fr> <http://www.inria.fr> }}"};

        for (String subscription : subscriptions) {
            testParameterizedRemoveResultVarsExceptGraphVar(subscription);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveResultVarsExceptGraphVarWithoutGraphVar() {
        testParameterizedRemoveResultVarsExceptGraphVar("SELECT ?s ?p ?o WHERE { GRAPH <http://www.inria.fr> { ?s ?p ?o }}");
    }

    private void testParameterizedRemoveResultVarsExceptGraphVar(String subscription) {
        Query rewrittenQuery =
                QueryFactory.create(PublishSubscribeUtils.removeResultVarsExceptGraphVar(subscription));

        Assert.assertTrue(rewrittenQuery.getResultVars().size() == 1);
    }

}
