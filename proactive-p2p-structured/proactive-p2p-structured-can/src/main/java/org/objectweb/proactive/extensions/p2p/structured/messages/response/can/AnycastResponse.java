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
package org.objectweb.proactive.extensions.p2p.structured.messages.response.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingList;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;

/**
 * Response associated to {@link AnycastRequest}. This kind of response will use
 * the same path as the initial request for its routing.
 * 
 * @author lpellegr
 */
public abstract class AnycastResponse extends Response<StringCoordinate> {

    private static final long serialVersionUID = 1L;

    private AnycastRoutingList anycastRoutingList = new AnycastRoutingList();

    public AnycastResponse(AnycastRequest request) {
        super(request, null);
        this.anycastRoutingList = request.getAnycastRoutingList();
    }

    /**
     * Returns the {@link AnycastRoutingList} containing the
     * {@link AnycastRoutingEntry} to use in order to route the response.
     * 
     * @return the {@link AnycastRoutingList} containing the
     *         {@link AnycastRoutingEntry} to use in order to route the
     *         response.
     */
    public AnycastRoutingList getAnycastRoutingList() {
        return this.anycastRoutingList;
    }

    /**
     * Merges the specified {@link AnycastResponse} with the current one. This
     * method has to be overridden to merge the data received as result. When
     * the merge operation is terminated, the specified response is discarded.
     * 
     * @param subResponse
     *            the subResponse to merge into the current one.
     */
    public synchronized void addSubResult(AnycastResponse subResponse) {
        // to be overridden
    }

}
