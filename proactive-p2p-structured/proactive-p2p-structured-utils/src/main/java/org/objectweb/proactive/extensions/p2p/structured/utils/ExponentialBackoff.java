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
package org.objectweb.proactive.extensions.p2p.structured.utils;

/**
 * Helper class to compute exponential backoff values.
 * 
 * @author lpellegr
 */
public class ExponentialBackoff {

    /**
     * Returns the binary exponential backoff value for the specified retry
     * index.
     * 
     * @param retryIndex
     * 
     * @return the binary exponential backoff value for the specified retry
     *         index.
     */
    public static int computeBinaryValue(int retryIndex) {
        return ((1 << retryIndex) - 1) / 2;
    }

    /**
     * Exponential backoff function taken from
     * http://dthain.blogspot.fr/2009/02/exponential-backoff-in-distributed.html
     * 
     * @param retryIndex
     * @param spreadOutFactor
     *            should be a random number in the range [1-2], so that its
     *            effect is to spread out the load over time.
     * @param initialTimeout
     *            is the initial timeout, and should be set at the outer limits
     *            of expected response time for the service. For example, if
     *            your service responds in 1ms on average but in 10ms for 99% of
     *            requests, then set t=10ms.
     * @param maximumTimeout
     *            should be as low as possible to keep your customers happy, but
     *            high enough that the system can definitely handle requests
     *            from all clients at that sustained rate.
     * 
     * @return exponential backoff value for the specified parameters.
     */
    public static int compteValue(int retryIndex, int spreadOutFactor,
                                  int initialTimeout, int maximumTimeout) {
        return Math.min(
                spreadOutFactor * initialTimeout * (1 << retryIndex),
                maximumTimeout);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println(compteValue(i, 1, 300, 3000));
        }
    }

}
