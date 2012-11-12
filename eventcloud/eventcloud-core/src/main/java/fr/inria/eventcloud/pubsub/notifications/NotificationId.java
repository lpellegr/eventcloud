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
package fr.inria.eventcloud.pubsub.notifications;

import java.io.Serializable;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.SubscriptionId;

/**
 * Used to uniquely identify a {@link Notification}. It is unique for a given
 * subscription identifier and event identifier.
 * 
 * @author lpellegr
 */
public class NotificationId implements Serializable {

    private static final long serialVersionUID = 130L;

    // hash value computed from the identifier of the subscription which is
    // matched and the event identifier (without meta information) matching it
    private final HashCode value;

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
        Hasher hasher = Hashing.murmur3_128().newHasher();
        hasher.putString(subscriptionId.toString());
        hasher.putString(eventId);

        this.value = hasher.hash();
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
    public boolean equals(Object that) {
        return that instanceof NotificationId
                && this.value.equals(((NotificationId) that).value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value.toString();
    }

}
