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
package fr.inria.eventcloud.deployment.cli.commands;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.factories.ProxyFactory;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.MulticastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import com.beust.jcommander.Parameter;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.deployment.cli.CommandLineReader;
import fr.inria.eventcloud.deployment.cli.converters.EventCloudIdConverter;
import fr.inria.eventcloud.messages.request.StatefulQuadruplePatternRequest;
import fr.inria.eventcloud.messages.response.StatefulQuadruplePatternResponse;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * This command is used to list subscriptions enabled for a given EC.
 * 
 * @author lpellegr
 */
public class ListSubscriptionsCommand extends Command<EventCloudsRegistry> {

    @Parameter(names = {"--stream-url"}, description = "Stream URL", converter = EventCloudIdConverter.class, required = true)
    private EventCloudId id;

    public ListSubscriptionsCommand() {
        super("list-subscriptions", "List subscriptions");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final CommandLineReader<EventCloudsRegistry> reader,
                        EventCloudsRegistry registry) {
        if (registry.contains(this.id)) {
            Proxy proxy = ProxyFactory.newProxy(registry.findTrackers(this.id));

            ListSubscriptionsResponse response =
                    (ListSubscriptionsResponse) PAFuture.getFutureValue(proxy.send(new ListSubscriptionsRequest()));

            int nbSubscriptions = response.getResult().size();

            if (nbSubscriptions > 0) {
                System.out.println(nbSubscriptions + " subscription"
                        + (nbSubscriptions > 1
                                ? "s" : "") + " found:");

                for (SubscriptionInformation si : response.getResult().values()) {
                    System.out.println("  - Id=" + si.id + ", creationTime="
                            + si.ctime + ", indexationTime=" + si.itime
                            + ", subscriber=" + si.subscriber + ", type="
                            + si.type + ", query='" + si.query + "'");
                }
            } else {
                System.out.println("No subscription found");
            }
        } else {
            System.out.println("EventCloud identified by stream URL '"
                    + this.id.getStreamUrl() + "' does not exist");
        }
    }

    private static class ListSubscriptionsRequest
            extends
            StatefulQuadruplePatternRequest<Map<String, SubscriptionInformation>> {

        private static final long serialVersionUID = 160L;

        public ListSubscriptionsRequest() {
            super(
                    QuadruplePattern.ANY,
                    new ResponseProvider<ListSubscriptionsResponse, Point<SemanticCoordinate>>() {
                        private static final long serialVersionUID = 160L;

                        @Override
                        public ListSubscriptionsResponse get() {
                            return new ListSubscriptionsResponse();
                        }
                    });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<String, SubscriptionInformation> onPeerValidatingKeyConstraints(CanOverlay<SemanticCoordinate> overlay,
                                                                                   MulticastRequest<SemanticCoordinate> request,
                                                                                   QuadruplePattern quadruplePattern) {
            TransactionalDatasetGraph txnGraph =
                    ((SemanticCanOverlay) overlay).getSubscriptionsDatastore()
                            .begin(AccessMode.READ_ONLY);

            try {
                QueryExecution qExec =
                        QueryExecutionFactory.create(
                                "SELECT ?sid ?ctime ?itime ?type ?subscriber ?query WHERE  { GRAPH ?sid { ?sid <"
                                        + PublishSubscribeConstants.SUBSCRIPTION_ID_PROPERTY
                                        + "> ?id . "
                                        + "?sid <"
                                        + PublishSubscribeConstants.SUBSCRIPTION_ORIGINAL_ID_PROPERTY
                                        + "> ?sid . "
                                        + "?sid <"
                                        + PublishSubscribeConstants.SUBSCRIPTION_CREATION_DATETIME_PROPERTY
                                        + "> ?ctime . "
                                        + "?sid <"
                                        + PublishSubscribeConstants.SUBSCRIPTION_INDEXATION_DATETIME_PROPERTY
                                        + "> ?itime . "
                                        + "?sid <"
                                        + PublishSubscribeConstants.SUBSCRIPTION_TYPE_PROPERTY
                                        + "> ?type . "
                                        + "?sid <"
                                        + PublishSubscribeConstants.SUBSCRIPTION_SUBSCRIBER_PROPERTY
                                        + "> ?subscriber . "
                                        + "?sid <"
                                        + PublishSubscribeConstants.SUBSCRIPTION_SPARQL_QUERY_PROPERTY
                                        + "> ?query . " + "}}",
                                txnGraph.getUnderlyingDataset());

                ResultSet rs = qExec.execSelect();

                try {
                    Map<String, SubscriptionInformation> result =
                            new HashMap<String, SubscriptionInformation>();

                    while (rs.hasNext()) {
                        QuerySolution solution = rs.next();

                        String oid = solution.get("sid").toString();
                        String ctime = solution.get("ctime").toString();
                        String itime = solution.get("itime").toString();
                        String type = solution.get("type").toString();
                        String subscriber =
                                solution.get("subscriber").toString();
                        String query = solution.get("query").toString();

                        result.put(oid, new SubscriptionInformation(
                                oid, ctime, itime, type, subscriber, query));
                    }

                    return result;
                } finally {
                    qExec.close();
                }
            } catch (Exception e) {
                // keep trace of the error but return a result to avoid the
                // overall system to crash
                e.printStackTrace();
                return new HashMap<String, SubscriptionInformation>(0);
            } finally {
                txnGraph.end();
            }
        }

    }

    private static class ListSubscriptionsResponse
            extends
            StatefulQuadruplePatternResponse<Map<String, SubscriptionInformation>> {

        private static final long serialVersionUID = 160L;

        public ListSubscriptionsResponse() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized Map<String, SubscriptionInformation> merge(List<SerializedValue<Map<String, SubscriptionInformation>>> intermediateResults) {
            Map<String, SubscriptionInformation> result =
                    new HashMap<String, SubscriptionInformation>();

            for (SerializedValue<Map<String, SubscriptionInformation>> intermediateResult : intermediateResults) {
                result.putAll(intermediateResult.getValue());
            }

            return result;
        }

    }

    private static class SubscriptionInformation implements Serializable {

        private static final long serialVersionUID = 160L;

        public String id;

        public String ctime;

        public String itime;

        public String type;

        public String subscriber;

        public String query;

        public SubscriptionInformation(String id, String ctime, String itime,
                String type, String subscriber, String query) {
            this.id = id;
            this.ctime = ctime;
            this.itime = itime;
            this.type = type;
            this.subscriber = subscriber;
            this.query = query;
        }

    }

}
