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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;
import java.util.UUID;

import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.infinispan.commons.util.Util;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Literal;

import fr.inria.eventcloud.utils.UniqueId;

/**
 * Uniquely identify a subscription which has been submitted on an EventCloud.
 * 
 * @author lpellegr
 */
@SerializeWith(SubscriptionId.SubscriptionIdExternalizer.class)
public final class SubscriptionId extends UniqueId {

    private static final long serialVersionUID = 160L;

    public static final SubscriptionIdExternalizer SERIALIZER =
            new SubscriptionIdExternalizer();

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
        return NodeFactory.createLiteral(super.toString());
    }

    public static SubscriptionId parseSubscriptionId(String subscriptionId) {
        return new SubscriptionId(decode(subscriptionId));
    }

    public static class SubscriptionIdExternalizer implements
            AdvancedExternalizer<SubscriptionId> {

        private static final long serialVersionUID = 160L;

        @Override
        public void writeObject(ObjectOutput output,
                                SubscriptionId subscriptionId)
                throws IOException {
            output.writeLong(subscriptionId.value.getMostSignificantBits());
            output.writeLong(subscriptionId.value.getLeastSignificantBits());
        }

        @Override
        public SubscriptionId readObject(ObjectInput input) throws IOException,
                ClassNotFoundException {
            return new SubscriptionId(new UUID(
                    input.readLong(), input.readLong()));
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<Class<? extends SubscriptionId>> getTypeClasses() {
            return Util.<Class<? extends SubscriptionId>> asSet(SubscriptionId.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer getId() {
            return 2;
        }

    }

}
