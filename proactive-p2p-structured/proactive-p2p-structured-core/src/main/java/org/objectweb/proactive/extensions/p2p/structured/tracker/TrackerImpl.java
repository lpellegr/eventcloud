/**
 * Copyright (c) 2011-2012 INRIA.
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

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of {@link Tracker} for active objects.
 * 
 * @author lpellegr
 */
public class TrackerImpl implements Tracker, InitActive, EndActive {

    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(TrackerImpl.class);

    protected transient String bindingName;

    private transient String bindingNameSuffix;

    private final UUID id;

    private String networkName;

    private double probabilityToStorePeer;

    // references to the remote peers
    private List<Peer> peers;

    // remote reference of the current tracker
    private Tracker stub;

    private transient Group<Tracker> typedGroupView;

    private transient Tracker untypedGroupView;

    protected OverlayType type;

    /**
     * Constructor.
     */
    public TrackerImpl() {
        this.id = UUID.randomUUID();

        this.probabilityToStorePeer =
                P2PStructuredProperties.TRACKER_STORAGE_PROBABILITY.getValue();

        // copy on write list to avoid to synchronize
        // when we iterate on the tracker
        this.peers = new CopyOnWriteArrayList<Peer>();

        try {
            this.untypedGroupView =
                    (Tracker) PAGroup.newGroup(Tracker.class.getCanonicalName());
            this.typedGroupView = PAGroup.getGroup(this.untypedGroupView);
        } catch (ClassNotReifiableException cnre) {
            cnre.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    /**
     * Constructs a new tracker with the specified
     * {@code probabilityToStorePeer}.
     * 
     * @param probabilityToStorePeer
     *            the probability to store a peer reference.
     */
    public TrackerImpl(double probabilityToStorePeer) {
        this();
        this.probabilityToStorePeer = probabilityToStorePeer;
    }

    /**
     * Constructor.
     * 
     * @param networkName
     *            the name of the network to which the tracker is associated.
     */
    public TrackerImpl(String networkName) {
        this();
        this.networkName = networkName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Tracker stub, String networkName) {
        if (this.stub == null) {
            this.stub = stub;
            this.networkName = networkName;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initActivity(Body body) {
        this.stub = (Tracker) PAActiveObject.getStubOnThis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endActivity(Body body) {
        if (this.bindingName != null) {
            try {
                PAActiveObject.unregister(this.bindingName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        PAGroup.waitAll(this.untypedGroupView.internalRemoveTracker(this.stub));

        this.typedGroupView = null;
        this.untypedGroupView = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean join(Tracker landmarkTracker) {
        if (!this.networkName.equals(landmarkTracker.getNetworkName())
                || ((this.type != null) && (this.type != landmarkTracker.getType()))) {
            return false;
        }

        this.typedGroupView.add(landmarkTracker);
        this.typedGroupView.addAll(landmarkTracker.getTypedGroupView());

        PAGroup.waitAll(this.untypedGroupView.internalAddTracker(this.stub));

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void inject(Peer remotePeer) throws NetworkAlreadyJoinedException {
        OverlayType remotePeerType = remotePeer.getType();

        if (this.type == null) {
            this.type = remotePeerType;
            remotePeer.create();
            this.storePeer(remotePeer);
            logger.debug("New network created from {}", remotePeer.getId());
        } else {
            if (this.type != remotePeerType) {
                throw new IllegalArgumentException(
                        "Illegal Peer type. The tracker manages a " + this.type
                                + " network and receives " + remotePeerType);
            }

            Peer landmarkPeer = this.getRandomPeer();
            boolean joinSucceed = false;

            // try to join until the operation succeeds (the operation
            // can fail if a concurrent join is detected).
            while (!joinSucceed) {
                joinSucceed = remotePeer.join(landmarkPeer);

                if (!joinSucceed) {
                    logger.debug(
                            "Retry join operation in {} ms because a concurrent join or leave operation has been detected",
                            P2PStructuredProperties.TRACKER_JOIN_RETRY_INTERVAL.getValue());
                    try {
                        Thread.sleep(P2PStructuredProperties.TRACKER_JOIN_RETRY_INTERVAL.getValue());
                        landmarkPeer = this.getRandomPeer();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (ProActiveRandom.nextDouble() <= this.getProbabilityToStorePeer()) {
                this.storePeer(remotePeer);
            }

            logger.info(
                    "Peer managing {} has joined from {}", remotePeer,
                    landmarkPeer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storePeer(Peer peerReference) {
        this.peers.add(peerReference);
        PAGroup.waitAll(this.untypedGroupView.internalStorePeer(peerReference));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePeer(Peer peerReference) {
        this.internalRemovePeer(peerReference);
        PAGroup.waitAll(this.untypedGroupView.internalRemovePeer(peerReference));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanWrapper internalStorePeer(Peer peerReference) {
        if (this.type == null) {
            this.type = peerReference.getType();
        }

        return new BooleanWrapper(this.peers.add(peerReference));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanWrapper internalRemovePeer(Peer peerReference) {
        return new BooleanWrapper(this.peers.remove(peerReference));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanWrapper internalAddTracker(Tracker trackerReference) {
        return new BooleanWrapper(this.typedGroupView.add(trackerReference));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanWrapper internalRemoveTracker(Tracker trackerReference) {
        return new BooleanWrapper(this.typedGroupView.remove(trackerReference));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String register() {
        try {
            this.bindingName =
                    PAActiveObject.registerByName(
                            this.stub, this.getBindingNameSuffix());
        } catch (ProActiveException pe) {
            pe.printStackTrace();
        }

        return this.bindingName;
    }

    public String getBindingNameSuffix() {
        if (this.bindingNameSuffix == null) {
            StringBuffer appender = new StringBuffer("tracker/");
            appender.append(this.networkName);
            appender.append("/");
            appender.append(this.id.toString());
            this.bindingNameSuffix = appender.toString();
        }

        return this.bindingNameSuffix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNetworkName() {
        return this.networkName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Peer getPeer(int index) {
        if (index < 0 || index >= this.peers.size()) {
            throw new IndexOutOfBoundsException("index " + index
                    + " is out of [0;" + this.peers.size() + "[");
        }
        return this.peers.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Peer> getPeers() {
        return this.peers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Peer getRandomPeer() {
        return this.peers.get(RandomUtils.nextInt(this.peers.size()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OverlayType getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getProbabilityToStorePeer() {
        return this.probabilityToStorePeer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProbabilityToStorePeer(double value) {
        this.probabilityToStorePeer = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group<Tracker> getTypedGroupView() {
        return this.typedGroupView;
    }

    public Tracker getUntypedGroupView() {
        return this.untypedGroupView;
    }

}
