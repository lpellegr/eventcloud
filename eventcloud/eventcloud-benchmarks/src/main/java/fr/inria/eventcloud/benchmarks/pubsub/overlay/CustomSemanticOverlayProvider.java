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
package fr.inria.eventcloud.benchmarks.pubsub.overlay;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;

import fr.inria.eventcloud.benchmarks.pubsub.BenchmarkStatsCollector;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.delayers.Observer;
import fr.inria.eventcloud.delayers.buffers.Buffer;
import fr.inria.eventcloud.delayers.buffers.ExtendedCompoundEvent;
import fr.inria.eventcloud.delayers.buffers.ThreeInOneBuffer;
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
            result.getPublishSubscribeOperationsDelayer().register(
                    new DelayerObserver(this.statsCollectorURL));
        }

        return result;
    }

    private static class DelayerObserver implements Observer<Object> {

        protected final BenchmarkStatsCollector collector;

        public DelayerObserver(String benchmarkStatsCollectorURL) {
            this.collector =
                    this.lookupBenchmarkStatsCollector(benchmarkStatsCollectorURL);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void bufferFlushed(Buffer<Object> buffer,
                                  SemanticCanOverlay overlay) {
            CustomSemanticOverlay customOverlay =
                    ((CustomSemanticOverlay) overlay);
            ThreeInOneBuffer buf = (ThreeInOneBuffer) buffer;

            if (buf.getCompoundEventBuffer() != null
                    && !buf.getCompoundEventBuffer().isEmpty()) {
                customOverlay.publicationsStorageEndTime =
                        System.currentTimeMillis();

                int size = 0;

                for (ExtendedCompoundEvent ce : buf.getCompoundEventBuffer()) {
                    size += ce.compoundEvent.size();
                }

                this.collector.reportNbQuadrupleStored(
                        customOverlay.getId(), size);
            }

            if (!buf.getQuadrupleBuffer().isEmpty()) {
                customOverlay.publicationsStorageEndTime =
                        System.currentTimeMillis();

                this.collector.reportNbQuadrupleStored(
                        customOverlay.getId(), buf.getQuadrupleBuffer().size());
            }

            if (!buf.getSubscriptionBuffer().isEmpty()) {
                customOverlay.subscriptionsStorageEndTime =
                        System.currentTimeMillis();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionTriggered(Buffer<Object> buffer,
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

}
