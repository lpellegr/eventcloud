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
package org.objectweb.proactive.extensions.p2p.structured.factories;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;

/**
 * This class is used to cache references to trackers and peers for multiple
 * proxies created on the same JVM. It ensures that all proxies created from a
 * same JVM use initially the same peer as entry point in the P2P network.
 * However, the property no longer holds once a peer is detected has failed.
 * 
 * TODO: ensure that all proxies share the same peer entry point under all
 * circumstances.
 * 
 * @author lpellegr
 * 
 * @see ProxyFactory
 */
public class ProxyCache implements Serializable {

    private static final long serialVersionUID = 160L;

    private long creationTime;

    private List<? extends Tracker> trackers;

    private List<? extends Peer> peers;

    private Peer selectedPeer;

    public ProxyCache(List<? extends Tracker> trackers) {
        this.trackers = trackers;
        this.peers = this.selectTracker().getPeers();

        this.selectedPeer =
                this.peers.get((int) (this.creationTime % this.peers.size()));

        if (!P2PStructuredProperties.PROXY_CACHE_RANDOM_SELECTION.getValue()) {
            this.creationTime = System.currentTimeMillis();
        }
    }

    public Peer selectPeer() {
        Peer result;

        if (P2PStructuredProperties.PROXY_CACHE_RANDOM_SELECTION.getValue()) {
            result = this.randomPeer();
        } else {
            synchronized (this) {
                result = this.selectedPeer;
            }
        }

        if (result == null) {
            throw new IllegalStateException("No peer available from trackers");
        }

        return result;
    }

    public void invalidate(Peer peer) {
        synchronized (this) {
            this.peers.remove(peer);
            // TODO: peer reference should also be removed from trackers
            // or a probing mechanism must be implemented at the tracker level

            if (this.peers.isEmpty()) {
                this.peers = this.selectTracker().getPeers();
            }

            this.selectedPeer =
                    this.peers.get((int) (this.creationTime % this.peers.size()));
        }
    }

    private Tracker selectTracker() {
        return this.trackers.get(RandomUtils.nextInt(this.trackers.size()));
    }

    private synchronized Peer randomPeer() {
        int nbPeers = this.peers.size();

        if (nbPeers == 0) {
            return null;
        }

        return this.peers.get(RandomUtils.nextInt(nbPeers));
    }

}
