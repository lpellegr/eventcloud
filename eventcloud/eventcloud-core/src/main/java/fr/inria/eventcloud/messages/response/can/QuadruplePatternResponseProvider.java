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

import org.objectweb.proactive.extensions.p2p.structured.messages.response.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * Default {@link ResponseProvider} for {@link QuadruplePatternResponse}.
 * 
 * @author lpellegr
 */
public class QuadruplePatternResponseProvider extends
        ResponseProvider<QuadruplePatternResponse, Coordinate<SemanticElement>> {

    private static final long serialVersionUID = 130L;

    /**
     * {@inheritDoc}
     */
    @Override
    public QuadruplePatternResponse get() {
        return new QuadruplePatternResponse();
    }

}
