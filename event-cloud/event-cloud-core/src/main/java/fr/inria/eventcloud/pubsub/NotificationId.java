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
package fr.inria.eventcloud.pubsub;

import java.io.Serializable;

import com.google.common.base.Objects;

import fr.inria.eventcloud.api.SubscriptionId;

/**
 * This class uniquely identify a {@link Notification}. The identifier is made
 * of a {@link SubscriptionId} and a timestamp value that defines when the
 * notification has been created.
 * <p>
 * Two {@link NotificationId}s with the same {@code subscriptionId} and
 * {@code timestamp} value identify two {@link Notification}s that are
 * complementary and are assumed to be delivered together.
 * 
 * @author lpellegr
 */
public class NotificationId implements Serializable {

    private static final long serialVersionUID = 1L;

    private final SubscriptionId subscriptionId;

    private final long timestamp;

    /**
     * Constructs a NotificationId from the specified parameters.
     * 
     * @param id
     * @param timestamp
     */
    public NotificationId(SubscriptionId id, long timestamp) {
        this.subscriptionId = id;
        // TODO: is it necessary to use a UUID to ensure uniqueness?
        this.timestamp = timestamp;
    }

    public SubscriptionId getSubscriptionId() {
        return this.subscriptionId;
    }

    /**
     * Returns the timestamp value associated to the notification. The timestamp
     * value is used to identify a set of notifications that have to be
     * delivered together.
     * 
     * @return the timestamp value associated to the notification.
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(this.subscriptionId, this.timestamp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NotificationId) {
            NotificationId id = (NotificationId) obj;
            return this.subscriptionId.equals(id.subscriptionId)
                    && this.timestamp == id.timestamp;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[subscriptionId=" + this.subscriptionId + ", hash="
                + this.timestamp + "]";
    }

}
