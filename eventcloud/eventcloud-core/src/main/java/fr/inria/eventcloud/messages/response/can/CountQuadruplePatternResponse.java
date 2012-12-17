/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.messages.response.can;

import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

/**
 * Response associated to {@link CountQuadruplePatternResponse}.
 * 
 * @author lpellegr
 */
public class CountQuadruplePatternResponse extends
        StatefulQuadruplePatternResponse<Long> {

    private static final long serialVersionUID = 140L;

    public CountQuadruplePatternResponse() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Long merge(List<SerializedValue<Long>> intermediateResults) {
        long result = 0;

        for (SerializedValue<Long> subResult : intermediateResults) {
            result += subResult.getValue();
        }

        return result;
    }

}
