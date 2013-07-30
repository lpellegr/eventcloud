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
package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;

/**
 * This operation is used to performed the join introduce phase: it aks to the
 * landmark peer the overlay information that should be affected to him and
 * updates the neighbors .
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 * 
 * @see CanOverlay#join(Peer)
 * @see CanOverlay#handleJoinIntroduceOperation(JoinIntroduceOperation)
 */
public class JoinIntroduceOperation<E extends Element> extends
        CallableOperation {

    private static final long serialVersionUID = 160L;

    private final OverlayId peerID;

    private final Peer remotePeer;

    public JoinIntroduceOperation(OverlayId peerID, Peer remotePeer) {
        super();
        this.peerID = peerID;
        this.remotePeer = remotePeer;
    }

    public OverlayId getPeerID() {
        return this.peerID;
    }

    public Peer getRemotePeer() {
        return this.remotePeer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        return ((CanOverlay<E>) overlay).handleJoinIntroduceOperation(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isJoinOperation() {
        return true;
    };

}
