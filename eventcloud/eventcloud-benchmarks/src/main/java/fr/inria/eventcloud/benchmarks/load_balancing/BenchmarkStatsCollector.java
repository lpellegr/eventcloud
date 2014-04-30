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
package fr.inria.eventcloud.benchmarks.load_balancing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.execution.RequestExecutor;

/**
 * Class in charge of collecting benchmark statistics. Also, it allows to create
 * a synchronization point to wait for a specific condition.
 * 
 * @author lpellegr
 */
@DefineGroups({
        @Group(name = "parallel", selfCompatible = true),
        @Group(name = "notify", selfCompatible = true),
        @Group(name = "wait", selfCompatible = false)})
@DefineRules({
        @Compatible(value = {"notify", "wait"}),
        @Compatible(value = {"parallel", "wait"}),
        @Compatible(value = {"parallel", "notify"})})
public class BenchmarkStatsCollector implements InitActive, RunActive {

    private int poolSize;

    private int nbJoinAcknowledged;

    private int nbJoinIntroduceAcknowledged;

    private RequestExecutor requestExecutor;

    private boolean conditionSatisfied;

    private List<Peer> peers;

    public enum ReportType {
        JOIN, JOIN_INTRODUCE
    };

    public BenchmarkStatsCollector() {
        this.conditionSatisfied = false;
    }

    public BenchmarkStatsCollector(int poolSize) {
        this.poolSize = poolSize;
    }

    public synchronized boolean clear() {
        this.nbJoinAcknowledged = 0;
        this.nbJoinIntroduceAcknowledged = 0;
        this.peers.clear();
        return true;
    }

    @MemberOf("parallel")
    public void register(Peer peer) {
        this.peers.add(peer);
    }

    @MemberOf("parallel")
    public List<Peer> getPeers() {
        return this.peers;
    }

    @MemberOf("notify")
    public synchronized boolean report(OverlayId overlayId, ReportType type) {
        if (type == ReportType.JOIN) {
            this.incrementNbJoinAcknowledged();
        } else if (type == ReportType.JOIN_INTRODUCE) {
            this.incrementNbJoinIntroduceAcknowledged();
        } else {
            throw new IllegalArgumentException("Unknow report type: " + type);
        }

        if (this.conditionSatisfied()) {
            this.conditionSatisfied = true;
            this.notifyAll();
        }

        return this.conditionSatisfied;
    }

    @MemberOf("wait")
    public synchronized void waitCondition(int timeout) throws TimeoutException {
        if (!this.conditionSatisfied()) {
            while (!this.conditionSatisfied()) {
                this.requestExecutor.incrementExtraActiveRequestCount(1);
                try {
                    this.wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    this.requestExecutor.decrementExtraActiveRequestCount(1);
                }

                if (!this.conditionSatisfied) {
                    break;
                }
            }
        } else {
            this.conditionSatisfied = true;
        }

        if (!this.conditionSatisfied) {
            throw new TimeoutException("Notified about "
                    + this.nbJoinAcknowledged + " join(s) and "
                    + this.nbJoinIntroduceAcknowledged
                    + " joinIntroduce(s) after " + timeout + " ms whereas "
                    + this.poolSize + " were expected.");
        }
    }

    private boolean conditionSatisfied() {
        return this.nbJoinAcknowledged == this.poolSize
                && this.nbJoinIntroduceAcknowledged == this.poolSize;
    }

    private void incrementNbJoinAcknowledged() {
        this.nbJoinAcknowledged++;
    }

    private void incrementNbJoinIntroduceAcknowledged() {
        this.nbJoinIntroduceAcknowledged++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initActivity(Body body) {
        this.peers = Collections.synchronizedList(new ArrayList<Peer>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runActivity(Body body) {
        MultiActiveService service = new MultiActiveService(body);
        this.requestExecutor =
                ((RequestExecutor) service.getServingController());

        service.multiActiveServing(100, false, false);
    }

    public static BenchmarkStatsCollector lookup(String URL) {
        try {
            return PAActiveObject.lookupActive(
                    BenchmarkStatsCollector.class, URL);
        } catch (ActiveObjectCreationException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
