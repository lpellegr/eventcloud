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
package org.objectweb.proactive.extensions.p2p.structured.deployment;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class allows to specify join constraints by creating a matrix that
 * indicates which {@code peerIndexThatJoins} must join which
 * {@code peerIndexToJoin} that is already created and deployed. These
 * constraints are useful to create a desired topology. In other words, it
 * forces some peers to join some other peers based on their position during the
 * insertion.
 * 
 * @author lpellegr
 */
public class InjectionConstraints {

    private Map<Integer, Integer> constraints;

    public InjectionConstraints() {
        this.constraints = new TreeMap<Integer, Integer>();
    }

    /**
     * Adds a new constraint.
     * <p>
     * {@code peerIndexThatJoins} must be greater than {@code 0} and
     * {@code peerIndexToJoin} must be lower than {@code peerIndexThatJoins} but
     * greater than or equals to {@code 0}.
     * 
     * @param peerIndexThatJoins
     *            indicates the index for which the constraints is applied.
     * @param peerIndexToJoin
     *            represents the constraint to apply: peer index to join.
     */
    public void addConstraint(int peerIndexThatJoins, int peerIndexToJoin) {
        if (peerIndexThatJoins == 0) {
            throw new IllegalArgumentException(
                    "peerIndexThatJoins must be greater than 0: "
                            + peerIndexThatJoins);
        }

        if (peerIndexThatJoins <= peerIndexToJoin) {
            throw new IllegalArgumentException("peerIndexThatJoins ("
                    + peerIndexThatJoins + ") <= peerIndexToJoin ("
                    + peerIndexToJoin + ")");
        }

        this.constraints.put(peerIndexThatJoins, peerIndexToJoin);
    }

    /**
     * Returns the constraints found for {@code peerIndexThatJoins} or
     * {@code -1} if none is found.
     * 
     * @param peerIndexThatJoins
     *            peer index to lookup a constraint.
     * 
     * @return the constraints found for {@code peerIndexThatJoins} or
     *         {@code -1} if none is found.
     */
    public int findConstraint(int peerIndexThatJoins) {
        Integer result = this.constraints.get(peerIndexThatJoins);

        if (result == null) {
            return -1;
        }

        return result;
    }

    /**
     * Creates a new fractal injection constraints object. Its purpose is to
     * force the peer created at step {@code i} to join the peer created at step
     * {@code i-1}.
     * 
     * @param nbPeers
     *            the number of peers that are constrained.
     * 
     * @return a new fractal injection constraints object.
     */
    public static InjectionConstraints newFractalInjectionConstraints(int nbPeers) {
        InjectionConstraints result = new InjectionConstraints();

        for (int i = 0; i < nbPeers - 1; i++) {
            result.addConstraint(i + 1, i);
        }

        return result;
    }

    /**
     * Creates a new uniform injection constraints object. Its purpose is to
     * force the insertion of peers to be distributed uniformly among the
     * available peers.
     * 
     * @param nbPeers
     *            the number of peers that are constrained.
     * 
     * @return a new uniform injection constraints object.
     */
    public static InjectionConstraints newUniformInjectionConstraints(int nbPeers) {
        InjectionConstraints result = new InjectionConstraints();

        int bucket = 1;

        for (int i = 0, count = 0; i < nbPeers - 1; i++, count++) {
            if (count == bucket) {
                bucket *= 2;
                count = 0;
            }

            result.addConstraint(i + 1, count);
        }

        return result;

    }

}
