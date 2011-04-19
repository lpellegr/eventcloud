package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.slf4j.LoggerFactory;

/**
 * PeerComponentImpl is a component extension of a {@link PeerImpl}. It is
 * composed of a {@link StructuredOverlay} which allows to have several
 * implementations of common operations for each peer-to-peer protocol to
 * implement.
 * <p>
 * Warning, this class must not be instantiate directly. In order to create a
 * new component peer you must use the {@link PeerFactory}.
 * 
 * @author bsauvan
 */
public class PeerComponentImpl extends PeerImpl implements Peer,
        ComponentInitActive, ComponentEndActive, Serializable {

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

        // sets setOverlay as immediate service to be sure
        // that the overlay field is set even if we execute
        // an another method in immediate service on a component peer
        body.setImmediateService("setOverlay", false);

        this.stub = null;
        // try {
        // Component peer = Fractive.getComponentRepresentativeOnThis();
        // this.stub = (Peer)
        // peer.getFcInterface(P2PStructuredProperties.PEER_SERVICES_ITF.getValue());
        // } catch (NoSuchInterfaceException nsie) {
        // logger.error("Cannot get stub of peer: " + nsie.getMessage(), nsie);
        // }
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
                this.stub =
                        (Peer) peer.getFcInterface(P2PStructuredProperties.PEER_SERVICES_ITF.getValue());
            } catch (NoSuchInterfaceException nsie) {
                logger.error(
                        "Cannot get stub of peer: " + nsie.getMessage(), nsie);
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
