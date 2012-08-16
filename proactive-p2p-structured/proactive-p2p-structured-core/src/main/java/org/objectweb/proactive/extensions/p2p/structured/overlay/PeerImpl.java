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
import java.util.UUID;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.extensions.p2p.structured.AbstractComponent;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.operations.AsynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
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
public class PeerImpl extends AbstractComponent implements Peer,
        PeerAttributeController, ComponentEndActive, Serializable {

    private static final long serialVersionUID = 1L;

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

    /**
     * No-arg constructor for ProActive.
     */
    public PeerImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        super.initComponentActivity(body);

        // these methods do not change the state of the peer
        body.setImmediateService("equals", false);
        body.setImmediateService("getId", false);
        body.setImmediateService("getType", false);
        body.setImmediateService("hashCode", false);
        body.setImmediateService("toString", false);

        body.setImmediateService("receiveImmediateService", false);
        body.setImmediateService("setAttributes", false);
        body.setImmediateService("route", false);
        body.setImmediateService("send", false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endComponentActivity(Body body) {
        // TODO: enable the leave operation when it works!

        // if (this.overlay.activated.get()) {
        // try {
        // this.leave();
        // } catch (NetworkNotJoinedException e) {
        // e.printStackTrace();
        // }
        // }

        if (this.overlay.datastore != null
                && this.overlay.datastore.isInitialized()) {
            this.overlay.datastore.close();
        }
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
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getId() {
        return this.overlay.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OverlayType getType() {
        return this.overlay.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActivated() {
        return this.overlay.activated.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean create() throws NetworkAlreadyJoinedException {
        if (this.overlay.activated.compareAndSet(false, true)) {
            return this.overlay.create();
        } else {
            throw new NetworkAlreadyJoinedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean join(Peer landmarkPeer) throws NetworkAlreadyJoinedException {
        if (this.overlay.activated.compareAndSet(false, true)) {
            if (!this.overlay.join(landmarkPeer)) {
                // a concurrent join operation has been detected
                // hence we have to reset the activated variable
                // to false in order to have the possibility to
                // try again later
                this.overlay.activated.set(false);
                return false;
            } else {
                return true;
            }
        } else {
            throw new NetworkAlreadyJoinedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean leave() throws NetworkNotJoinedException {
        if (this.overlay.activated.compareAndSet(true, false)) {
            return this.overlay.leave();
        } else {
            throw new NetworkNotJoinedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperation receive(SynchronousOperation operation) {
        return operation.handle(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void receive(AsynchronousOperation operation) {
        operation.handle(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperation receiveImmediateService(SynchronousOperation operation) {
        return operation.handle(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void receiveImmediateService(AsynchronousOperation operation) {
        operation.handle(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void route(RequestResponseMessage<?> msg) {
        msg.route(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendv(Request<?> request) {
        this.overlay.dispatchv(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<?> send(Request<?> request) throws DispatchException {
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
    public boolean equals(Object obj) {
        return obj instanceof PeerImpl
                && this.getId().equals(((PeerImpl) obj).getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (this.overlay == null) {
            // toString is performed on a stub
            return "stub" + Integer.toString(System.identityHashCode(this));
        }

        return this.overlay.toString();
    }

}
