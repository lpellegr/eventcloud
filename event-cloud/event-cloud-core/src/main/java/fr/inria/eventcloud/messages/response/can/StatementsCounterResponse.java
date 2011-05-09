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
package fr.inria.eventcloud.messages.response.can;

import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastResponseRouter;

import fr.inria.eventcloud.messages.request.can.StatementsCounterRequest;

/**
 * Response associated to {@link StatementsCounterRequest}.
 * 
 * @author lpellegr
 */
public class StatementsCounterResponse extends AnycastResponse {

    private static final long serialVersionUID = 1L;

    private final Map<StatementsCounterRequest.Entry, Long> entries;

    public StatementsCounterResponse(StatementsCounterRequest request) {
        super(request);
        this.entries = request.getNbStatementsByPeer();
    }

    public Map<StatementsCounterRequest.Entry, Long> getEntries() {
        return this.entries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Router<? extends RequestResponseMessage<StringCoordinate>, StringCoordinate> getRouter() {
        return new AnycastResponseRouter<StatementsCounterResponse>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(AnycastResponse subResponse) {
        for (Entry<StatementsCounterRequest.Entry, Long> entry : ((StatementsCounterResponse) subResponse).getEntries()
                .entrySet()) {
            if (!this.entries.containsKey(entry.getKey())) {
                this.entries.put(entry.getKey(), entry.getValue());
            }
        }
    }

}
