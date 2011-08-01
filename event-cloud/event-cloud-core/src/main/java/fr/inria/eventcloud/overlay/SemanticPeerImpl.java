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
package fr.inria.eventcloud.overlay;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerComponentImpl;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingsNotificationListener;
import fr.inria.eventcloud.api.listeners.EventsNotificationListener;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.JenaDatastore;
import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.messages.request.can.AddQuadrupleRequest;
import fr.inria.eventcloud.messages.request.can.ContainsQuadrupleRequest;
import fr.inria.eventcloud.messages.request.can.DeleteQuadrupleRequest;
import fr.inria.eventcloud.messages.request.can.DeleteQuadruplesRequest;
import fr.inria.eventcloud.messages.request.can.FindQuadruplesRequest;
import fr.inria.eventcloud.messages.response.can.BooleanForwardResponse;
import fr.inria.eventcloud.messages.response.can.FindQuadruplesResponse;

/**
 * SemanticPeerImpl is a concrete implementation of {@link SemanticPeer}. It is
 * a peer constructed by using a {@link CanOverlay} and a {@link JenaDatastore}.
 * It exposes the methods provided by the {@link SemanticDatastore} interface in
 * order to provide semantic operations like add, delete, find, etc. but also to
 * execute a SPARQL query.
 * <p>
 * Warning, you have to use the {@link SemanticFactory} in order to create a new
 * SemanticPeer component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
/*
 * TODO implement the PublishSubscribeApi interface
 */
public class SemanticPeerImpl extends PeerComponentImpl implements SemanticPeer {

    private static final long serialVersionUID = 1L;

    private ExecutorService threadPool;

    /**
     * No-arg constructor for ProActive.
     */
    public SemanticPeerImpl() {
        // keep it empty because it is called each time we have to reify
        // a SemanticPeer object (e.g. each time you call getRandomPeer()
        // from a tracker)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Peer stub, StructuredOverlay overlay) {
        super.init(
                stub,
                new CanOverlay(
                        new SparqlRequestResponseManager(),
                        new JenaDatastore(
                                new File(
                                        EventCloudProperties.REPOSITORIES_PATH.getValue()),
                                EventCloudProperties.REPOSITORIES_AUTO_REMOVE.getValue())));
        this.threadPool = Executors.newFixedThreadPool(50);
    }

    /*
     * PutGetApi implementation
     * 
     * The boolean value which is returned in the PutGetApi by some methods 
     * is used to ensure that ProActive calls are synchronous.
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(Quadruple quad) {
        try {
            PAFuture.waitFor(super.send(new AddQuadrupleRequest(quad)));
        } catch (DispatchException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(Collection<Quadruple> quads) {
        final CountDownLatch doneSignal = new CountDownLatch(quads.size());

        for (final Quadruple quad : quads) {
            this.threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    add(quad);
                    doneSignal.countDown();
                }
            });
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(InputStream in, SerializationFormat format) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Quadruple quad) {
        try {
            return ((BooleanForwardResponse) PAFuture.getFutureValue(super.send(new ContainsQuadrupleRequest(
                    quad)))).getResult();
        } catch (DispatchException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Quadruple quad) {
        try {
            PAFuture.waitFor(super.send(new DeleteQuadrupleRequest(quad)));
        } catch (DispatchException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Collection<Quadruple> quads) {
        final CountDownLatch doneSignal = new CountDownLatch(quads.size());

        for (final Quadruple quad : quads) {
            this.threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    delete(quad);
                    doneSignal.countDown();
                }
            });
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> delete(QuadruplePattern quadPattern) {
        try {
            PAFuture.waitFor(super.send(new DeleteQuadruplesRequest(
                    quadPattern.getGraph(), quadPattern.getSubject(),
                    quadPattern.getPredicate(), quadPattern.getObject())));
        } catch (DispatchException e) {
            e.printStackTrace();
        }

        // TODO retrieve the quadruples that have been removed
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> find(QuadruplePattern quadPattern) {
        try {
            return ((FindQuadruplesResponse) PAFuture.getFutureValue((super.send(new FindQuadruplesRequest(
                    quadPattern.getGraph(), quadPattern.getSubject(),
                    quadPattern.getPredicate(), quadPattern.getObject()))))).getResult();
        } catch (DispatchException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlResponse<?> executeSparqlQuery(String sparqlQuery) {
        sparqlQuery = sparqlQuery.trim();

        if (sparqlQuery.startsWith("ASK")) {
            return this.executeSparqlAsk(sparqlQuery);
        } else if (sparqlQuery.startsWith("CONSTRUCT")) {
            return this.executeSparqlConstruct(sparqlQuery);
        } else if (sparqlQuery.startsWith("DESCRIBE")) {
            return this.executeSparqlDescribe(sparqlQuery);
        } else if (sparqlQuery.startsWith("SELECT")) {
            return this.executeSparqlSelect(sparqlQuery);
        } else {
            throw new IllegalArgumentException("Unknow query form for query: "
                    + sparqlQuery);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlAskResponse executeSparqlAsk(String sparqlAskQuery) {
        return ((SparqlRequestResponseManager) super.overlay.getRequestResponseManager()).executeSparqlAsk(sparqlAskQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstruct) {
        return ((SparqlRequestResponseManager) super.overlay.getRequestResponseManager()).executeSparqlConstruct(sparqlConstruct);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlDescribeResponse executeSparqlDescribe(String sparqlDescribeQuery) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlSelectResponse executeSparqlSelect(String sparqlSelect) {
        return ((SparqlRequestResponseManager) super.overlay.getRequestResponseManager()).executeSparqlSelect(sparqlSelect);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        body.setImmediateService("add", false);
        body.setImmediateService("contains", false);
        body.setImmediateService("delete", false);
        body.setImmediateService("find", false);
        body.setImmediateService("executeSparqlAsk", false);
        body.setImmediateService("executeSparqlConstruct", false);
        body.setImmediateService("executeSparqlDescribe", false);
        body.setImmediateService("executeSparqlSelect", false);

        super.initComponentActivity(body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endComponentActivity(Body body) {
        this.threadPool.shutdown();

        super.endComponentActivity(body);
    }

    /*
     * PublishSubscribeApi implementation 
     */

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
        // TODO Auto-generated method stub

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
