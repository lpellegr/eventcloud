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
package org.objectweb.proactive.extensions.p2p.structured.initializers;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

/**
 * Network initializer that provides a concrete implementation of
 * {@link #createActivePeer()} and {@link #createComponentPeer()} for a
 * Content-Adressable Network. The CAN network that is created by this
 * initializer is a 3-dimensional CAN network.
 * 
 * @author lpellegr
 */
public class CanNetworkInitializer extends NetworkInitializer {

    public CanNetworkInitializer() {
        super();
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Peer createActivePeer() {
        return PeerFactory.newActivePeer(new CanOverlay());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Peer createComponentPeer() {
        return PeerFactory.newComponentPeer(new CanOverlay());
    }

}
