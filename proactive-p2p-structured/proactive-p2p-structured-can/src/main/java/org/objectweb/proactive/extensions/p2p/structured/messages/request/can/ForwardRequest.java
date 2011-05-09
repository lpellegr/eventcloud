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
package org.objectweb.proactive.extensions.p2p.structured.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.ForwardResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.UnicastConstraintsValidator;

/**
 * A {@code ForwardRequest} is a query message which may be used in order to
 * <strong>reach</strong> a peer which manages a specified coordinate on a CAN
 * structured peer-to-peer network.
 * 
 * @author lpellegr
 */
public class ForwardRequest extends Request<StringCoordinate> {

    private static final long serialVersionUID = 1L;

    /**
     * The zone which is managed by the sender. It is used in order to send the
     * response when the keyToReach has been reached.
     */
    private StringCoordinate senderCoordinate;

    public ForwardRequest(StringCoordinate coordinateToReach) {
        super(new UnicastConstraintsValidator(coordinateToReach));
    }

    /**
     * Returns the key which is managed by the sender in order to send the
     * response when the keyToReach has been reached.
     * 
     * @return the key which is managed by the sender in order to send the
     *         response when the keyToReach has been reached.
     */
    public StringCoordinate getSenderCoordinate() {
        return this.senderCoordinate;
    }

    /**
     * {@inheritDoc}
     */
    public Router<ForwardRequest, StringCoordinate> getRouter() {
        return new UnicastRequestRouter<ForwardRequest>();
    }

    /**
     * {@inheritDoc}
     */
    public void route(StructuredOverlay overlay) {
        if (this.senderCoordinate == null) {
            this.senderCoordinate =
                    ((CanOverlay) overlay).getZone().getLowerBound();
        }
        super.route(overlay);
    }

    /**
     * {@inheritDoc}
     */
    public Response<StringCoordinate> createResponse() {
        return new ForwardResponse(this);
    }

}
