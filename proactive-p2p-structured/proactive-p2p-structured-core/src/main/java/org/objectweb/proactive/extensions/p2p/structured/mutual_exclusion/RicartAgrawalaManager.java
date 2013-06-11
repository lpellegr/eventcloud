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
package org.objectweb.proactive.extensions.p2p.structured.mutual_exclusion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.operations.mutual_exclusion.RicartAgrawalaReply;
import org.objectweb.proactive.extensions.p2p.structured.operations.mutual_exclusion.RicartAgrawalaRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Distributed mutual exclusion manager based on the Ricart Agrawala algorithm.
 * 
 * @author lpellegr
 */
public class RicartAgrawalaManager implements MutualExclusionManager {

    private static final Logger log =
            LoggerFactory.getLogger(RicartAgrawalaManager.class);

    private final StructuredOverlay overlay;

    private boolean requestingCS;

    private long localTimestamp;

    private int nbRepliesMissing;

    private List<Peer> repliesDeferred;

    private RicartAgrawalaReply genericReply = new RicartAgrawalaReply();

    public RicartAgrawalaManager(StructuredOverlay overlay) {
        this.nbRepliesMissing = 0;
        this.overlay = overlay;
        this.repliesDeferred = new ArrayList<Peer>();
        this.requestingCS = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void requestCriticalSection(Collection<Peer> processes) {
        log.trace("Requesting critical section on {}", this.overlay.getId());

        this.requestingCS = true;
        this.nbRepliesMissing = processes.size();
        this.localTimestamp = System.currentTimeMillis();

        for (Peer process : processes) {
            process.receive(new RicartAgrawalaRequest(
                    this.overlay.getStub(), this.localTimestamp));
        }

        this.overlay.incrementExtraActiveRequestCount(1);

        log.trace(
                "  waiting for {} replies on {}", this.nbRepliesMissing,
                this.overlay.getId());
        while (this.nbRepliesMissing != 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.overlay.decrementExtraActiveRequestCount(1);
    }

    public synchronized void receiveRequest(Peer requestSource, long timestamp) {
        log.trace(
                "Received CS request on {}, requestingCS={}, timestampReceived={}, localTimestamp={}",
                this.overlay.getId(), this.requestingCS, timestamp,
                this.localTimestamp);

        if (!this.requestingCS) {
            requestSource.receive(this.getGenericReply());
            log.trace("  replying CASE 1");
        } else if (this.requestingCS && timestamp < this.localTimestamp) {
            requestSource.receive(this.getGenericReply());
            log.trace("  replying CASE 2");
        } else if (this.requestingCS && timestamp == this.localTimestamp
                && requestSource.getId().compareTo(this.overlay.getId()) < 0) {
            requestSource.receive(this.getGenericReply());
            log.trace("  replying CASE 3");
        } else {
            this.repliesDeferred.add(requestSource);
            log.trace("  reply deferred");
        }
    }

    public synchronized void receiveReply() {
        log.trace("Received CS reply on {}", this.overlay.getId());

        if (this.nbRepliesMissing > 0) {
            this.nbRepliesMissing--;
            if (this.nbRepliesMissing == 0) {
                log.trace("  all expected replies received, entering critical section");
                this.notifyAll();
            } else {
                log.trace("  waiting for {} more reply", this.nbRepliesMissing);
            }
        } else {
            log.trace("  no reply expected");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void releaseCriticalSection() {
        log.trace("Releasing critical section on {}", this.overlay.getId());
        this.requestingCS = false;

        Iterator<Peer> it = this.repliesDeferred.iterator();
        while (it.hasNext()) {
            Peer peer = it.next();
            peer.receive(this.getGenericReply());
            it.remove();
        }
    }

    /*
     * This method does not required to be synchronized since all the methods 
     * that use it are already synchronized on the current object.
     */
    public RicartAgrawalaReply getGenericReply() {
        if (this.genericReply == null) {
            this.genericReply = new RicartAgrawalaReply();
        }

        return this.genericReply;
    }

}
