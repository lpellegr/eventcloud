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
package fr.inria.eventcloud.proxies;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxies;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.multiactivity.MultiActiveService;

import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.utils.Callback;

/**
 * PutGetProxyImpl is a concrete implementation of {@link PutGetProxy}. This
 * class has to be instantiated as a ProActive/GCM component.
 * 
 * @author lpellegr
 * @author bsauvan
 * 
 * @see ProxyFactory
 */
@DefineGroups({@Group(name = "parallel", selfCompatible = true)})
public class PutGetProxyImpl extends Proxy implements PutGetProxy,
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
     * Empty constructor required by ProActive.
     */
    public PutGetProxyImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(EventCloudCache proxy) {
        if (super.eventCloudCache == null) {
            super.eventCloudCache = proxy;
            super.proxy = Proxies.newProxy(super.eventCloudCache.getTrackers());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean add(Quadruple quad) {
        return super.selectPeer().add(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean add(Collection<Quadruple> quads) {
        return super.selectPeer().add(quads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean add(InputStream in, SerializationFormat format) {
        RdfParser.parse(in, format, new Callback<Quadruple>() {
            @Override
            public void execute(Quadruple quad) {
                PutGetProxyImpl.this.add(quad);
            }
        });

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean contains(Quadruple quad) {
        return super.selectPeer().contains(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean delete(Quadruple quad) {
        return super.selectPeer().delete(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean delete(Collection<Quadruple> quads) {
        return super.selectPeer().delete(quads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public List<Quadruple> delete(QuadruplePattern quadPattern) {
        return super.selectPeer().delete(quadPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public long count(QuadruplePattern quadPattern) {
        return super.selectPeer().count(quadPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public long count(String sparqlQuery) throws MalformedSparqlQueryException {
        return super.selectPeer().count(sparqlQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public List<Quadruple> find(QuadruplePattern quadPattern) {
        return super.selectPeer().find(quadPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public SparqlResponse<?> executeSparqlQuery(String sparqlQuery)
            throws MalformedSparqlQueryException {
        return super.selectPeer().executeSparqlQuery(sparqlQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public SparqlAskResponse executeSparqlAsk(String sparqlAskQuery)
            throws MalformedSparqlQueryException {
        return super.selectPeer().executeSparqlAsk(sparqlAskQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstructQuery)
            throws MalformedSparqlQueryException {
        return super.selectPeer().executeSparqlConstruct(sparqlConstructQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public SparqlDescribeResponse executeSparqlDescribe(String sparqlDescribeQuery) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public SparqlSelectResponse executeSparqlSelect(String sparqlSelectQuery)
            throws MalformedSparqlQueryException {
        return super.selectPeer().executeSparqlSelect(sparqlSelectQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runComponentActivity(Body body) {
        new MultiActiveService(body).multiActiveServing(
                EventCloudProperties.MAO_SOFT_LIMIT_PUTGET_PROXIES.getValue(),
                false, false);
    }

    /**
     * Lookups a put/get proxy component on the specified {@code componentUri}.
     * 
     * @param componentUri
     *            the URL of the put/get proxy component.
     * 
     * @return the reference on the {@link PutGetApi} interface of the put/get
     *         proxy component.
     * 
     * @throws IOException
     *             if an error occurs during the construction of the stub.
     * 
     * @deprecated This method will be removed for the next release. Please use
     *             {@link ProxyFactory#lookupPutGetProxy(String)} instead.
     */
    @Deprecated
    public static PutGetProxy lookup(String componentUri) throws IOException {
        return ComponentUtils.lookupFcInterface(
                componentUri, PUTGET_SERVICES_ITF, PutGetProxy.class);
    }

}
