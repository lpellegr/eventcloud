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
package fr.inria.eventcloud.proxies;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxies;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.messages.request.can.PublishQuadrupleRequest;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.utils.Callback;

/**
 * PublishProxyImpl is a concrete implementation of {@link PublishProxy}. This
 * class has to be instantiated as a ProActive/GCM component.
 * 
 * @author lpellegr
 * @author bsauvan
 * 
 * @see ProxyFactory
 */
public class PublishProxyImpl extends Proxy implements PublishProxy,
        PublishProxyAttributeController {

    /**
     * ADL name of the publish proxy component.
     */
    public static final String PUBLISH_PROXY_ADL =
            "fr.inria.eventcloud.proxies.PublishProxy";

    /**
     * Functional interface name of the publish proxy component.
     */
    public static final String PUBLISH_SERVICES_ITF = "publish-services";

    private String endpointUrl;

    /**
     * Empty constructor required by ProActive.
     */
    public PublishProxyImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(EventCloudCache proxy) {
        if (super.eventCloudCache == null) {
            super.eventCloudCache = proxy;
            super.proxy = Proxies.newProxy(super.eventCloudCache.getTrackers());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Quadruple quad) {
        if (quad.getPublicationTime() == -1) {
            quad.setPublicationTime();
        }

        if (quad.getPublicationSource() == null && this.endpointUrl != null) {
            quad.setPublicationSource(this.endpointUrl);
        }

        // TODO: use an asynchronous call with no response (see issue 16)

        // the quadruple is routed without taking into account the publication
        // datetime
        try {
            super.sendv(new PublishQuadrupleRequest(quad));
        } catch (DispatchException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(CompoundEvent event) {
        long publicationTime = System.currentTimeMillis();

        for (Quadruple quad : event) {
            quad.setPublicationTime(publicationTime);
            this.publish(quad);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Collection<CompoundEvent> events) {
        for (CompoundEvent event : events) {
            this.publish(event);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(InputStream in, SerializationFormat format) {
        RdfParser.parse(in, format, new Callback<Quadruple>() {
            @Override
            public void execute(Quadruple quad) {
                PublishProxyImpl.this.publish(quad);
            }
        });
    }

    public static PublishProxy lookup(String componentUri) throws IOException {
        return ComponentUtils.lookupFcInterface(
                componentUri, PUBLISH_SERVICES_ITF, PublishProxy.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEndpointUrl() {
        return this.endpointUrl;
    }

}
