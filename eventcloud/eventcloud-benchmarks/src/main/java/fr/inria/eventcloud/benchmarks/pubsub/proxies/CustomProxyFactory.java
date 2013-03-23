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
package fr.inria.eventcloud.benchmarks.pubsub.proxies;

import java.util.HashMap;

import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.factories.AbstractFactory;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxyImpl;

/**
 * Proxy factory for {@link CustomPublishProxy}.
 * 
 * @author lpellegr
 */
public class CustomProxyFactory extends ProxyFactory {

    public static CustomPublishProxy newCustomPublishProxy(String registryUrl,
                                                           EventCloudId id)
            throws EventCloudIdNotManaged {
        return (CustomPublishProxy) ProxyFactory.createPublishProxy(
                CustomPublishProxyImpl.PUBLISH_PROXY_ADL,
                CustomPublishProxy.class, new HashMap<String, Object>(),
                registryUrl, id);
    }

    public static CustomPublishProxy newCustomPublishProxy(NodeProvider nodeProvider,
                                                           String registryUrl,
                                                           EventCloudId id)
            throws EventCloudIdNotManaged {
        return (CustomPublishProxy) ProxyFactory.createPublishProxy(
                CustomPublishProxyImpl.PUBLISH_PROXY_ADL,
                CustomPublishProxy.class,
                AbstractFactory.getContextFromNodeProvider(
                        nodeProvider, PublishProxyImpl.PROXY_VN), registryUrl,
                id);
    }

}
