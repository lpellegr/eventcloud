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
package org.objectweb.proactive.extensions.p2p.structured.exceptions;

import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * An exception thrown when a {@link Peer} tries to join another peer which is
 * not activated (i.e. a peer which has created or joined no network).
 * 
 * @author lpellegr
 */
public class PeerNotActivatedException extends Exception {

    private static final long serialVersionUID = 160L;

    private final OverlayId peerId;

    /**
     * Constructs a {@code PeerNotActivatedRuntimeException} with no specified
     * detail message.
     */
    public PeerNotActivatedException(OverlayId peerId) {
        super("The landmark peer to join is not activated: " + peerId);
        this.peerId = peerId;
    }

    /**
     * Returns the identifier of the peer which is not activated.
     * 
     * @return the identifier of the peer which is not activated.
     */
    public OverlayId getPeerId() {
        return this.peerId;
    }

}
