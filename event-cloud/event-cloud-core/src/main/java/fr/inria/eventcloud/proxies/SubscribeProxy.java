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
package fr.inria.eventcloud.proxies;

import java.io.Serializable;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.properties.AlterableElaProperty;
import fr.inria.eventcloud.pubsub.Notification;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * A SubscribeProxy is a proxy that implements the {@link SubscribeApi}. It has
 * to be used by a user who wants to execute asynchronous subscribe operations
 * on an Event Cloud.
 * <p>
 * This proxy offers the possibility to reconstruct an Event from the binding
 * which has matched a subscription by a call to
 * {@link SubscribeProxy#reconstructCompoundEvent(Subscription, Binding)} or
 * {@link SubscribeProxy#reconstructCompoundEvent(SubscriptionId, Node)} and
 * also by an using an {@link CompoundEventNotificationListener} when you
 * subscribe with
 * {@link #subscribe(String, fr.inria.eventcloud.api.listeners.NotificationListener)}
 * . The reconstruction is an heavy operation that may be used carefully.
 * Indeed, to reconstruct an {@link CompoundEvent} from its identifier, a
 * {@link QuadruplePattern} query must be sent to all the peers matching the
 * graph value corresponding to the event identifier. Because three dimensions
 * among four are not fixed, a lot of peers are contacted. Moreover, due to the
 * fact that the proxies and the event cloud infrastructure are decoupled (and
 * because each quadruple that belongs to an Event is published asynchronously),
 * it is not possible to guarantee that all the quadruples that belong to the
 * event identifier have been retrieved after the execution of the first
 * {@link QuadruplePattern}. That's why the reconstruction consists in polling
 * periodically the network with a {@link QuadruplePattern} while all the
 * quadruples that belong to the event identifier are not retrieved.
 * <p>
 * To avoid the reception of replica during the polling, a list of hashes (where
 * each hash correspond to the hash of the subject, predicate and object values
 * that belong to a quadruple previously received) is sent along the
 * {@link QuadruplePattern}. Thus, on the peer which receives the
 * {@link QuadruplePattern} to execute, it is possible to evict the quadruples
 * which have already been received by computing a diff.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public interface SubscribeProxy extends Proxy, SubscribeApi, Serializable {

    /**
     * The init method is a convenient method for components which is used to
     * initialize the {@link EventCloudCache}. Once this method is called and
     * the value is set, the next calls perform no action.
     * 
     * @param proxy
     *            the event cloud proxy instance to set to the subscribe proxy.
     * 
     * @param componentUri
     *            the URI at which the component is bind.
     * 
     * @param properties
     *            a set of {@link AlterableElaProperty} properties to use for
     *            initializing the {@link SubscribeProxy}.
     */
    public void init(EventCloudCache proxy, String componentUri,
                     AlterableElaProperty[] properties);

    /**
     * Searches the {@link Subscription} associated to the specified
     * {@link SubscriptionId}.
     * 
     * @param id
     *            the identifier associated to the {@link Subscription} to
     *            lookup.
     * 
     * @return the subscription associated to the {@code id} if the {@code id}
     *         is contained into the list of the subscription identifiers that
     *         have been register from this proxy, {@code false} otherwise.
     */
    public Subscription find(SubscriptionId id);

    /**
     * Reconstructs a {@link CompoundEvent} from the specified
     * {@code subscription} and {@code binding}. A call to this method block
     * until the whole event has been retrieved. <strong>This operation must be
     * used carefully</strong>. It is the invoker responsability to parallelize
     * several calls to this method.
     * 
     * @param subscription
     *            the subscription that is used to retrieve the name of the
     *            graph variable.
     * 
     * @param binding
     *            the binding containing the value associated to the graph
     *            variable extracted from the subscription. The value which is
     *            read from the binding is the event identifier.
     * 
     * @return the compound event which has been reconstructed.
     */
    public CompoundEvent reconstructCompoundEvent(Subscription subscription,
                                                  Binding binding);

    /**
     * Reconstructs a {@link CompoundEvent} from the specified {@code eventId}.
     * A call to this method block until the whole compound event has been
     * retrieved. <strong>This operation must be used carefully</strong>. It is
     * the invoker responsibility to parallelize several calls to this method.
     * 
     * @param id
     *            the subscription identifier that identifies which subscription
     *            is matched.
     * 
     * @param eventId
     *            the event identifier to use for retrieving the quadruples that
     *            belong to the compound event.
     * 
     * @return the event which has been reconstructed.
     */
    public CompoundEvent reconstructCompoundEvent(SubscriptionId id,
                                                  Node eventId);

    /**
     * Used internally to send back a notification.
     * 
     * @param notification
     *            the notification that is received.
     */
    public void receive(Notification notification);

}
