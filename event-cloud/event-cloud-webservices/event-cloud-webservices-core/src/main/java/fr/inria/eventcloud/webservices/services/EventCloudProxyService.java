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
package fr.inria.eventcloud.webservices.services;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.EventCloudsRegistryImpl;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.proxies.Proxy;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * Contains information which are necessary to deploy an eventcloud proxy (e.g.
 * {@link PublishProxy}, {@link SubscribeProxy}, ...) as a web service.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            proxy type.
 */
public abstract class EventCloudProxyService<T extends Proxy> {

    protected static Logger log =
            LoggerFactory.getLogger(EventCloudProxyService.class);

    protected final String registryUrl;

    protected final String streamUrl;

    protected T proxy;

    public EventCloudProxyService(String registryUrl, String streamUrl) {
        this.registryUrl = registryUrl;
        this.streamUrl = streamUrl;
    }

    @PostConstruct
    public void init() {
        try {
            this.proxy = this.createProxy();
            log.info("{} proxy deployed", this.proxy.getClass().getName());
        } catch (EventCloudIdNotManaged e) {
            throw new IllegalStateException(e);
        }
    }

    public abstract T createProxy() throws EventCloudIdNotManaged;

    public void terminateProxy() {
        try {
            EventCloudsRegistry registry =
                    EventCloudsRegistryImpl.lookup(this.registryUrl);
            registry.unregisterProxy(
                    new EventCloudId(this.streamUrl), this.proxy);

            ComponentUtils.terminateComponent(this.proxy);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
