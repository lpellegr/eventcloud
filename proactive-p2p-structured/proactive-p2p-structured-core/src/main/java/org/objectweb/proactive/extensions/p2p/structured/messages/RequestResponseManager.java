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
package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.Serializable;

/**
 * The request/response manager is in charge of maintaining the state of the
 * requests which are dispatched over the overlay by using message passing.
 * 
 * @author lpellegr
 */
public abstract class RequestResponseManager implements Serializable {

    private static final long serialVersionUID = 150L;

    private final AggregationTable aggregationTable;

    private final ResponseTable responseTable;

    protected RequestResponseManager(int concurrencyLevel) {
        this.aggregationTable = new AggregationTable(this, concurrencyLevel);
        this.responseTable = new ResponseTable(concurrencyLevel);
    }

    /**
     * Returns the {@link ResponseEntry} associated to the given
     * {@code responseId}.
     * 
     * @param responseId
     *            the response identifier to look for.
     * 
     * @return the {@link ResponseEntry} associated to the given
     *         {@code responseId} or {@code null} if no entry was found.
     */
    public ResponseEntry getResponseEntry(MessageId responseId) {
        return this.responseTable.get(responseId);
    }

    public ResponseEntry putResponseEntry(Request<?> request,
                                          ResponseEntry responseEntry) {
        return this.responseTable.put(request, responseEntry);
    }

    public ResponseEntry removeResponseEntry(MessageId responseId) {
        return this.responseTable.remove(responseId).responseEntry;
    }

    public static void notifyRequester(Response<?> response) {
        response.setDeliveryTime();
        response.getResponseDestination().receive(
                new FinalResponse(response.getId(), response));
    }

    public void notifyRequester(MessageId responseId, MessageId aggregationId) {
        ResponseTable.Entry entry = this.responseTable.remove(responseId);

        if (entry == null) {
            throw new IllegalArgumentException("No entry found for id: "
                    + responseId);
        }

        Response<?> response = entry.responseEntry.getResponse();
        response.setDeliveryTime();

        if (aggregationId == null) {
            entry.responseDestination.receive(new FinalResponse(
                    responseId, response));
        } else {
            this.aggregationTable.put(aggregationId, response);
        }
    }

    public AggregationTable getAggregationTable() {
        return this.aggregationTable;
    }

    public ResponseTable getResponseTable() {
        return this.responseTable;
    }

    public void clear() {
        this.aggregationTable.clear();
        this.responseTable.clear();
    }

    public void close() {
    }

}
