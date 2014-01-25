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
package fr.inria.eventcloud.benchmarks.load_balancing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.execution.RequestExecutor;

/**
 * Class in charge of collecting benchmark statistics. Also, it allows to create
 * a synchronization point to wait for a specific condition.
 * 
 * @author lpellegr
 */
@DefineGroups({
        @Group(name = "notify", selfCompatible = false),
        @Group(name = "wait", selfCompatible = false)})
@DefineRules({@Compatible(value = {"notify", "wait"})})
public class BenchmarkStatsCollector implements InitActive, RunActive {

    private int totalExpected;

    private Map<OverlayId, Long> results;

    private RequestExecutor requestExecutor;

    private boolean conditionSatisfied;

    public BenchmarkStatsCollector() {
        this.conditionSatisfied = false;
    }

    public BenchmarkStatsCollector(int totalExpected) {
        this.totalExpected = totalExpected;
    }

    public boolean clear() {
        this.results.clear();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initActivity(Body body) {
        this.results = new HashMap<OverlayId, Long>();
    }

    @MemberOf("notify")
    public long report(OverlayId overlayId, long nbQuadruples) {
        this.results.put(overlayId, nbQuadruples);

        if (this.conditionSatisfied()) {
            this.conditionSatisfied = true;

            synchronized (this.results) {
                this.results.notifyAll();
            }
        }

        return nbQuadruples;
    }

    @MemberOf("wait")
    public void wait(int timeout) throws TimeoutException {
        if (!this.conditionSatisfied()) {
            synchronized (this.results) {
                while (!this.conditionSatisfied()) {
                    this.requestExecutor.incrementExtraActiveRequestCount(1);
                    try {
                        this.results.wait(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        this.requestExecutor.decrementExtraActiveRequestCount(1);
                    }

                    if (!this.conditionSatisfied) {
                        break;
                    }
                }
            }
        } else {
            this.conditionSatisfied = true;
        }

        if (!this.conditionSatisfied) {
            throw new TimeoutException("Notified about "
                    + this.totalNumberOfQuadruples()
                    + " quadruple(s) stored after " + timeout + " ms whereas "
                    + this.totalExpected + " were expected.");
        }
    }

    private boolean conditionSatisfied() {
        return this.totalNumberOfQuadruples() == this.totalExpected;
    }

    private int totalNumberOfQuadruples() {
        int sum = 0;

        for (long i : this.results.values()) {
            sum += i;
        }

        return sum;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runActivity(Body body) {
        MultiActiveService service = new MultiActiveService(body);
        this.requestExecutor =
                ((RequestExecutor) service.getServingController());

        service.multiActiveServing(30, false, false);
    }

    public Map<OverlayId, Long> getResults() {
        return this.results;
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
