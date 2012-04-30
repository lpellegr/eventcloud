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
package org.objectweb.proactive.extensions.p2p.structured.proxies;

import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * This interface defines some methods to send requests with or without response
 * over a structured P2P network. A proxy plays the role of a gateway between
 * the entity that wants to communicate and the P2P network.
 * 
 * @author lpellegr
 */
public interface Proxy {

    /**
     * Sends a request over the overlay by using message passing but without any
     * response in return.
     * 
     * @param request
     *            the request to handle.
     * 
     * @throws DispatchException
     *             if a problem occurs when the query is dispatched.
     */
    void sendv(Request<?> request) throws DispatchException;

    /**
     * Sends a request over the overlay by using message passing but without any
     * response in return.
     * 
     * @param request
     *            the request to handle.
     * @param peer
     *            the stub from where the request is sent.
     * 
     * @throws DispatchException
     *             if a problem occurs when the query is dispatched.
     */
    void sendv(Request<?> request, Peer peer) throws DispatchException;

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
    Response<?> send(Request<?> request) throws DispatchException;

    /**
     * Sends a request over the overlay by using message passing.
     * 
     * @param request
     *            the request to handle.
     * @param peer
     *            the stub from where the request is sent.
     * 
     * @return the response in agreement with the request type sent.
     * 
     * @throws DispatchException
     *             if a problem occurs when the query is dispatched.
     */
    Response<?> send(Request<?> request, Peer peer) throws DispatchException;

    /**
     * Returns a peer stub randomly selected among the stubs managed by a
     * tracker.
     * 
     * @return a peer stub randomly selected among the stubs managed by a
     *         tracker.
     */
    Peer selectPeer();

}
