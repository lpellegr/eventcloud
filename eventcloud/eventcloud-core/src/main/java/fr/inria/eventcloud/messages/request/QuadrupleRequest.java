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
package fr.inria.eventcloud.messages.request;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.ForwardRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.ForwardResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.UnicastConstraintsValidator;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;
import fr.inria.eventcloud.overlay.can.SemanticPointFactory;

/**
 * QuadrupleRequest is a request that is used to reach the peer which manages
 * the values associated to the {@link Quadruple} which is specified when the
 * request is constructed.
 * 
 * @author lpellegr
 */
public abstract class QuadrupleRequest extends
        ForwardRequest<SemanticCoordinate> {

    private static final long serialVersionUID = 160L;

    protected Quadruple quadruple;

    public QuadrupleRequest(Quadruple quad) {
        this(
                quad,
                new ResponseProvider<ForwardResponse<SemanticCoordinate>, Point<SemanticCoordinate>>() {
                    private static final long serialVersionUID = 160L;

                    @Override
                    public ForwardResponse<SemanticCoordinate> get() {
                        return new ForwardResponse<SemanticCoordinate>();
                    }
                });
    }

    public QuadrupleRequest(
            Quadruple quad,
            ResponseProvider<ForwardResponse<SemanticCoordinate>, Point<SemanticCoordinate>> responseProvider) {
        super(SemanticPointFactory.newSemanticCoordinate(quad),
                responseProvider);
        this.quadruple = quad;
    }

    public void onDestinationReached(StructuredOverlay overlay, Quadruple quad) {
        // to be overridden if necessary
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Router<ForwardRequest<SemanticCoordinate>, Point<SemanticCoordinate>> getRouter() {
        return new UnicastRequestRouter<ForwardRequest<SemanticCoordinate>, SemanticCoordinate>() {
            @Override
            protected void onDestinationReached(StructuredOverlay overlay,
                                                ForwardRequest<SemanticCoordinate> msg) {
                QuadrupleRequest.this.onDestinationReached(
                        overlay, QuadrupleRequest.this.getQuadruple());
            };
        };
    }

    public Quadruple getQuadruple() {
        return this.quadruple;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void customReadObject(ObjectInputStream stream)
            throws ClassNotFoundException, IOException {
        // do nothing because #customWriteObject do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void customWriteObject(ObjectOutputStream stream)
            throws IOException {
        // do not serialize the constraints validator to avoid to serialize and
        // convey two times the same the data. The constraint validator can be
        // reconstructed from the quadruple which is embedded
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        super.constraintsValidator =
                new UnicastConstraintsValidator<SemanticCoordinate>(
                        SemanticPointFactory.newSemanticCoordinate(this.quadruple));
    }

}
