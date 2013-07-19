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
package org.objectweb.proactive.extensions.p2p.structured.proxies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.extensions.p2p.structured.AbstractComponent;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.messages.FinalResponse;
import org.objectweb.proactive.extensions.p2p.structured.messages.FinalResponseReceiver;
import org.objectweb.proactive.extensions.p2p.structured.messages.MessageDispatcher;
import org.objectweb.proactive.extensions.p2p.structured.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.Response;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseCombiner;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

/**
 * Defines methods to send requests with or without response over a structured
 * P2P network. A proxy plays the role of a gateway between the entity that
 * wants to communicate and the P2P network.
 * 
 * @author lpellegr
 */
@DefineGroups({@Group(name = "parallel", selfCompatible = true)})
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

    private List<? extends Tracker> trackers;

    private List<Peer> peers;

    protected String url;

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void sendv(Request<?> request) {
        this.sendv(request, this.selectPeer());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void sendv(final Request<?> request, final Peer peer) {
        if (request.getResponseProvider() != null) {
            throw new IllegalArgumentException(
                    "Response provider specified for a request without reply expected");
        }

        try {
            this.messageDispatcher.dispatchv(request, peer);
        } catch (ProActiveRuntimeException e) {
            // peer not reachable, try with another
            this.peers.remove(peer);
            this.sendv(request, this.selectPeer());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public Response<?> send(final Request<?> request) {
        return this.send(request, this.selectPeer());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public Response<?> send(final Request<?> request, Peer peer) {
        if (request.getResponseProvider() == null) {
            throw new IllegalArgumentException(
                    "Cannot send a request and expect a reply without any response provider");
        }

        try {
            return this.messageDispatcher.dispatch(request, peer);
        } catch (ProActiveRuntimeException e) {
            // peer not reachable, try with another
            this.peers.remove(peer);
            return this.send(request, this.selectPeer());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public Serializable send(List<? extends Request<?>> requests,
                             Serializable context,
                             ResponseCombiner responseCombiner) {
        return this.send(requests, context, responseCombiner, this.selectPeer());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
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
            this.peers.remove(peer);
            return this.send(
                    requests, context, responseCombiner, this.selectPeer());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void receive(FinalResponse response) {
        this.messageDispatcher.push(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public Peer selectPeer() {
        if (this.peers == null || this.peers.isEmpty()) {
            List<Peer> newStubs = this.selectTracker().getPeers();

            if (newStubs.isEmpty()) {
                throw new IllegalStateException(
                        "No peer available from trackers");
            }

            this.peers.addAll(newStubs);
        }

        return this.peers.get(RandomUtils.nextInt(this.peers.size()));
    }

    private Tracker selectTracker() {
        return this.trackers.get(RandomUtils.nextInt(this.trackers.size()));
    }

    public String getUrl() {
        return this.url;
    }

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
                P2PStructuredProperties.MAO_SOFT_LIMIT_PROXIES.getValue(),
                false, false);
    }

    protected String prefixName() {
        return "proxy";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(List<Tracker> trackers) {
        try {
            Component component = Fractive.getComponentRepresentativeOnThis();

            this.url =
                    Fractive.registerByName(component, this.prefixName() + "-"
                            + UUID.randomUUID().toString());

            this.peers = Collections.synchronizedList(new ArrayList<Peer>());
            this.trackers = trackers;

            this.messageDispatcher =
                    new MessageDispatcher(
                            this.id,
                            this.multiActiveService,
                            (FinalResponseReceiver) component.getFcInterface("receive"));
        } catch (NoSuchInterfaceException e) {
            throw new IllegalArgumentException(e);
        } catch (ProActiveException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
