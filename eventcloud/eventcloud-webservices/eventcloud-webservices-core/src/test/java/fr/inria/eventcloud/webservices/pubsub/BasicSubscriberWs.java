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
package fr.inria.eventcloud.webservices.pubsub;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.util.MutableInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.wrappers.BindingWrapper;
import fr.inria.eventcloud.webservices.CompoundEventNotificationConsumer;
import fr.inria.eventcloud.webservices.api.subscribers.BindingSubscriberWsApi;
import fr.inria.eventcloud.webservices.api.subscribers.CompoundEventSubscriberWsApi;
import fr.inria.eventcloud.webservices.api.subscribers.SignalSubscriberWsApi;

/**
 * Provides a basic implementation of {@link SignalSubscriberWsApi},
 * {@link BindingSubscriberWsApi} and {@link CompoundEventSubscriberWsApi} by
 * storing all incoming signals, bindings and events into in-memory lists. These
 * lists can be retrieved at any time for any purpose.
 * 
 * @author bsauvan
 */
public class BasicSubscriberWs implements SignalSubscriberWsApi,
        BindingSubscriberWsApi, CompoundEventSubscriberWsApi {

    private static Logger log =
            LoggerFactory.getLogger(CompoundEventNotificationConsumer.class);

    public final MutableInteger signalsReceived;

    public final List<BindingWrapper> bindingsReceived;

    public final List<CompoundEvent> eventsReceived;

    /**
     * Creates a {@link BasicSubscriberWs}.
     */
    public BasicSubscriberWs() {
        this.signalsReceived = new MutableInteger(0);
        this.bindingsReceived = new ArrayList<BindingWrapper>();
        this.eventsReceived = new ArrayList<CompoundEvent>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySignal(String subscriptionId) {
        synchronized (this.signalsReceived) {
            this.signalsReceived.add(1);
            this.signalsReceived.notifyAll();
        }

        log.info("New signal received for subscription " + subscriptionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyBinding(String subscriptionId, BindingWrapper binding) {
        synchronized (this.bindingsReceived) {
            this.bindingsReceived.add(binding);
            this.bindingsReceived.notifyAll();
        }

        log.info("New binding received");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyCompoundEvent(String subscriptionId, CompoundEvent event) {
        synchronized (this.eventsReceived) {
            this.eventsReceived.add(event);
            this.eventsReceived.notifyAll();
        }

        log.info("New compound event received:\n{}", event);
    }

}
