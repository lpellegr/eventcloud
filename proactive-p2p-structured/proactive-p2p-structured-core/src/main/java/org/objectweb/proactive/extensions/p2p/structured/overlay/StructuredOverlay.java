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

    private final AtomicLong sequencer;

    protected RequestResponseManager messageManager;

    protected MultiActiveService multiActiveService;

    protected MutualExclusionManager mutualExclusionManager;

    /**
     * Indicates whether the current peer is activated (i.e. if the peer has
     * already joined a network or not).
     */
    protected boolean activated;

    /**
     * The ProActive stub reference to the outer peer active object.
     */
    protected Peer stub;

    /**
     * Stub URL
     */
    protected String url;

    protected SerializableProvider<? extends StructuredOverlay> overlayProvider;

    protected StructuredOverlay() {
        this.activated = false;
        this.id = new OverlayId();
        this.mutualExclusionManager = new RicartAgrawalaManager(this);
        this.sequencer = new AtomicLong();
    }

    protected StructuredOverlay(RequestResponseManager messageManager) {
        this();
        this.messageManager = messageManager;
    }

    public abstract void create();

    public abstract void join(Peer landmarkPeer);

    public abstract void leave();

    public abstract OverlayType getType();

    public abstract String dump();

    /**
     * Returns a boolean indicating whether the current overlay is activated
     * (i.e. if the overlay has handled a join operation but not yet a leave
     * operation).
     * 
     * @return {@code true} if the overlay is activated (i.e. if the overlay has
     *         handled a join operation but not yet a leave operation).
     *         {@code false} otherwise.
     */
    public boolean isActivated() {
        return this.activated;
    }

    /**
     * Returns the unique identifier associated to the overlay.
     * 
     * @return the unique identifier associated to the overlay.
     */
    public OverlayId getId() {
        return this.id;
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

    public MessageId newMessageId() {
        return new MessageId(this.bodyId, this.sequencer.getAndIncrement());
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
