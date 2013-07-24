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
package fr.inria.eventcloud.deployment.cli.commands;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.proactive.api.PAActiveObject;

import com.beust.jcommander.Parameter;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.deployment.cli.CommandLineReader;
import fr.inria.eventcloud.deployment.cli.converters.EventCloudIdConverter;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;

/**
 * This command subscribes to an EventCloud identified by the specified stream
 * URL.
 * 
 * @author lpellegr
 */
public class SubscribeEventCloudCommand extends Command<EventCloudsRegistry> {

    @Parameter(names = {"--stream-url"}, description = "Stream URL", converter = EventCloudIdConverter.class, required = true)
    private EventCloudId id;

    @Parameter(names = {"--sparql-query", "-q"}, description = "SPARQL query used to subscribe")
    private String sparqlQuery = "SELECT ?g WHERE { GRAPH ?g { ?s ?p ?o. } }";

    public SubscribeEventCloudCommand() {
        super(
                "subscribe-eventcloud",
                "Subscribes to the EventCloud identified by the specified stream URL",
                new String[] {"subscribe"});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(CommandLineReader<EventCloudsRegistry> reader,
                        EventCloudsRegistry registry) {
        if (registry.contains(this.id)) {
            try {
                SubscribeApi subscribeProxy =
                        ProxyFactory.newSubscribeProxy(
                                PAActiveObject.getUrl(registry), this.id);

                Subscription subscription = new Subscription(this.sparqlQuery);

                subscribeProxy.subscribe(
                        subscription,
                        new ConsoleCompoundEventNotificationListener());

                while (System.in.read() != 'q') {
                    System.out.println("Type 'q' to unsubscribe");
                }

                subscribeProxy.unsubscribe(subscription.getId());
            } catch (EventCloudIdNotManaged e) {
                throw new IllegalStateException(e);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            System.out.println("EventCloud identified by stream URL '"
                    + this.id.getStreamUrl() + "' does not exist");
        }
    }

    private static class ConsoleCompoundEventNotificationListener extends
            CompoundEventNotificationListener {

        private static final long serialVersionUID = 160L;

        private AtomicLong counter = new AtomicLong(1);

        @Override
        public void onNotification(SubscriptionId id, CompoundEvent solution) {
            System.out.println("Received notification #"
                    + this.counter.getAndIncrement() + ":\n" + solution);
            System.out.println();
        }
    }

}
