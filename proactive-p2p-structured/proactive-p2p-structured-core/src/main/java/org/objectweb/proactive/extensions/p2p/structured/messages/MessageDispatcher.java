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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerInternal;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message dispatcher.
 * 
 * @author lpellegr
 */
public class MessageDispatcher {

    private static final Logger log =
            LoggerFactory.getLogger(MessageDispatcher.class);

    private final UniqueID proxyId;

    private final AtomicLong sequencer;

    private final Map<MessageId, Entry> entries;

    private final MultiActiveService multiActiveService;

    private FinalResponseReceiver receiverStub;

    public MessageDispatcher(UniqueID proxyId,
            MultiActiveService multiActiveService,
            FinalResponseReceiver finalResponseReceiver) {
        this.entries =
                new ConcurrentHashMap<MessageId, Entry>(
                        P2PStructuredProperties.MAO_SOFT_LIMIT_PROXIES.getValue());
        this.multiActiveService = multiActiveService;
        this.proxyId = proxyId;
        this.sequencer = new AtomicLong();
        this.receiverStub = finalResponseReceiver;
    }

    /**
     * Dispatches the request over the overlay by using message passing. The
     * request is supposed to create a response which is sent back to the
     * sender.
     * 
     * @param request
     *            the request to dispatch.
     * @param peer
     *            the first peer to use for routing the request.
     * 
     * @return a response associated to the type of the request.
     */
    public Response<?> dispatch(Request<?> request, Peer peer) {
        request.id = this.newMessageId();
        request.responseDestination = SerializedValue.create(this.receiverStub);

        this.entries.put(request.id, new Entry());

        peer.route(request);

        return (Response<?>) this.pull(request.id);
    }

    /**
     * Dispatches the specified requests in parallel and wait for a response for
     * each request sent. Once responses are received they are combiner with the
     * specifies response combiner and returned to the requester.
     * 
     * @param requests
     *            the requests to dispatch in parallel.
     * @param context
     *            a context that can be any serializable object.
     * @param responseCombiner
     *            the response combiner used to combine intermediate responses.
     * @param peer
     *            the peer from where the request is sent.
     * 
     * @return a response associated to the type of the requests sent once the
     *         intermediate responses have been combined with the specified
     *         response combiner.
     */
    public <T extends Request<?>> Serializable dispatch(List<T> requests,
                                                        Serializable context,
                                                        ResponseCombiner responseCombiner,
                                                        Peer peer) {
        MessageId aggregationId = this.newMessageId();

        for (Request<?> request : requests) {
            request.id = this.newMessageId();
            request.aggregationId = aggregationId;
            request.responseDestination =
                    SerializedValue.create(this.receiverStub);
        }

        this.entries.put(aggregationId, new Entry());
        ((PeerInternal) peer).dispatch(
                requests, context, responseCombiner, this.receiverStub);

        return this.pull(aggregationId);
    }

    /**
     * Dispatches the request over the overlay by using message passing. The
     * request is supposed to generate no response. Sending a request with a
     * response provider will result in an error.
     * 
     * @param request
     *            the request to dispatch.
     * @param peer
     *            the first peer to use for routing the request.
     */
    public void dispatchv(Request<?> request, Peer peer) {
        request.id = this.newMessageId();

        this.entries.put(request.id, new Entry());

        peer.route(request);
    }

    /**
     * Pulls the response for the specified {@code requestId}. If the response
     * has not been yet received, a synchronization point is created in order to
     * wait the response.
     * 
     * @param requestId
     *            indicates what is the id of the response to retrieve.
     * 
     * @return the response for the specified requestId.
     */
    public Serializable pull(MessageId requestId) {
        this.multiActiveService.getRequestExecutor()
                .incrementExtraActiveRequestCount(1);

        // waits for the final response
        this.wait(requestId);

        log.debug("Final response received for request {}", requestId);

        Serializable response =
                this.entries.remove(requestId).response.getResult();

        return response;
    }

    /**
     * Creates a synchronization point in order to wait the response associated
     * to the specified {@code requestId}.
     * 
     * @param requestId
     *            indicates for which request we are waiting the final response.
     */
    private void wait(MessageId requestId) {
        log.debug("Waiting final response for request with id {}", requestId);

        Entry entry = this.entries.get(requestId);

        synchronized (entry) {
            while (entry.response == null) {
                try {
                    entry.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        this.multiActiveService.getRequestExecutor()
                .decrementExtraActiveRequestCount(1);
    }

    /**
     * Pushes the final response and wakes up the initial sender thread that a
     * new response has been received. The notification is done to remove the
     * synchronization point (which has been previously set by
     * {@link #wait(MessageId)}).
     * 
     * @param response
     *            the final response to send back.
     */
    public void push(FinalResponse response) {
        Entry entry = this.entries.get(response.getId());

        if (entry == null) {
            throw new IllegalArgumentException(
                    "Pushing final response for a request id that is not managed: "
                            + response.getId());
        }

        synchronized (entry) {
            entry.response = response;
            entry.notify();
        }
    }

    private MessageId newMessageId() {
        return new MessageId(this.proxyId, this.sequencer.getAndIncrement());
    }

    private static class Entry {

        public FinalResponse response;

    }

}
