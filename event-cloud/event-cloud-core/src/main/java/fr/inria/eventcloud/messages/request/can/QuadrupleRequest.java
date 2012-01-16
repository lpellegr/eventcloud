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
package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.ForwardRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * QuadrupleRequest is a request that is used to reach the peer which manages
 * the values associated to the {@link Quadruple} which is specified when the
 * request is constructed.
 * 
 * @author lpellegr
 */
public abstract class QuadrupleRequest extends ForwardRequest {

    private static final long serialVersionUID = 1L;

    private SerializedValue<Quadruple> quadruple;

    public QuadrupleRequest(final Quadruple quad) {
        super(SemanticCoordinate.create(quad));
        this.quadruple = SerializedValue.create(quad);
    }

    public void onDestinationReached(StructuredOverlay overlay, Quadruple quad) {
        // to be overridden if necessary
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Router<ForwardRequest, StringCoordinate> getRouter() {
        return new UnicastRequestRouter<ForwardRequest>() {
            @Override
            protected void onDestinationReached(StructuredOverlay overlay,
                                                ForwardRequest msg) {
                QuadrupleRequest.this.onDestinationReached(
                        overlay, getQuadruple());
            };
        };
    }

    public Quadruple getQuadruple() {
        return this.quadruple.getValue();
    }

}
