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

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;

/**
 * A PutGetProxy is a proxy that implements the {@link PutGetApi}. It has to be
 * used by a user who wants to execute put/get synchronous operations on an
 * Event Cloud.
 * 
 * @author lpellegr
 */
public class PutGetProxy extends ProxyCache implements Proxy, PutGetApi {

    /**
     * Constructs a PutGetProxy by using the specified EventCloudProxy.
     * 
     * @param proxy
     *            the EventCloudProxy that is used to retrieve an entry-point
     *            into the Event-Cloud.
     */
    public PutGetProxy(EventCloudCache proxy) {
        super(proxy);
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
    public boolean add(InputStream in, SerializationFormat format) {
        read(in, format, new QuadrupleAction() {
            @Override
            public void performAction(Quadruple quad) {
                add(new Quadruple(
                        quad.getGraph(), quad.getSubject(),
                        quad.getPredicate(), quad.getObject()));
            }
        });

        return true;
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
    public Collection<Quadruple> delete(QuadruplePattern quadPattern) {
        return super.selectPeer().find(quadPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> find(QuadruplePattern quadPattern) {
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

}
