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
package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.extensions.p2p.structured.AbstractComponent;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PeerNotActivatedException;
import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.messages.FinalResponseReceiver;
import org.objectweb.proactive.extensions.p2p.structured.messages.Message;
import org.objectweb.proactive.extensions.p2p.structured.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseCombiner;
import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.RunnableOperation;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;
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
        @Group(name = "commonObjectMethods", selfCompatible = true),
        @Group(name = "join", selfCompatible = false),
        @Group(name = "leave", selfCompatible = false),
        @Group(name = "reassign", selfCompatible = false),
        @Group(name = "receiveCallableOperation", selfCompatible = true, parameter = "org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation", condition = "this.areCompatible"),
        @Group(name = "receiveRunnableOperation", selfCompatible = true, parameter = "org.objectweb.proactive.extensions.p2p.structured.operations.RunnableOperation", condition = "isCompatible"),
        @Group(name = "routing", selfCompatible = true)})
@DefineRules({
        // common object methods group is compatible with all other groups
        @Compatible(value = {"commonObjectMethods", "join"}),
        @Compatible(value = {"commonObjectMethods", "leave"}),
        @Compatible(value = {"commonObjectMethods", "reassign"}),
        @Compatible(value = {"commonObjectMethods", "receiveCallableOperation"}),
        @Compatible(value = {"commonObjectMethods", "receiveRunnableOperation"}),
        @Compatible(value = {"commonObjectMethods", "routing"}),
        // callable operations compatibility
        @Compatible(value = {"receiveCallableOperation", "join"}, condition = "this.isCallableOperationCompatibleWithJoin"),
        @Compatible(value = {"receiveCallableOperation", "leave"}, condition = "this.isCallableOperationCompatibleWithLeave"),
        @Compatible(value = {"receiveCallableOperation", "reassign"}, condition = "this.isCallableOperationCompatibleWithReassign"),
        @Compatible(value = {"receiveCallableOperation", "routing"}, condition = "isCompatibleWithRouting"),
        // runnable operation compatibility
        @Compatible(value = {"receiveRunnableOperation", "join"}, condition = "this.isRunnableOperationCompatibleWithJoin"),
        @Compatible(value = {"receiveRunnableOperation", "leave"}, condition = "this.isRunnableOperationCompatibleWithLeave"),
        @Compatible(value = {"receiveRunnableOperation", "reassign"}, condition = "this.isRunnableOperationCompatibleWithReassign"),
        @Compatible(value = {"receiveRunnableOperation", "routing"}, condition = "isCompatibleWithRouting"),
        // callable and runnable operations are compatible under some conditions
        @Compatible(value = {
                "receiveCallableOperation", "receiveRunnableOperation"}, condition = "this.areCompatible")})
public class PeerImpl extends AbstractComponent implements PeerInterface,
        PeerAttributeController, Serializable {

    private static final long serialVersionUID = 160L;

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

    protected transient MultiActiveService multiActiveService;

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
        this.multiActiveService = new ComponentMultiActiveService(body);
        this.multiActiveService.policyServing(
                new PeerServingPolicy(this), null,
                P2PStructuredProperties.MAO_LIMIT_PEERS.getValue(), false,
                false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initAttributes(Peer stub,
                               DeploymentConfiguration deploymentConfiguration,
                               SerializableProvider<? extends StructuredOverlay> overlayProvider) {
        assert !this.initialized;

        this.overlay = overlayProvider.get();
        this.overlay.bodyId = PAActiveObject.getBodyOnThis().getID();
        this.overlay.multiActiveService = this.multiActiveService;
        this.overlay.deploymentConfiguration = deploymentConfiguration;
        this.overlay.overlayProvider = overlayProvider;
        this.overlay.stub = stub;
        this.overlay.url = PAActiveObject.getUrl(stub);

        this.initialized = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetAttributes() {
        if (super.initialized) {
            this.overlay.close();
            this.overlay = null;
            super.resetAttributes();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("commonObjectMethods")
    public OverlayId getId() {
        return this.overlay.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("commonObjectMethods")
    public OverlayType getType() {
        return this.overlay.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("commonObjectMethods")
    public Status getStatus() {
        return this.overlay.status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create() throws NetworkAlreadyJoinedException {
        if (this.overlay.status != Status.NOT_ACTIVATED) {
            throw new NetworkAlreadyJoinedException();
        }

        this.overlay.status = Status.PERFORMING_CREATE;
        this.overlay.create();
        this.overlay.status = Status.ACTIVATED;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("join")
    public void join(Peer landmarkPeer) throws NetworkAlreadyJoinedException,
            PeerNotActivatedException {
        this.overlay.maintenanceId = this.overlay.newMaintenanceId();
        this._join(landmarkPeer);
        this.overlay.maintenanceId = null;
    }

    private void _join(Peer landmarkPeer) throws NetworkAlreadyJoinedException,
            PeerNotActivatedException {
        if (this.overlay.status != Status.NOT_ACTIVATED) {
            throw new NetworkAlreadyJoinedException();
        }

        if (landmarkPeer.getStatus() == Status.NOT_ACTIVATED) {
            throw new PeerNotActivatedException(landmarkPeer.getId());
        }

        this.overlay.status = Status.PERFORMING_JOIN;
        this.overlay.join(landmarkPeer);
        this.overlay.status = Status.ACTIVATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("leave")
    public void leave() throws NetworkNotJoinedException {
        this.overlay.maintenanceId = this.overlay.newMaintenanceId();
        this._leave();
        this.overlay.maintenanceId = null;
    }

    private void _leave() throws NetworkNotJoinedException {
        if (this.overlay.status != Status.ACTIVATED) {
            throw new NetworkNotJoinedException();
        }

        this.overlay.status = Status.PERFORMING_LEAVE;
        this.overlay.leave();
        this.overlay.status = Status.NOT_ACTIVATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("reassign")
    public void reassign(Peer landmarkPeer) throws NetworkNotJoinedException {
        this.overlay.maintenanceId = this.overlay.newMaintenanceId();

        if (this.overlay.status == Status.ACTIVATED) {
            this._leave();
        }

        try {
            this._join(landmarkPeer);
        } catch (NetworkAlreadyJoinedException e) {
            throw new IllegalStateException(e);
        } catch (PeerNotActivatedException e) {
            throw new IllegalStateException(e);
        }

        this.overlay.maintenanceId = null;
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
    @MemberOf("receiveRunnableOperation")
    public void receive(RunnableOperation operation) {
        operation.handle(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("routing")
    public void forward(Message<?> msg) {
        msg.incrementHopCount(1);
        msg.route(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("routing")
    public void route(Message<?> msg) {
        msg.route(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Peer other) {
        return this.overlay.id.compareTo(other.getId());
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
    @MemberOf("routing")
    public <T extends Request<?>> void dispatch(List<T> requests,
                                                Serializable context,
                                                ResponseCombiner responseCombiner,
                                                FinalResponseReceiver responseDestination) {
        this.overlay.messageManager.getAggregationTable().register(
                requests.get(0).getAggregationId(), context, responseCombiner,
                responseDestination, requests.size());

        for (Request<?> request : requests) {
            request.route(this.overlay);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void inject(List<org.objectweb.proactive.core.body.request.Request> requests,
                       MaintenanceId maintenanceId) {
        Body body = PAActiveObject.getBodyOnThis();

        for (org.objectweb.proactive.core.body.request.Request request : requests) {
            try {
                body.receiveRequest(request);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RenegotiateSessionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("commonObjectMethods")
    public boolean equals(Object obj) {
        return obj instanceof PeerImpl
                && this.getId().equals(((PeerImpl) obj).getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("commonObjectMethods")
    public int hashCode() {
        return this.getId().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("commonObjectMethods")
    public String toString() {
        if (this.overlay == null) {
            // toString is performed on a stub
            return "stub" + Integer.toString(System.identityHashCode(this));
        }

        return this.overlay.toString();
    }

    protected boolean areCompatible(CallableOperation op1, CallableOperation op2) {
        return this.overlay.areCompatible(op1, op2);
    }

    protected boolean areCompatible(CallableOperation callableOperation,
                                    RunnableOperation runnableOperation) {
        return this.overlay.areCompatibleCallableOperationRunnableOperation(
                callableOperation, runnableOperation);
    }

    protected boolean isCallableOperationCompatibleWithJoin(CallableOperation callableOperation) {
        return this.overlay.isCompatibleWithJoin(callableOperation);
    }

    protected boolean isCallableOperationCompatibleWithLeave(CallableOperation callableOperation) {
        return this.overlay.isCompatibleWithLeave(callableOperation);
    }

    protected boolean isCallableOperationCompatibleWithReassign(CallableOperation callableOperation) {
        return this.overlay.isCompatibleWithReassign(callableOperation);
    }

    protected boolean isRunnableOperationCompatibleWithJoin(RunnableOperation runnableOperation) {
        return this.overlay.isCompatibleWithJoin(runnableOperation);
    }

    protected boolean isRunnableOperationCompatibleWithLeave(RunnableOperation runnableOperation) {
        return this.overlay.isCompatibleWithLeave(runnableOperation);
    }

    protected boolean isRunnableOperationCompatibleWithReassign(RunnableOperation runnableOperation) {
        return this.overlay.isCompatibleWithReassign(runnableOperation);
    }

}
