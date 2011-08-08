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

import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.messages.request.can.PublishQuadrupleRequest;

/**
 * A PublishProxy is a proxy that implements the {@link PublishApi}. It has to
 * be used by a user who wants to execute publish operations on an Event Cloud.
 * 
 * @author lpellegr
 */
public class PublishProxy extends Proxy implements PublishApi {

    /**
     * Constructs a PublishProxy by using the specified EventCloudProxy.
     * 
     * @param proxy
     *            the EventCloudProxy that is used to retrieve an entry-point
     *            into the Event-Cloud.
     */
    public PublishProxy(EventCloudProxy proxy) {
        super(proxy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Quadruple quad) {
        // TODO: use an asynchronous call with no response (see issue 16)
        try {
            super.proxy.selectTracker().getRandomPeer().send(
                    new PublishQuadrupleRequest(quad));
        } catch (DispatchException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Event event) {
        // TODO try to improve the publication of several quadruples
        // first insight: use a thread-pool
        for (Quadruple quad : event.getQuadruples()) {
            this.publish(quad);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Collection<Event> events) {
        // TODO use a thread-pool
        for (Event event : events) {
            this.publish(event);
        }
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

}
