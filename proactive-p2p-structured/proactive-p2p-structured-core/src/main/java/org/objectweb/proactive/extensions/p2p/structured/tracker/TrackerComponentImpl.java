package org.objectweb.proactive.extensions.p2p.structured.tracker;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.objectweb.proactive.core.component.body.ComponentRunActive;
import org.objectweb.proactive.extensions.p2p.structured.api.TrackerFactory;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
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
 * Warning, this class must not be instantiate directly. It is the implementation
 * of a component tracker. In order to create a new component tracker you must use
 * the {@link TrackerFactory}.
 * 
 * @author bsauvan
 */
public class TrackerComponentImpl extends TrackerImpl implements Tracker, ComponentInitActive, ComponentRunActive, ComponentEndActive, Serializable {

    private static final long serialVersionUID = 1L;

    static {
        TrackerComponentImpl.logger = LoggerFactory.getLogger(TrackerComponentImpl.class);
    }

    /**
     * Constructor.
     */
    public TrackerComponentImpl() {
    }

    /**
     * {@inheritDoc}
     */
    public void initComponentActivity(Body body) {
        super.initActivity(body);
        this.stub = null;
    }

    /**
     * {@inheritDoc}
     */
    public void runComponentActivity(Body body) {
        super.runActivity(body);
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
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAssociatedNetworkName(String networkName) {
        if (this.associatedNetworkName == null) {
            this.associatedNetworkName = networkName;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStub() {
        try {
            Component tracker = Fractive.getComponentRepresentativeOnThis();
            this.stub = (Tracker) tracker.getFcInterface(P2PStructuredProperties.TRACKER_SERVICES_ITF.getValue());
        } catch (ClassCastException e) {
        } catch (NullPointerException npe) {
        } catch (NoSuchInterfaceException nsie) {
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setType(OverlayType type) {
        if (this.type == null) {
            this.type = type;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String register() {
        String bindingName = null;
        try {
            bindingName = Fractive.registerByName(Fractive.getComponentRepresentativeOnThis(),
                    this.getBindingName());
        } catch (ProActiveException pe) {
            pe.printStackTrace();
        }

        return bindingName;
    }

}
