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
package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.extensions.p2p.structured.messages.MessageId;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseManager;
import org.objectweb.proactive.extensions.p2p.structured.mutual_exclusion.MutualExclusionManager;
import org.objectweb.proactive.extensions.p2p.structured.mutual_exclusion.RicartAgrawalaManager;
import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.RunnableOperation;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.execution.RequestExecutor;

/**
 * The StructuredOverlay class contains the logic associated to methods exposed
 * by the {@link Peer} class. Each structured p2p protocol is assumed to be
 * provide a concrete implementation of this class.
 * 
 * @author lpellegr
 */
public abstract class StructuredOverlay implements DataHandler {

    protected OverlayId id;

    protected UniqueID bodyId;

    // sequencer used to generate message identifiers
    private final AtomicLong messageSequencer;

    // sequencer used to generate maintenance identifiers
    private long maintenanceSequencer;

    // identifier used to identify a set of related maintenance requests and
    // operations triggered for a join, a leave or a reassign invocation
    // which is currently being handled
    protected MaintenanceId maintenanceId;

    protected RequestResponseManager messageManager;

    protected MultiActiveService multiActiveService;

    protected MutualExclusionManager mutualExclusionManager;

    protected Status status;

    // the ProActive stub reference to the outer peer active object.
    protected Peer stub;

    // stub URL
    protected String url;

    protected SerializableProvider<? extends StructuredOverlay> overlayProvider;

    protected StructuredOverlay() {
        this.id = new OverlayId();
        this.mutualExclusionManager = new RicartAgrawalaManager(this);
        this.maintenanceSequencer = 0;
        this.messageSequencer = new AtomicLong();
        this.status = Status.NOT_ACTIVATED;
    }

    protected StructuredOverlay(RequestResponseManager messageManager) {
        this();
        this.messageManager = messageManager;
    }

    public abstract void create();

    /**
     * Forces the current peer to join a peer that is already member of a
     * network.
     * 
     * @param landmarkPeer
     *            the landmark node to join.
     */
    public abstract void join(Peer landmarkPeer);

    public abstract void leave();

    public abstract OverlayType getType();

    public abstract String dump();

    /**
     * Returns the unique identifier associated to the overlay.
     * 
     * @return the unique identifier associated to the overlay.
     */
    public OverlayId getId() {
        return this.id;
    }

    /**
     * Returns the maintenance id that being handled.
     * 
     * @return the maintenanceId being handled or {@code null} if no maintenance
     *         operation is executing.
     */
    public MaintenanceId getMaintenanceId() {
        return this.maintenanceId;
    }

    /**
     * Returns the mutual exclusion manager.
     * 
     * @return the mutualExclusionManager
     */
    public MutualExclusionManager getMutualExclusionManager() {
        return this.mutualExclusionManager;
    }

    public SerializableProvider<? extends StructuredOverlay> getOverlayProvider() {
        return this.overlayProvider;
    }

    public String getPeerURL() {
        return this.url;
    }

    public Status getStatus() {
        return this.status;
    }

    /**
     * Returns the stub (i.e the remote reference) to the active peer associated
     * to this overlay.
     * 
     * @return the stub (i.e the remote reference) to the active peer associated
     *         to this overlay.
     */
    public Peer getStub() {
        return this.stub;
    }

    public RequestResponseManager getRequestResponseManager() {
        return this.messageManager;
    }

    public MaintenanceId newMaintenanceId() {
        return new MaintenanceId(this.id, this.maintenanceSequencer++);
    }

    public MessageId newMessageId() {
        return new MessageId(
                this.bodyId, this.messageSequencer.getAndIncrement());
    }

    /**
     * {@inheritDoc}
     * 
     * To be overridden if necessary.
     */
    @Override
    public void assignDataReceived(Serializable dataReceived) {
    }

    /**
     * {@inheritDoc}
     * 
     * To be overridden if necessary.
     */
    @Override
    public Serializable retrieveAllData() {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * To be overridden if necessary.
     */
    @Override
    public Serializable retrieveDataIn(Object interval) {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * To be overridden if necessary.
     */
    @Override
    public Serializable removeDataIn(Object interval) {
        return null;
    }

    /**
     * This method should be overridden to close resources after a peer has
     * terminated its activity (i.e. when
     * {@link ComponentEndActive#endComponentActivity(org.objectweb.proactive.Body)}
     * is called).
     */
    public void close() {
        if (this.messageManager != null) {
            this.messageManager.close();
        }
    }

    /*
     * Multi-active objects compatibilities. These methods have to be overridden 
     * in the concrete overlay implementation when required.
     */

    protected boolean isCompatibleWithJoin(CallableOperation op) {
        return false;
    }

    protected boolean isCompatibleWithLeave(CallableOperation op) {
        return false;
    }

    protected boolean isCompatibleWithReassign(CallableOperation op) {
        return false;
    }

    protected boolean isCompatibleWithJoin(RunnableOperation op) {
        return false;
    }

    protected boolean isCompatibleWithLeave(RunnableOperation op) {
        return false;
    }

    protected boolean isCompatibleWithReassign(RunnableOperation op) {
        return false;
    }

    /*
     * MultiActiveService extra methods
     */

    public void incrementExtraActiveRequestCount(int count) {
        this.getRequestExecutor().incrementExtraActiveRequestCount(count);
    }

    public void decrementExtraActiveRequestCount(int count) {
        this.getRequestExecutor().decrementExtraActiveRequestCount(count);
    }

    private RequestExecutor getRequestExecutor() {
        return (RequestExecutor) this.multiActiveService.getServingController();
    }

}
