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
import java.util.UUID;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;

import fr.inria.eventcloud.utils.Base64UUID;

/**
 * Uniquely identify a subscription which has been submitted on an eventcloud.
 * 
 * @author lpellegr
 */
public class SubscriptionId implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String value;

    /**
     * Constructs a subscription identifier from the specified value.
     * 
     * @param value
     *            the value to use in order to create the identifier.
     */
    private SubscriptionId(String value) {
        this.value = value;
    }

    /**
     * Creates a new Jena {@link Node_Literal} representing the current
     * subscription identifier.
     * 
     * @return a new Jena {@link Node_Literal} representing the current
     *         subscription identifier.
     */
    public Node toJenaNode() {
        return Node.createLiteral(this.value.toString());
    }

    /**
     * Parses the string argument as a SubscriptionId.
     * 
     * @param base64uuid
     *            a <code>String</code> containing the
     *            <code>SubscriptionId</code> representation to be parsed.
     * 
     * @return the <code>SubscriptionId</code> represented by the argument.
     */
    public static final SubscriptionId parseFrom(String base64uuid) {
        return new SubscriptionId(base64uuid);
    }

    /**
     * Generates a new subscription identifier randomly by using
     * {@link UUID#randomUUID()}.
     * 
     * @return a new subscription identifier generated randomly by using
     *         {@link UUID#randomUUID()}.
     */
    public static final SubscriptionId random() {
        return new SubscriptionId(Base64UUID.encode(UUID.randomUUID()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SubscriptionId) {
            return this.value.equals(((SubscriptionId) obj).value);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value.toString();
    }

}
