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
 * All the tests contained by a test case that extends this class are executed
 * two times. The first time by using a {@link CanActiveObjectsNetworkDeployer}
 * and the second time by using a {@link CanComponentsNetworkDeployer}.
 * <p>
 * This class does not provide any setup method that automatically deploy the
 * network. This means that the calls to
 * {@link JunitByMethodNetworkDeployer#deploy(int)} and has to be performed
 * manually into the method which needs to use a p2p network. After each method
 * which has execute the deploy method, an undeploy operation is automatically
 * performed. For a class that provides a setup and teardown method annotated
 * respectively with the Junit {@code @Before} and {@code @After} annotations
 * and that deploy and undeploy the network with the same parameters (i.e.
 * number of trackers and peers) you can have a look to
 * {@link JunitByClassNetworkDeployer}.
 * <p>
 * The purpose of this class is to be used when you have to deploy a network
 * with different number of trackers and peers for several methods which are
 * under test into a same class.
 * 
 * @author lpellegr
 */
@RunWith(Parameterized.class)
public class JunitByMethodParameterizedCanNetworkDeployer extends
        JunitByMethodNetworkDeployer {

    public JunitByMethodParameterizedCanNetworkDeployer(NetworkDeployer deployer) {
        super(deployer);
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
