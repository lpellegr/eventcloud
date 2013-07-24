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

import java.util.LinkedList;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;

/**
 * List used by {@link AnycastRequest} and {@link AnycastResponse} to store
 * {@link AnycastRoutingEntry}. It contains an {@link AnycastRoutingEntry} for
 * each {@link Peer} met while routing an {@link AnycastRequest}.
 * 
 * @author lpellegr
 */
public class AnycastRoutingList<E extends Element> extends
        LinkedList<AnycastRoutingEntry<E>> {

    private static final long serialVersionUID = 150L;

    /**
     * Search the specified {@link UUID} which is associated to a {@link Peer}
     * identifier in the current list composed of {@link AnycastRoutingEntry}.
     * 
     * @param peerId
     *            the {@link UUID} to look for.
     * @return the {@link AnycastRoutingEntry} found or <code>null</code> if not
     *         found.
     */
    public AnycastRoutingEntry<E> getRoutingResponseEntryBy(OverlayId peerId) {
        for (AnycastRoutingEntry<E> entry : this) {
            if (entry.getPeerId().equals(peerId)) {
                return entry;
            }
        }

        return null;
    }

}
