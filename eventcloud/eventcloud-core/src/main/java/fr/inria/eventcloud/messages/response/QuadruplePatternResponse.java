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
package fr.inria.eventcloud.messages.response;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.messages.request.StatelessQuadruplePatternRequest;

/**
 * Response associated to {@link StatelessQuadruplePatternRequest}.
 * 
 * @author lpellegr
 */
public class QuadruplePatternResponse extends
        StatefulQuadruplePatternResponse<List<Quadruple>> {

    private static final long serialVersionUID = 160L;

    public QuadruplePatternResponse() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<Quadruple> merge(List<SerializedValue<List<Quadruple>>> intermediateResults) {
        List<Quadruple> result = new ArrayList<Quadruple>();

        for (SerializedValue<List<Quadruple>> intermediateResult : intermediateResults) {
            result.addAll(intermediateResult.getValue());
        }

        return result;
    }

}
