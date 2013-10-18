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
package fr.inria.eventcloud.messages.request;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.messages.response.StatelessQuadruplePatternResponse;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.reasoner.AtomicQuery;

/**
 * This class is used to route and to handle an unsubscribe request. It returns
 * a {@link StatelessQuadruplePatternResponse}.
 * 
 * @author lpellegr
 */
public class UnsubscribeRequest extends StatelessQuadruplePatternRequest {

    private static final long serialVersionUID = 160L;

    private final SerializedValue<SubscriptionId> originalSubscriptionId;

    // indicates whether the original subscription was using a bindings
    // notification listener or not
    private final boolean useBindingNotificationListener;

    public UnsubscribeRequest(SubscriptionId originalSubscriptionId,
            AtomicQuery atomicQuery, boolean useBindingNotificationListener) {
        this(originalSubscriptionId, atomicQuery,
                useBindingNotificationListener, false);
    }

    public UnsubscribeRequest(SubscriptionId originalSubscriptionId,
            AtomicQuery atomicQuery, boolean useBindingNotificationListener,
            boolean async) {
        super(atomicQuery.getQuadruplePattern());

        if (async) {
            super.responseProvider = null;
        }

        this.originalSubscriptionId =
                SerializedValue.create(originalSubscriptionId);
        this.useBindingNotificationListener = useBindingNotificationListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerValidatingKeyConstraints(CanOverlay<SemanticElement> overlay,
                                               QuadruplePattern quadruplePattern) {
        ((SemanticCanOverlay) overlay).deleteSubscriptions(
                this.originalSubscriptionId.getValue(),
                this.useBindingNotificationListener);
    }

}
