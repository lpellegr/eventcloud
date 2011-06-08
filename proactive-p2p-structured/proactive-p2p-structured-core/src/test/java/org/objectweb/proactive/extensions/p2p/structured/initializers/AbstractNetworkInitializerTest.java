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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.initializers;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * An abstract network initializer that may be used by the tests that have to
 * initialize a p2p network with the same number of peers before each call to a
 * Junit method marked as {@code @Test}.
 * 
 * @author lpellegr
 */
public class AbstractNetworkInitializerTest {

    private NetworkInitializer initializer;

    private int nbPeers;

    public AbstractNetworkInitializerTest(NetworkInitializer initializer,
            int nbPeers) {
        this.initializer = initializer;
        this.nbPeers = nbPeers;
    }

    @Before
    public void setUp() {
        this.initializer.setUp(nbPeers);
    }

    @After
    public void tearDown() {
        // this.initializer.tearDown();
    }

    public Peer get(int index) {
        return this.initializer.get(index);
    }

    public Peer getc(int index) {
        return this.initializer.getc(index);
    }

    public Peer selectPeer() {
        return this.initializer.selectPeer();
    }

    public Peer selectComponentPeer() {
        return this.initializer.selectComponentPeer();
    }

    public List<Peer> getPeers() {
        return this.initializer.getPeers();
    }

    public List<Peer> getComponentPeers() {
        return this.initializer.getComponentPeers();
    }

}
