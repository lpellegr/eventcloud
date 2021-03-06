/**
 * Copyright (c) 2011-2014 INRIA.
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
package org.objectweb.proactive.extensions.p2p.structured.deployment;

/**
 * Defines the different states that may be listened during the deployment of a
 * structured p2p network by using a {@link NetworkDeployer}.
 * 
 * @author lpellegr
 */
public interface NetworkDeployerListener {

    void deploymentStarted();

    void deployingTrackers();

    void trackersDeployed();

    void injectingPeers();

    void peersInjected();

    void deploymentEnded();

    void undeploymentStarted();

    void undeploymentEnded();

}
