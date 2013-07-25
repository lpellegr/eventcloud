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
package fr.inria.eventcloud.pubsub.notifications;

import java.util.List;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;

/**
 * Defines a notification that embeds a list of quadruples as value.
 * 
 * @author lpellegr
 */
public class QuadruplesNotification extends Notification<List<Quadruple>> {

    private static final long serialVersionUID = 151L;

    public QuadruplesNotification(SubscriptionId subscriptionId, Node eventId,
            String source, List<Quadruple> quadruples) {
        super(subscriptionId, eventId, source, quadruples);
    }

}
