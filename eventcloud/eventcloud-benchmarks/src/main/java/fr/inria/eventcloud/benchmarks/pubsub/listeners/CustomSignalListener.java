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
package fr.inria.eventcloud.benchmarks.pubsub.listeners;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.benchmarks.pubsub.BenchmarkStatsCollector;

/**
 * Custom signal listener for pub/sub benchmark.
 * 
 * @author lpellegr
 */
public class CustomSignalListener extends SignalNotificationListener {

    private static final long serialVersionUID = 160L;

    private final NotificationManager<String> manager;

    public CustomSignalListener(BenchmarkStatsCollector collector,
            int nbEventsExpected) {
        super();

        this.manager =
                new NotificationManager<String>(collector, nbEventsExpected) {
                    private static final long serialVersionUID = 160L;

                    @Override
                    public Node getEventId(String metaGraphValue) {
                        return NodeFactory.createURI(Quadruple.removeMetaInformation(metaGraphValue));
                    }
                };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotification(SubscriptionId id, String solution) {
        this.manager.handleNewEvent(id, solution);
    }

}
