package org.objectweb.proactive.extensions.p2p.structured.tracker;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.p2p.structured.api.TrackerFactory;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.StructuredP2PException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tracker assists in the communication between peers. It is used in order to
 * help a peer to join an existing network and to store several peers references in
 * order to retrieve them later as an entry point.
 * <p>
 * A tracker can join an another tracker. When a tracker A join a tracker B
 * which already has references to other trackers C and D for example, a
 * reference on tracker B, C and D will be added.
 * 
 * Warning, this class must not be instantiate directly. In order to create a
 * new active tracker you must use the {@link TrackerFactory}.
 * 
 * @author lpellegr
 */
public class TrackerImpl implements Tracker, InitActive, EndActive, RunActive, Serializable {

    private static final long serialVersionUID = 1L;

    protected static transient Logger logger = LoggerFactory.getLogger(TrackerImpl.class);

    private double probabilityToStorePeer = P2PStructuredProperties.TRACKER_STORAGE_PROBABILITY.getValue();

    protected String name = UUID.randomUUID().toString();

    protected String associatedNetworkName;

    private Tracker trackersGroup;

    /**
     * The remote peers list that the tracker maintains.
     */
    private List<Peer> storedPeers = new ArrayList<Peer>(100);

    private boolean isInGroup = false;

    protected Tracker stub;

    /**
     * The type of peers this tracker works.
     */
    protected OverlayType type;

    /**
     * Constructor.
     */
    public TrackerImpl() {
    }

    /**
     * Constructs a new Tracker by specifying the type of the tracker.
     * 
     * @param type
     *            the type of tracker (i.e. the kind of peers that can be add on
     *            the network).
     */
    public TrackerImpl(OverlayType type) {
        this.type = type;
    }

    /**
     * Constructor.
     * 
     * @param probabilityToStorePeer
     *            the probability to store a peer reference.
     */
    public TrackerImpl(double probabilityToStorePeer) {
        this.probabilityToStorePeer = probabilityToStorePeer;
    }

    /**
     * Constructor.
     * 
     * @param type
     *            the type of tracker (ie. the kind of peers that can be add on
     *            the network).
     * @param probabilityToStorePeer
     *            the probability to store a peer reference.
     */
    public TrackerImpl(OverlayType type, double probabilityToStorePeer) {
        this(probabilityToStorePeer);
        this.type = type;
    }

    /**
     * Constructs a new tracker by specifying the type of the tracker and the
     * tracker associated network name.
     * 
     * @param type
     *            the type of tracker (i.e. the kind of peers that can be add on
     *            the network).
     * @param associatedNetworkName
     *            the name of the network to which the tracker is associated.
     */
    public TrackerImpl(OverlayType type, String associatedNetworkName) {
        this.type = type;
        this.associatedNetworkName = associatedNetworkName;
    }

    /**
     * Constructor.
     * 
     * @param type
     *            the type of tracker (ie. the kind of peers that can be add on
     *            the network).
     * @param associatedNetworkName
     *            the name of the network to which the tracker is associated.
     * @param trackerName
     *            the name of the tracker.
     */
    public TrackerImpl(OverlayType type, String associatedNetworkName, String trackerName) {
        this.type = type;
        this.associatedNetworkName = associatedNetworkName;
        this.name = trackerName;
    }

    /**
     * {@inheritDoc}
     */
    public void initActivity(Body body) {
        this.stub = (Tracker) PAActiveObject.getStubOnThis();
        try {
            this.trackersGroup = (Tracker) PAGroup.newGroup(Tracker.class.getCanonicalName());
        } catch (ClassNotReifiableException cnre) {
            cnre.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            service.blockingServeOldest();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endActivity(Body body) {
        if (this.isInGroup) {
            try {
                PAActiveObject.unregister(this.getBindingName());
            } catch (IOException e) {
                // the tracker we try to unregister is not registered
            }
            PAGroup.waitAll(trackersGroup.notifyLeave(this.stub));
            this.isInGroup = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name) {
        // Nothing to do. This method is only useful for component tracker.
    }

    /**
     * {@inheritDoc}
     */
    public String getAssociatedNetworkName() {
        return this.associatedNetworkName;
    }

    /**
     * {@inheritDoc}
     */
    public void setAssociatedNetworkName(String networkName) {
        // Nothing to do. This method is only useful for component tracker.
    }

    /**
     * {@inheritDoc}
     */
    public void setStub() {
        // Nothing to do. This method is only useful for component tracker.
    }

    /**
     * {@inheritDoc}
     */
    public OverlayType getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    public void setType(OverlayType type) {
        // Nothing to do. This method is only useful for component tracker.
    }

    /**
     * {@inheritDoc}
     */
    public double getProbabilityToStorePeer() {
        return this.probabilityToStorePeer;
    }

    /**
     * {@inheritDoc}
     */
    public void setProbabilityToStorePeer(double p) {
        this.probabilityToStorePeer = p;
    }

    /**
     * {@inheritDoc}
     */
    public Group<Tracker> getGroup() {
        if (!this.isInGroup) {
            this.isInGroup = true;
        }
        return PAGroup.getGroup(this.trackersGroup);
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper addTrackerToGroup(Tracker remoteReference) {
        return new BooleanWrapper(PAGroup.getGroup(this.trackersGroup).add(remoteReference));
    }

    /**
     * {@inheritDoc}
     */
    public int getNbOfPeersStored() {
        return this.storedPeers.size();
    }

    /**
     * {@inheritDoc}
     */
    public Peer getRandomPeer() {
        if (this.storedPeers.size() == 0) {
            return null;
        }
        
        int randomPeerIndex = ProActiveRandom.nextInt(this.storedPeers.size());
        Peer randomPeer = this.storedPeers.get(randomPeerIndex);
        
        if (this.checkAlertness(randomPeer)) {
            return randomPeer;
        } else {
            this.storedPeers.remove(randomPeerIndex);
            return this.getRandomPeer();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Peer getStoredPeerAt(int index) {
        return this.storedPeers.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public List<Peer> getStoredPeers() {
        for (int i=0; i<this.storedPeers.size(); i++) {
            if (!this.checkAlertness(this.storedPeers.get(i))) {
                this.storedPeers.remove(i);
            }
        }
        
        return this.storedPeers;
    }

    /**
     * Returns a boolean indicating if the specified <code>peer</code> is
     * accessible and activated or not.
     * 
     * @param peer
     *            the remote peer to check.
     *            
     * @return <code>true</code> if the peer is accessible and activated,
     *         <code>false</code> otherwise.
     */
    private boolean checkAlertness(Peer peer) {
        try {
            return peer.isActivated();
        } catch(ProActiveRuntimeException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean addOnNetwork(Peer remotePeer) {
        if (remotePeer.getType() != this.type) {
            throw new IllegalArgumentException(
                    "Illegal Peer type. This tracker manages a "
                    + this.type + " network");
        } else if (this.storedPeers.size() == 0) {
            try {
                remotePeer.create();
            } catch (StructuredP2PException e) {
                e.printStackTrace();
            }
            this.storePeer(remotePeer);
            if (logger.isInfoEnabled()) {
                logger.info("Peer " + remotePeer.getId() + " has created a new network");
            }
        } else {
            try {
                Peer peerToJoin = this.getLandmarkPeerToJoin();

                // try to join until the operation succeeds (the operation
                // can fail if a concurrent join is detected).
                while (!remotePeer.join(peerToJoin)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Retry join operation in "
                                + P2PStructuredProperties.TRACKER_JOIN_RETRY_INTERVAL.getValue()
                                + " ms because a concurrent join or leave operation has been detected");
                    }
                    try {
                        Thread.sleep(P2PStructuredProperties.TRACKER_JOIN_RETRY_INTERVAL.getValue());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
                if (ProActiveRandom.nextDouble() 
                        <= this.getProbabilityToStorePeer()) {
                    this.storePeer(remotePeer);
                }
                
                if (logger.isInfoEnabled()) {
                    logger.info("Peer managing " + remotePeer + " has joined from " + peerToJoin);
                }
            } catch (NetworkAlreadyJoinedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Returns the landmark node to join when a {@link #addOnNetwork(Peer)} is
     * performed.
     * 
     * @return the landmark node to join when a {@link #addOnNetwork(Peer)} is
     *         performed.
     */
    protected Peer getLandmarkPeerToJoin() {
        return this.getRandomPeer();
    }
    
    /**
     * {@inheritDoc}
     */
    public void storePeer(Peer peerReference) {
        this.storedPeers.add(peerReference);
        PAGroup.waitAll(this.trackersGroup._storePeer(peerReference));
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper _storePeer(Peer peerReference) {
        return new BooleanWrapper(this.storedPeers.add(peerReference));
    }

    /**
     * {@inheritDoc}
     */
    public void removePeer(Peer peerReference) {
        this._removePeer(peerReference);
        PAGroup.waitAll(this.trackersGroup._removePeer(peerReference));
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper _removePeer(Peer peerReference) {
        return new BooleanWrapper(this.storedPeers.remove(peerReference));
    }

    /**
     * {@inheritDoc}
     */
    public boolean join(Tracker remoteTracker) {
        boolean result = true;
        Group<Tracker> newTrackersReferences = remoteTracker.getGroup();
        Group<Tracker> trackersGroup = PAGroup.getGroup(this.trackersGroup);

        result &= trackersGroup.add(remoteTracker);
        trackersGroup.addAll(newTrackersReferences);

        BooleanWrapper resultGroup = this.trackersGroup.addTrackerToGroup(this.stub);
        while (PAGroup.size(resultGroup) > 0) {
            result &= ((BooleanWrapper) PAGroup.waitAndGetOneThenRemoveIt(resultGroup)).getBooleanValue();
        }

        if (!this.isInGroup) {
            this.isInGroup = true;
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper notifyLeave(Tracker trackerReference) {
        return new BooleanWrapper(PAGroup.getGroup(this.trackersGroup).remove(trackerReference));
    }

    /**
     * {@inheritDoc}
     */
    public String getBindingName() {
        return (!this.associatedNetworkName.equals("default") ? "/"
                + this.associatedNetworkName + "/" : "")
                + this.name;
    }

    /**
     * {@inheritDoc}
     */
    public String register() {
        String bindingName = null;
        try {
            bindingName = PAActiveObject.registerByName(this.stub, this.getBindingName());
        } catch (ProActiveException pe) {
            pe.printStackTrace();
        }

        return bindingName;
    }

    /**
     * Register the specified tracker in the RMI registry of the current
     * machine.
     * 
     * @param tracker
     *            the active tracker to register in the RMI registry.
     * 
     * @return the URI on which the tracker is bind in the RMI registry.
     */
    public static String register(Tracker tracker) {
        return tracker.register();
    }

    public static String registerAndStoreBindingName(Tracker tracker,
            String pathToTrackersPropertiesFile) {
        String bindingName = TrackerImpl.register(tracker);
        new TrackersProperties(pathToTrackersPropertiesFile).store(tracker
                .getAssociatedNetworkName(), bindingName, tracker.getType().toString());
        return bindingName;
    }

}
