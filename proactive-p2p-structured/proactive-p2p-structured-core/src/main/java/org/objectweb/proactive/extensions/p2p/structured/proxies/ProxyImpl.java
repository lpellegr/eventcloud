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
package org.objectweb.proactive.extensions.p2p.structured.proxies;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.p2p.structured.AbstractComponent;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;

/**
 * This concrete implementation maintains a list of the peer stubs which are
 * used to send a request. This copy is periodically updated from the trackers.
 * 
 * @author lpellegr
 */
public class ProxyImpl extends AbstractComponent implements Closeable, Proxy {

    private List<? extends Tracker> trackers;

    private List<Peer> peerStubs;

    // timer that updates peer stubs periodically
    private ScheduledExecutorService updateStubsService;

    protected ProxyImpl(List<? extends Tracker> trackers) {
        this.trackers = trackers;
        this.peerStubs = new ArrayList<Peer>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendv(Request<?> request) throws DispatchException {
        this.sendv(request, this.selectPeer());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendv(Request<?> request, Peer peer) throws DispatchException {
        if (request.getResponseProvider() != null) {
            throw new IllegalArgumentException(
                    "Response provider specified for a request with no answer");
        }

        try {
            peer.sendv(request);
        } catch (ProActiveRuntimeException e) {
            this.evictPeer(peer);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<?> send(final Request<?> request) throws DispatchException {
        return this.send(request, this.selectPeer());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<?> send(final Request<?> request, Peer peer)
            throws DispatchException {
        if (request.getResponseProvider() == null) {
            throw new IllegalArgumentException(
                    "Impossible to send a request with response without any response provider");
        }

        try {
            return peer.send(request);
        } catch (ProActiveRuntimeException e) {
            this.evictPeer(peer);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Peer selectPeer() {
        synchronized (this.peerStubs) {
            if (this.peerStubs.isEmpty()) {
                this.peerStubs.addAll(this.selectTracker().getPeers());

                if (this.updateStubsService == null) {
                    this.updateStubsService =
                            Executors.newSingleThreadScheduledExecutor();
                    this.updateStubsService.scheduleAtFixedRate(new Runnable() {

                        @Override
                        public void run() {
                            ProxyImpl.this.peerStubs.clear();
                            ProxyImpl.this.peerStubs.addAll(ProxyImpl.this.selectTracker()
                                    .getPeers());
                        }
                    }, 600, 600, TimeUnit.SECONDS);
                }
            }

            if (this.peerStubs.isEmpty()) {
                return null;
            }

            return this.peerStubs.get(RandomUtils.nextInt(this.peerStubs.size()));
        }
    }

    private Tracker selectTracker() {
        return this.trackers.get(RandomUtils.nextInt(this.trackers.size()));
    }

    private synchronized void evictPeer(Peer peer) {
        synchronized (this.peerStubs) {
            this.peerStubs.remove(peer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (this.updateStubsService != null) {
            this.updateStubsService.shutdownNow();
        }
    }

}
