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
package fr.inria.eventcloud.api;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;

import fr.inria.eventcloud.utils.UniqueId;

/**
 * Uniquely identify a subscription which has been submitted on an EventCloud.
 * 
 * @author lpellegr
 */
public class SubscriptionId extends UniqueId {

    private static final long serialVersionUID = 140L;

    public static final Serializer SERIALIZER = new Serializer();

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

    public static final class Serializer implements
            org.mapdb.Serializer<SubscriptionId>, Serializable {

        private static final long serialVersionUID = 140L;

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(DataOutput out, SubscriptionId value)
                throws IOException {
            out.writeLong(value.value.getMostSignificantBits());
            out.writeLong(value.value.getLeastSignificantBits());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SubscriptionId deserialize(DataInput in, int available)
                throws IOException {
            return new SubscriptionId(new UUID(in.readLong(), in.readLong()));
        }

    }

}
