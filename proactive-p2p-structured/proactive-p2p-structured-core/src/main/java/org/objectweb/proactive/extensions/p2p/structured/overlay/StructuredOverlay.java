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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;

/**
 * The StructuredOverlay class contains the logic associated to methods exposed
 * by the {@link Peer} class. Each structured p2p protocol is assumed to be
 * provide a concrete implementation of this class.
 * 
 * @author lpellegr
 */
public abstract class StructuredOverlay implements DataHandler {

    protected final UUID id;

    protected RequestResponseManager messageManager;

    /**
     * Indicates whether the current peer is activated (i.e. if the peer has
     * already joined a network or not).
     */
    protected AtomicBoolean activated;

    /**
     * The ProActive stub reference to the outer peer active object.
     */
    protected Peer stub;

    protected StructuredOverlay() {
        this.activated = new AtomicBoolean();
        this.id = UUID.randomUUID();
    }

    protected StructuredOverlay(RequestResponseManager messageManager) {
        this();
        this.messageManager = messageManager;
    }

    /**
     * Dispatches the request over the overlay by using message passing. The
     * request is sent asynchronously without any response expected.
     * 
     * @param request
     *            the request to dispatch.
     */
    public void dispatchv(Request<?> request) {
        this.messageManager.dispatchv(request, this);
    }

    /**
     * Dispatches the request over the overlay by using message passing. The
     * request is supposed to create a response which is sent back to the
     * sender.
     * 
     * @param request
     *            the request to dispatch.
     * 
     * @return a response associated to the type of the request.
     */
    public Response<?> dispatch(Request<?> request) throws DispatchException {
        return this.messageManager.dispatch(request, this);
    }

    public abstract boolean create();

    public abstract boolean join(Peer landmarkPeer);

    public abstract boolean leave();

    public abstract OverlayType getType();

    public abstract String dump();

    /**
     * Returns a boolean indicating whether the current overlay is activated
     * (i.e. the overlay has handled a join operation but not a leave operation
     * yet).
     * 
     * @return {@code true} if the overlay is activated (i.e. the overlay has
     *         handled a join operation but not a leave operation yet),
     *         {@code false} otherwise.
     */
    public boolean isActivated() {
        return this.activated.get();
    }

    /**
     * Returns the unique identifier associated to the overlay.
     * 
     * @return the unique identifier associated to the overlay.
     */
    public UUID getId() {
        return this.id;
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

    /**
     * Returns the {@link ResponseEntry} associated to the given
     * {@code responseId} from the {@link RequestResponseManager}.
     * 
     * @param responseId
     *            the response identifier to look for.
     * @return the {@link ResponseEntry} associated to the given
     *         {@code responseId} or {@code null} if no entry was found.
     */
    public ResponseEntry getResponseEntry(UUID responseId) {
        return this.messageManager.getResponsesReceived().get(responseId);
    }

    public Map<UUID, ResponseEntry> getResponseEntries() {
        return this.messageManager.getResponsesReceived();
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

}
