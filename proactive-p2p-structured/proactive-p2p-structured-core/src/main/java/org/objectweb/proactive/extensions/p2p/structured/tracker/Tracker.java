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
package org.objectweb.proactive.extensions.p2p.structured.tracker;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.factories.TrackerFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * A tracker assists in the communication between peers. It serves as an
 * entry-point in a peer-to-peer network by maintaining several peers
 * references. These references are stored according to a specified probability
 * rate. Please also note that a tracker can only maintain reference to peers of
 * the same type (e.g. peers of type CAN or Chord but not the both at the same
 * time). Take care of it when you call {@link #inject(Peer)}.
 * <p>
 * A tracker can join an another tracker. When a tracker A join a tracker B
 * which already has references to other trackers C and D for example, a
 * reference on tracker B, C and D will be added. Thus, a tracker has only to
 * join one tracker in order to be bind to all the trackers which belong to the
 * same network.
 * <p>
 * Warning, this class must not be instantiate directly. In order to create a
 * new active tracker you must use the {@link TrackerFactory}.
 * 
 * @author lpellegr
 */
public interface Tracker extends Serializable {

    /**
     * The init method is a convenient method for components which is used to
     * initialize a {@link Tracker}. Once this method is called and the stub
     * value is set, the next calls perform no action.
     * 
     * @param stub
     *            the tracker remote reference.
     * 
     * @param networkName
     *            the network name to use.
     */
    public void init(Tracker stub, String networkName);

    /**
     * Drives the current tracker to join the specified {@code landmarkTracker}.
     * 
     * @param landmarkTracker
     *            the landmark remote tracker to use.
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise (e.g. if a join has already be done).
     */
    public boolean join(Tracker landmarkTracker);

    /**
     * Adds the given {@code remotePeer} on the network managed by the tracker.
     * 
     * @param remotePeer
     *            the peer to add on the network.
     * 
     * @throws NetworkAlreadyJoinedException
     *             if the specified {@code remotePeer} has already joined a
     *             network.
     * 
     * @throws IllegalArgumentException
     *             if the specified {@code remotePeer} does not manage the same
     *             overlay type as the peers that are already maintained by the
     *             tracker.
     */
    public void inject(Peer remotePeer) throws NetworkAlreadyJoinedException;

    /**
     * Stores the specified {@code peerReference} locally and call
     * {@link #internalStorePeer(Peer)} on each Tracker that belongs to the
     * group of tracker maintained by this peer.
     * 
     * @param peerReference
     *            the peer reference to store.
     */
    public void storePeer(Peer peerReference);

    /**
     * Removes the specified {@code peerReference} locally and call
     * {@link #internalRemovePeer(Peer)} on each Tracker that belongs to the
     * group of tracker maintained by this peer.
     * 
     * @param peerReference
     *            the peer reference to remove.
     */
    public void removePeer(Peer peerReference);

    /**
     * Adds the specified {@code peerReference} into the list of peer references
     * that are maintained. This method is exposed at the API level in order to
     * have the possibility to call it on an untyped ProActive group of internal
     * type Tracker.
     * 
     * @param peerReference
     *            the remote peer reference to remove.
     * 
     * @return {@code true} if the operation has succeed, {@code false}
     *         otherwise.
     */
    public BooleanWrapper internalStorePeer(Peer peerReference);

    /**
     * Removes the specified {@code peerReference} from the list of peer
     * references that are maintained. This method is exposed at the API level
     * in order to have the possibility to call it on an untyped ProActive group
     * of internal type Tracker.
     * 
     * @param peerReference
     *            the remote peer reference to remove.
     * 
     * @return {@code true} if the operation has succeed, {@code false}
     *         otherwise.
     */
    public BooleanWrapper internalRemovePeer(Peer peerReference);

    /**
     * Adds the specified {@code trackerReference} into the tracker group. This
     * method is exposed at the API level in order to have the possibility to
     * call it on an untyped ProActive group of internal type Tracker.
     * 
     * @param trackerReference
     *            the remote peer reference to remove.
     * 
     * @return {@code true} if the operation has succeed, {@code false}
     *         otherwise.
     */
    public BooleanWrapper internalAddTracker(Tracker trackerReference);

    /**
     * Removes the specified {@code trackerReference} from the tracker group.
     * This method is exposed at the API level in order to have the possibility
     * to call it on an untyped ProActive group of internal type Tracker.
     * 
     * @param trackerReference
     *            the remote peer reference to remove.
     * 
     * @return {@code true} if the operation has succeed, {@code false}
     *         otherwise.
     */
    public BooleanWrapper internalRemoveTracker(Tracker trackerReference);

    /**
     * Register the tracker into the RMI registry.
     * 
     * @return the URL where the object has been bind to.
     */
    public String register();

    /**
     * Returns the unique identifier associated to this tracker.
     * 
     * @return the unique identifier associated to this tracker.
     */
    public UUID getId();

    /**
     * Returns the network name the tracker belongs to.
     * 
     * @return the network name the tracker belongs to.
     */
    public String getNetworkName();

    /**
     * Returns the probability to store a {@link Peer} reference.
     * 
     * @return the probability to store a {@link Peer} reference.
     */
    public double getProbabilityToStorePeer();

    /**
     * Returns the {@link Peer} stored at the specified index.
     * 
     * @param index
     *            the index to use.
     * @return a {@link Peer}.
     */
    public Peer getPeer(int index);

    /**
     * Returns a list containing all the peer references that are maintained.
     * 
     * @return a list containing all the peer references that are maintained.
     */
    public List<Peer> getPeers();

    /**
     * Returns a random and valid peer from the stored peers list.
     * 
     * @return a random and valid peer.
     */
    public Peer getRandomPeer();

    /**
     * Returns the typed group view (the group contains the trackers that have
     * been joined and that belongs to the same network name).
     * 
     * @return the typed group view (the group contains the trackers that have
     *         been joined and that belongs to the same network name).
     */
    public Group<Tracker> getTypedGroupView();

    /**
     * Returns the type of peer references the tracker maintain. The type is
     * determined by the first call to {@link #inject(Peer)}.
     * 
     * @return the type of peer references the tracker maintain
     */
    public OverlayType getType();

    /**
     * Sets the probability to keep in mind a peer reference to the specified
     * {@code value}.
     * 
     * @param value
     *            the new value to set.
     */
    public void setProbabilityToStorePeer(double value);

}
