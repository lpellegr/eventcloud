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

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        this.proxy = this.createProxy();
        log.info("{} proxy deployed", proxy.getClass().getName());
    }

    public abstract T createProxy();

}
