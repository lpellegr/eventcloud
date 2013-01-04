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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import com.google.common.base.Objects;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.SubscriptionId;

/**
 * Used to uniquely identify a {@link Notification}. It is unique for a given
 * subscription identifier and event identifier.
 * 
 * @author lpellegr
 */
public class NotificationId implements Serializable {

    private static final long serialVersionUID = 140L;

    protected final SubscriptionId subscriptionId;

    protected final String eventId;

    public NotificationId(SubscriptionId subscriptionId, Node eventId) {
        this(subscriptionId, eventId.getURI());
    }

    /**
     * Constructs a NotificationId from the identifier of the subscription which
     * is matched and the event identifier matching it.
     * 
     * @param subscriptionId
     *            the subscription identifier that identifies the subscription
     *            which is matched and triggers the notification.
     * @param eventId
     *            the identifier of the event matching a subscription that
     *            triggers a notification.
     */
    protected NotificationId(SubscriptionId subscriptionId, String eventId) {
        this.subscriptionId = subscriptionId;
        this.eventId = eventId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(this.subscriptionId, this.eventId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
        if (that instanceof NotificationId) {
            NotificationId other = (NotificationId) that;

            return this.subscriptionId.equals(other.subscriptionId)
                    && this.eventId.equals(other.eventId);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{eventId=" + this.eventId
                + ", subscriptionId=" + this.subscriptionId.toString() + "}";
    }

    public static final class Serializer implements
            org.mapdb.Serializer<NotificationId>, Serializable {

        private static final long serialVersionUID = 140L;

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(DataOutput out, NotificationId notificationId)
                throws IOException {
            out.writeUTF(notificationId.eventId);

            SubscriptionId.SERIALIZER.serialize(
                    out, notificationId.subscriptionId);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NotificationId deserialize(DataInput in, int available)
                throws IOException {
            String eventId = in.readUTF();

            SubscriptionId subscriptionId =
                    SubscriptionId.SERIALIZER.deserialize(in, available);

            return new NotificationId(subscriptionId, eventId);
        }

    }

}
