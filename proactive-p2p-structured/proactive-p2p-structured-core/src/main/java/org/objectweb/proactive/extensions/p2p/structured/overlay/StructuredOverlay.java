/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

import org.objectweb.proactive.Body;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.datastore.Datastore;
import org.objectweb.proactive.extensions.p2p.structured.overlay.datastore.PersistentDatastore;

/**
 * The StructuredOverlay class contains the logic associated to methods exposed
 * by the {@link Peer} class. Each structured p2p protocol is assumed to be
 * provide a concrete implementation of this class.
 * 
 * @author lpellegr
 */
public abstract class StructuredOverlay implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final UUID id;

    protected transient PersistentDatastore datastore;

    protected RequestResponseManager messagingManager;

    /**
     * Indicates whether the current peer is activated (i.e. if the peer has
     * already joined or not).
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

    protected StructuredOverlay(RequestResponseManager messagingManager,
            PersistentDatastore datastore) {
        this();
        this.datastore = datastore;
        this.messagingManager = messagingManager;
        // messagingManager maybe null if the overlay
        // is not assumed to support request/response
        if (this.messagingManager != null) {
            this.messagingManager.init(this);
        }
    }

    protected StructuredOverlay(RequestResponseManager queryManager) {
        this(queryManager, null);
    }

    public void initActivity(Body body) {
        if (this.datastore != null) {
            this.datastore.open();
        }
    }

    public void endActivity(Body body) {
        if (this.datastore != null) {
            this.datastore.close();
        }
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
     * Returns the datastore instance.
     * 
     * @return the datastore instance.
     */
    public Datastore getDatastore() {
        return this.datastore;
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
        return this.messagingManager;
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
        return this.messagingManager.getResponsesReceived().get(responseId);
    }

    public Map<UUID, ResponseEntry> getResponseEntries() {
        return this.messagingManager.getResponsesReceived();
    }

}
