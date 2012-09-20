/**
 * Copyright (c) 2011-2012 INRIA.
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
package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;

/**
 * This operation is used to performed the join welcome phase: it consists to
 * acknowledge the landmark peer that the data from the introduce phase have
 * been received and set. Therefore, it is time to the landmark node to update
 * its information.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 * 
 * @see CanOverlay#join(Peer)
 * @see CanOverlay#handleJoinWelcomeMessage(JoinWelcomeOperation)
 */
public class JoinWelcomeOperation<E extends Element> implements
        CallableOperation {

    private static final long serialVersionUID = 1L;

    public JoinWelcomeOperation() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        return ((CanOverlay<E>) overlay).handleJoinWelcomeMessage(this);
    }

}
