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

/**
 * Defines the different states that may occurs when a {@link NetworkDeployer}
 * is used.
 * 
 * @author lpellegr
 */
public enum NetworkDeployerState {

    /**
     * State associated to {@link NetworkDeployer} that is waiting for a deploy
     * operation.
     */
    STANDBY,
    /**
     * As soon as a call to a deploy operation is performed, the state is
     * changed to this value.
     */
    DEPLOYING,
    /**
     * This state value means that the deployer has deployed a network.
     */
    DEPLOYED,
    /**
     * The deployer is performing an undeploy operation.
     */
    UNDEPLOYING;

}
