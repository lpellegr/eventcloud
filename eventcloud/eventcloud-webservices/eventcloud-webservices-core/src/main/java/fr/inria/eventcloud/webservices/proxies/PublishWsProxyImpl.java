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
package fr.inria.eventcloud.webservices.proxies;

import java.util.Collection;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.proxies.PublishProxyImpl;
import fr.inria.eventcloud.webservices.api.PublishWsApi;

/**
 * Extension of {@link PublishProxyImpl} in order to be able to expose a publish
 * proxy as a web service.
 * 
 * @author bsauvan
 */
public class PublishWsProxyImpl extends PublishProxyImpl implements
        PublishWsApi {

    /**
     * ADL name of the publish web service proxy component.
     */
    public static final String PUBLISH_WEBSERVICE_PROXY_ADL =
            "fr.inria.eventcloud.webservices.proxies.PublishWsProxy";

    /**
     * Functional interface name of the publish web service proxy component.
     */
    public static final String PUBLISH_WEBSERVICES_ITF = "publish-webservices";

    /**
     * Empty constructor required by ProActive.
     */
    public PublishWsProxyImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishQuadruple(Quadruple quad) {
        this.publish(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishCompoundEvent(CompoundEvent event) {
        this.publish(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishCompoundEventCollection(Collection<CompoundEvent> events) {
        this.publish(events);
    }

}
