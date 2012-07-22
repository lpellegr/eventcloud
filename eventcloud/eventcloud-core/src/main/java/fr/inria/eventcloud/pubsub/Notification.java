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
package fr.inria.eventcloud.pubsub;

import java.io.Serializable;

import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.wrappers.BindingWrapper;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * Defines a notification that is received by a {@link SubscribeProxy}. A
 * notification contains the smallest entity which can be received (i.e. a
 * {@link Binding}) as a sub-solution for a given subscription.
 * 
 * Two notifications with the same {@link NotificationId} are complementary:
 * they contain independent sub-solutions which belong to the same solution.
 * 
 * @author lpellegr
 */
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    private final NotificationId id;

    private final BindingWrapper binding;

    private final String source;

    public Notification(NotificationId id, String source, Binding binding) {
        this.id = id;
        this.binding = new BindingWrapper(binding);
        this.source = source;
    }

    public NotificationId getId() {
        return this.id;
    }

    public Binding getBinding() {
        return this.binding;
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
        return 31 * (31 + this.id.hashCode()) + this.binding.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Notification) {
            Notification notif = (Notification) obj;

            return this.id.equals(notif.id) && this.binding == notif.binding;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Notification[id=" + this.id + ", binding=" + this.binding
                + ", source=" + this.source + "]";
    }

}
