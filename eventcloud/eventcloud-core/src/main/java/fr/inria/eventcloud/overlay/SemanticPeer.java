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
package fr.inria.eventcloud.overlay;

import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerInterface;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * 
 * @author bsauvan
 */
public interface SemanticPeer extends PeerInterface {

    /**
     * Publishes the specified quadruple.
     * 
     * @param quad
     *            the quadruple to publish.
     */
    void publish(Quadruple quad);

    /**
     * Publishes the specified compound event.
     * 
     * @param event
     *            the compound event to publish.
     */
    void publish(CompoundEvent event);

    /**
     * Indexes the specified subscription.
     * 
     * @param subscription
     *            the subscription to index.
     */
    void subscribe(Subscription subscription);

}
