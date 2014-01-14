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
package fr.inria.eventcloud.benchmarks.pubsub.proxies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.benchmarks.pubsub.BenchmarkStatsCollector;
import fr.inria.eventcloud.proxies.PublishProxyImpl;

/**
 * Custom publish proxy for benchmark purposes.
 * 
 * @author lpellegr
 */
public class CustomPublishProxyImpl extends PublishProxyImpl implements
        CustomPublishProxy {

    public static final String PUBLISH_PROXY_ADL =
            "fr.inria.eventcloud.benchmarks.pubsub.proxies.CustomPublishProxy";

    private Map<String, Long> pointToPointEntryMeasurements;

    private List<Event> events;

    private BenchmarkStatsCollector collector;

    private int waitPeriodBetweenPublications;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean assignEvents(Event[] events) {
        boolean result = true;

        for (Event event : events) {
            result &= this.events.add(event);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish() {
        // this method should be called only once
        this.pointToPointEntryMeasurements =
                new HashMap<String, Long>(this.events.size());

        if (!this.events.isEmpty()) {
            if (this.events.get(0) instanceof CompoundEvent) {
                Iterator<Event> it = this.events.iterator();

                while (it.hasNext()) {
                    CompoundEvent ce = (CompoundEvent) it.next();

                    this.pointToPointEntryMeasurements.put(ce.getGraph()
                            .getURI(), System.currentTimeMillis());

                    super.publish(ce);

                    if (it.hasNext()) {
                        this.forceWaitingPeriod();
                    }
                }
            } else {
                Iterator<Event> it = this.events.iterator();

                while (it.hasNext()) {
                    Quadruple q = (Quadruple) it.next();

                    this.pointToPointEntryMeasurements.put(q.getGraph()
                            .getURI(), System.currentTimeMillis());

                    super.publish(q);

                    if (it.hasNext()) {
                        this.forceWaitingPeriod();
                    }
                }
            }
        }

        this.collector.reportMeasurements(this.pointToPointEntryMeasurements);
    }

    private void forceWaitingPeriod() {
        if (this.waitPeriodBetweenPublications > 0) {
            try {
                Thread.sleep(this.waitPeriodBetweenPublications);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean clear() {
        this.pointToPointEntryMeasurements.clear();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        super.initComponentActivity(body);

        this.events = new ArrayList<Event>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean init(String collectorURL, int waitPeriodBetweenPublications) {
        try {
            this.collector =
                    PAActiveObject.lookupActive(
                            BenchmarkStatsCollector.class, collectorURL);
            this.waitPeriodBetweenPublications = waitPeriodBetweenPublications;

            return true;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
