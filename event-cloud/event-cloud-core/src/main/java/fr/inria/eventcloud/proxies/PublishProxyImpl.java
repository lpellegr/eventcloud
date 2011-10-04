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
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.messages.request.can.PublishQuadrupleRequest;

/**
 * PublishProxyImpl is a concrete implementation of {@link PublishProxy}. This
 * class has to be instantiated as a ProActive/GCM component.
 * 
 * @author lpellegr
 * @author bsauvan
 * 
 * @see ProxyFactory
 */
public class PublishProxyImpl extends ProxyCache implements PublishProxy {

    /**
     * Empty constructor required by ProActive.
     */
    public PublishProxyImpl() {
    }

    /**
     * {@inheritDoc}
     */
    public void init(EventCloudCache proxy) {
        if (this.proxy == null) {
            this.proxy = proxy;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Quadruple quad) {
        if (!quad.isTimestamped()) {
            quad.timestamp();
        }

        // TODO: use an asynchronous call with no response (see issue 16)

        // the quadruple is routed without taking into account the publication
        // datetime
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
        long publicationDateTime = System.currentTimeMillis();

        boolean isAlreadyTimestamped = false;

        for (Quadruple quad : event) {
            // reset the quadruple if it is already timestamped in order to
            // allow to publish events which have been received as notifications
            if (isAlreadyTimestamped
                    || (isAlreadyTimestamped = quad.isTimestamped())) {
                quad.reset();
            }
            this.publish(quad.timestamp(publicationDateTime));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Collection<Event> events) {
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
