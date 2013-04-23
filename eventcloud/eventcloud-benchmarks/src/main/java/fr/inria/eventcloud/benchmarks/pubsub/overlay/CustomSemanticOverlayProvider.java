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

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.delayers.CustomBuffer;
import fr.inria.eventcloud.delayers.Observer;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.providers.SemanticOverlayProvider;

public class CustomSemanticOverlayProvider extends SemanticOverlayProvider {

    private static final long serialVersionUID = 140L;

    private final boolean markStorageEndTime;

    public CustomSemanticOverlayProvider(boolean inMemory,
            boolean markStorageEndTime) {
        super(inMemory);

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
            Observer<CustomBuffer> observer = new Observer<CustomBuffer>() {
                @Override
                public void bufferFlushed(CustomBuffer buffer,
                                          SemanticCanOverlay overlay) {
                    CustomSemanticOverlay customOverlay =
                            ((CustomSemanticOverlay) overlay);

                    int compoundEventsBufferSize = 0;
                    if (EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
                        compoundEventsBufferSize =
                                buffer.getCompoundEvents().size();
                    }

                    if (buffer.getQuadruples().size() > 0
                            || compoundEventsBufferSize > 0) {
                        customOverlay.publicationsStorageEndTime =
                                System.currentTimeMillis();
                    }

                    if (buffer.getSubscriptions().size() > 0) {
                        customOverlay.subscriptionsStorageEndTime =
                                System.currentTimeMillis();
                    }
                }

                @Override
                public void postActionTriggered(CustomBuffer buffer,
                                                SemanticCanOverlay overlay) {
                    // do nothing
                }
            };

            result.getPublishSubscribeOperationsDelayer()
                    .getQuadruplesOperator()
                    .register(observer);
            result.getPublishSubscribeOperationsDelayer()
                    .getSubscriptionsOperator()
                    .register(observer);

            if (EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
                result.getPublishSubscribeOperationsDelayer()
                        .getCompoundEventsOperator()
                        .register(observer);
            }
        }

        return result;
    }
}
