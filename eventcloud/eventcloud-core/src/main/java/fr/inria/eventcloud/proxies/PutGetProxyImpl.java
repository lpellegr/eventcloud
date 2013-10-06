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
package fr.inria.eventcloud.proxies;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.messages.Response;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.messages.SparqlMessageContext;
import fr.inria.eventcloud.messages.SparqlQueryType;
import fr.inria.eventcloud.messages.SparqlResponseCombiner;
import fr.inria.eventcloud.messages.request.AddQuadrupleRequest;
import fr.inria.eventcloud.messages.request.ContainsQuadrupleRequest;
import fr.inria.eventcloud.messages.request.CountQuadruplePatternRequest;
import fr.inria.eventcloud.messages.request.DeleteQuadrupleRequest;
import fr.inria.eventcloud.messages.request.DeleteQuadruplesRequest;
import fr.inria.eventcloud.messages.request.QuadruplePatternRequest;
import fr.inria.eventcloud.messages.request.SparqlAtomicRequest;
import fr.inria.eventcloud.messages.response.BooleanForwardResponse;
import fr.inria.eventcloud.messages.response.CountQuadruplePatternResponse;
import fr.inria.eventcloud.messages.response.QuadruplePatternResponse;
import fr.inria.eventcloud.reasoner.SparqlReasoner;
import fr.inria.eventcloud.utils.Callback;
import fr.inria.eventcloud.utils.RDFReader;

/**
 * PutGetProxyImpl is a concrete implementation of {@link PutGetProxy}. This
 * class has to be instantiated as a ProActive/GCM component.
 * 
 * @author lpellegr
 * @author bsauvan
 * 
 * @see ProxyFactory
 */
public class PutGetProxyImpl extends EventCloudProxy implements PutGetProxy,
        PutGetProxyAttributeController {

    /**
     * ADL name of the put/get proxy component.
     */
    public static final String PUTGET_PROXY_ADL =
            "fr.inria.eventcloud.proxies.PutGetProxy";

    /**
     * Functional interface name of the put/get proxy component.
     */
    public static final String PUTGET_SERVICES_ITF = "putget-services";

    /**
     * GCM Virtual Node name of the put/get proxy component.
     */
    public static final String PUTGET_PROXY_VN = "PutGetProxyVN";

    /**
     * Empty constructor required by ProActive.
     */
    public PutGetProxyImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runComponentActivity(Body body) {
        super.multiActiveService = new ComponentMultiActiveService(body);
        super.multiActiveService.multiActiveServing(
                EventCloudProperties.MAO_SOFT_LIMIT_PUTGET_PROXIES.getValue(),
                false, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initAttributes(EventCloudCache proxy) {
        assert !this.initialized;

        this.eventCloudCache = proxy;
        super.initAttributes(this.eventCloudCache.getProxyCache());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetAttributes() {
        if (super.initialized) {
            this.eventCloudCache = null;
            super.resetAttributes();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String prefixName() {
        return "putget-proxy";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelNotSelfCompatible")
    public boolean add(Quadruple quad) {
        PAFuture.waitFor(this.addAsync(quad));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelNotSelfCompatible")
    public boolean add(Collection<Quadruple> quads) {
        List<Response<?>> results = new ArrayList<Response<?>>(quads.size());

        for (final Quadruple quad : quads) {
            results.add(this.addAsync(quad));
        }

        PAFuture.waitForAll(results);

        return true;
    }

    private Response<?> addAsync(Quadruple quad) {
        return super.send(new AddQuadrupleRequest(quad));
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     */
    @Override
    @MemberOf("parallelNotSelfCompatible")
    public void add(URL url, SerializationFormat format) throws IOException {
        final Builder<Response<?>> results = ImmutableList.builder();

        InputStream in = url.openConnection().getInputStream();

        RDFReader.read(in, format, new Callback<Quadruple>() {
            @Override
            public void execute(Quadruple quad) {
                results.add(PutGetProxyImpl.this.addAsync(quad));
            }
        });

        in.close();

        PAFuture.waitForAll(results.build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelNotSelfCompatible")
    public boolean contains(Quadruple quad) {
        return ((BooleanForwardResponse) PAFuture.getFutureValue(super.send(new ContainsQuadrupleRequest(
                quad)))).getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelNotSelfCompatible")
    public boolean delete(Quadruple quad) {
        PAFuture.waitFor(this.deleteAsync(quad));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelNotSelfCompatible")
    public boolean delete(Collection<Quadruple> quads) {
        List<Response<?>> results = new ArrayList<Response<?>>(quads.size());

        for (final Quadruple quad : quads) {
            results.add(this.deleteAsync(quad));
        }

        PAFuture.waitForAll(results);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelNotSelfCompatible")
    public List<Quadruple> delete(QuadruplePattern quadPattern) {
        QuadruplePatternResponse response =
                (QuadruplePatternResponse) PAFuture.getFutureValue(super.send(new DeleteQuadruplesRequest(
                        quadPattern.getGraph(), quadPattern.getSubject(),
                        quadPattern.getPredicate(), quadPattern.getObject())));

        return response.getResult();
    }

    private Response<?> deleteAsync(Quadruple quad) {
        return super.send(new DeleteQuadrupleRequest(quad));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelNotSelfCompatible")
    public long count(QuadruplePattern quadPattern) {
        return ((CountQuadruplePatternResponse) PAFuture.getFutureValue((super.send(new CountQuadruplePatternRequest(
                quadPattern.getGraph(), quadPattern.getSubject(),
                quadPattern.getPredicate(), quadPattern.getObject()))))).getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelNotSelfCompatible")
    public long count(String sparqlQuery) throws MalformedSparqlQueryException {
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
    @MemberOf("parallelNotSelfCompatible")
    public List<Quadruple> find(QuadruplePattern quadPattern) {
        return ((QuadruplePatternResponse) PAFuture.getFutureValue((super.send(new QuadruplePatternRequest(
                quadPattern.getGraph(), quadPattern.getSubject(),
                quadPattern.getPredicate(), quadPattern.getObject()))))).getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelNotSelfCompatible")
    public SparqlResponse<?> executeSparqlQuery(String sparqlQuery)
            throws MalformedSparqlQueryException {
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
    @MemberOf("parallelNotSelfCompatible")
    public SparqlAskResponse executeSparqlAsk(String sparqlAskQuery)
            throws MalformedSparqlQueryException {
        List<SparqlAtomicRequest> requests =
                SparqlReasoner.parse(sparqlAskQuery);

        Serializable result =
                super.send(
                        requests, new SparqlMessageContext(
                                sparqlAskQuery, SparqlQueryType.ASK),
                        SparqlResponseCombiner.getInstance());

        return (SparqlAskResponse) PAFuture.getFutureValue(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelNotSelfCompatible")
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstructQuery)
            throws MalformedSparqlQueryException {
        List<SparqlAtomicRequest> requests =
                SparqlReasoner.parse(sparqlConstructQuery);

        Serializable result =
                super.send(
                        requests,
                        new SparqlMessageContext(
                                sparqlConstructQuery, SparqlQueryType.CONSTRUCT),
                        SparqlResponseCombiner.getInstance());

        return (SparqlConstructResponse) PAFuture.getFutureValue(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelNotSelfCompatible")
    public SparqlDescribeResponse executeSparqlDescribe(String sparqlDescribeQuery) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelNotSelfCompatible")
    public SparqlSelectResponse executeSparqlSelect(String sparqlSelectQuery)
            throws MalformedSparqlQueryException {
        List<SparqlAtomicRequest> requests =
                SparqlReasoner.parse(sparqlSelectQuery);

        Serializable result =
                super.send(
                        requests, new SparqlMessageContext(
                                sparqlSelectQuery, SparqlQueryType.SELECT),
                        SparqlResponseCombiner.getInstance());

        return (SparqlSelectResponse) PAFuture.getFutureValue(result);
    }

}
