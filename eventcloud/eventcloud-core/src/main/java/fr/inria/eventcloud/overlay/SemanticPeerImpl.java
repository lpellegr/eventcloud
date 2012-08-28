/**
 * Copyright (c) 2011-2012 INRIA.
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

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerImpl;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.SystemUtil;
import org.soceda.socialfilter.relationshipstrengthengine.RelationshipStrengthEngineManager;

import com.hp.hpl.jena.rdf.model.StmtIterator;

import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.messages.request.can.AddQuadrupleRequest;
import fr.inria.eventcloud.messages.request.can.ContainsQuadrupleRequest;
import fr.inria.eventcloud.messages.request.can.CountQuadruplePatternRequest;
import fr.inria.eventcloud.messages.request.can.DeleteQuadrupleRequest;
import fr.inria.eventcloud.messages.request.can.DeleteQuadruplesRequest;
import fr.inria.eventcloud.messages.request.can.QuadruplePatternRequest;
import fr.inria.eventcloud.messages.response.can.BooleanForwardResponse;
import fr.inria.eventcloud.messages.response.can.CountQuadruplePatternResponse;
import fr.inria.eventcloud.messages.response.can.QuadruplePatternResponse;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.utils.Callback;

/**
 * SemanticPeerImpl is a concrete implementation of {@link SemanticPeer}. It is
 * a peer constructed by using a {@link CanOverlay} and a
 * {@link TransactionalTdbDatastore}. It exposes the methods provided by the
 * {@link PutGetApi} interface in order to provide semantic operations like
 * {@code add}, {@code delete}, {@code find}, etc. but also to execute a SPARQL
 * query.
 * <p>
 * Warning, you have to use the {@link SemanticFactory} in order to create a new
 * SemanticPeer component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class SemanticPeerImpl extends PeerImpl implements SemanticPeer,
        BindingController {

    private static final long serialVersionUID = 1L;

    /**
     * ADL name of the semantic peer component.
     */
    public static final String SEMANTIC_PEER_ADL =
            "fr.inria.eventcloud.overlay.SemanticPeer";

    /**
     * Functional client interface name to bind the social filter.
     */
    public static final String SOCIAL_FILTER_SERVICES_ITF =
            "social-filter-services";

    private ExecutorService threadPool;

    /**
     * No-arg constructor for ProActive.
     */
    public SemanticPeerImpl() {
        // keep it empty because it is called each time we have to reify
        // a SemanticPeer object (e.g. each time a call to getRandomPeer()
        // from a tracker is performed)
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

        this.p2pConfigurationProperty = "eventcloud.configuration";
        super.initComponentActivity(body);
        EventCloudProperties.loadConfiguration();

        // FIXME issue #24
        this.threadPool =
                Executors.newFixedThreadPool(SystemUtil.getOptimalNumberOfThreads());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endComponentActivity(Body body) {
        super.endComponentActivity(body);

        this.threadPool.shutdown();
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
                    SemanticPeerImpl.this.add(quad);
                    doneSignal.countDown();
                }
            });
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(InputStream in, SerializationFormat format) {
        RdfParser.parse(in, format, new Callback<Quadruple>() {
            @Override
            public void execute(Quadruple quad) {
                SemanticPeerImpl.this.add(quad);
            }
        });

        return true;
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
                    SemanticPeerImpl.this.delete(quad);
                    doneSignal.countDown();
                }
            });
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> delete(QuadruplePattern quadPattern) {
        try {
            QuadruplePatternResponse response =
                    (QuadruplePatternResponse) PAFuture.getFutureValue(super.send(new DeleteQuadruplesRequest(
                            quadPattern.getGraph(), quadPattern.getSubject(),
                            quadPattern.getPredicate(), quadPattern.getObject())));
            return response.getResult();
        } catch (DispatchException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count(QuadruplePattern quadPattern) {
        try {
            return ((CountQuadruplePatternResponse) PAFuture.getFutureValue((super.send(new CountQuadruplePatternRequest(
                    quadPattern.getGraph(), quadPattern.getSubject(),
                    quadPattern.getPredicate(), quadPattern.getObject()))))).getResult();
        } catch (DispatchException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count(String sparqlQuery) {
        SparqlResponse<?> response = this.executeSparqlQuery(sparqlQuery);

        if (response instanceof SparqlAskResponse) {
            return ((SparqlAskResponse) response).getResult()
                    ? 1 : 0;
        } else if (response instanceof SparqlConstructResponse) {
            StmtIterator it =
                    ((SparqlConstructResponse) response).getResult()
                            .listStatements();
            long result = 0;
            while (it.hasNext()) {
                it.next();
                result++;
            }
            return result;
        } else if (response instanceof SparqlSelectResponse) {
            ResultSetWrapper it = ((SparqlSelectResponse) response).getResult();
            long result = 0;
            while (it.hasNext()) {
                it.nextBinding();
                result++;
            }
            return result;
        }

        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> find(QuadruplePattern quadPattern) {
        try {
            return ((QuadruplePatternResponse) PAFuture.getFutureValue((super.send(new QuadruplePatternRequest(
                    quadPattern.getGraph(), quadPattern.getSubject(),
                    quadPattern.getPredicate(), quadPattern.getObject()))))).getResult();
        } catch (DispatchException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
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
        return ((SemanticRequestResponseManager) super.overlay.getRequestResponseManager()).executeSparqlAsk(
                sparqlAskQuery, super.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstruct) {
        return ((SemanticRequestResponseManager) super.overlay.getRequestResponseManager()).executeSparqlConstruct(
                sparqlConstruct, super.overlay);
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
        return ((SemanticRequestResponseManager) super.overlay.getRequestResponseManager()).executeSparqlSelect(
                sparqlSelect, super.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bindFc(String clientItfName, Object serverItf)
            throws NoSuchInterfaceException {
        if (clientItfName.equals(SOCIAL_FILTER_SERVICES_ITF)) {
            ((SemanticCanOverlay) this.overlay).setSocialFilter((RelationshipStrengthEngineManager) serverItf);
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] listFc() {
        return new String[] {SOCIAL_FILTER_SERVICES_ITF};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object lookupFc(String clientItfName)
            throws NoSuchInterfaceException {
        if (clientItfName.equals(SOCIAL_FILTER_SERVICES_ITF)) {
            return ((SemanticCanOverlay) this.overlay).getSocialFilter();
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException {
        if (clientItfName.equals(SOCIAL_FILTER_SERVICES_ITF)) {
            ((SemanticCanOverlay) this.overlay).setSocialFilter(null);
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

}
