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
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
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
 * @author lpellegr
 */
public class Tracker implements InitActive, EndActive, RunActive, Serializable {

    private static final long serialVersionUID = 1L;

    private static final transient Logger logger = LoggerFactory.getLogger(Tracker.class);

    private double probabilityToStorePeer = P2PStructuredProperties.TRACKER_STORAGE_PROBABILITY.getValue();

    private String name = UUID.randomUUID().toString();

    private String associatedNetworkName;

    private Tracker trackersGroup;

    /**
     * The remote peers list that the tracker maintains.
     */
    private List<Peer> storedPeers = new ArrayList<Peer>(100);

    private boolean isInGroup = false;

    private Tracker stub;

    /**
     * The type of peers this tracker works.
     */
    private OverlayType type;

    /**
     * Constructor.
     */
    public Tracker() {
    }

    public Tracker(double probabilityToStorePeer) {
        this.probabilityToStorePeer = probabilityToStorePeer;
    }

    /**
     * Constructs a new Tracker by specifying the type of the tracker.
     * 
     * @param type
     *            the type of tracker (i.e. the kind of peers that can be add on
     *            the network).
     */
    public Tracker(OverlayType type) {
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
    public Tracker(OverlayType type, String associatedNetworkName) {
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
    public Tracker(OverlayType type, String associatedNetworkName, String trackerName) {
        this.type = type;
        this.associatedNetworkName = associatedNetworkName;
        this.name = trackerName;
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
    public Tracker(OverlayType type, double probabilityToStorePeer) {
        this(probabilityToStorePeer);
        this.type = type;
    }

    /**
     * Join an existing tracker in order to duplicate peers references.
     * 
     * @param remoteTracker
     *            the tracker to use for the operation.
     * @return <code>true</code> if the operation has succeeded,
     *         <code>false</code> otherwise.
     */
    public boolean join(Tracker remoteTracker) {
        Group<Tracker> newTrackersReferences = remoteTracker.getGroup();
        Group<Tracker> trackersGroup = PAGroup.getGroup(this.trackersGroup);

        boolean result = true;
        result &= trackersGroup.add(remoteTracker);
        trackersGroup.addAll(newTrackersReferences);

        BooleanWrapper resultGroup = this.trackersGroup.addTrackerToGroup(
                                        (Tracker) PAActiveObject.getStubOnThis());
        while (PAGroup.size(resultGroup) > 0) {
            result &= ((BooleanWrapper) PAGroup.waitAndGetOneThenRemoveIt(
                                               resultGroup)).getBooleanValue();
        }

        if (!this.isInGroup) {
            this.isInGroup = true;
        }

        return result;
    }

    public BooleanWrapper addTrackerToGroup(Tracker remoteReference) {
        return new BooleanWrapper(PAGroup.getGroup(this.trackersGroup).add(remoteReference));
    }

    public Group<Tracker> getGroup() {
        if (!this.isInGroup) {
            this.isInGroup = true;
        }
        return PAGroup.getGroup(this.trackersGroup);
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
     * Add on the network that the tracker manages, the given peer.
     * 
     * @param remotePeer
     *            the peer to add on the network.
     * @throws StructuredP2PException 
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
                logger.info("A new peer has created a network");
            }
        } else {
			try {
				Peer peerToJoin = this.getLandmarkPeerToJoin();
				
				// try to join until the operation succeeds (the operation
				// can fail if a concurrent join is detected).
				while (!remotePeer.join(peerToJoin));
				
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

    public void storePeer(Peer peerReference) {
        this.storedPeers.add(peerReference);
        PAGroup.waitAll(this.trackersGroup._storePeer(peerReference));
    }

    public void removePeer(Peer peerReference) {
        this._removePeer(peerReference);
        PAGroup.waitAll(this.trackersGroup._removePeer(peerReference));
    }

    public BooleanWrapper _storePeer(Peer peerReference) {
        return new BooleanWrapper(this.storedPeers.add(peerReference));
    }

    public BooleanWrapper _removePeer(Peer peerReference) {
        return new BooleanWrapper(this.storedPeers.remove(peerReference));
    }

    public String getAssociatedNetworkName() {
        return this.associatedNetworkName;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Returns the number of peers that the tracker manages.
     * 
     * @return the number of peers that the tracker manages.
     */
    public int getNbOfPeersStored() {
        return this.storedPeers.size();
    }

	/**
	 * Returns a random and valid peer from the stored peers list.
	 * 
	 * @return a random and valid peer.
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
     * Returns the {@link Peer} stored at the specified index.
     * 
     * @param index
     *            the index to use.
     * @return a {@link Peer}.
     */
    public Peer getStoredPeerAt(int index) {
        return this.storedPeers.get(index);
    }

    /**
     * Returns the stored peers.
     * 
     * @return the stored peers.
     */
    public List<Peer> getStoredPeers() {
    	for (int i=0; i<this.storedPeers.size(); i++) {
    		if (!this.checkAlertness(this.storedPeers.get(i))) {
    			this.storedPeers.remove(i);
    		}
    	}
    	
        return this.storedPeers;
    }

    public OverlayType getType() {
        return this.type;
    }

    /**
     * Returns the probability to store a {@link Peer} reference.
     * 
     * @return the probability to store a {@link Peer} reference.
     */
    public double getProbabilityToStorePeer() {
        return this.probabilityToStorePeer;
    }

    public void setProbabilityToStorePeer(double p) {
        this.probabilityToStorePeer = p;
    }

    public void initActivity(Body body) {
        this.stub = (Tracker) PAActiveObject.getStubOnThis();
        try {
            this.trackersGroup = (Tracker) PAGroup.newGroup(Tracker.class.getCanonicalName());
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

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

    public BooleanWrapper notifyLeave(Tracker trackerReference) {
        return new BooleanWrapper(PAGroup.getGroup(this.trackersGroup).remove(trackerReference));
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            service.blockingServeOldest();
        }
    }

    public String getBindingName() {
        return (!this.associatedNetworkName.equals("default") ? "/"
                + this.associatedNetworkName + "/" : "")
                + this.name;
    }

    public String register() {
        String bindingName = null;
        try {
            bindingName = PAActiveObject.registerByName(
                    PAActiveObject.getStubOnThis(), this.getBindingName());
        } catch (Exception e) {
            e.printStackTrace();
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
        String bindName = null;
        try {
            bindName = PAActiveObject.registerByName(
                    tracker, tracker.getBindingName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bindName;
    }

    public static String registerAndStoreBindingName(Tracker tracker,
            String pathToTrackersPropertiesFile) {
        String bindingName = Tracker.register(tracker);
        new TrackersProperties(pathToTrackersPropertiesFile).store(tracker
                .getAssociatedNetworkName(), bindingName, tracker.getType().toString());
        return bindingName;
    }

}
