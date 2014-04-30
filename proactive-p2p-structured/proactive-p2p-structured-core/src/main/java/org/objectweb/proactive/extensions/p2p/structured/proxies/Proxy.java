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
package org.objectweb.proactive.extensions.p2p.structured.proxies;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.Response;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseCombiner;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * This interface defines some methods to send requests with or without response
 * over a structured P2P network. A proxy plays the role of a gateway between
 * the entity that wants to communicate and the P2P network.
 * 
 * @author lpellegr
 */
public interface Proxy extends ProxyAttributeController {

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
     * Dispatches the specified requests in parallel.
     * 
     * @param requests
     *            the requests to dispatch in parallel.
     * @param context
     *            a context that can be any serializable object.
     * @param responseCombiner
     *            the response combiner used to combine intermediate responses.
     * 
     * @return a response associated to the type of the requests sent once the
     *         intermediate responses have been combined with the specified
     *         response combiner.
     */
    Serializable send(List<? extends Request<?>> requests,
                      Serializable context, ResponseCombiner responseCombiner);

    /**
     * Dispatches the specified requests in parallel from the specified peer.
     * 
     * @param requests
     *            the requests to dispatch in parallel.
     * @param context
     *            a context that can be any serializable object.
     * @param responseCombiner
     *            the response combiner used to combine intermediate responses.
     * @param peer
     *            the peer from where the request is sent.
     * 
     * @return a response associated to the type of the requests sent once the
     *         intermediate responses have been combined with the specified
     *         response combiner.
     */
    Serializable send(List<? extends Request<?>> requests,
                      Serializable context, ResponseCombiner responseCombiner,
                      Peer peer);

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
     * Returns a peer stub randomly selected among the stubs managed by a
     * tracker.
     * 
     * @return a peer stub randomly selected among the stubs managed by a
     *         tracker.
     */
    Peer selectPeer();

}
