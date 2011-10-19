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

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.proxies.Proxy;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslator;

/**
 * 
 * @author lpellegr
 * 
 * @param <T>
 */
public abstract class EventCloudService<T extends Proxy> {

    protected static Logger log = LoggerFactory.getLogger(PublishService.class);

    protected T proxy;

    protected WsNotificationTranslator translator;

    protected String registryUrl;

    protected String eventCloudIdUrl;

    public EventCloudService(String registryUrl, String eventCloudIdUrl) {
        this.registryUrl = registryUrl;
        this.eventCloudIdUrl = eventCloudIdUrl;
    }

    @PostConstruct
    public void setUp() {
        this.translator = new WsNotificationTranslator();
        this.proxy = this.createProxy();

        log.info("New proxy of type {} deployed", proxy.getClass().getName());
    }

    public abstract T createProxy();

}
