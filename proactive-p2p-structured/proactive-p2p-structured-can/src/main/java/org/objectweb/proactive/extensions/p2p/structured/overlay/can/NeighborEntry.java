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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.io.Serializable;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;

/**
 * NeighborEntry is an entry in a {@link NeighborTable}.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class NeighborEntry<E extends Element> implements Serializable {

    private static final long serialVersionUID = 160L;

    private final OverlayId neighborIdentifier;

    private final Peer neighborStub;

    private Zone<E> neighborZone;

    public NeighborEntry(Peer peerStub) {
        this.neighborStub = peerStub;

        @SuppressWarnings("unchecked")
        GetIdAndZoneResponseOperation<E> response =
                (GetIdAndZoneResponseOperation<E>) PAFuture.getFutureValue(this.neighborStub.receive(new GetIdAndZoneOperation<E>()));

        this.neighborIdentifier = response.getPeerIdentifier();
        this.neighborZone = response.getPeerZone();
    }

    public NeighborEntry(OverlayId peerIdentifier, Peer peerStub,
            Zone<E> peerZone) {
        this.neighborIdentifier = peerIdentifier;
        this.neighborStub = peerStub;
        this.neighborZone = peerZone;
    }

    public OverlayId getId() {
        return this.neighborIdentifier;
    }

    public Peer getStub() {
        return this.neighborStub;
    }

    public Zone<E> getZone() {
        return this.neighborZone;
    }

    public synchronized void setZone(Zone<E> newZone) {
        this.neighborZone = newZone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.neighborIdentifier.hashCode() + 31
                * this.neighborZone.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NeighborEntry
                && this.neighborIdentifier.equals(((NeighborEntry<?>) obj).getId())
                && this.neighborZone.equals(((NeighborEntry<?>) obj).getZone());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "NeighborEntry[peerId=" + this.neighborIdentifier + ", zone="
                + this.neighborZone + "]";
    }

}
