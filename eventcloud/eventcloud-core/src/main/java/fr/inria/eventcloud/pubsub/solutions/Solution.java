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
package fr.inria.eventcloud.pubsub.solutions;

import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * A solution is a value that is delivered to a user who has subscribed with a
 * subscription which is matched by the solution. It contains the sub solutions
 * that are received by a {@link SubscribeProxy} asynchronously for a given
 * subscription.
 * 
 * @param <C>
 *            The type of the collection used to store the chunks that are
 *            received.
 * 
 * @author lpellegr
 */

public abstract class Solution<C> {

    protected final C chunks;

    /**
     * Constructs a new Solution from the specified chunk (sub solution).
     * 
     * @param chunk
     *            part of the solution.
     */
    public Solution(C chunk) {
        this.chunks = chunk;
    }

    /**
     * Merges the specified sub solution into the solution.
     * 
     * @param chunk
     *            the sub solution to add to the solution.
     */
    public abstract void merge(C chunk);

    /**
     * Indicates whether the solution is ready for delivery.
     * 
     * @return {@code true} if the solution is ready for delivery, {@code false}
     *         otherwise.
     */
    public abstract boolean isReady();

    /**
     * Returns the chunks contained by the solution.
     * 
     * @return the chunks contained by the solution.
     */
    public C getChunks() {
        return this.chunks;
    }

}
