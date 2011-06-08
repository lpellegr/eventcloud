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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.datastore.JenaDatastore;
import fr.inria.eventcloud.messages.response.can.BooleanForwardResponse;

/**
 * A ContainsQuadrupleRequest is a request that is used to know if there is a
 * peer that contain the quadruple that is specified when the object is
 * constructed.
 * 
 * @author lpellegr
 */
public class ContainsQuadrupleRequest extends QuadrupleRequest {

    private static final long serialVersionUID = 1L;

    public ContainsQuadrupleRequest(final Quadruple quad) {
        super(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<StringCoordinate> createResponse(StructuredOverlay overlay) {
        return new BooleanForwardResponse(
                this,
                ((JenaDatastore) overlay.getDatastore()).contains(super.getQuadruple()));
    }

}
