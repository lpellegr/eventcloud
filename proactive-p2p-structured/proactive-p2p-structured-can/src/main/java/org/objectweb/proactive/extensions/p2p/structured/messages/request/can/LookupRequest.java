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
package org.objectweb.proactive.extensions.p2p.structured.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.LookupResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastRequestRouter;

/**
 * A {@code LookupRequest} is a query message which may be used in order to
 * <strong>find</strong> a peer which manages a specified coordinate on a CAN
 * structured peer-to-peer network.
 * 
 * @author lpellegr
 */
public class LookupRequest extends ForwardRequest {

    private static final long serialVersionUID = 1L;

    protected Peer remotePeerReached;

    public LookupRequest(StringCoordinate coordinateToReach) {
        super(coordinateToReach);
    }

    public Peer getRemotePeerReached() {
        return this.remotePeerReached;
    }

    public void setRemotePeerReached(Peer remotePeerReached) {
        this.remotePeerReached = remotePeerReached;
    }

    /**
     * {@inheritDoc}
     */
    public Router<ForwardRequest, StringCoordinate> getRouter() {
        return new UnicastRequestRouter<ForwardRequest>() {
            protected void onDestinationReached(StructuredOverlay overlay,
                                                ForwardRequest msg) {
                ((LookupRequest) msg).setRemotePeerReached(overlay.getStub());
            };
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<StringCoordinate> createResponse(StructuredOverlay overlay) {
        return new LookupResponse(this, this.remotePeerReached);
    }

}
