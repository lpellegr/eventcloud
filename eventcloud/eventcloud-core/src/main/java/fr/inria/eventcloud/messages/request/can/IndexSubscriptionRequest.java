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
package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * Request used to index a subscription or a rewritten subscription after the
 * publication of a quadruple. While the rewritten subscription is indexed, it
 * is possible to have received some quadruples that match the rewritten
 * subscription. That's why an algorithm similar to the one from
 * {@link PublishQuadrupleRequest} is applied to rewrite the subscription for
 * each quadruple that matches it. This type of request is used for SBCE1, SBCE2
 * and SBCE3.
 * 
 * @see PublishQuadrupleRequest
 * 
 * @author lpellegr
 */
public class IndexSubscriptionRequest extends StatelessQuadruplePatternRequest {

    private static final long serialVersionUID = 140L;

    private static final Logger log =
            LoggerFactory.getLogger(IndexSubscriptionRequest.class);

    protected SerializedValue<Subscription> subscription;

    /**
     * Constructs an IndexRewrittenSubscriptionRequest from the specified
     * rewritten {@code subscription}.
     * 
     * @param subscription
     *            the rewritten subscription to index.
     */
    public IndexSubscriptionRequest(Subscription subscription) {
        super(subscription.getSubSubscriptions()[0].getAtomicQuery()
                .getQuadruplePattern(), null);

        this.subscription = SerializedValue.create(subscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerValidatingKeyConstraints(final CanOverlay<SemanticElement> overlay,
                                               QuadruplePattern quadruplePattern) {
        SemanticCanOverlay semanticOverlay = (SemanticCanOverlay) overlay;
        Subscription subscription = this.subscription.getValue();

        if (P2PStructuredProperties.ENABLE_BENCHMARKS_INFORMATION.getValue()) {
            log.info("It took "
                    + (System.currentTimeMillis() - subscription.getCreationTime())
                    + "ms to receive subscription : "
                    + subscription.getSparqlQuery());
        }

        log.debug("Indexing subscription {} on peer {}", subscription, overlay);

        semanticOverlay.getIndexSubscriptionRequestDelayer().receive(
                subscription);
    }

}
