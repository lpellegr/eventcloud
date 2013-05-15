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
package fr.inria.eventcloud.delayers;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * A delayer class in charge of buffering and delaying publish/subscribe
 * operations such as publish and subscribe requests. It concerns publish
 * requests made with SBCE1, 2 and 3 but also subscriptions and rewritten
 * subscriptions generated by SBCE1 and 2.
 * 
 * @author lpellegr
 */
public class PublishSubscribeOperationsDelayer extends
        Delayer<Object, CustomBuffer> {

    private static final Logger log =
            LoggerFactory.getLogger(PublishSubscribeOperationsDelayer.class);

    private final IndexSubscriptionRequestOperator subscriptionsOperator;

    private final PublishQuadrupleRequestOperator quadruplesOperator;

    private final PublishCompoundEventRequestOperator compoundEventsOperator;

    public PublishSubscribeOperationsDelayer(SemanticCanOverlay overlay) {
        super(
                overlay,
                log,
                "findQuadruplesAndOrSubscriptionsMatching",
                "quadruplesAndOrSubscriptions",
                EventCloudProperties.PUBLISH_SUBSCRIBE_OPERATIONS_DELAYER_BUFFER_SIZE.getValue(),
                EventCloudProperties.PUBLISH_SUBSCRIBE_OPERATIONS_DELAYER_TIMEOUT.getValue());

        this.subscriptionsOperator =
                new IndexSubscriptionRequestOperator(overlay);
        this.quadruplesOperator = new PublishQuadrupleRequestOperator(overlay);

        if (EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
            this.compoundEventsOperator =
                    new PublishCompoundEventRequestOperator(overlay);
        } else {
            this.compoundEventsOperator = null;
        }
    }

    public void receive(ExtendedCompoundEvent event) {
        synchronized (super.buffer) {
            this.buffer.add(event);

            this.commitOrCreateCommitThread();
        }
    }

    public void receive(Quadruple q) {
        synchronized (super.buffer) {
            this.buffer.add(q);

            this.commitOrCreateCommitThread();
        }
    }

    public void receive(Subscription s) {
        synchronized (super.buffer) {
            this.buffer.add(s);

            this.commitOrCreateCommitThread();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int commit() {
        synchronized (this.buffer) {
            int size = this.buffer.size();

            Stopwatch flushBufferStopwatch = null;
            Stopwatch triggerActionStopwatch = null;

            if (log.isTraceEnabled()) {
                flushBufferStopwatch = new Stopwatch();
                triggerActionStopwatch = new Stopwatch();
                flushBufferStopwatch.start();
            }

            if (this.isQuadruplesBufferNotEmpty()) {
                this.quadruplesOperator.flushBuffer(super.buffer);
            }

            if (this.isCompoundEventsBufferNotEmpty()) {
                this.compoundEventsOperator.flushBuffer(super.buffer);
            }

            if (log.isTraceEnabled()) {
                flushBufferStopwatch.stop();
                triggerActionStopwatch.start();
            }

            if (this.isQuadruplesBufferNotEmpty()) {
                this.quadruplesOperator.triggerAction(super.buffer);
            }

            if (this.isCompoundEventsBufferNotEmpty()) {
                this.compoundEventsOperator.triggerAction(super.buffer);
            }

            if (log.isTraceEnabled()) {
                triggerActionStopwatch.stop();
                flushBufferStopwatch.start();
            }

            if (this.isSubscriptionsBufferNotEmpty()) {
                this.subscriptionsOperator.flushBuffer(super.buffer);
            }

            if (log.isTraceEnabled()) {
                flushBufferStopwatch.stop();
                triggerActionStopwatch.start();
            }

            if (this.isSubscriptionsBufferNotEmpty()) {
                this.subscriptionsOperator.triggerAction(super.buffer);
            }

            if (log.isTraceEnabled()) {
                triggerActionStopwatch.stop();

                log.trace(
                        "Buffer flushed in {} ms on {}",
                        flushBufferStopwatch.elapsed(TimeUnit.MILLISECONDS),
                        this.overlay);
                log.trace(
                        "Fired {} in {} ms on {}", super.postActionName,
                        triggerActionStopwatch.elapsed(TimeUnit.MILLISECONDS),
                        this.overlay);
            }

            this.buffer.clear();

            return size;
        }
    }

    private boolean isCompoundEventsBufferNotEmpty() {
        return super.buffer.getExtendedCompoundEvents() != null
                ? !super.buffer.getExtendedCompoundEvents().isEmpty() : false;
    }

    private boolean isQuadruplesBufferNotEmpty() {
        return !super.buffer.getQuadruples().isEmpty();
    }

    private boolean isSubscriptionsBufferNotEmpty() {
        return !super.buffer.getSubscriptions().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void flushBuffer() {
        // do nothing since we are overriding the commit method as the flush
        // buffer operation is composed of multiple flush operations that
        // should not be executed in sequence but in turns with its associated
        // trigger action method
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void triggerAction() {
        // do nothing, see commit method above
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CustomBuffer createEmptyBuffer(int bufsize) {
        return new CustomBuffer(bufsize);
    }

    public PublishCompoundEventRequestOperator getCompoundEventsOperator() {
        return this.compoundEventsOperator;
    }

    public PublishQuadrupleRequestOperator getQuadruplesOperator() {
        return this.quadruplesOperator;
    }

    public IndexSubscriptionRequestOperator getSubscriptionsOperator() {
        return this.subscriptionsOperator;
    }

}
