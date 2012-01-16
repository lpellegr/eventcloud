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
import java.util.UUID;

import com.google.common.base.Objects;

import fr.inria.eventcloud.api.SubscriptionId;

/**
 * This class uniquely identify a {@link Notification}.
 * 
 * @author lpellegr
 */
public class NotificationId implements Serializable {

    private static final long serialVersionUID = 1L;

    // original subscription identifier
    private final SubscriptionId subscriptionId;

    private final UUID uuid;

    /**
     * Constructs a NotificationId from the specified parameters.
     * 
     * @param id
     *            the subscription identifier that identifies the subscription
     *            which is matched by the notification.
     */
    public NotificationId(SubscriptionId id) {
        this.subscriptionId = id;
        this.uuid = UUID.randomUUID();
    }

    public SubscriptionId getSubscriptionId() {
        return this.subscriptionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(this.subscriptionId, this.uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NotificationId) {
            NotificationId nid = (NotificationId) obj;
            return this.subscriptionId.equals(nid.subscriptionId)
                    && this.uuid.equals(nid.uuid);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("subscriptionId", this.subscriptionId)
                .add("uuid", this.uuid)
                .toString();
    }

}
