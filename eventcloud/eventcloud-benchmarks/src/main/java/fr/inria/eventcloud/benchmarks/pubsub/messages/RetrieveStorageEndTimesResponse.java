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
package fr.inria.eventcloud.benchmarks.pubsub.messages;

import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import fr.inria.eventcloud.benchmarks.pubsub.StorageTimes;
import fr.inria.eventcloud.messages.response.can.StatefulQuadruplePatternResponse;

/**
 * Response associated to {@link RetrieveStorageEndTimesRequest}.
 * 
 * @author lpellegr
 */
public class RetrieveStorageEndTimesResponse extends
        StatefulQuadruplePatternResponse<StorageTimes> {

    private static final long serialVersionUID = 150L;

    public RetrieveStorageEndTimesResponse() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized StorageTimes merge(List<SerializedValue<StorageTimes>> intermediateResults) {
        StorageTimes result = null;

        for (SerializedValue<StorageTimes> subResult : intermediateResults) {
            StorageTimes i = subResult.getValue();

            if (result == null) {
                result = subResult.getValue();
                continue;
            }

            if (i.getPublicationsEndTime() > result.getPublicationsEndTime()) {
                result.setPublicationsEndTime(i.getPublicationsEndTime());
            }

            if (i.getSubscriptionsEndTime() > result.getSubscriptionsEndTime()) {
                result.setSubscriptionsEndTime(i.getSubscriptionsEndTime());
            }
        }

        return result;
    }

}
