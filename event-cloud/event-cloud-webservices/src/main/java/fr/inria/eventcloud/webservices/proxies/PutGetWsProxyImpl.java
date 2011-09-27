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
package fr.inria.eventcloud.webservices.proxies;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.proxies.PutGetProxyImpl;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;

/**
 * PutGetWsProxyImpl is an extension of {@link PutGetProxyImpl} in order to be
 * able to expose the proxy as a web service.
 * 
 * @author bsauvan
 */
public class PutGetWsProxyImpl extends PutGetProxyImpl implements PutGetWsApi {

    /**
     * Empty constructor required by ProActive.
     */
    public PutGetWsProxyImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addQuadruple(Quadruple quad) {
        return this.add(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addQuadrupleCollection(Collection<Quadruple> quads) {
        return this.add(quads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsQuadruple(Quadruple quad) {
        return this.contains(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteQuadruple(Quadruple quad) {
        return this.delete(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteQuadrupleCollection(Collection<Quadruple> quads) {
        return this.delete(quads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> deleteQuadruplePattern(QuadruplePattern quadPattern) {
        return this.delete(quadPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> findQuadruplePattern(QuadruplePattern quadPattern) {
        return this.find(quadPattern);
    }

}