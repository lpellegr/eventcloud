package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.objectweb.proactive.core.component.body.ComponentRunActive;
import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.slf4j.LoggerFactory;

/**
 * A Peer contains all operations which are common to peer-to-peer protocols.
 * These operations are the join and leave but also the send operation in order
 * to send messages. A Peer is composed of a {@link StructuredOverlay} which
 * allows to have several implementations of common operations for each protocol
 * to implement.
 * 
 * Warning, this class must not be instantiate directly. It is the implementation
 * of a component peer. In order to create a new component peer you must use the
 * {@link PeerFactory}.
 * 
 * @author bsauvan
 */
public class PeerComponentImpl extends PeerImpl implements Peer, ComponentInitActive, ComponentRunActive, ComponentEndActive, Serializable {

    private static final long serialVersionUID = 1L;

    static {
        logger = LoggerFactory.getLogger(PeerComponentImpl.class);
    }

    /**
     * The no-argument constructor as commanded by ProActive.
     */
    public PeerComponentImpl() {
    }

    /**
     * {@inheritDoc}
     */
    public void initComponentActivity(Body body) {
        super.initActivity(body);

        this.stub = null;
        //        try {
        //            Component peer = Fractive.getComponentRepresentativeOnThis();
        //            this.stub = (Peer) peer.getFcInterface(P2PStructuredProperties.PEER_SERVICES_ITF.getValue());
        //        } catch (NoSuchInterfaceException nsie) {
        //            logger.error("Cannot get stub of peer: " + nsie.getMessage(), nsie);
        //        }
    }

    /**
     * {@inheritDoc}
     */
    public void runComponentActivity(Body body) {
        if (this.overlay != null) {
            this.overlay.runActivity(body);
        } else {
            Service service = new Service(body);
            while (body.isActive()) {
                service.serveOldest();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endComponentActivity(Body body) {
        super.endActivity(body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStub() {
        if (this.stub == null) {
            try {
                Component peer = Fractive.getComponentRepresentativeOnThis();
                this.stub = (Peer) peer.getFcInterface(P2PStructuredProperties.PEER_SERVICES_ITF
                        .getValue());
            } catch (NoSuchInterfaceException nsie) {
                logger.error("Cannot get stub of peer: " + nsie.getMessage(), nsie);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OverlayType getType() {
        // TODO Get available before startComponent
        if (this.overlay != null) {
            return this.overlay.getType();
        } else {
            return OverlayType.CAN;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setOverlay(StructuredOverlay structuredOverlay) {
        if (this.overlay == null) {
            structuredOverlay.setLocalPeer(this);
            structuredOverlay.initActivity(this.getBody());
        }
        this.overlay = structuredOverlay;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
    	return obj instanceof PeerComponentImpl
    			&& this.getId().equals(((PeerComponentImpl) obj).getId());
    }

}
