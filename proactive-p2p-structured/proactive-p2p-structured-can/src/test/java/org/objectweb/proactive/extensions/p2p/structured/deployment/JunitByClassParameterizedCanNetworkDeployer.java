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

import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * All the tests contained by a test case that extends this class are run two
 * times. The first time by using a {@link CanActiveObjectsNetworkDeployer} and
 * the second time by using a {@link CanComponentsNetworkDeployer}. This class
 * differs from {@link JunitByMethodNetworkDeployer} by offering an
 * implementation of a setup and teardown method that is executed automatically
 * before and after each test. In the setup method, a network is deployed
 * according to the number of trackers and peers which have been specified into
 * the class constructor whereas in the teardown method the network which has
 * been previously deployed is destroyed.
 * <p>
 * The purpose of this class is to be used when you have to deploy a network
 * with a the same number of trackers and peers for several methods which are
 * under test into a same class.
 * 
 * @author lpellegr
 */
@RunWith(Parameterized.class)
public class JunitByClassParameterizedCanNetworkDeployer extends
        JunitByClassNetworkDeployer {

    public JunitByClassParameterizedCanNetworkDeployer(
            NetworkDeployer deployer, int nbPeers) {
        this(deployer, 1, nbPeers);
    }

    public JunitByClassParameterizedCanNetworkDeployer(
            NetworkDeployer deployer, int nbTrackers, int nbPeers) {
        super(deployer, nbTrackers, nbPeers);
    }

    /**
     * Defines the parameters that are injected at runtime to the class
     * constructor that extends this class. Each item from the list which is
     * returned triggers the instantiation of a new Junit test case.
     * 
     * @return the parameters that are injected at runtime to the class
     *         constructor that extends this class.
     */
    @Parameterized.Parameters
    public static List<NetworkDeployer[]> deployers() {
        return JunitParameterizedCanNetworkDeployer.getDeployersToParameterize();
    }

}
