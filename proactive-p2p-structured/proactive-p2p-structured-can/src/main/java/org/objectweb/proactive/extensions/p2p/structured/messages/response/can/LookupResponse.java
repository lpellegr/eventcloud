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
package org.objectweb.proactive.extensions.p2p.structured.messages.response.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.LookupRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Response associated to {@link LookupRequest}.
 * 
 * @author lpellegr
 */
public class LookupResponse extends ForwardResponse {

    private static final long serialVersionUID = 1L;

    private Peer peerFound;

    public LookupResponse(LookupRequest query, Peer peerFound) {
        super(query);
        this.peerFound = peerFound;
    }

    public Peer getPeerFound() {
        return this.peerFound;
    }

}
