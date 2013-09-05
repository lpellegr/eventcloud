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
package org.objectweb.proactive.extensions.p2p.structured.deployment;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxies;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;

/**
 * This class differs from {@link JunitByMethodNetworkDeployer} by offering an
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
public abstract class JunitByClassNetworkDeployer {

    protected final NetworkDeployer deployer;

    private final int nbTrackers;

    private final int nbPeers;

    private Proxy proxy;

    public JunitByClassNetworkDeployer(NetworkDeployer deployer,
            int nbTrackers, int nbPeers) {
        this.deployer = deployer;
        this.nbTrackers = nbTrackers;
        this.nbPeers = nbPeers;

        JunitHelper.setTestingDeploymentConfiguration(deployer.descriptor);
    }

    @Before
    public void setUp() {
        this.deployer.deploy(this.nbTrackers, this.nbPeers);
        this.proxy = Proxies.newProxy(this.deployer.getTrackers());
    }

    @After
    public void tearDown() {
        this.deployer.undeploy();

        ComponentUtils.terminateComponent(this.proxy);
        this.proxy = null;
    }

    public Peer getPeer(int index) {
        return this.deployer.getPeer(index);
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public Peer getRandomPeer() {
        return this.deployer.getRandomPeer();
    }

    public Tracker getRandomTracker() {
        return this.deployer.getRandomTracker();
    }

}
