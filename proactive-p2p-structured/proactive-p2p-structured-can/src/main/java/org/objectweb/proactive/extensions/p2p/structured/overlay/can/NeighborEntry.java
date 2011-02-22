package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.io.Serializable;
import java.util.UUID;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * NeighborEntry is an entry in a {@link NeighborTable}.
 * 
 * @author Laurent Pellegrino
 */
public class NeighborEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID neighborIdentifier;

    private final Peer neighborStub;

    private Zone neighborZone;

    public NeighborEntry(Peer peerStub) {
        this.neighborStub = peerStub;

        GetIdAndZoneResponseOperation response = 
        	(GetIdAndZoneResponseOperation) PAFuture.getFutureValue(
        			this.neighborStub.receiveOperationIS(new GetIdAndZoneOperation()));

        this.neighborIdentifier = response.getPeerIdentifier();
        this.neighborZone = response.getPeerZone();
    }

    public NeighborEntry(UUID peerIdentifier, Peer peerStub, Zone peerZone) {
        super();
        this.neighborIdentifier = peerIdentifier;
        this.neighborStub = peerStub;
        this.neighborZone = peerZone;
    }

    public UUID getId() {
        return this.neighborIdentifier;
    }

    public Peer getStub() {
        return this.neighborStub;
    }

    public Zone getZone() {
        return this.neighborZone;
    }

    public void setZone(Zone newZone) {
    	this.neighborZone = newZone;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
    	return this.neighborIdentifier.hashCode() 
    				+ 31 * this.neighborZone.hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
    	return obj instanceof NeighborEntry
    			&& this.neighborIdentifier.equals(((NeighborEntry) obj).getId())
    				&& this.neighborZone.equals(((NeighborEntry) obj).getZone());
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
    	return "NeighborEntry[peerId=" + this.neighborIdentifier + ", zone=" + this.neighborZone + "]";
    }
    
}
