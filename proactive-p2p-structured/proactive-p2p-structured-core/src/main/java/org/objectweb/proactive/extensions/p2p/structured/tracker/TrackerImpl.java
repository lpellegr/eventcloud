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
package org.objectweb.proactive.extensions.p2p.structured.tracker;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.p2p.structured.AbstractComponent;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PeerNotActivatedException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of {@link Tracker} for components.
 * 
 * @author lpellegr
 * @author bsauvan
 */
@DefineGroups({
        @org.objectweb.proactive.annotation.multiactivity.Group(name = "parallel", selfCompatible = true),
        @org.objectweb.proactive.annotation.multiactivity.Group(name = "peers", selfCompatible = false)})
@DefineRules({@Compatible(value = {"peers", "parallel"})})
public class TrackerImpl extends AbstractComponent implements Tracker,
        TrackerAttributeController, ComponentEndActive {

    private static final long serialVersionUID = 150L;

    /**
     * ADL name of the tracker component.
     */
    public static final String TRACKER_ADL =
            "org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker";

    /**
     * Functional interface name of the tracker component.
     */
    public static final String TRACKER_SERVICES_ITF = "tracker-services";

    /**
     * GCM Virtual Node name of the tracker component.
     */
    public static final String TRACKER_VN = "TrackerVN";

    private static Logger logger = LoggerFactory.getLogger(TrackerImpl.class);

    protected transient String bindingName;

    private transient String bindingNameSuffix;

    private UUID id;

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
     * Empty constructor required by ProActive.
     */
    public TrackerImpl() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        super.initComponentActivity(body);

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
     * {@inheritDoc}
     */
    @Override
    public void runComponentActivity(Body body) {
        new MultiActiveService(body).multiActiveServing(
                P2PStructuredProperties.MAO_SOFT_LIMIT_TRACKERS.getValue(),
                false, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endComponentActivity(Body body) {
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
    public void setAttributes(Tracker stub, String networkName) {
        if (this.stub == null) {
            this.stub = stub;
            this.networkName = networkName;
        }
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
        Peer peerToJoin = null;

        if (this.type != null) {
            peerToJoin = this.getRandomPeer();
        }

        try {
            this.inject(remotePeer, peerToJoin);
        } catch (PeerNotActivatedException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void inject(Peer remotePeer, Peer landmarkPeer)
            throws NetworkAlreadyJoinedException, PeerNotActivatedException {
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

            remotePeer.join(landmarkPeer);

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
    @MemberOf("peers")
    public void storePeer(Peer peerReference) {
        this.peers.add(peerReference);
        PAGroup.waitAll(this.untypedGroupView.internalStorePeer(peerReference));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("peers")
    public void removePeer(Peer peerReference) {
        this.internalRemovePeer(peerReference);
        PAGroup.waitAll(this.untypedGroupView.internalRemovePeer(peerReference));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("peers")
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
    @MemberOf("peers")
    public BooleanWrapper internalRemovePeer(Peer peerReference) {
        return new BooleanWrapper(this.peers.remove(peerReference));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public BooleanWrapper internalAddTracker(Tracker trackerReference) {
        return new BooleanWrapper(this.typedGroupView.add(trackerReference));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public BooleanWrapper internalRemoveTracker(Tracker trackerReference) {
        return new BooleanWrapper(this.typedGroupView.remove(trackerReference));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public String register() {
        try {
            this.bindingName =
                    Fractive.registerByName(
                            Fractive.getComponentRepresentativeOnThis(),
                            this.getBindingNameSuffix());
        } catch (ProActiveException pe) {
            pe.printStackTrace();
        }

        return this.bindingName;
    }

    @MemberOf("parallel")
    public synchronized String getBindingNameSuffix() {
        if (this.bindingNameSuffix == null) {
            StringBuffer appender = new StringBuffer("tracker/");
            appender.append(this.networkName);
            appender.append('/');
            appender.append(this.id.toString());
            this.bindingNameSuffix = appender.toString();
        }

        return this.bindingNameSuffix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public UUID getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public String getNetworkName() {
        return this.networkName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("peers")
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
    @MemberOf("peers")
    public List<Peer> getPeers() {
        return this.peers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("peers")
    public Peer getRandomPeer() {
        return this.peers.get(RandomUtils.nextInt(this.peers.size()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public OverlayType getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
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
