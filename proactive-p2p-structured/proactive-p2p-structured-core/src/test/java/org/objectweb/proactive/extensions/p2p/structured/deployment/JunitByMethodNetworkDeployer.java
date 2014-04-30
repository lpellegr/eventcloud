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

import org.junit.After;
import org.objectweb.proactive.extensions.p2p.structured.factories.ProxyFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

/**
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
public abstract class JunitByMethodNetworkDeployer {

    private final NetworkDeployer deployer;

    /**
     * 
     * @param deployer
     *            the network deployer that is used to initialize and to deploy
     *            a network. This dependency is injected automatically by Junit
     *            at runtime.
     */
    public JunitByMethodNetworkDeployer(NetworkDeployer deployer) {
        this.deployer = deployer;

        JunitHelper.setTestingDeploymentConfiguration(deployer.descriptor);
    }

    @After
    public void tearDown() {
        // undeploy only if a call to deploy has been performed
        if (this.deployer.getState() == NetworkDeployerState.DEPLOYED) {
            this.deployer.undeploy();
            ProxyFactory.clear();
        }
    }

    public Peer createPeer() {
        return this.deployer.createPeer();
    }

    public void deploy(int nbPeers) {
        this.deployer.deploy(nbPeers);
    }

    public void deploy(int nbTrackers, int nbPeers) {
        this.deployer.deploy(nbTrackers, nbPeers);
    }

    public Peer getPeer(int index) {
        return this.deployer.getPeer(index);
    }

    public Peer getRandomPeer() {
        return this.deployer.getRandomPeer();
    }

    public Tracker getRandomTracker() {
        return this.deployer.getRandomTracker();
    }

}
