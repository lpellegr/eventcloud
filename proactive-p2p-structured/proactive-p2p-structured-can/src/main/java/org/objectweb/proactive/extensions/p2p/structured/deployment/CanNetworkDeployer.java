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
package org.objectweb.proactive.extensions.p2p.structured.deployment;

import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.factories.TrackerFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

/**
 * Class used to deploy a Content-Addressable Network where each {@link Peer}
 * and {@link Tracker} is initialized as a Component.
 * 
 * @author lpellegr
 */
public final class CanNetworkDeployer extends NetworkDeployer {

    private static final long serialVersionUID = 140L;

    public CanNetworkDeployer(DeploymentDescriptor deploymentDescriptor) {
        super(deploymentDescriptor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Peer createPeer() {
        // TODO add support for deployment thanks to the node provider
        return PeerFactory.newPeer(super.descriptor.getOverlayProvider());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Tracker createTracker(String networkName) {
        // TODO add support for deployment thanks to the node provider
        return TrackerFactory.newTracker(networkName);
    }

}
