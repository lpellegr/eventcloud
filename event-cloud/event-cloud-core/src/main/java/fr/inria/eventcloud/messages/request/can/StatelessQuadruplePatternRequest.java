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

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.messages.response.can.StatelessQuadruplePatternResponse;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * StatelessQuadruplePatternRequest is a {@link QuadruplePattern} that is
 * distributed among all the peers that match the values contained by the
 * quadruple pattern which is specified when the request is created. This
 * request is said stateless because it does not maintain any state about the
 * action which is executed by overriding
 * {@link #onPeerValidatingKeyConstraints(CanOverlay, QuadruplePattern)}.
 * 
 * @author lpellegr
 */
public abstract class StatelessQuadruplePatternRequest extends AnycastRequest {

    private static final long serialVersionUID = 1L;

    protected SerializedValue<QuadruplePattern> quadruplePattern;

    public StatelessQuadruplePatternRequest(QuadruplePattern quadPattern) {
        super(new DefaultAnycastConstraintsValidator(
                SemanticCoordinate.create(quadPattern)));
        this.quadruplePattern =
                new SerializedValue<QuadruplePattern>(quadPattern);
    }

    /**
     * Defines a behavior to execute when we are on a peer which match the
     * constraints.
     * 
     * @param overlay
     * @param quadruplePattern
     */
    public abstract void onPeerValidatingKeyConstraints(CanOverlay overlay,
                                                        QuadruplePattern quadruplePattern);

    /**
     * {@inheritDoc}
     */
    @Override
    public AnycastRequestRouter<StatelessQuadruplePatternRequest> getRouter() {
        return new AnycastRequestRouter<StatelessQuadruplePatternRequest>() {
            @Override
            public void onPeerValidatingKeyConstraints(final CanOverlay overlay,
                                                       final AnycastRequest request) {
                StatelessQuadruplePatternRequest.this.onPeerValidatingKeyConstraints(
                        overlay, quadruplePattern.getValue());
            }
        };
    }

    public SerializedValue<QuadruplePattern> getQuadruplePattern() {
        return this.quadruplePattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<StringCoordinate> createResponse(StructuredOverlay overlay) {
        return new StatelessQuadruplePatternResponse(this);
    }

}
