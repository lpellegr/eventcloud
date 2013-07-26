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
package fr.inria.eventcloud.benchmarks.pubsub.overlay;

import java.io.IOException;
import java.util.Set;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;

import fr.inria.eventcloud.benchmarks.pubsub.BenchmarkStatsCollector;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.delayers.CustomBuffer;
import fr.inria.eventcloud.delayers.ExtendedCompoundEvent;
import fr.inria.eventcloud.delayers.Observer;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.providers.SemanticOverlayProvider;

public class CustomSemanticOverlayProvider extends SemanticOverlayProvider {

    private static final long serialVersionUID = 160L;

    private final String statsCollectorURL;

    private final boolean markStorageEndTime;

    public CustomSemanticOverlayProvider(String statsCollectorURL,
            boolean inMemory, boolean markStorageEndTime) {
        super(inMemory);

        this.statsCollectorURL = statsCollectorURL;
        this.markStorageEndTime = markStorageEndTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CustomSemanticOverlay get() {
        TransactionalTdbDatastore[] datastores = this.createDatastores();

        CustomSemanticOverlay result =
                new CustomSemanticOverlay(
                        datastores[0], datastores[1], datastores[2]);

        if (this.markStorageEndTime) {
            result.getPublishSubscribeOperationsDelayer()
                    .getQuadruplesOperator()
                    .register(new QuadruplesObserver(this.statsCollectorURL));
            result.getPublishSubscribeOperationsDelayer()
                    .getSubscriptionsOperator()
                    .register(new SubscriptionsObserver(this.statsCollectorURL));

            if (EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
                result.getPublishSubscribeOperationsDelayer()
                        .getCompoundEventsOperator()
                        .register(
                                new CompoundEventsObserver(
                                        this.statsCollectorURL));
            }
        }

        return result;
    }

    private static abstract class OperatorObserver implements
            Observer<CustomBuffer> {

        protected final BenchmarkStatsCollector collector;

        public OperatorObserver(String benchmarkStatsCollectorURL) {
            this.collector =
                    this.lookupBenchmarkStatsCollector(benchmarkStatsCollectorURL);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void bufferFlushed(CustomBuffer buffer,
                                  SemanticCanOverlay overlay) {
            // to be overriden if required
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void postActionTriggered(CustomBuffer buffer,
                                        SemanticCanOverlay overlay) {
            // to be overriden if required
        }

        private BenchmarkStatsCollector lookupBenchmarkStatsCollector(String benchmarkStatsCollectorURL) {
            if (benchmarkStatsCollectorURL != null) {
                try {
                    return PAActiveObject.lookupActive(
                            BenchmarkStatsCollector.class,
                            benchmarkStatsCollectorURL);
                } catch (ActiveObjectCreationException e) {
                    throw new IllegalStateException(e);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }

            return null;
        }

    }

    private static class CompoundEventsObserver extends OperatorObserver {

        public CompoundEventsObserver(String benchmarkStatsCollectorURL) {
            super(benchmarkStatsCollectorURL);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void bufferFlushed(CustomBuffer buffer,
                                  SemanticCanOverlay overlay) {
            CustomSemanticOverlay customOverlay =
                    ((CustomSemanticOverlay) overlay);

            int cumulatedSize =
                    countNbCompoundEvents(buffer.getExtendedCompoundEvents());

            if (cumulatedSize > 0) {
                customOverlay.publicationsStorageEndTime =
                        System.currentTimeMillis();

                this.collector.reportNbQuadrupleStored(
                        customOverlay.getId(), cumulatedSize);
            }
        }

        private static int countNbCompoundEvents(Set<ExtendedCompoundEvent> set) {
            if (set == null) {
                return 0;
            }

            return set.size();
        }

    }

    private static class QuadruplesObserver extends OperatorObserver {

        public QuadruplesObserver(String benchmarkStatsCollectorURL) {
            super(benchmarkStatsCollectorURL);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void bufferFlushed(CustomBuffer buffer,
                                  SemanticCanOverlay overlay) {
            CustomSemanticOverlay customOverlay =
                    ((CustomSemanticOverlay) overlay);

            if (buffer.getQuadruples().size() > 0) {
                customOverlay.publicationsStorageEndTime =
                        System.currentTimeMillis();

                this.collector.reportNbQuadrupleStored(
                        customOverlay.getId(), buffer.getQuadruples().size());
            }
        }

    }

    private static class SubscriptionsObserver extends OperatorObserver {

        public SubscriptionsObserver(String benchmarkStatsCollectorURL) {
            super(benchmarkStatsCollectorURL);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void bufferFlushed(CustomBuffer buffer,
                                  SemanticCanOverlay overlay) {
            CustomSemanticOverlay customOverlay =
                    ((CustomSemanticOverlay) overlay);

            if (buffer.getSubscriptions().size() > 0) {
                customOverlay.subscriptionsStorageEndTime =
                        System.currentTimeMillis();
            }
        }

    }

}
