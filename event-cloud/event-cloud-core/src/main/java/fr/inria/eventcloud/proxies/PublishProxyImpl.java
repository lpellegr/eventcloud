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
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.messages.request.can.PublishQuadrupleRequest;
import fr.inria.eventcloud.pubsub.PublishSubscribeConstants;

/**
 * PublishProxyImpl is a concrete implementation of {@link PublishProxy}. This
 * class has to be instantiated as a ProActive/GCM component.
 * 
 * @author lpellegr
 * @author bsauvan
 * 
 * @see ProxyFactory
 */
public class PublishProxyImpl extends ProxyCache implements
        ComponentInitActive, PublishProxy {

    /**
     * Empty constructor required by ProActive.
     */
    public PublishProxyImpl() {
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
        // publish a quadruples that indicates what is the number of quadruples
        // contained by the event
        this.publish(new Quadruple(
                event.getGraph(), event.getGraph(),
                PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE,
                Node.createLiteral(Integer.toString(event.getQuadruples()
                        .size() + 1))));

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
