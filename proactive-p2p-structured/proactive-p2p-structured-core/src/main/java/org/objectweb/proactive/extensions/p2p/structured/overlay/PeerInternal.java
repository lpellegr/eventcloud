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
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.messages.FinalResponseReceiver;
import org.objectweb.proactive.extensions.p2p.structured.messages.Message;
import org.objectweb.proactive.extensions.p2p.structured.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseCombiner;

/**
 * Keep separated the methods used internally but that should not be exposed to
 * users.
 * 
 * @author lpellegr
 */
public interface PeerInternal {

    /**
     * Dispatches the specified requests in parallel
     * 
     * @param requests
     *            the requests to dispatch in parallel.
     * @param context
     *            a context that can be any serializable object.
     * @param responseCombiner
     *            the response combiner used to combine intermediate responses.
     * @param responseDestination
     *            a reference to the requester to send the final response.
     */
    <T extends Request<?>> void dispatch(List<T> requests,
                                         Serializable context,
                                         ResponseCombiner responseCombiner,
                                         FinalResponseReceiver responseDestination);

    /**
     * Routes the specified {@code msg} by using double dispatch. The main
     * difference between this method and {@link Peer#route(Message)} is that
     * the former increases the number of hop count where the latter does not.
     * 
     * @param msg
     *            the message to route.
     */
    void forward(Message<?> msg);

    /**
     * Injects the specified list of requests in the request queue of the
     * current peer.
     * 
     * @param requests
     *            the requests to inject.
     */
    void inject(List<org.objectweb.proactive.core.body.request.Request> requests);

}
