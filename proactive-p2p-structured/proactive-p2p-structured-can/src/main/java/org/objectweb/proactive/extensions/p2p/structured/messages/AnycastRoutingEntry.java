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
package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.Serializable;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Contains information about {@link Peer}s met while routing an
 * {@link AnycastRequest}.
 * 
 * @author lpellegr
 */
public class AnycastRoutingEntry implements Serializable {

    private static final long serialVersionUID = 140L;

    private UUID peerId;

    private Peer peerStub;

    public UUID getPeerId() {
        return this.peerId;
    }

    public AnycastRoutingEntry(UUID peerID, Peer peerStub) {
        super();
        this.peerId = peerID;
        this.peerStub = peerStub;
    }

    public Peer getPeerStub() {
        return this.peerStub;
    }

}
