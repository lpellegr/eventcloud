/**
 * Copyright (c) 2011-2013 INRIA.
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
package org.objectweb.proactive.extensions.p2p.structured.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.Response;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.ForwardResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.UnicastConstraintsValidator;

/**
 * A {@code ForwardRequest} is a query message that may be used in order to
 * <strong>reach</strong> a peer which manages a specified coordinate on a CAN
 * structured peer-to-peer network.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class ForwardRequest<E extends Element> extends Request<Coordinate<E>> {

    private static final long serialVersionUID = 151L;

    /*
     * The zone which is managed by the sender. It is used in order to send the
     * response when the keyToReach has been reached.
     */
    private SerializedValue<Coordinate<E>> senderCoordinate;

    public ForwardRequest(Coordinate<E> coordinateToReach) {
        super(new UnicastConstraintsValidator<E>(coordinateToReach),
                new ResponseProvider<ForwardResponse<E>, Coordinate<E>>() {
                    private static final long serialVersionUID = 151L;

                    @Override
                    public ForwardResponse<E> get() {
                        return new ForwardResponse<E>();
                    }
                });
    }

    public ForwardRequest(
            Coordinate<E> coordinateToReach,
            ResponseProvider<? extends Response<Coordinate<E>>, Coordinate<E>> responseProvider) {
        super(new UnicastConstraintsValidator<E>(coordinateToReach),
                responseProvider);
    }

    /**
     * Returns the key which is managed by the sender in order to send the
     * response when the keyToReach has been reached.
     * 
     * @return the key which is managed by the sender in order to send the
     *         response when the keyToReach has been reached.
     */
    public Coordinate<E> getSenderCoordinate() {
        return this.senderCoordinate.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Router<ForwardRequest<E>, Coordinate<E>> getRouter() {
        return new UnicastRequestRouter<ForwardRequest<E>, E>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void route(StructuredOverlay overlay) {
        if (this.senderCoordinate == null && this.responseProvider != null) {
            this.senderCoordinate =
                    SerializedValue.create(((CanOverlay<E>) overlay).getZone()
                            .getLowerBound());
        }

        super.route(overlay);
    }

}
