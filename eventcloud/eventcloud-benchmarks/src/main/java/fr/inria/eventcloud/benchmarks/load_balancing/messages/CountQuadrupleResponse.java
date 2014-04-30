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
package fr.inria.eventcloud.benchmarks.load_balancing.messages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import fr.inria.eventcloud.messages.response.StatefulQuadruplePatternResponse;

/**
 * Response associated to {@link CountQuadrupleResponse}.
 * 
 * @author lpellegr
 */
public class CountQuadrupleResponse extends
        StatefulQuadruplePatternResponse<Map<OverlayId, Long>> {

    private static final long serialVersionUID = 160L;

    public CountQuadrupleResponse() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Map<OverlayId, Long> merge(List<SerializedValue<Map<OverlayId, Long>>> intermediateResults) {
        Map<OverlayId, Long> result = new HashMap<OverlayId, Long>();

        for (SerializedValue<Map<OverlayId, Long>> subResult : intermediateResults) {
            result.putAll(subResult.getValue());
        }

        return result;
    }

}
