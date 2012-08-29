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
package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.ForwardRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.ForwardResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.overlay.can.SemanticCoordinateFactory;
import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * QuadrupleRequest is a request that is used to reach the peer which manages
 * the values associated to the {@link Quadruple} which is specified when the
 * request is constructed.
 * 
 * @author lpellegr
 */
public abstract class QuadrupleRequest extends ForwardRequest<SemanticElement> {

    private static final long serialVersionUID = 1L;

    private SerializedValue<Quadruple> quadruple;

    public QuadrupleRequest(Quadruple quad) {
        this(
                quad,
                new ResponseProvider<ForwardResponse<SemanticElement>, Coordinate<SemanticElement>>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public ForwardResponse<SemanticElement> get() {
                        return new ForwardResponse<SemanticElement>();
                    }
                });
    }

    public QuadrupleRequest(
            Quadruple quad,
            ResponseProvider<ForwardResponse<SemanticElement>, Coordinate<SemanticElement>> responseProvider) {
        super(SemanticCoordinateFactory.newSemanticCoordinate(quad),
                responseProvider);
        this.quadruple = SerializedValue.create(quad);
    }

    public void onDestinationReached(StructuredOverlay overlay, Quadruple quad) {
        // to be overridden if necessary
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Router<ForwardRequest<SemanticElement>, Coordinate<SemanticElement>> getRouter() {
        return new UnicastRequestRouter<ForwardRequest<SemanticElement>, SemanticElement>() {
            @Override
            protected void onDestinationReached(StructuredOverlay overlay,
                                                ForwardRequest<SemanticElement> msg) {
                QuadrupleRequest.this.onDestinationReached(
                        overlay, QuadrupleRequest.this.getQuadruple());
            };
        };
    }

    public Quadruple getQuadruple() {
        return this.quadruple.getValue();
    }

}