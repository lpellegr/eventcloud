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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Set;

import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.util.Util;

import com.google.common.base.Objects;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.SubscriptionId;

/**
 * Used to uniquely identify a {@link Notification}. It is unique for a given
 * subscription identifier and event identifier.
 * 
 * @author lpellegr
 */
public final class NotificationId implements Serializable {

    private static final long serialVersionUID = 160L;

    public static final NotificationIdExternalizer SERIALIZER =
            new NotificationIdExternalizer();

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
        if (this == that) {
            return true;
        }

        if (that instanceof NotificationId) {
            NotificationId other = (NotificationId) that;

            return this.subscriptionId.equals(other.subscriptionId)
                    && this.eventId.equals(other.eventId);
        }

        return false;
    }

    public boolean isFor(SubscriptionId subscriptionId) {
        return this.subscriptionId.equals(subscriptionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{eventId=" + this.eventId
                + ", subscriptionId=" + this.subscriptionId.toString() + "}";
    }

    public static class NotificationIdExternalizer implements
            AdvancedExternalizer<NotificationId> {

        private static final long serialVersionUID = 160L;

        @Override
        public void writeObject(ObjectOutput output,
                                NotificationId notificationId)
                throws IOException {
            SubscriptionId.SERIALIZER.writeObject(
                    output, notificationId.subscriptionId);
            output.writeUTF(notificationId.eventId);
        }

        @Override
        public NotificationId readObject(ObjectInput input) throws IOException,
                ClassNotFoundException {
            return new NotificationId(
                    SubscriptionId.SERIALIZER.readObject(input),
                    input.readUTF());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<Class<? extends NotificationId>> getTypeClasses() {
            return Util.<Class<? extends NotificationId>> asSet(NotificationId.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer getId() {
            return 1;
        }

    }

}
