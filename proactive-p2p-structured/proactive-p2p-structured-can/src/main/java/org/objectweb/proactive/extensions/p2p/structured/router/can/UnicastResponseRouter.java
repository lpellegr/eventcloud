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
package org.objectweb.proactive.extensions.p2p.structured.router.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseManager;
import org.objectweb.proactive.extensions.p2p.structured.messages.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;

/**
 * Router used to route a {@link Response} from a peer to an another.
 * 
 * @param <T>
 *            the response type to route.
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class UnicastResponseRouter<T extends Response<Coordinate<E>>, E extends Element>
        extends Router<T, Coordinate<E>> {

    public UnicastResponseRouter() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void makeDecision(StructuredOverlay overlay, T response) {
        RequestResponseManager.notifyRequester(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handle(StructuredOverlay overlay, T response) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void route(StructuredOverlay overlay, T response) {
        // nothing to do
    }

}
