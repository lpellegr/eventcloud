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
package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.extensions.p2p.structured.AbstractComponent;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PeerNotActivatedException;
import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.RunnableOperation;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.ServingPolicy;
import org.objectweb.proactive.multiactivity.compatibility.StatefulCompatibilityMap;
import org.objectweb.proactive.multiactivity.execution.RequestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PeerImpl is a concrete implementation of {@link Peer}. It is composed of a
 * {@link StructuredOverlay} which allows to have different implementations of
 * peer-to-peer protocols.
 * <p>
 * <strong>Warning, this class must not be instantiated directly. It has a
 * public constructor in order to be compatible with ProActive but to create a
 * new peer component you have to use the {@link PeerFactory}.</strong>
 * 
 * @author lpellegr
 * @author bsauvan
 */
@DefineGroups({
        @Group(name = "join", selfCompatible = false),
        @Group(name = "leave", selfCompatible = false),
        @Group(name = "parallel", selfCompatible = true),
        @Group(name = "receiveCallableOperation", selfCompatible = true, parameter = "org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation", condition = "isCompatible")})
@DefineRules({
        @Compatible(value = {"receiveCallableOperation", "join"}, condition = "!this.isJoinOperation"),
        @Compatible(value = {"receiveCallableOperation", "leave"}, condition = "!this.isLeaveOperation"),
        @Compatible(value = {"receiveCallableOperation", "parallel"}),})
public class PeerImpl extends AbstractComponent implements Peer,
        PeerAttributeController, ComponentEndActive, Serializable {

    private static final long serialVersionUID = 140L;

    /**
     * ADL name of the peer component.
     */
    public static final String PEER_ADL =
            "org.objectweb.proactive.extensions.p2p.structured.overlay.Peer";

    /**
     * Functional interface name of the peer component.
     */
    public static final String PEER_SERVICES_ITF = "peer-services";

    /**
     * GCM Virtual Node name of the peer component.
     */
    public static final String PEER_VN = "PeerVN";

    protected static Logger logger = LoggerFactory.getLogger(PeerImpl.class);

    protected transient StructuredOverlay overlay;

    private transient MultiActiveService multiActiveService;

    /**
     * Empty constructor required by ProActive.
     */
    public PeerImpl() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        super.initComponentActivity(body);

        body.setImmediateService("setAttributes", false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runComponentActivity(Body body) {
        this.multiActiveService = new MultiActiveService(body);
        this.multiActiveService.multiActiveServing(
                P2PStructuredProperties.MAO_SOFT_LIMIT_PEERS.getValue(), false,
                false);

        // this.multiActiveService.policyServing(createCustomServingPolicy(
        // body, multiActiveService,
        // P2PStructuredProperties.MAO_SOFT_LIMIT.getValue()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endComponentActivity(Body body) {
        this.overlay.close();
    }

    @SuppressWarnings("unused")
    private static final ServingPolicy createCustomServingPolicy(final Body body,
                                                                 final MultiActiveService multiActiveService,
                                                                 final int maxThreads) {
        final String[] prioritizedMethods = {"route"}; // table if we need to
                                                       // add new prioritized
                                                       // methods
        return new ServingPolicy() {
            @Override
            public List<org.objectweb.proactive.core.body.request.Request> runPolicy(StatefulCompatibilityMap compatibility) {
                List<org.objectweb.proactive.core.body.request.Request> result =
                        new LinkedList<org.objectweb.proactive.core.body.request.Request>();
                List<org.objectweb.proactive.core.body.request.Request> queue =
                        compatibility.getQueueContents();

                if (queue.size() == 0) {
                    return result;
                }

                Collection<org.objectweb.proactive.core.body.request.Request> execQueue =
                        compatibility.getExecutingRequests();
                if (execQueue.size()
                        - ((RequestExecutor) multiActiveService.getServingController()).getExtraActiveRequestCount() >= maxThreads) {
                    Iterator<org.objectweb.proactive.core.body.request.Request> itQ =
                            queue.iterator();
                    while (itQ.hasNext()) {
                        org.objectweb.proactive.core.body.request.Request r =
                                itQ.next();
                        if (compatibility.isCompatibleWithRequests(r, execQueue)
                                && Arrays.asList(prioritizedMethods).contains(
                                        r.getMethodName())) {
                            result.add(r);
                            queue.remove(r);
                            return result;
                        }
                    }
                } else {
                    org.objectweb.proactive.core.body.request.Request current;
                    Iterator<org.objectweb.proactive.core.body.request.Request> itQ =
                            queue.iterator();
                    int i = -1;
                    while (itQ.hasNext()
                            && execQueue.size()
                                    + result.size()
                                    - ((RequestExecutor) multiActiveService.getServingController()).getExtraActiveRequestCount() < maxThreads) {
                        current = itQ.next();
                        i++;
                        if (compatibility.isCompatibleWithRequests(
                                current, result)
                                && compatibility.isCompatibleWithRequests(
                                        current, execQueue)
                                && compatibility.getIndexOfLastCompatibleWith(
                                        current, queue) >= i - 1) {
                            result.add(current);
                            itQ.remove();
                        } else {
                            return result;
                        }
                    }
                }
                return result;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(Peer stub,
                              SerializableProvider<? extends StructuredOverlay> overlayProvider) {
        if (this.overlay == null) {
            this.overlay = overlayProvider.get();
            this.overlay.stub = stub;
            this.overlay.getRequestResponseManager().multiActiveService =
                    this.multiActiveService;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public UUID getId() {
        return this.overlay.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public OverlayType getType() {
        return this.overlay.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean isActivated() {
        return this.overlay.activated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean create() throws NetworkAlreadyJoinedException {
        if (this.overlay.isActivated()) {
            throw new NetworkAlreadyJoinedException();
        } else {
            this.overlay.create();
            return this.overlay.activated = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("join")
    public void join(Peer landmarkPeer) throws NetworkAlreadyJoinedException,
            PeerNotActivatedException {
        // the update to the internal variables do not have to be synchronized
        // because this operation is supposed to be handled in FIFO (i.e. in
        // exclusion with all other methods)
        if (this.overlay.isActivated()) {
            throw new NetworkAlreadyJoinedException();
        }

        if (!landmarkPeer.isActivated()) {
            throw new PeerNotActivatedException(landmarkPeer.getId());
        }

        this.overlay.join(landmarkPeer);
        this.overlay.activated = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("leave")
    public void leave() throws NetworkNotJoinedException {
        // same as the join this method should be handled in FIFO order
        // regarding other methods
        if (this.overlay.isActivated()) {
            this.overlay.leave();
            this.overlay.activated = false;
        } else {
            throw new NetworkNotJoinedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("receiveCallableOperation")
    public ResponseOperation receive(CallableOperation operation) {
        return operation.handle(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void receive(RunnableOperation operation) {
        operation.handle(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void route(RequestResponseMessage<?> msg) {
        msg.route(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void sendv(Request<?> request) {
        this.overlay.dispatchv(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public Response<?> send(Request<?> request) {
        return this.overlay.dispatch(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String dump() {
        return this.overlay.dump();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean equals(Object obj) {
        return obj instanceof PeerImpl
                && this.getId().equals(((PeerImpl) obj).getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public int hashCode() {
        return this.getId().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public String toString() {
        if (this.overlay == null) {
            // toString is performed on a stub
            return "stub" + Integer.toString(System.identityHashCode(this));
        }

        return this.overlay.toString();
    }

}
