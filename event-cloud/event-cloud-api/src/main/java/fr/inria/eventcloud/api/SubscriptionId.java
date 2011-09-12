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
package fr.inria.eventcloud.api;

import java.io.Serializable;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;

import fr.inria.eventcloud.utils.LongLong;

/**
 * Uniquely identify a subscription or a sub-subscription that has been
 * submitted on an Event Cloud.
 * 
 * @author lpellegr
 */
public class SubscriptionId implements Serializable {

    private static final long serialVersionUID = 1L;

    private LongLong hashValue;

    public SubscriptionId() {
        // empty constructor for webservices
    }

    /**
     * Constructs a subscription identifier from the specified hash value. The
     * hash value is supposed to be a 128 bits hash value.
     * 
     * @param hashValue
     *            the hash value to use in order to create the identifier.
     * 
     * @throws IllegalArgumentException
     *             if the specified hash value is not a 128 bits hash value.
     */
    public SubscriptionId(LongLong hashValue) {
        this.hashValue = hashValue;
    }

    /**
     * Creates a new Jena {@link Node_Literal} representing the current
     * subscription identifier.
     * 
     * @return a new Jena {@link Node_Literal} representing the current
     *         subscription identifier.
     */
    public Node toJenaNode() {
        return Node.createLiteral(this.hashValue.toString());
    }

    /**
     * Parses the string argument as a SubscriptionId.
     * 
     * @param subscriptionId
     *            a <code>String</code> containing the
     *            <code>SubscriptionId</code> representation to be parsed.
     * 
     * @return the <code>SubscriptionId</code> represented by the argument.
     */
    public static final SubscriptionId parseFrom(String subscriptionId) {
        return new SubscriptionId(LongLong.fromString(subscriptionId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SubscriptionId) {
            return this.hashValue.equals(((SubscriptionId) obj).hashValue);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.hashValue.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.hashValue.toString();
    }

}
