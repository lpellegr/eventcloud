/**
 * Copyright (c) 2011 INRIA.
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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.messages.request.can.StatefulQuadruplePatternRequest;
import fr.inria.eventcloud.messages.request.can.StatelessQuadruplePatternRequest;

/**
 * Response associated to {@link StatelessQuadruplePatternRequest}.
 * 
 * @author lpellegr
 */
public class QuadruplePatternResponse extends
        StatefulQuadruplePatternResponse<Collection<Quadruple>> {

    private static final long serialVersionUID = 1L;

    public QuadruplePatternResponse(
            StatefulQuadruplePatternRequest<Collection<Quadruple>> request) {
        super(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> mergeSubResults(List<SerializedValue<Collection<Quadruple>>> subResults) {
        List<Quadruple> quadruples = new ArrayList<Quadruple>();

        for (SerializedValue<Collection<Quadruple>> subResult : subResults) {
            quadruples.addAll(subResult.getValue());
        }

        return new Collection<Quadruple>(quadruples);
    }

}
