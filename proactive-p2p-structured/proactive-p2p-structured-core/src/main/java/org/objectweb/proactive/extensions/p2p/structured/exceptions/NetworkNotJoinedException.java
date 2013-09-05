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
package org.objectweb.proactive.extensions.p2p.structured.exceptions;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * An exception thrown when the {@link Peer#leave()} method from a {@link Peer}
 * is called whereas the peer has not joined the network by using the
 * {@link Peer#join(Peer)} method.
 * 
 * @author lpellegr
 */
public class NetworkNotJoinedException extends StructuredP2PException {

    private static final long serialVersionUID = 160L;

    /**
     * Constructs a {@code NetworkNotJoinedException} with no specified detail
     * message.
     */
    public NetworkNotJoinedException() {
        super();
    }

}
