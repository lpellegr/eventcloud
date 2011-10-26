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
package org.objectweb.proactive.extensions.p2p.structured.deployment;

import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.factories.TrackerFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

/**
 * Class used to deploy a Content-Addressable Network where each {@link Peer}
 * and {@link Tracker} is initialized as an Active Object.
 * 
 * @author lpellegr
 */
public final class CanActiveObjectsNetworkDeployer extends NetworkDeployer {

    private static final long serialVersionUID = 1L;

    public CanActiveObjectsNetworkDeployer() {
        super();
    }

    public CanActiveObjectsNetworkDeployer(DeploymentConfiguration configuration) {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Peer createPeer(NodeProvider nodeProvider) {
        // TODO use the node provider parameter
        return PeerFactory.newActivePeer(SerializableProvider.create(CanOverlay.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Tracker createTracker(String networkName,
                                    NodeProvider nodeProvider) {
        // TODO use the node provider parameter
        return TrackerFactory.newActiveTracker(networkName);
    }

}
