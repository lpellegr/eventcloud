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

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PeerNotActivatedException;
import org.objectweb.proactive.extensions.p2p.structured.messages.Message;
import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.RunnableOperation;

/**
 * A peer defines all operations which are common to structured peer-to-peer
 * protocols. This interface acts as a Facade in order to simplify the
 * operations which are exposed (easier to use, understand and test).
 * 
 * @author lpellegr
 */
public interface Peer extends Comparable<Peer>, Serializable {

    /**
     * Returns the unique identifier associated to this peer.
     * 
     * @return the unique identifier associated to this peer.
     */
    OverlayId getId();

    /**
     * Returns the overlay type for the current peer.
     * 
     * @return the overlay type for the current peer.
     */
    OverlayType getType();

    /**
     * Returns the status of the peer.
     * 
     * @return the status of the peer.
     */
    Status getStatus();

    /**
     * This method is used to initialize the state of the peer in the special
     * case where it is the first peer on the network.
     * 
     * @throws NetworkAlreadyJoinedException
     *             if the peer has already joined or created an existing
     *             network.
     */
    void create() throws NetworkAlreadyJoinedException;

    /**
     * Forces the current peer to join an existing network by using the
     * specified peer reference. When a peer has joined a network it has to
     * leave the network before to join a new one otherwise a
     * {@link NetworkAlreadyJoinedException} will be thrown.
     * 
     * @param landmarkPeer
     *            the peer used as entry point.
     * 
     * @throws NetworkAlreadyJoinedException
     *             if the current peer has already joined a network.
     * @throws PeerNotActivatedException
     *             if the specified {@code landmarkPeer} is not activated.
     */
    void join(Peer landmarkPeer) throws NetworkAlreadyJoinedException,
            PeerNotActivatedException;

    /**
     * Forces the current peer to leave the network it has joined.
     * 
     * @throws NetworkNotJoinedException
     *             if the current peer try leave without having joined a
     *             network.
     */
    void leave() throws NetworkNotJoinedException;

    /**
     * Reassigns the peer to another part of the overlay by forcing it to leave
     * its position and joining the specified {@code landmarkPeer}.
     * 
     * @param landmarkPeer
     *            the new peer to join.
     * 
     * @throws NetworkNotJoinedException
     *             if the current peer is not member of a network.
     */
    void reassign(Peer landmarkPeer) throws NetworkNotJoinedException;

    /**
     * Receives and handles the specified {@code operation} asynchronously by
     * returning a future.
     * 
     * @param operation
     *            the operation to handle.
     * 
     * @return a response according to the operation type handled.
     */
    ResponseOperation receive(CallableOperation operation);

    /**
     * Receives and handles the specified {@code operation} asynchronously.
     * 
     * @param operation
     *            the operation to handle.
     */
    void receive(RunnableOperation operation);

    /**
     * Routes the specified {@code msg} by using double dispatch.
     * 
     * @param msg
     */
    void route(Message<?> msg);

    /**
     * Returns debug information as String.
     * 
     * @return debug information as String.
     */
    String dump();

    /**
     * {@inheritDoc}
     */
    @Override
    String toString();

}
