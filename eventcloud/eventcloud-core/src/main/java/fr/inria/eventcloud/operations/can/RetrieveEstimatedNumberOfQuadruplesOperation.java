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
package fr.inria.eventcloud.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.GenericResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

import fr.inria.eventcloud.datastore.stats.StatsRecorder;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Returns the number of quadruples memorised by the {@link StatsRecorder} of
 * the specified datastore instance.
 * 
 * @author lpellegr
 */
public class RetrieveEstimatedNumberOfQuadruplesOperation extends
        CallableOperation {

    private static final long serialVersionUID = 160L;

    private final boolean useSubscriptionDatastore;

    public RetrieveEstimatedNumberOfQuadruplesOperation() {
        this.useSubscriptionDatastore = false;
    }

    public RetrieveEstimatedNumberOfQuadruplesOperation(
            boolean useSubscriptionDatastore) {
        this.useSubscriptionDatastore = useSubscriptionDatastore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperation handle(StructuredOverlay overlay) {

        SemanticCanOverlay canOverlay = (SemanticCanOverlay) overlay;
        StatsRecorder statsRecorder;

        if (this.useSubscriptionDatastore) {
            statsRecorder =
                    canOverlay.getSubscriptionsDatastore().getStatsRecorder();
        } else {
            statsRecorder = canOverlay.getMiscDatastore().getStatsRecorder();
        }

        return new GenericResponseOperation<Long>(
                statsRecorder.getNbQuadruples());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWithRouting() {
        return true;
    }

}
