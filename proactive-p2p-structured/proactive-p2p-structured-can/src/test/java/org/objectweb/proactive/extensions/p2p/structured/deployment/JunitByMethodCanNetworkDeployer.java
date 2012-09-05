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

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;

/**
 * All the tests contained by a test case that extends this class are executed
 * by using a {@link CanNetworkDeployer}.
 * <p>
 * This class does not provide any setup method that automatically deploy the
 * network. This means that the calls to
 * {@link JunitByMethodNetworkDeployer#deploy(int)} and has to be performed
 * manually into the method which needs to use a p2p network. After each method
 * which has execute the deploy method, an undeploy operation is automatically
 * performed. For a class that provides a setup and teardown method annotated
 * respectively with the Junit {@code @Before} and {@code @After} annotations
 * and that deploys and undeploys the network with the same parameters (i.e.
 * number of trackers and peers) you can have a look to
 * {@link JunitByClassNetworkDeployer}.
 * <p>
 * The purpose of this class is to be used when you have to deploy a network
 * with different number of trackers and peers for several methods which are
 * under test into a same class.
 * 
 * @author lpellegr
 */
public class JunitByMethodCanNetworkDeployer<E extends Element> extends
        JunitByMethodNetworkDeployer {

    /**
     * Creates a new Junit deployer with the specified deployment configuration.
     * No {@link InjectionConstraints} for is specified.
     * 
     * @param deploymentDescriptor
     *            the deployment descriptor to use.
     */
    public JunitByMethodCanNetworkDeployer(
            CanDeploymentDescriptor<E> deploymentDescriptor) {
        super(new CanNetworkDeployer(deploymentDescriptor));
    }

}
