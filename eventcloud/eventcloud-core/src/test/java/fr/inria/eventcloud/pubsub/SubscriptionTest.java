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
package fr.inria.eventcloud.pubsub;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastoreBuilder;

/**
 * Test cases associated to {@link Subscription}.
 * 
 * @author lpellegr
 */
public class SubscriptionTest {

    @Test
    public void testParseSubscription() {
        String sparqlQuery =
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?name ?mail WHERE { GRAPH ?g { ?id foaf:name ?name . ?id foaf:email ?email } }";

        Subscription subscription =
                new Subscription(
                        new SubscriptionId(), new SubscriptionId(),
                        new SubscriptionId(), System.currentTimeMillis(),
                        System.currentTimeMillis(), sparqlQuery,
                        "rmi://oops:1099", null,
                        NotificationListenerType.BINDING);

        List<Quadruple> quads = subscription.toQuadruples();

        TransactionalTdbDatastore datastore =
                new TransactionalTdbDatastoreBuilder().build();
        datastore.open();

        TransactionalDatasetGraph txnGraph = datastore.begin(AccessMode.WRITE);

        try {
            txnGraph.add(quads);
            txnGraph.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        Subscription deserializedSubscription =
                Subscription.parseFrom(datastore, subscription.getId());

        List<Quadruple> newQuads = deserializedSubscription.toQuadruples();

        for (Quadruple quad : newQuads) {
            Assert.assertTrue(quads.contains(quad));
        }

        Assert.assertEquals(quads.size(), newQuads.size());

        datastore.close();
    }

}
