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
package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.util.LinkedList;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.MulticastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.MulticastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * List used by {@link MulticastRequest} and {@link MulticastResponse} to store
 * {@link ReversePathEntry reverse path entries}. It contains an
 * {@link ReversePathEntry} for each {@link Peer} met while routing a
 * {@link MulticastRequest}.
 * 
 * @author lpellegr
 */
public class ReversePathStack<E extends Coordinate> extends
        LinkedList<ReversePathEntry<E>> {

    private static final long serialVersionUID = 160L;

    /**
     * Search the specified {@link UUID} which is associated to a {@link Peer}
     * identifier in the current list composed of {@link ReversePathEntry}.
     * 
     * @param peerId
     *            the {@link UUID} to look for.
     * @return the {@link ReversePathEntry} found or <code>null</code> if not
     *         found.
     */
    public ReversePathEntry<E> getRoutingResponseEntryBy(OverlayId peerId) {
        for (ReversePathEntry<E> entry : this) {
            if (entry.getPeerId().equals(peerId)) {
                return entry;
            }
        }

        return null;
    }

}
