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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.proxy;

import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.objectweb.proactive.extensions.p2p.structured.utils.SystemUtil;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangRIOT;

import com.hp.hpl.jena.sparql.core.Quad;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.ProxyFactory;
import fr.inria.eventcloud.api.PublishSubscribeApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingsNotificationListener;
import fr.inria.eventcloud.api.listeners.EventsNotificationListener;

/**
 * A PublishSubscribeProxy is a proxy that implements the
 * {@link PublishSubscribeApi}. It has to be used by a user who wants to execute
 * publish/subscribe asynchronous operations on an Event-Cloud. This class has
 * to be instantiated as a ProActive active object.
 * 
 * @author lpellegr
 * 
 * @see ProxyFactory
 */
public final class PublishSubscribeProxy extends Proxy implements
        PublishSubscribeApi, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Empty constructor required by ProActive.
     */
    public PublishSubscribeProxy() {
        super();
    }

    // TODO: add support for ELA properties. At least for the maximum number of
    // requests per seconds (by using a queue and a scheduled Timer).

    public PublishSubscribeProxy(EventCloudProxy proxy) {
        super(proxy);
    }

    @Override
    public void publish(Quadruple quad) {
        // TODO Auto-generated method stub

    }

    @Override
    public void publish(Event event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void publish(Collection<Event> events) {
        // TODO Auto-generated method stub

    }

    @Override
    public void publish(InputStream in, SerializationFormat format) {
        // TODO define the number of threads to use and if the thread pool has
        // to be shared between all the methods?
        final ExecutorService threadPool =
                Executors.newFixedThreadPool(SystemUtil.getOptimalNumberOfThreads());

        Sink<Quad> sink = new Sink<Quad>() {
            @Override
            public void send(final Quad quad) {
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        publish(new Quadruple(
                                quad.getGraph(), quad.getSubject(),
                                quad.getPredicate(), quad.getObject()));
                    }
                });
            }

            @Override
            public void close() {
                threadPool.shutdown();
            }

            @Override
            public void flush() {
            }

        };

        LangRIOT parser;

        switch (format) {
            case TriG:
                // TODO define baseURI
                parser = RiotReader.createParserTriG(in, "", sink);
                break;
            case NQuads:
                parser = RiotReader.createParserNQuads(in, sink);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknow SerializationFormat: " + format);
        }

        parser.parse();
    }

    @Override
    public SubscriptionId subscribe(String sparqlQuery,
                                    BindingsNotificationListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SubscriptionId subscribe(String sparqlQuery,
                                    EventsNotificationListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unsubscribe(SubscriptionId id) {
        // TODO Auto-generated method stub

    }

}
