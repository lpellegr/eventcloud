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
package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerImpl;

/**
 * Table used to aggregate responses for requests dispatched through a call to
 * {@link PeerImpl#dispatch(List, Serializable, ResponseCombiner, FinalResponseReceiver)}
 * .
 * 
 * @author lpellegr
 */
public class AggregationTable {

    private final RequestResponseManager parent;

    private final ConcurrentMap<MessageId, Entry> entries;

    public AggregationTable(RequestResponseManager parent, int concurrencyLevel) {
        this.entries =
                new ConcurrentHashMap<MessageId, Entry>(
                        16, 0.75f, concurrencyLevel);
        this.parent = parent;
    }

    public void register(MessageId aggregationId, Serializable context,
                         ResponseCombiner responseCombiner,
                         FinalResponseReceiver responseDestination,
                         int nbResponseAggregated) {
        this.entries.put(aggregationId, new Entry(
                context, responseCombiner, responseDestination,
                nbResponseAggregated));
    }

    public void put(MessageId aggregationId, Response<?> response) {
        Entry entry = this.entries.get(aggregationId);

        if (entry == null) {
            throw new IllegalArgumentException(
                    "No entry found for aggregation id: " + aggregationId);
        }

        synchronized (entry) {
            entry.responsesReceived.add(response);
            entry.nbResponseExpected--;

            if (entry.nbResponseExpected == 0) {
                Serializable result =
                        entry.responseCombiner.combine(
                                entry.responsesReceived, this.parent,
                                entry.context);

                entry.responseDestination.receive(new FinalResponse(
                        aggregationId, result));

                this.entries.remove(aggregationId);
            }
        }
    }

    public void clear() {
        this.entries.clear();
    }

    protected static class Entry {

        public final Serializable context;

        public final ResponseCombiner responseCombiner;

        public final List<Response<?>> responsesReceived;

        public final FinalResponseReceiver responseDestination;

        public int nbResponseExpected;

        public Entry(Serializable context, ResponseCombiner responseCombiner,
                FinalResponseReceiver responseDestination,
                int nbResponseExpected) {
            this.context = context;
            this.responseCombiner = responseCombiner;
            this.responseDestination = responseDestination;
            this.nbResponseExpected = nbResponseExpected;
            this.responsesReceived =
                    new ArrayList<Response<?>>(nbResponseExpected);
        }

    }

}
