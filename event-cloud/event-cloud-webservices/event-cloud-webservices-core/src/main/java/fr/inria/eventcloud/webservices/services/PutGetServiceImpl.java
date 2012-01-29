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
package fr.inria.eventcloud.webservices.services;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;

/**
 * Defines a put/get web service. All the calls to the requests will be
 * redirected to a {@link PutGetProxy} in order to be treated into an Event
 * Cloud.
 * 
 * @author lpellegr
 */
public class PutGetServiceImpl extends EventCloudProxyService<PutGetProxy> implements
        PutGetWsApi {

    public PutGetServiceImpl(String registryUrl, String eventCloudIdUrl) {
        super(registryUrl, eventCloudIdUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addQuadruple(Quadruple quad) {
        return super.proxy.add(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addQuadrupleCollection(Collection<Quadruple> quads) {
        return super.proxy.add(quads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsQuadruple(Quadruple quad) {
        return super.proxy.contains(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteQuadruple(Quadruple quad) {
        return super.proxy.delete(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteQuadrupleCollection(Collection<Quadruple> quads) {
        return super.proxy.delete(quads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> deleteQuadruplePattern(QuadruplePattern quadPattern) {
        return super.proxy.delete(quadPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> findQuadruplePattern(QuadruplePattern quadPattern) {
        return super.proxy.find(quadPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlResponse<?> executeSparqlQuery(String sparqlQuery) {
        return super.proxy.executeSparqlQuery(sparqlQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlAskResponse executeSparqlAsk(String sparqlAskQuery) {
        return super.proxy.executeSparqlAsk(sparqlAskQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstructQuery) {
        return super.proxy.executeSparqlConstruct(sparqlConstructQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlDescribeResponse executeSparqlDescribe(String sparqlDescribeQuery) {
        return super.proxy.executeSparqlDescribe(sparqlDescribeQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlSelectResponse executeSparqlSelect(String sparqlSelectQuery) {
        return super.proxy.executeSparqlSelect(sparqlSelectQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PutGetProxy createProxy() {
        return ProxyFactory.getInstance(
                super.registryUrl,
                EventCloudId.parseEventCloudIdUrl(super.eventcloudIdUrl))
                .createPutGetProxy();
    }

}
