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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangRIOT;

import com.hp.hpl.jena.sparql.core.Quad;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * Any user side proxy have to implement this abstract proxy class that stores
 * an {@link EventCloudProxy} which serves as a cache.
 * 
 * @author lpellegr
 */
public abstract class Proxy {

    protected EventCloudProxy proxy;

    protected Proxy() {
    }

    protected Proxy(EventCloudProxy proxy) {
        this.proxy = proxy;
    }

    protected SemanticPeer selectPeer() {
        return this.proxy.selectTracker().getRandomPeer();
    }

    public static final void read(InputStream in, SerializationFormat format,
                                  final QuadrupleAction action) {
        // TODO define the number of threads to use and if the thread pool has
        // to be shared between all the methods?
        final ExecutorService threadPool =
        // Executors.newFixedThreadPool(SystemUtil.getOptimalNumberOfThreads());
                Executors.newFixedThreadPool(1);

        Sink<Quad> sink = new Sink<Quad>() {
            @Override
            public void send(final Quad quad) {
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        action.performAction(new Quadruple(
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

    public static interface QuadrupleAction {

        public void performAction(Quadruple quad);

    }

}
