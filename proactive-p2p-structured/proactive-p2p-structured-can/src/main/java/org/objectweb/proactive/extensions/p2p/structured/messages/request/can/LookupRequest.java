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
package org.objectweb.proactive.extensions.p2p.structured.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.LookupResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastRequestRouter;

/**
 * A {@code LookupRequest} is a query message which may be used in order to
 * <strong>find</strong> a peer which manages a specified coordinate on a CAN
 * structured peer-to-peer network.
 * 
 * @param <E>
 *            the {@link Coordinate}s type manipulated.
 * 
 * @author lpellegr
 */
public class LookupRequest<E extends Coordinate> extends ForwardRequest<E> {

    private static final long serialVersionUID = 160L;

    protected Peer remotePeerReached;

    public LookupRequest(Point<E> coordinateToReach) {
        super(coordinateToReach,
                new ResponseProvider<LookupResponse<E>, Point<E>>() {
                    private static final long serialVersionUID = 160L;

                    @Override
                    public LookupResponse<E> get() {
                        return new LookupResponse<E>();
                    }
                });
    }

    public LookupRequest(
            Point<E> coordinateToReach,
            ResponseProvider<? extends LookupResponse<E>, Point<E>> responseProvider) {
        super(coordinateToReach, responseProvider);
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
    @Override
    public Router<ForwardRequest<E>, Point<E>> getRouter() {
        return new UnicastRequestRouter<ForwardRequest<E>, E>() {
            @Override
            protected void onDestinationReached(StructuredOverlay overlay,
                                                ForwardRequest<E> msg) {
                ((LookupRequest<E>) msg).setRemotePeerReached(overlay.getStub());
            };
        };
    }

}
