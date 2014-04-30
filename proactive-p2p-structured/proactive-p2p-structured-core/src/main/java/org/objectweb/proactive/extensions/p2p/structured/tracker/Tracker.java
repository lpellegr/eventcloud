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
package org.objectweb.proactive.extensions.p2p.structured.tracker;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PeerNotActivatedException;
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
     * Drives the current tracker to join the specified {@code landmarkTracker}.
     * 
     * @param landmarkTracker
     *            the landmark remote tracker to use.
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise (e.g. if a join has already be done).
     */
    boolean join(Tracker landmarkTracker);

    /**
     * Adds the given {@code remotePeer} on the network managed by the tracker.
     * 
     * @param remotePeer
     *            the peer to add on the network.
     * 
     * @throws NetworkAlreadyJoinedException
     *             if the specified {@code remotePeer} has already joined a
     *             network.
     * @throws IllegalArgumentException
     *             if the specified {@code remotePeer} does not manage the same
     *             overlay type as the peers that are already maintained by the
     *             tracker.
     */
    void inject(Peer remotePeer) throws NetworkAlreadyJoinedException;

    /**
     * Adds the given {@code remotePeer} on the network managed by the tracker
     * by joining the specified {@code landmarkPeer}.
     * 
     * @param remotePeer
     *            the peer to add on the network.
     * @param landmarkPeer
     *            the peer which is joined by the remotePeer.
     * 
     * @throws NetworkAlreadyJoinedException
     *             if the specified {@code remotePeer} has already joined a
     *             network.
     * @throws PeerNotActivatedException
     *             if the specified {@code landmarkPeer} is not activated.
     * @throws IllegalArgumentException
     *             if the specified {@code remotePeer} does not manage the same
     *             overlay type as the peers that are already maintained by the
     *             tracker.
     */
    void inject(Peer remotePeer, Peer landmarkPeer)
            throws NetworkAlreadyJoinedException, PeerNotActivatedException;

    /**
     * Forces the specified peer to leave the network and removes the associated
     * reference from the trackers.
     * 
     * @param remotePeer
     *            the peer to make it left.
     * 
     * @throws NetworkNotJoinedException
     *             if the specified {@code remotePeer} belongs to no network.
     * @throws PeerNotActivatedException
     *             if the specified {@code landmarkPeer} is not activated.
     */
    void takeout(Peer remotePeer) throws NetworkNotJoinedException,
            PeerNotActivatedException;

    /**
     * Stores the specified {@code peerReference} locally and call
     * {@link #internalStorePeer(Peer)} on each Tracker that belongs to the
     * group of tracker maintained by this peer.
     * 
     * @param peerReference
     *            the peer reference to store.
     */
    void storePeer(Peer peerReference);

    /**
     * Removes the specified {@code peerReference} locally and call
     * {@link #internalRemovePeer(Peer)} on each Tracker that belongs to the
     * group of tracker maintained by this peer.
     * 
     * @param peerReference
     *            the peer reference to remove.
     */
    void removePeer(Peer peerReference);

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
    BooleanWrapper internalStorePeer(Peer peerReference);

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
    BooleanWrapper internalRemovePeer(Peer peerReference);

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
    BooleanWrapper internalAddTracker(Tracker trackerReference);

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
    BooleanWrapper internalRemoveTracker(Tracker trackerReference);

    /**
     * Register the tracker into the RMI registry.
     * 
     * @return the URL where the object has been bind to.
     */
    String register();

    /**
     * Returns the unique identifier associated to this tracker.
     * 
     * @return the unique identifier associated to this tracker.
     */
    UUID getId();

    /**
     * Returns the network name the tracker belongs to.
     * 
     * @return the network name the tracker belongs to.
     */
    String getNetworkName();

    /**
     * Returns the probability to store a {@link Peer} reference.
     * 
     * @return the probability to store a {@link Peer} reference.
     */
    double getProbabilityToStorePeer();

    /**
     * Returns the {@link Peer} stored at the specified index.
     * 
     * @param index
     *            the index to use.
     * @return a {@link Peer}.
     */
    Peer getPeer(int index);

    /**
     * Returns a list containing all the peer references that are maintained.
     * 
     * @return a list containing all the peer references that are maintained.
     */
    List<Peer> getPeers();

    /**
     * Returns a random and valid peer from the stored peers list.
     * 
     * @return a random and valid peer.
     */
    Peer getRandomPeer();

    /**
     * Removes a peer reference maintained by the tracker at random.
     * 
     * @return the peer reference which has been removed.
     */
    Peer removeRandomPeer();

    /**
     * Returns the typed group view (the group contains the trackers that have
     * been joined and that belongs to the same network name).
     * 
     * @return the typed group view (the group contains the trackers that have
     *         been joined and that belongs to the same network name).
     */
    Group<Tracker> getTypedGroupView();

    /**
     * Returns the type of peer references the tracker maintain. The type is
     * determined by the first call to {@link #inject(Peer)}.
     * 
     * @return the type of peer references the tracker maintain
     */
    OverlayType getType();

    /**
     * Sets the probability to keep in mind a peer reference to the specified
     * {@code value}.
     * 
     * @param value
     *            the new value to set.
     */
    void setProbabilityToStorePeer(double value);

}
