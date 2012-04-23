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
package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;

/**
 * {@link ResponseEntry} is used for storing some information (e.g. the number
 * of replies expected, the number of replies received, etc.) about a request
 * which is being handled. These information are useful to create a
 * synchronization point.
 * <p>
 * Each time a new response is received, it has to be merged with the previous
 * one before to set it as the new value associated to this entry.
 * 
 * @author lpellegr
 */
public class ResponseEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        /**
         * This status means that the number of responses expected is equals to
         * the number of replies received.
         */
        RECEIPT_COMPLETED,
        /**
         * This status means that the number of responses received is smaller
         * than the number of replies expected.
         */
        RECEIPT_IN_PROGRESS
    };

    private Status status = Status.RECEIPT_IN_PROGRESS;

    /**
     * The response corresponding to the last response which has been merged.
     */
    private Response<?> response;

    /**
     * The maximum number of replies expected.
     */
    private final int expectedResponsesCount;

    /**
     * The current number of responses received.
     */
    private int responsesCount = 0;

    /**
     * Constructs a new entry with the specified
     * <code>expectedResponsesNumber</code>.
     * 
     * @param expectedResponsesCount
     *            the maximum number of responses expected.
     */
    public ResponseEntry(int expectedResponsesCount) {
        this.expectedResponsesCount = expectedResponsesCount;
    }

    /**
     * Returns the current number of responses received.
     * 
     * @return the current number of responses received.
     */
    public synchronized int getResponsesCount() {
        return this.responsesCount;
    }

    /**
     * Returns the maximum number of responses expected.
     * 
     * @return the maximum number of responses expected.
     */
    public int getExpectedResponsesCount() {
        return this.expectedResponsesCount;
    }

    /**
     * Returns the current status of the entry.
     * 
     * @return the current status of the entry.
     */
    public synchronized Status getStatus() {
        return this.status;
    }

    /**
     * Returns the last message merged.
     * 
     * @return the last message merged.
     */
    public synchronized Response<?> getResponse() {
        return this.response;
    }

    public synchronized void incrementResponsesCount(int increment) {
        this.responsesCount += increment;
        if (this.responsesCount == this.expectedResponsesCount) {
            this.status = Status.RECEIPT_COMPLETED;
        }
    }

    /**
     * Sets the specified message as response.
     * 
     * @param response
     *            the new response to associate to this entry.
     */
    public synchronized void setResponse(Response<?> response) {
        this.response = response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[responsesCount="
                + this.responsesCount + ", expectedResponsesCount="
                + this.expectedResponsesCount + "]";
    }

}
