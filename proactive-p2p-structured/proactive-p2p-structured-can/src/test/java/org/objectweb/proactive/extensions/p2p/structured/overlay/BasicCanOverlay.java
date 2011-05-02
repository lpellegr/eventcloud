package org.objectweb.proactive.extensions.p2p.structured.overlay;

import org.objectweb.proactive.extensions.p2p.structured.operations.can.MergeOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanRequestResponseManager;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;

/**
 * A basic implementation of {@link AbstractCanOverlay} which an empty behavior
 * for the operations related to the management of the data.
 * 
 * @author lpellegr
 */
public class BasicCanOverlay extends AbstractCanOverlay {

    private static final long serialVersionUID = 1L;

    public BasicCanOverlay() {
        super(new CanRequestResponseManager() {
            private static final long serialVersionUID = 1L;
        });
    }

    @Override
    protected void affectDataReceived(Object dataReceived) {
    }

    @Override
    protected void mergeDataReceived(MergeOperation msg) {
    }

    @Override
    protected Object retrieveAllData() {
        return null;
    }

    @Override
    protected Object getDataIn(Zone zone) {
        return null;
    }

    @Override
    protected void removeDataIn(Zone zone) {
    }

}