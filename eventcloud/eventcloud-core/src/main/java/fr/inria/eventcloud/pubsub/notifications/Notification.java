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

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * Defines a notification that is received by a {@link SubscribeProxy}. A
 * notification contains one or more chunks associated to a solution that
 * matches a subscription. It depends on the notification listener used by the
 * subscriber and the publish/subscribe algorithm used.
 * <p>
 * Two notifications with the same {@link NotificationId} are complementary:
 * they contain independent sub-solutions (chunks) which belong to the same
 * solution.
 * 
 * @param T
 *            the type of the chunks contained by the notification.
 * 
 * @author lpellegr
 */
public abstract class Notification<T> implements Serializable {

    private static final long serialVersionUID = 130L;

    private final NotificationId id;

    private final SubscriptionId subscriptionId;

    private final T content;

    private final String source;

    public Notification(SubscriptionId subscriptionId, Node eventId,
            String source, T binding) {
        this(subscriptionId, eventId.getURI(), source, binding);
    }

    public Notification(SubscriptionId subscriptionId, String eventId,
            String source, T binding) {
        this(new NotificationId(subscriptionId, eventId), subscriptionId,
                source, binding);
    }

    public Notification(NotificationId id, SubscriptionId subscriptionId,
            String source, T binding) {
        this.id = id;
        this.subscriptionId = subscriptionId;
        this.content = binding;
        this.source = source;
    }

    public NotificationId getId() {
        return this.id;
    }

    public SubscriptionId getSubscriptionId() {
        return this.subscriptionId;
    }

    public T getContent() {
        return this.content;
    }

    /**
     * Returns the URL of the peer which has sent the notification.
     * 
     * @return the URL of the peer which has sent the notification.
     */
    public String getSource() {
        return this.source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        return obj instanceof Notification
                && this.id.equals(((Notification<T>) obj).id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.id.toString();
    }

}
