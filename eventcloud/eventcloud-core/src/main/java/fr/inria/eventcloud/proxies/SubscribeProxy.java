/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.proxies;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.notifications.BindingNotification;
import fr.inria.eventcloud.pubsub.notifications.NotificationId;
import fr.inria.eventcloud.pubsub.notifications.PollingSignalNotification;
import fr.inria.eventcloud.pubsub.notifications.QuadruplesNotification;
import fr.inria.eventcloud.pubsub.notifications.SignalNotification;

/**
 * A SubscribeProxy is a proxy that implements the {@link SubscribeApi}. It has
 * to be used by a user who wants to execute asynchronous subscribe operations
 * on an EventCloud.
 * <p>
 * This proxy offers the possibility to reconstruct an Event from the binding
 * which has matched a subscription by a call to
 * {@link SubscribeProxy#reconstructCompoundEvent(NotificationId, SubscriptionId, Node)}
 * and also by an using a {@link CompoundEventNotificationListener} when you
 * subscribe. The reconstruction is an heavy operation that may be used
 * carefully. Indeed, to reconstruct an {@link CompoundEvent} from its
 * identifier, a {@link QuadruplePattern} query must be sent to all the peers
 * matching the graph value corresponding to the event identifier. Because three
 * dimensions among four are not fixed, a lot of peers are contacted. Moreover,
 * due to the fact that the proxies and the EventCloud infrastructure are
 * decoupled (and because each quadruple that belongs to an Event is published
 * asynchronously), it is not possible to guarantee that all the quadruples that
 * belong to the event identifier have been retrieved after the execution of the
 * first {@link QuadruplePattern}. That's why a solution for the reconstruction
 * consists to poll periodically the network with a {@link QuadruplePattern}
 * while all the quadruples that belong to the event identifier are not
 * retrieved.
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
     * Clears the subscribe proxy state by removing all the content in cache
     * (subscriptions, part of events received, event ids handled, etc.).
     * Calling this method while receiving events may lead to duplicates since
     * the identifiers of the events already received are no longer available.
     * 
     * @return {@code true} if the operation suceeded, {@code false} otherwise.
     */
    boolean clear();

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
    Subscription find(SubscriptionId id);

    /**
     * Reconstructs a {@link CompoundEvent} from the specified {@code eventId}.
     * A call to this method block until the whole compound event has been
     * retrieved. <strong>This operation must be used carefully</strong>. It is
     * the invoker responsibility to parallelize several calls to this method.
     * 
     * @param notificationId
     *            the notification identifier.
     * 
     * @param subscriptionId
     *            the subscription identifier that identifies which subscription
     *            is matched.
     * 
     * @param eventId
     *            the event identifier to use for retrieving the quadruples that
     *            belong to the compound event.
     * 
     * @return the event which has been reconstructed.
     */
    CompoundEvent reconstructCompoundEvent(NotificationId notificationId,
                                           SubscriptionId subscriptionId,
                                           Node eventId);

    /**
     * Used internally to send back a {@link BindingNotification} with SBCE1 or
     * SBCE2.
     * 
     * @param notification
     *            the notification that is received.
     */
    void receiveSbce1Or2(BindingNotification notification);

    /**
     * Used internally to send back a {@link BindingNotification} with SBCE3.
     * 
     * @param notification
     *            the notification that is received.
     */
    void receiveSbce3(BindingNotification notification);

    /**
     * Used internally to send back a {@link QuadruplesNotification} with SBCE1
     * or SBCE2.
     * 
     * @param notification
     *            the notification that is received.
     */
    void receiveSbce2(QuadruplesNotification notification);

    /**
     * Used internally to send back a {@link CompoundEvent} with SBCE3.
     * 
     * @param notification
     *            the notification that is received.
     */
    void receiveSbce3(QuadruplesNotification notification);

    /**
     * Used internally to send back a {@link SignalNotification} with SBCE1 or
     * SBCE2.
     * 
     * @param notification
     *            the notification that is received.
     */
    void receiveSbce1Or2(SignalNotification notification);

    /**
     * Used internally to send back a {@link SignalNotification} with SBCE3.
     * 
     * @param notification
     *            the notification that is received.
     */
    void receiveSbce3(SignalNotification notification);

    /**
     * Used internally to send back a {@link PollingSignalNotification}.
     * 
     * @param notification
     *            the notification that is received.
     */
    void receiveSbce1(PollingSignalNotification notification);

    void unsubscribeAll() throws IllegalStateException;

}
