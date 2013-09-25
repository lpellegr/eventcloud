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
package fr.inria.eventcloud.webservices.deployment;

import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;

import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.deployment.ComponentPoolManager;
import fr.inria.eventcloud.webservices.factories.WsProxyFactory;

/**
 * Extension of {@link ComponentPoolManager} to allow proxies contained in the
 * pools to be exposed as web services.
 * 
 * @author bsauvan
 */
public class WsComponentPoolManager extends ComponentPoolManager {

    private static final long serialVersionUID = 160L;

    /**
     * Empty constructor required by ProActive.
     */
    public WsComponentPoolManager() {
    }

    /**
     * Creates a {@link WsComponentPoolManager}.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment of components.
     */
    public WsComponentPoolManager(NodeProvider nodeProvider) {
        super(nodeProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized PublishApi newGenericPublishProxy() {
        return WsProxyFactory.newGenericPublishProxy(this.nodeProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized SubscribeApi newGenericSubscribeProxy() {
        return WsProxyFactory.newGenericSubscribeProxy(this.nodeProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized PutGetApi newGenericPutGetProxy() {
        return WsProxyFactory.newGenericPutGetProxy(this.nodeProvider);
    }

}
