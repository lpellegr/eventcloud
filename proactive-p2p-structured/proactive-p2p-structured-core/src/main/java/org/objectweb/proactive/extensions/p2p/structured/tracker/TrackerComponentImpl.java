/**
 * Copyright (c) 2011 INRIA.
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
