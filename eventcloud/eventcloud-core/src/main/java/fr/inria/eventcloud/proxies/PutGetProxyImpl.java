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
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxies;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
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
public class PutGetProxyImpl extends AbstractProxy implements PutGetProxy,
        PutGetProxyAttributeController {

    private static final Logger log =
            LoggerFactory.getLogger(PutGetProxyImpl.class);

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
    public boolean add(Quadruple quad) {
        return super.selectPeer().add(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(Collection<Quadruple> quads) {
        return super.selectPeer().add(quads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(URL url, SerializationFormat format) {
        try {
            InputStream in = url.openConnection().getInputStream();

            RdfParser.parse(in, format, new Callback<Quadruple>() {
                @Override
                public void execute(Quadruple quad) {
                    PutGetProxyImpl.this.add(quad);
                }
            });

            in.close();

            return true;
        } catch (IOException ioe) {
            log.error("An error occurred when reading from the given URL", ioe);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Quadruple quad) {
        return super.selectPeer().contains(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Quadruple quad) {
        return super.selectPeer().delete(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Collection<Quadruple> quads) {
        return super.selectPeer().delete(quads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> delete(QuadruplePattern quadPattern) {
        return super.selectPeer().delete(quadPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count(QuadruplePattern quadPattern) {
        return super.selectPeer().count(quadPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count(String sparqlQuery) {
        return super.selectPeer().count(sparqlQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> find(QuadruplePattern quadPattern) {
        return super.selectPeer().find(quadPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlResponse<?> executeSparqlQuery(String sparqlQuery) {
        return super.selectPeer().executeSparqlQuery(sparqlQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlAskResponse executeSparqlAsk(String sparqlAskQuery) {
        return super.selectPeer().executeSparqlAsk(sparqlAskQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstructQuery) {
        return super.selectPeer().executeSparqlConstruct(sparqlConstructQuery);
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
    public SparqlSelectResponse executeSparqlSelect(String sparqlSelectQuery) {
        return super.selectPeer().executeSparqlSelect(sparqlSelectQuery);
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
