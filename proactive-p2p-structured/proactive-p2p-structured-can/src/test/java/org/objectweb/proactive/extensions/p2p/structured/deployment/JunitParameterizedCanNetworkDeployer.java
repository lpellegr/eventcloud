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

import java.util.Arrays;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

/**
 * This class is used to parameterize the Junit tests that have to be tested
 * with the instantiation of {@link Tracker}s and {@link Peer}s as Active
 * Objects and Components.
 * 
 * @author lpellegr
 */
public final class JunitParameterizedCanNetworkDeployer {

    /**
     * Defines the parameters that are injected at runtime to the class
     * constructor that extends this class. Each item from the list which is
     * returned triggers the instantiation of a new Junit test case.
     * 
     * @return the parameters that are injected at runtime to the class
     *         constructor that extends this class.
     */
    public static List<NetworkDeployer[]> getDeployersToParameterize() {
        return Arrays.asList(new NetworkDeployer[][] {
                {new CanActiveObjectsNetworkDeployer(
                        new TestingDeploymentConfiguration())},
                {new CanComponentsNetworkDeployer(
                        new TestingDeploymentConfiguration())}});
    }

}
