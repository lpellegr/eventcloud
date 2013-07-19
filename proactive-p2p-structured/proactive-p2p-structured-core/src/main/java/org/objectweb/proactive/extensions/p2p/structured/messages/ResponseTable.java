package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The purpose of this class is to maintain the response entries received for a
 * request that has been dispatched.
 * 
 * @author lpellegr
 */
public class ResponseTable {

    private final ConcurrentMap<MessageId, Entry> entries;

    public ResponseTable(int concurrencyLevel) {
        this.entries =
                new ConcurrentHashMap<MessageId, Entry>(
                        16, 0.75f, concurrencyLevel);
    }

    public void clear() {
        this.entries.clear();
    }

    public ResponseEntry get(MessageId id) {
        return this.entries.get(id).responseEntry;
    }

    public ResponseEntry put(Request<?> request, ResponseEntry responseEntry) {
        Entry result =
                this.entries.put(request.id, new Entry(
                        request.responseDestination, responseEntry));

        if (result != null) {
            responseEntry = result.responseEntry;
        }

        if (request.getHopCount() == 0) {
            // does not forward requester stub since we maintain it in a table
            // on the first peer that receive the request. However this
            // reference should kept with the request if we care about fault
            // tolerance
            request.responseDestination = null;
        }

        return responseEntry;
    }

    public Entry remove(MessageId id) {
        return this.entries.remove(id);
    }

    protected static class Entry {

        public final FinalResponseReceiver responseDestination;

        public final ResponseEntry responseEntry;

        public Entry(FinalResponseReceiver responseDestination,
                ResponseEntry responseEntry) {
            this.responseDestination = responseDestination;
            this.responseEntry = responseEntry;
        }

    }

}
