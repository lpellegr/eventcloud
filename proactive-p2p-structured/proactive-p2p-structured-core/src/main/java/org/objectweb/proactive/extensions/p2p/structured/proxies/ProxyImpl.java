/**
 * Copyright (c) 2011-2014 INRIA.
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
package org.objectweb.proactive.extensions.p2p.structured.proxies;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.extensions.p2p.structured.AbstractComponent;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.factories.ProxyCache;
import org.objectweb.proactive.extensions.p2p.structured.messages.FinalResponse;
import org.objectweb.proactive.extensions.p2p.structured.messages.FinalResponseReceiver;
import org.objectweb.proactive.extensions.p2p.structured.messages.MessageDispatcher;
import org.objectweb.proactive.extensions.p2p.structured.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.Response;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseCombiner;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

/**
 * Defines methods to send requests with or without response over a structured
 * P2P network. A proxy plays the role of a gateway between the entity that
 * wants to communicate and the P2P network.
 * 
 * @author lpellegr
 */
@DefineGroups({
        @Group(name = "parallelSelfCompatible", selfCompatible = true),
        @Group(name = "parallelNotSelfCompatible", selfCompatible = false),
        @Group(name = "receive", selfCompatible = true)})
@DefineRules({
        @Compatible(value = {"parallelSelfCompatible", "receive"}),
        @Compatible(value = {"parallelNotSelfCompatible", "receive"})})
public class ProxyImpl extends AbstractComponent implements
        FinalResponseReceiver, Proxy {

    /**
     * ADL name of the proxy component.
     */
    public static final String PROXY_ADL =
            "org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy";

    /**
     * Functional interface name of the proxy component.
     */
    public static final String PROXY_SERVICES_ITF = "proxy-services";

    /**
     * GCM Virtual Node name of the proxy component.
     */
    public static final String PROXY_VN = "ProxyVN";

    private UniqueID id;

    private MessageDispatcher messageDispatcher;

    protected MultiActiveService multiActiveService;

    protected ProxyCache proxyCache;

    protected String url;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        super.initComponentActivity(body);

        this.id = body.getID();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runComponentActivity(Body body) {
        this.multiActiveService = new ComponentMultiActiveService(body);
        this.multiActiveService.multiActiveServing(
                P2PStructuredProperties.MAO_LIMIT_PROXIES.getValue(), false,
                false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initAttributes(ProxyCache proxyCache) {
        assert !this.initialized;

        try {
            Component component = Fractive.getComponentRepresentativeOnThis();

            this.url =
                    Fractive.registerByName(component, this.prefixName() + "-"
                            + UUID.randomUUID().toString());

            this.proxyCache = proxyCache;

            this.messageDispatcher =
                    new MessageDispatcher(
                            this.id,
                            this.multiActiveService,
                            (FinalResponseReceiver) component.getFcInterface("receive"));

            this._initAttributes();

            this.initialized = true;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalArgumentException(e);
        } catch (ProActiveException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected void _initAttributes() {
        // to be overriden if required
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetAttributes() {
        if (super.initialized) {
            this.url = null;
            this.proxyCache = null;
            this.messageDispatcher = null;

            super.resetAttributes();
        }
    }

    protected String prefixName() {
        return "proxy";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelSelfCompatible")
    public void sendv(Request<?> request) {
        this.sendv(request, this.selectPeer());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelSelfCompatible")
    public void sendv(final Request<?> request, final Peer peer) {
        if (request.getResponseProvider() != null) {
            throw new IllegalArgumentException(
                    "Response provider specified for a request without reply expected");
        }

        try {
            this.messageDispatcher.dispatchv(request, peer);
        } catch (ProActiveRuntimeException e) {
            // peer not reachable, try with another
            this.proxyCache.invalidate(peer);
            this.sendv(request, this.selectPeer());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelSelfCompatible")
    public Response<?> send(final Request<?> request) {
        return this.send(request, this.selectPeer());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelSelfCompatible")
    public Response<?> send(final Request<?> request, Peer peer) {
        if (request.getResponseProvider() == null) {
            throw new IllegalArgumentException(
                    "Cannot send a request and expect a reply without any response provider");
        }

        try {
            return this.messageDispatcher.dispatch(request, peer);
        } catch (ProActiveRuntimeException e) {
            // peer not reachable, try with another
            this.proxyCache.invalidate(peer);
            return this.send(request, this.selectPeer());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelSelfCompatible")
    public Serializable send(List<? extends Request<?>> requests,
                             Serializable context,
                             ResponseCombiner responseCombiner) {
        return this.send(requests, context, responseCombiner, this.selectPeer());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelSelfCompatible")
    public Serializable send(List<? extends Request<?>> requests,
                             Serializable context,
                             ResponseCombiner responseCombiner, Peer peer) {
        for (Request<?> request : requests) {
            if (request.getResponseProvider() == null) {
                throw new IllegalArgumentException(
                        "Cannot send a request and expect a reply without any response provider");
            }
        }

        try {
            return this.messageDispatcher.dispatch(
                    requests, context, responseCombiner, peer);
        } catch (ProActiveRuntimeException e) {
            // peer not reachable, try with another
            this.proxyCache.invalidate(peer);
            return this.send(
                    requests, context, responseCombiner, this.selectPeer());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("receive")
    public void receive(FinalResponse response) {
        this.messageDispatcher.push(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallelSelfCompatible")
    public Peer selectPeer() {
        return this.proxyCache.selectPeer();
    }

    public String getUrl() {
        return this.url;
    }

}
