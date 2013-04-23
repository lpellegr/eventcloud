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
package fr.inria.eventcloud.deployment;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassNetworkDeployer;

import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * An {@link EventCloudDeployer} configured for testing and decorated by a
 * {@link JunitByClassNetworkDeployer}.
 * 
 * @author lpellegr
 */
public class JunitByClassEventCloudDeployer extends JunitByClassNetworkDeployer {

    public JunitByClassEventCloudDeployer(int nbTrackers, int nbPeers) {
        this(new EventCloudDeploymentDescriptor(), nbTrackers, nbPeers);
    }

    public JunitByClassEventCloudDeployer(
            EventCloudDeploymentDescriptor deploymentDescriptor,
            int nbTrackers, int nbPeers) {
        super(new JunitEventCloudDeployer(deploymentDescriptor), nbTrackers,
                nbPeers);
    }

    public SemanticPeer getRandomSemanticPeer() {
        return (SemanticPeer) PAFuture.getFutureValue(super.getRandomTracker()
                .getRandomPeer());
    }

    public SemanticTracker getRandomSemanticTracker() {
        return (SemanticTracker) PAFuture.getFutureValue(super.getRandomTracker());
    }

}
