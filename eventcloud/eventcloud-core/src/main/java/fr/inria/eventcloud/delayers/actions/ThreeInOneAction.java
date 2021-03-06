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
package fr.inria.eventcloud.delayers.actions;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.delayers.buffers.Buffer;
import fr.inria.eventcloud.delayers.buffers.ThreeInOneBuffer;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * 
 * 
 * @author lpellegr
 */
public final class ThreeInOneAction extends Action<Object> {

    private final QuadrupleAction quadrupleAction;

    private final SubscriptionAction subscriptionAction;

    private final CompoundEventAction compoundEventAction;

    public ThreeInOneAction(SemanticCanOverlay overlay, int threadPoolSize) {
        super(overlay, threadPoolSize);

        this.quadrupleAction = new QuadrupleAction(overlay, threadPoolSize);
        this.subscriptionAction =
                new SubscriptionAction(overlay, threadPoolSize);

        if (EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
            this.compoundEventAction =
                    new CompoundEventAction(overlay, threadPoolSize);
        } else {
            this.compoundEventAction = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void perform(Buffer<Object> buffer) {
        ThreeInOneBuffer buf = (ThreeInOneBuffer) buffer;

        if (!buf.getQuadrupleBuffer().isEmpty()) {
            this.quadrupleAction.perform(buf.getQuadrupleBuffer());
        }

        if (!buf.getSubscriptionBuffer().isEmpty()) {
            this.subscriptionAction.perform(buf.getSubscriptionBuffer());
        }

        if (this.compoundEventAction != null
                && !buf.getCompoundEventBuffer().isEmpty()) {
            this.compoundEventAction.perform(buf.getCompoundEventBuffer());
        }
    }

}
