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
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.benchmarks.pubsub.BenchmarkStatsCollector;

/**
 * Custom binding listener for pub/sub benchmark.
 * 
 * @author lpellegr
 */
public class CustomBindingListener extends BindingNotificationListener {

    private static final long serialVersionUID = 151L;

    private NotificationManager<Binding> manager;

    public CustomBindingListener(BenchmarkStatsCollector collector,
            int nbEventsExpected) {
        super();

        this.manager =
                new NotificationManager<Binding>(collector, nbEventsExpected) {
                    private static final long serialVersionUID = 151L;

                    @Override
                    public Node getEventId(Binding solution) {
                        return Quadruple.removeMetaInformation(solution.get(PublishSubscribeConstants.GRAPH_VAR));
                    }
                };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotification(SubscriptionId id, Binding solution) {
        this.manager.handleNewEvent(id, solution);
    }

}
