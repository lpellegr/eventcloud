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
package org.objectweb.proactive.extensions.p2p.structured.proxies;

import java.io.Closeable;

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
public interface Proxy extends Closeable {

    /**
     * Sends a request over the overlay by using message passing but without any
     * response in return.
     * 
     * @param request
     *            the request to handle.
     */
    void sendv(Request<?> request);

    /**
     * Sends a request over the overlay by using message passing but without any
     * response in return.
     * 
     * @param request
     *            the request to handle.
     * @param peer
     *            the stub from where the request is sent.
     */
    void sendv(Request<?> request, Peer peer);

    /**
     * Sends a request over the overlay by using message passing.
     * 
     * @param request
     *            the request to handle.
     * 
     * @return the response in agreement with the request type sent.
     */
    Response<?> send(Request<?> request);

    /**
     * Sends a request over the overlay by using message passing.
     * 
     * @param request
     *            the request to handle.
     * @param peer
     *            the stub from where the request is sent.
     * 
     * @return the response in agreement with the request type sent.
     */
    Response<?> send(Request<?> request, Peer peer);

    /**
     * Returns a peer stub randomly selected among the stubs managed by a
     * tracker.
     * 
     * @return a peer stub randomly selected among the stubs managed by a
     *         tracker.
     */
    Peer selectPeer();

}
