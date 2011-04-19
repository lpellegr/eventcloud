package org.objectweb.proactive.extensions.p2p.structured.tracker;

import java.util.List;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * A tracker assists in the communication between peers. It is used in order to
 * help a peer to join an existing network and to store several peers references
 * in order to retrieve them later as an entry point.
 * <p>
 * A tracker can join an another tracker. When a tracker A join a tracker B
 * which already has references to other trackers C and D for example, a
 * reference on tracker B, C and D will be added.
 * 
 * @author lpellegr
 */
public interface Tracker {

    public String getName();

    /**
     * Sets the name of the tracker.
     * 
     * @param name
     *            the new name of the tracker.
     */
    public void setName(String name);

    public String getAssociatedNetworkName();

    /**
     * Sets the associated network name.
     * 
     * @param networkName
     *            the new associated network name.
     */
    public void setAssociatedNetworkName(String networkName);

    public void setStub();

    public OverlayType getType();

    /**
     * Sets the type of peers this tracker works.
     * 
     * @param type
     *            the new type of peers this tracker works.
     */
    public void setType(OverlayType type);

    /**
     * Returns the probability to store a {@link Peer} reference.
     * 
     * @return the probability to store a {@link Peer} reference.
     */
    public double getProbabilityToStorePeer();

    public void setProbabilityToStorePeer(double p);

    public Group<Tracker> getGroup();

    public BooleanWrapper addTrackerToGroup(Tracker remoteReference);

    /**
     * Returns the number of peers that the tracker manages.
     * 
     * @return the number of peers that the tracker manages.
     */
    public int getNbOfPeersStored();

    /**
     * Returns a random and valid peer from the stored peers list.
     * 
     * @return a random and valid peer.
     */
    public Peer getRandomPeer();

    /**
     * Returns the {@link Peer} stored at the specified index.
     * 
     * @param index
     *            the index to use.
     * @return a {@link Peer}.
     */
    public Peer getStoredPeerAt(int index);

    /**
     * Returns the stored peers.
     * 
     * @return the stored peers.
     */
    public List<Peer> getStoredPeers();

    /**
     * Adds the given {@code remotePeer} on the network managed by the tracker.
     * 
     * @param remotePeer
     *            the peer to add on the network.
     */
    public boolean addOnNetwork(Peer remotePeer);

    public void storePeer(Peer peerReference);

    public BooleanWrapper _storePeer(Peer peerReference);

    public void removePeer(Peer peerReference);

    public BooleanWrapper _removePeer(Peer peerReference);

    /**
     * Join an existing tracker in order to duplicate peers references.
     * 
     * @param remoteTracker
     *            the tracker to use for the operation.
     * @return <code>true</code> if the operation has succeeded,
     *         <code>false</code> otherwise.
     */
    public boolean join(Tracker remoteTracker);

    public BooleanWrapper notifyLeave(Tracker trackerReference);

    public String getBindingName();

    public String register();

}
