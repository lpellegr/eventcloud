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
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.operations.mutual_exclusion.RicartAgrawalaReply;
import org.objectweb.proactive.extensions.p2p.structured.operations.mutual_exclusion.RicartAgrawalaRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.MaintenanceId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Distributed mutual exclusion manager based on the Ricart-Agrawala algorithm.
 * 
 * http://dl.acm.org/citation.cfm?doid=358527.358537
 * 
 * @author lpellegr
 */
public class RicartAgrawalaManager implements MutualExclusionManager {

    private static final Logger log =
            LoggerFactory.getLogger(RicartAgrawalaManager.class);

    // egrep "Requesting|Waiting|Entering|Received|CS reply|Releasing|Critical"

    private final StructuredOverlay overlay;

    private boolean requestingCS;

    private long sequenceNumber;

    private long highestSequenceNumber;

    private int nbRepliesMissing;

    private List<Peer> repliesDeferred;

    private final RicartAgrawalaReply deferredReply;

    private final RicartAgrawalaReply nonDeferredReply;

    private boolean deferred;

    public RicartAgrawalaManager(StructuredOverlay overlay) {
        this.requestingCS = false;
        this.sequenceNumber = 0;
        this.highestSequenceNumber = 0;
        this.nbRepliesMissing = 0;

        this.deferred = false;
        this.deferredReply = new RicartAgrawalaReply(true);
        this.nonDeferredReply = new RicartAgrawalaReply(false);
        this.overlay = overlay;
        this.repliesDeferred = new ArrayList<Peer>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requestCriticalSection(Collection<Peer> processes,
                                          MaintenanceId maintenanceId) {
        log.trace(
                "Requesting critical section on {} with {} processes {}",
                this.overlay.getId(), processes.size(), processes);

        synchronized (this) {
            this.requestingCS = true;
            this.sequenceNumber = this.highestSequenceNumber + 1;
        }

        this.nbRepliesMissing = processes.size();

        for (Peer process : processes) {
            process.receive(new RicartAgrawalaRequest(
                    maintenanceId, this.overlay.getStub(),
                    this.overlay.getId(), this.sequenceNumber));
        }

        this.overlay.incrementExtraActiveRequestCount(1);

        log.trace(
                "Waiting for {} replies on {}", this.nbRepliesMissing,
                this.overlay.getId());

        synchronized (this) {
            while (this.nbRepliesMissing != 0) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        this.overlay.decrementExtraActiveRequestCount(1);

        log.trace("Entering critical section on {}", this.overlay.getId());

        return !this.deferred;
    }

    public void receiveRequest(RicartAgrawalaRequest request) {
        long k = request.getRequesterSequenceNumber();

        log.trace(
                "Received CS request on {}, requestingCS={}, highestSN={}, requesterSN={}, requesterID={}",
                this.overlay.getId(), this.requestingCS,
                this.highestSequenceNumber, k, request.getRequesterId());

        this.highestSequenceNumber = Math.max(this.highestSequenceNumber, k);

        // deferReply will be true if we have priority over node identified by
        // request.getRequesterId()
        boolean deferReply;

        synchronized (this) {
            deferReply =
                    this.requestingCS
                            && ((k > this.sequenceNumber) || (k == this.sequenceNumber && request.getRequesterId()
                                    .compareTo(this.overlay.getId()) > 0));
        }

        if (deferReply) {
            this.repliesDeferred.add(request.getRequester());
            log.trace(
                    "CS reply deferred on {} for requesterSN={} and requesterID={}",
                    this.overlay.getId(), k, request.getRequesterId());
        } else {
            request.getRequester().receive(this.nonDeferredReply);
            log.trace(
                    "CS reply sent from {} to {} for requesterSN={} and requesterID={}",
                    this.overlay.getId(), request.getRequesterId(), k,
                    request.getRequesterId());
        }
    }

    public void receiveReply(RicartAgrawalaReply reply) {
        log.trace("Received CS reply on {}", this.overlay.getId());
        synchronized (this) {
            this.nbRepliesMissing--;
            this.deferred |= reply.wasDeferred();

            if (this.nbRepliesMissing == 0) {
                log.trace(
                        "All replies received for CS on {}",
                        this.overlay.getId());

                this.notifyAll();
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseCriticalSection() {
        log.trace("Releasing critical section on {}", this.overlay.getId());

        this.requestingCS = false;

        for (Peer requester : this.repliesDeferred) {
            requester.receive(this.deferredReply);
        }
        this.repliesDeferred.clear();

        log.trace("Critical section released on {}", this.overlay.getId());
    }

}
