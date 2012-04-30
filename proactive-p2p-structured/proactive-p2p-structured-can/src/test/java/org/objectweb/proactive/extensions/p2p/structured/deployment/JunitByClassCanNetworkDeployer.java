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

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;

/**
 * All the tests contained by a test case that extends this class are runned by
 * using a {@link CanNetworkDeployer}. This class differs from
 * {@link JunitByMethodNetworkDeployer} by offering an implementation of a setup
 * and teardown method that is executed automatically before and after each
 * test. In the setup method, a network is deployed according to the number of
 * trackers and peers which have been specified into the class constructor
 * whereas in the teardown method the network which has been previously deployed
 * is destroyed.
 * <p>
 * The purpose of this class is to be used when you have to deploy a network
 * with the same number of trackers and peers for several methods which are
 * under test into a same class.
 * 
 * @author lpellegr
 */
public class JunitByClassCanNetworkDeployer extends JunitByClassNetworkDeployer {

    /**
     * Creates a Junit deployer with given number of trackers and peers but also
     * with the specified {@link DeploymentConfiguration} and
     * {@code overlayProvider}.
     * 
     * @param nbTrackers
     *            the number of trackers to instantiate.
     * @param nbPeers
     *            the number of peers to deploy.
     * @param configuration
     *            the deployment configuration to use.
     * @param overlayProvider
     *            the overlay provider to use in order to create peers.
     */
    public JunitByClassCanNetworkDeployer(

    int nbTrackers, int nbPeers, DeploymentConfiguration configuration,
            SerializableProvider<? extends CanOverlay> overlayProvider) {
        super(new CanNetworkDeployer(configuration, overlayProvider),
                nbTrackers, nbPeers);
    }

    /**
     * Creates a Junit deployer with the given number of trackers and peers but
     * also with the specified {@code overlayProvider}.
     * 
     * @param nbTrackers
     *            the number of trackers to instantiate.
     * @param nbPeers
     *            the number of peers to deploy.
     * @param overlayProvider
     *            the overlay provider to use in order to create peers.
     */
    public JunitByClassCanNetworkDeployer(int nbTrackers, int nbPeers,
            SerializableProvider<? extends CanOverlay> overlayProvider) {
        super(new CanNetworkDeployer(
                new TestingDeploymentConfiguration(), overlayProvider),
                nbTrackers, nbPeers);
    }

    /**
     * Creates a Junit deployer with the given number of trackers and peers but
     * also with the specified {@link DeploymentConfiguration}.
     * 
     * @param nbTrackers
     *            the number of trackers to instantiate.
     * @param nbPeers
     *            the number of peers to deploy.
     * @param configuration
     *            the deployment configuration to use.
     */
    public JunitByClassCanNetworkDeployer(int nbTrackers, int nbPeers,
            DeploymentConfiguration configuration) {
        super(new CanNetworkDeployer(configuration), nbTrackers, nbPeers);
    }

    /**
     * Creates a Junit deployer with the specified number of trackers and peers.
     * It uses an instance of {@link TestingDeploymentConfiguration} for the
     * deployment configuration.
     * 
     * @param nbTrackers
     *            the number of trackers to instantiate.
     * @param nbPeers
     *            the number of peers to deploy.
     */
    public JunitByClassCanNetworkDeployer(int nbTrackers, int nbPeers) {
        super(new CanNetworkDeployer(new TestingDeploymentConfiguration()),
                nbTrackers, nbPeers);
    }

    /**
     * Creates a Junit deployer with one tracker and the specified number of
     * peers. It uses an instance of {@link TestingDeploymentConfiguration} for
     * the deployment configuration.
     * 
     * @param nbPeers
     *            the number of peers to deploy.
     */
    public JunitByClassCanNetworkDeployer(int nbPeers) {
        this(1, nbPeers);
    }

}
