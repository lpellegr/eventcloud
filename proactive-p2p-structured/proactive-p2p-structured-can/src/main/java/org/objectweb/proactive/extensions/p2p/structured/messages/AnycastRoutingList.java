package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.util.LinkedList;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * List used by {@link AnycastRequest} and
 * {@link AnycastResponseMessage} to store {@link AnycastRoutingEntry}. It
 * contains an {@link AnycastRoutingEntry} for each {@link Peer} met while
 * routing an {@link AnycastRequest}.
 * 
 * @author lpellegr
 */
public class AnycastRoutingList extends LinkedList<AnycastRoutingEntry> {

    private static final long serialVersionUID = 1L;

    /**
     * Search the specified {@link UUID} which is associated to a {@link Peer}
     * identifier in the current list composed of {@link AnycastRoutingEntry}.
     * 
     * @param peerID
     *            the {@link UUID} to look for.
     * @return the {@link AnycastRoutingEntry} found or <code>null</code> if
     *         not found.
     */
    public AnycastRoutingEntry getRoutingResponseEntryBy(UUID peerID) {
        for (AnycastRoutingEntry entry : this) {
            if (entry.getPeerId().equals(peerID)) {
                return entry;
            }
        }

        return null;
    }
    
}
