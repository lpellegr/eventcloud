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
package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.JenaDatastore;
import fr.inria.eventcloud.overlay.SparqlRequestResponseManager;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * Indexes a {@link Subscription} into the network. The indexing operation
 * consist in reaching all the peer that matches the first sub subscription
 * contained by the subscription. Then, when we are on one of these peer we have
 * to put the subscription into the local cache and to store the subscription
 * into the local datastore.
 * 
 * @author lpellegr
 */
public class IndexSubscriptionRequest extends StatelessQuadruplePatternRequest {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(IndexSubscriptionRequest.class);

    protected SerializedValue<Subscription> subscription;

    public IndexSubscriptionRequest(Subscription subscription) {
        super(subscription.getSubSubscriptions()[0].getAtomicQuery()
                .getQuadruplePattern());
        this.subscription = new SerializedValue<Subscription>(subscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerValidatingKeyConstraints(CanOverlay overlay,
                                               QuadruplePattern quadruplePattern) {
        Subscription subscription = this.subscription.getValue();

        // puts the subscription into the cache
        ((SparqlRequestResponseManager) overlay.getRequestResponseManager()).getSubscriptionsCache()
                .put(subscription.getId(), subscription);

        // writes the subscription into the datastore
        JenaDatastore datastore = (JenaDatastore) overlay.getDatastore();
        datastore.add(subscription.toQuadruples());

        log.debug(
                "Subscription {} has been indexed on peer {}",
                subscription.getId(), overlay);
    }

}
