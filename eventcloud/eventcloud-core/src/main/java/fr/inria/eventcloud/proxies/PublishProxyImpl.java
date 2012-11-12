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
import java.net.URL;
import java.util.Collection;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxies;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.configuration.EventCloudProperties;
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
@DefineGroups({@Group(name = "parallel", selfCompatible = true)})
public class PublishProxyImpl extends AbstractProxy implements PublishProxy,
        PublishProxyAttributeController {

    private static final Logger log =
            LoggerFactory.getLogger(PublishProxyImpl.class);

    /**
     * ADL name of the publish proxy component.
     */
    public static final String PUBLISH_PROXY_ADL =
            "fr.inria.eventcloud.proxies.PublishProxy";

    /**
     * Functional interface name of the publish proxy component.
     */
    public static final String PUBLISH_SERVICES_ITF = "publish-services";

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
    @MemberOf("parallel")
    public void publish(Quadruple quad) {
        if (quad.getPublicationTime() == -1) {
            quad.setPublicationTime();
        }

        if (P2PStructuredProperties.ENABLE_BENCHMARKS_INFORMATION.getValue()) {
            log.info("About to publish quad : " + quad.getSubject() + " "
                    + quad.getPredicate() + " " + quad.getObject());
        }

        // the quadruple is routed without taking into account the publication
        // datetime (neither the other meta information)
        super.sendv(new PublishQuadrupleRequest(quad));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void publish(CompoundEvent compoundEvent) {
        long publicationTime = System.currentTimeMillis();

        if (EventCloudProperties.INTEGRATION_LOG.getValue()) {
            // log information for integration test purposes
            log.info("EventCloud Entry {}", compoundEvent.getGraph());
        }

        Quadruple metaQuadruple =
                CompoundEvent.createMetaQuadruple(compoundEvent);
        metaQuadruple.setPublicationTime(publicationTime);
        this.publish(metaQuadruple);

        for (Quadruple quad : compoundEvent) {
            quad.setPublicationTime(publicationTime);
            this.publish(quad);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void publish(Collection<CompoundEvent> events) {
        for (CompoundEvent event : events) {
            this.publish(event);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void publish(URL url, SerializationFormat format) {
        try {
            InputStream in = url.openConnection().getInputStream();

            RdfParser.parse(in, format, new Callback<Quadruple>() {
                @Override
                public void execute(Quadruple quad) {
                    PublishProxyImpl.this.publish(quad);
                }
            });

            in.close();
        } catch (IOException ioe) {
            log.error("An error occurred when reading from the given URL", ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runComponentActivity(Body body) {
        new MultiActiveService(body).multiActiveServing(
                EventCloudProperties.MAO_SOFT_LIMIT_PUBLISH_PROXIES.getValue(),
                false, false);
    }

    /**
     * Lookups a publish proxy component on the specified {@code componentUri}.
     * 
     * @param componentUri
     *            the URL of the publish proxy component.
     * 
     * @return the reference on the {@link PublishApi} interface of the publish
     *         proxy component.
     * 
     * @throws IOException
     *             if an error occurs during the construction of the stub.
     * 
     * @deprecated This method will be removed for the next release. Please use
     *             {@link ProxyFactory#lookupPublishProxy(String)} instead.
     */
    @Deprecated
    public static PublishProxy lookup(String componentUri) throws IOException {
        return ComponentUtils.lookupFcInterface(
                componentUri, PUBLISH_SERVICES_ITF, PublishProxy.class);
    }

}
