package org.objectweb.proactive.extensions.p2p.structured.tracker;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;

/**
 * Extends {@link TrackerComponentImpl} to provide a component implementation.
 * 
 * @author bsauvan
 */
public class TrackerComponentImpl extends TrackerImpl implements Tracker,
        ComponentInitActive, ComponentEndActive {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public TrackerComponentImpl() {
    }

    /**
     * {@inheritDoc}
     */
    public void initComponentActivity(Body body) {
        // /!\ do not call super.initActivity(body)
        // in this method or reset the stub variable
        // to null because the call to initActivity will
        // initialize the stub variable with the reference
        // to the ProActive stub whereas the remote reference
        // must be a component stub!
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
    public String register() {
        try {
            super.bindingName =
                    Fractive.registerByName(
                            Fractive.getComponentRepresentativeOnThis(),
                            super.getBindingNameSuffix());
        } catch (ProActiveException pe) {
            pe.printStackTrace();
        }
        
        return super.bindingName;
    }

}
