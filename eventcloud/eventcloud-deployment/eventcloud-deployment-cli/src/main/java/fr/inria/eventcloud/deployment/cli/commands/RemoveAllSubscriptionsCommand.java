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

import java.util.List;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.factories.ProxyFactory;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.MulticastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import com.beust.jcommander.Parameter;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
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
 * This command is used to remove all subscriptions on a given EventCloud.
 * 
 * @author lpellegr
 */
public class RemoveAllSubscriptionsCommand extends Command<EventCloudsRegistry> {

    @Parameter(names = {"--stream-url"}, description = "Stream URL", converter = EventCloudIdConverter.class, required = true)
    private EventCloudId id;

    public RemoveAllSubscriptionsCommand() {
        super("remove-all-subscriptions",
                "Remove all subscriptions (original and rewritten ones)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final CommandLineReader<EventCloudsRegistry> reader,
                        EventCloudsRegistry registry) {
        if (registry.contains(this.id)) {
            Proxy proxy = ProxyFactory.newProxy(registry.findTrackers(this.id));

            PAFuture.waitFor(proxy.send(new RemoveAllSubscriptionsRequest()));

            System.out.println("Subscriptions removed on EC '" + this.id + "'");
        } else {
            System.out.println("EventCloud identified by stream URL '"
                    + this.id.getStreamUrl() + "' does not exist");
        }
    }

    private static class RemoveAllSubscriptionsRequest extends
            StatefulQuadruplePatternRequest<Boolean> {

        private static final long serialVersionUID = 160L;

        public RemoveAllSubscriptionsRequest() {
            super(
                    QuadruplePattern.ANY,
                    new ResponseProvider<RemoveAllSubscriptionsResponse, Point<SemanticCoordinate>>() {
                        private static final long serialVersionUID = 160L;

                        @Override
                        public RemoveAllSubscriptionsResponse get() {
                            return new RemoveAllSubscriptionsResponse();
                        }
                    });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean onPeerValidatingKeyConstraints(CanOverlay<SemanticCoordinate> overlay,
                                                      MulticastRequest<SemanticCoordinate> request,
                                                      QuadruplePattern quadruplePattern) {
            TransactionalDatasetGraph txnGraph =
                    ((SemanticCanOverlay) overlay).getSubscriptionsDatastore()
                            .begin(AccessMode.WRITE);

            try {
                txnGraph.delete(QuadruplePattern.ANY);
                txnGraph.commit();
            } catch (Exception e) {
                // keep trace of the error but return a result to avoid the
                // overall system to crash
                e.printStackTrace();
                return false;
            } finally {
                txnGraph.end();
            }

            return true;
        }

    }

    private static class RemoveAllSubscriptionsResponse extends
            StatefulQuadruplePatternResponse<Boolean> {

        private static final long serialVersionUID = 160L;

        public RemoveAllSubscriptionsResponse() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized Boolean merge(List<SerializedValue<Boolean>> intermediateResults) {
            boolean result = true;

            for (SerializedValue<Boolean> intermediateResult : intermediateResults) {
                result &= intermediateResult.getValue();
            }

            return result;
        }

    }

}
