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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

/**
 * This operation is used to performed the join welcome phase: it consists to
 * acknowledge the landmark peer that the data from the introduce phase have
 * been received and set. Therefore, it is time to the landmark node to update
 * its information.
 * 
 * @author lpellegr
 * 
 * @see CanOverlay#join(Peer)
 * @see CanOverlay#handleJoinWelcomeMessage(JoinWelcomeOperation)
 */
public class JoinWelcomeOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    public JoinWelcomeOperation() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        return ((CanOverlay) overlay).handleJoinWelcomeMessage(this);
    }

}
