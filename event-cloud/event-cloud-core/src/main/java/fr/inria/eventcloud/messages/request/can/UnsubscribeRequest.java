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

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * This class is used to route and to handle an unsubscribe request.
 * 
 * @author lpellegr
 */
public class UnsubscribeRequest extends StatelessQuadruplePatternRequest {

    private static final long serialVersionUID = 1L;

    private final SerializedValue<SubscriptionId> originalSubscriptionId;

    public UnsubscribeRequest(Subscription subscription) {
        super(subscription.getSubSubscriptions()[0].getAtomicQuery()
                .getQuadruplePattern());
        this.originalSubscriptionId =
                new SerializedValue<SubscriptionId>(subscription.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerValidatingKeyConstraints(CanOverlay overlay,
                                               QuadruplePattern quadruplePattern) {
        ((SemanticCanOverlay) overlay).deleteSubscription(this.originalSubscriptionId.getValue());
    }

}
