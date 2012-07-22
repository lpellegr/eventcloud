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
package fr.inria.eventcloud.api;

import java.util.UUID;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;

import fr.inria.eventcloud.utils.UniqueId;

/**
 * Uniquely identify a subscription which has been submitted on an eventcloud.
 * 
 * @author lpellegr
 */
public class SubscriptionId extends UniqueId {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a unique subscription id .
     */
    public SubscriptionId() {
        super();
    }

    private SubscriptionId(UUID uuid) {
        super(uuid);
    }

    /**
     * Creates a Jena {@link Node_Literal} representing the subscription id.
     * 
     * @return a Jena {@link Node_Literal} representing the subscription id.
     */
    public Node toJenaNode() {
        return Node.createLiteral(super.toString());
    }

    public static SubscriptionId parseSubscriptionId(String subscriptionId) {
        return new SubscriptionId(decode(subscriptionId));
    }

}
