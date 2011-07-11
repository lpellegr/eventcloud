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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.operations.AsynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;

/**
 * A peer contains all operations which are common to peer-to-peer protocols.
 * 
 * @author lpellegr
 */
public interface Peer extends Serializable {

    /**
     * The init method is a convenient method for components which is used to
     * initialize the {@link StructuredOverlay}. Once this method is called and
     * the values are set, the next calls perform no action.
     * 
     * @param stub
     *            the remote peer reference to set.
     * 
     * @param overlay
     *            the overlay instance to set to the peer.
     */
    public void init(Peer stub, StructuredOverlay overlay);

    /**
     * Returns the unique identifier associated to this peer.
     * 
     * @return the unique identifier associated to this peer.
     */
    public UUID getId();

    /**
     * Returns the overlay type for the current peer.
     * 
     * @return the overlay type for the current peer.
     */
    public OverlayType getType();

    /**
     * Returns a boolean indicating the current peer is activated (i.e. it has
     * already joined a network and has not yet left).
     * 
     * @return {@code true} if the peer has already joined a network and has not
     *         yet left, {@code false} otherwise.
     */
    public boolean isActivated();

    /**
     * This method is used to initialize the state of the peer in the special
     * case where it is the first peer on the network.
     * 
     * @return a boolean indicating if the operation has succeeded or not.
     * 
     * @throws NetworkAlreadyJoinedException
     *             if the peer has already joined or created an existing
     *             network.
     */
    public boolean create() throws NetworkAlreadyJoinedException;

    /**
     * Forces the current peer to join an existing network by using the
     * specified peer reference. When a peer has joined a network it has to
     * leave the network before to join a new one otherwise a
     * {@link NetworkAlreadyJoinedException} will be thrown.
     * 
     * @param landmarkPeer
     *            the peer used as entry point.
     * @return Returns {@code true} if the operation has succeeded,
     *         {@code false} otherwise (e.g. if a concurrent join or leave
     *         operation is detected).
     * @throws NetworkAlreadyJoinedException
     *             if the current peer has already joined a network.
     */
    public boolean join(Peer landmarkPeer) throws NetworkAlreadyJoinedException;

    /**
     * Forces the current peer to leave the network it has joined.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     * 
     * @throws NetworkNotJoinedException
     *             if the current peer try leave without having joined a
     *             network.
     */
    public boolean leave() throws NetworkNotJoinedException;

    /**
     * Receives and handles the specified {@code operation} synchronously.
     * 
     * @param operation
     *            the operation to handle.
     * 
     * @return a response according to the operation type handled.
     */
    public ResponseOperation receive(SynchronousOperation operation);

    /**
     * Receives and handles the specified {@code operation} asynchronously.
     * 
     * @param operation
     *            the operation to handle.
     */
    public void receive(AsynchronousOperation operation);

    /**
     * Receives in immediate service and handles the specified {@code operation}
     * synchronously.
     * <p>
     * To receive the operation in immediate service completely by-passes the
     * message queue model that comes with the active objects, thus breaks the
     * theoretical model and may introduce race conditions.
     * 
     * @param operation
     *            the operation to handle.
     * 
     * @return a response according to the operation type handled.
     */
    public ResponseOperation receiveImmediateService(SynchronousOperation operation);

    /**
     * Receives in immediate service and handles the specified {@code operation}
     * synchronously.
     * <p>
     * To receive the operation in immediate service completely by-passes the
     * message queue model that comes with the active objects, thus breaks the
     * theoretical model and may introduce race conditions.
     * 
     * @param operation
     *            the operation to handle.
     */
    public void receiveImmediateService(AsynchronousOperation operation);

    /**
     * Routes the specified {@code msg}.
     * 
     * @param msg
     *            the message to route.
     */
    public void route(RequestResponseMessage<?> msg);

    /**
     * Sends a request over the overlay by using message passing.
     * 
     * @param request
     *            the request to handle.
     * 
     * @return the response in agreement with the request type sent.
     * 
     * @throws DispatchException
     *             if a problem occurs when the query is dispatched.
     */
    public Response<?> send(Request<?> request) throws DispatchException;

    /**
     * Returns debug information as String.
     * 
     * @return debug information as String.
     */
    public String dump();

    /**
     * {@inheritDoc}
     */
    public String toString();

}
