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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Help class to check CAN properties.
 * 
 * TODO: works in 2D but the test seems not to be valid in 4D. Should check if
 * the issue is not related to the precision of coordinates.
 * 
 * @author lpellegr
 */
public class CanHelper {

    private static Logger log = LoggerFactory.getLogger(CanHelper.class);

    /**
     * Checks the neighborhood for all the specified peers.
     * 
     * @param peers
     *            a collection of peers from the same network.
     * 
     * @return an object containing identified errors.
     */
    public static Neighborhoods checkNeighborhood(Collection<Peer> peers) {
        List<Neighborhood> errors = new ArrayList<Neighborhood>();

        for (Peer peer : peers) {
            errors.add(checkNeighborhood(peer));
        }

        return new Neighborhoods(errors);
    }

    public static <E extends Coordinate> Neighborhood checkNeighborhood(Peer peer) {
        NeighborTable<E> neighborTable = CanOperations.getNeighborTable(peer);

        GetIdAndZoneResponseOperation<Coordinate> idAndZone =
                CanOperations.getIdAndZoneResponseOperation(peer);

        OverlayId peerId = idAndZone.getPeerIdentifier();
        Zone<Coordinate> zone = idAndZone.getPeerZone();

        List<NeighborError> errors = new ArrayList<CanHelper.NeighborError>();

        for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (byte direction = 0; direction < 2; direction++) {
                List<NeighborEntry<E>> neighborEntries =
                        new ArrayList<NeighborEntry<E>>(neighborTable.get(
                                dimension, direction).values());

                final byte nextDimension =
                        CanOverlay.getNextDimension(dimension);

                Collections.sort(
                        neighborEntries, new Comparator<NeighborEntry<E>>() {
                            @Override
                            public int compare(NeighborEntry<E> e1,
                                               NeighborEntry<E> e2) {
                                return e1.getZone()
                                        .getLowerBound()
                                        .getCoordinate(nextDimension)
                                        .compareTo(
                                                e2.getZone()
                                                        .getLowerBound()
                                                        .getCoordinate(
                                                                nextDimension));
                            }
                        });

                Coordinate oldBound2 = zone.getLowerBound(nextDimension);

                for (int i = 0; i < neighborEntries.size(); i++) {
                    Zone<E> neighborZone = neighborEntries.get(i).getZone();

                    Coordinate bound1 =
                            neighborZone.getLowerBound(nextDimension);
                    Coordinate bound2 =
                            neighborZone.getUpperBound(nextDimension);

                    int comparison = oldBound2.compareTo(bound1);

                    boolean successive = comparison == 0;
                    boolean lower = comparison > 0;
                    boolean upper = comparison < 0;

                    boolean isValid =
                            successive
                                    || (i == 0 && lower)
                                    || (i == neighborEntries.size() - 1 && upper);

                    if (!isValid) {
                        if (lower) {
                            errors.add(new InvalidNeighbor(neighborEntries.get(
                                    i).getId(), dimension, direction));
                        } else {
                            errors.add(new MissingNeighbor(dimension, direction));
                        }
                    }

                    log.trace(
                            "On peer {}, dim={}, dir={}, valid? {}", peerId,
                            dimension, direction, isValid);

                    oldBound2 = bound2;
                }
            }
        }

        return new Neighborhood(peerId, errors);
    }

    public static class Neighborhoods implements Iterable<Neighborhood> {

        private final List<Neighborhood> neighborhoods;

        private Neighborhoods(List<Neighborhood> neighborhoods) {
            this.neighborhoods = neighborhoods;
        }

        public boolean areValid() {
            for (Neighborhood n : this.neighborhoods) {
                if (!n.isValid()) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public Iterator<Neighborhood> iterator() {
            return this.neighborhoods.iterator();
        }

    }

    public static class Neighborhood {

        private final List<NeighborError> neighborErrors;

        private final OverlayId peerId;

        private Neighborhood(OverlayId peerId,
                List<NeighborError> neighborErrors) {
            this.peerId = peerId;
            this.neighborErrors = neighborErrors;
        }

        public boolean isValid() {
            return this.neighborErrors.isEmpty();
        }

        public List<NeighborError> getNeighborErrors() {
            return this.neighborErrors;
        }

        public OverlayId getPeerId() {
            return this.peerId;
        }

        @Override
        public String toString() {
            if (this.neighborErrors.isEmpty()) {
                return "Neighborhood of " + this.peerId + " seems valid";
            } else {
                StringBuilder result = new StringBuilder();

                for (NeighborError e : this.neighborErrors) {
                    result.append(e.toString());
                    result.append("\n");
                }

                return result.toString();
            }
        }
    }

    public static abstract class NeighborError {

        private final byte dimension;

        private final byte direction;

        protected NeighborError(byte dimension, byte direction) {
            super();
            this.dimension = dimension;
            this.direction = direction;
        }

        public byte getDimension() {
            return this.dimension;
        }

        public byte getDirection() {
            return this.direction;
        }

        public abstract String getDescription();

        @Override
        public String toString() {
            return this.getDescription();
        }

    }

    private static class MissingNeighbor extends NeighborError {

        protected MissingNeighbor(byte dimension, byte direction) {
            super(dimension, direction);
        }

        @Override
        public String getDescription() {
            return "Missing neighbor on dimension " + super.dimension
                    + " and direction " + super.direction;
        }

    }

    private static class InvalidNeighbor extends NeighborError {

        private final OverlayId neighborId;

        protected InvalidNeighbor(OverlayId neighborId, byte dimension,
                byte direction) {
            super(dimension, direction);
            this.neighborId = neighborId;
        }

        @Override
        public String getDescription() {
            return "The zone view of neighbor identified by " + this.neighborId
                    + " on dimension " + super.dimension + " and direction "
                    + super.direction + " is invalid";
        }

    }

}
