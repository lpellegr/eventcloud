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
package fr.inria.eventcloud.deployment.cli.commands;

import java.io.IOException;

import com.beust.jcommander.Parameter;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.deployment.cli.CommandLineReader;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * This command registers a subscription into an {@link EventCloudDescription} and print on
 * the standard output the notifications that are received.
 * 
 * @author lpellegr
 */
public class SubscribeCommand extends Command<SubscribeProxy> {

    @Parameter(names = {"-q", "--sparql-query"}, description = "The SPARQL query to use for the subscription", required = true)
    private String sparqlQuery;

    public SubscribeCommand() {
        super("subscribe",
                "Subscribe on the Event Cloud with the specified SPARQL query");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final CommandLineReader<SubscribeProxy> reader,
                        SubscribeProxy proxy) {
        Subscription subscription = new Subscription(this.sparqlQuery);

        proxy.subscribe(subscription, new BindingNotificationListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onNotification(SubscriptionId id, Binding solution) {
                try {
                    reader.getReader().println(solution.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        System.out.println("Subscription for query '" + this.sparqlQuery
                + "' has been registered with id " + subscription.getId());
    }

}
