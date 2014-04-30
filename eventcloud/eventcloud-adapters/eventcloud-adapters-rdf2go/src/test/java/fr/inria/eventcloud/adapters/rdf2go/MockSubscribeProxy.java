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
package fr.inria.eventcloud.adapters.rdf2go;

import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;

/**
 * This class is assumed to play the role of a mock SubscribeProxy in order to
 * test if the translation between RDF2Go and Jena objects work.
 * 
 * @author lpellegr
 */
public class MockSubscribeProxy implements SubscribeApi {

    public MockSubscribeProxy() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(Subscription subscription,
                          BindingNotificationListener listener) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(Subscription subscription,
                          CompoundEventNotificationListener listener) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(Subscription subscription,
                          SignalNotificationListener listener) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(SubscriptionId id) {
    }

}
