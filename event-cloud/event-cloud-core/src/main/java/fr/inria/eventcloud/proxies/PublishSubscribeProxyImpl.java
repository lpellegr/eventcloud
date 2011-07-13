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

import java.io.InputStream;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.body.ComponentInitActive;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingsNotificationListener;
import fr.inria.eventcloud.api.listeners.EventsNotificationListener;
import fr.inria.eventcloud.factories.ProxyFactory;

/**
 * PublishSubscribeProxyImpl is a concrete implementation of
 * {@link PublishSubscribeProxy}. This class has to be instantiated as a
 * ProActive/GCM component.
 * 
 * @author lpellegr
 * @author bsauvan
 * 
 * @see ProxyFactory
 */
public class PublishSubscribeProxyImpl extends Proxy implements
        ComponentInitActive, PublishSubscribeProxy {

    private static final long serialVersionUID = 1L;

    /**
     * Empty constructor required by ProActive.
     */
    public PublishSubscribeProxyImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public void initComponentActivity(Body body) {
        body.setImmediateService("init", false);
    }

    /**
     * {@inheritDoc}
     */
    public void init(EventCloudProxy proxy) {
        if (this.proxy == null) {
            this.proxy = proxy;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Quadruple quad) {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Event event) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Collection<Event> events) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(InputStream in, SerializationFormat format) {
        read(in, format, new QuadrupleAction() {
            @Override
            public void performAction(Quadruple quad) {
                publish(quad);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(String sparqlQuery,
                                    BindingsNotificationListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(String sparqlQuery,
                                    EventsNotificationListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(SubscriptionId id) {
        // TODO Auto-generated method stub

    }

}
