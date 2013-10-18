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
import fr.inria.eventcloud.deployment.EventCloudComponentsManager;
import fr.inria.eventcloud.webservices.factories.WsProxyFactory;

/**
 * Extension of {@link EventCloudComponentsManager} that allows proxies
 * pre-allocated or created by the pool to be exposed as web services.
 * 
 * @author bsauvan
 */
public class WsEventCloudComponentsManager extends EventCloudComponentsManager {

    private static final long serialVersionUID = 160L;

    /**
     * Empty constructor required by ProActive.
     */
    public WsEventCloudComponentsManager() {
    }

    /**
     * Creates a {@link WsEventCloudComponentsManager}.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment of components.
     */
    public WsEventCloudComponentsManager(NodeProvider nodeProvider,
            int nbTrackers, int nbPeers, int nbPublishProxies,
            int nbSubscribeProxies, int nbPutGetProxies) {
        super(nodeProvider, nbTrackers, nbPeers, nbPublishProxies,
                nbSubscribeProxies, nbPutGetProxies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PublishApi newGenericPublishProxy() {
        return WsProxyFactory.newGenericPublishProxy(this.nodeProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SubscribeApi newGenericSubscribeProxy() {
        return WsProxyFactory.newGenericSubscribeProxy(this.nodeProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PutGetApi newGenericPutGetProxy() {
        return WsProxyFactory.newGenericPutGetProxy(this.nodeProvider);
    }

}
