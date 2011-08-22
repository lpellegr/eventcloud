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

import junit.framework.Assert;

import org.junit.Test;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.datastore.InMemoryJenaDatastore;

/**
 * Test cases associated to {@link Subscription}.
 * 
 * @author lpellegr
 */
public class SubscriptionTest {

    @Test
    public void testParseSubscription() {
        Subscription subscription =
                new Subscription(
                        "rmi://oops:1099",
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?name ?mail WHERE { GRAPH ?g { ?id foaf:name ?name . ?id foaf:email ?email } }");

        Collection<Quadruple> quads = subscription.toQuadruples();

        InMemoryJenaDatastore datastore = new InMemoryJenaDatastore();
        datastore.open();
        datastore.add(quads);

        Subscription deserializedSubscription =
                Subscription.parseFrom(datastore, subscription.getId());

        Collection<Quadruple> newQuads =
                deserializedSubscription.toQuadruples();

        for (Quadruple quad : newQuads) {
            Assert.assertTrue(quads.contains(quad));
        }

        datastore.close();
    }

}
