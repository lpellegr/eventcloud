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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.EnlargeZoneOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.JoinIntroduceOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.JoinIntroduceResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.LeaveOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.RefreshNeighborOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.RemoveNeighborOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.ReplaceNeighborOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.UpdateNeighborOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.UpdateNeighborOperation2;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.RequestResponseManager;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.MakeDeepCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

/**
 * CanOverlay is an implementation of Content-Addressable Network (CAN)
 * protocol. Each peer manages neighbors from a {@link NeighborTable} and is
 * composed of a {@link Zone} which indicates the space associated to resources
 * to manage.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public abstract class CanOverlay<E extends Element> extends StructuredOverlay {

    private static final Logger log = LoggerFactory.getLogger(CanOverlay.class);

    private ScheduledExecutorService maintenanceTask;

    private NeighborTable<E> neighborTable;

    private LinkedList<SplitEntry> splitHistory;

    protected Zone<E> zone;

    /**
     * Constructs a new overlay with messagingManager set to
     * {@link CanRequestResponseManager}.
     */
    public CanOverlay() {
        this(new CanRequestResponseManager());
    }

    /**
     * Constructs a new overlay with the specified
     * {@code requestResponseManager}.
     * 
     * @param requestResponseManager
     *            the {@link RequestResponseManager} to use.
     */
    public CanOverlay(RequestResponseManager requestResponseManager) {
        super(requestResponseManager);

        this.neighborTable = new NeighborTable<E>();
        this.splitHistory = new LinkedList<SplitEntry>();
    }

    /**
     * Iterates on the {@link NeighborTable} in order to check whether each
     * neighbor neighbors the current peer or not. When the neighbor is
     * outdated, it is removed from the neighbor table.
     */
    public void removeOutdatedNeighbors() {
        Iterator<NeighborEntry<E>> it = null;
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                it = this.neighborTable.get(dim, direction).values().iterator();
                while (it.hasNext()) {
                    if (this.zone.neighbors(it.next().getZone()) == -1) {
                        it.remove();
                    }
                }
            }
        }
    }

    /**
     * Returns the {@link NeighborEntry} which is the nearest from the specified
     * {@code coordinate} and which contains the specified coordinate on
     * {@code dimension-1} dimensions.
     * <p>
     * Currently, no metric has been chosen to evaluate the nearest peer.
     * Therefore, a peer is randomly selected from the set of peers verifying
     * the second property as previously explained.
     * 
     * @param coordinate
     *            the coordinate to compare with.
     * 
     * @param dimension
     *            the dimension.
     * 
     * @param direction
     *            the direction.
     * 
     * @return the {@link NeighborEntry} which is the nearest from the specified
     *         {@code coordinate} and which contains the specified coordinate on
     *         {@code dimension-1} dimensions.
     * 
     * @see CanOverlay#neighborsVerifyingDimensions(Collection, Coordinate,
     *      byte)
     */
    public final NeighborEntry<E> nearestNeighbor(Coordinate<E> coordinate,
                                                  byte dimension, byte direction) {
        List<NeighborEntry<E>> neighbors =
                this.neighborsVerifyingDimensions(this.neighborTable.get(
                        dimension, direction).values(), coordinate, dimension);

        // no neighbors satisfying the coordinate on the specified dimension AND
        // direction
        if (neighbors.size() == 0) {
            throw new IllegalStateException(
                    "No neighbor to route to for coordinate " + coordinate
                            + " on dimension " + dimension + " and direction "
                            + direction + " , dump is:\n{}" + this.dump());
        }

        // from neighbors which verify the dimensions get those which
        // have the best rank!
        if (neighbors.size() > 1) {
            neighbors = this.neighborsWithBestRank(neighbors, coordinate);
        }

        // TODO: choose a metric to evaluate the nearest peer
        NeighborEntry<E> entry =
                neighbors.get(RandomUtils.nextInt(neighbors.size()));

        if (log.isDebugEnabled()) {
            if (this.zone.neighbors(entry.getZone()) == -1) {
                log.error("Neighbor chosen to route the message is "
                        + entry.getZone()
                        + ". However it does not neighbor the current peer.");
            } else {
                log.debug("Neighbor chosen to route the message is "
                        + entry.getZone());
            }
        }
        return entry;
    }

    /**
     * Returns a list of neighbors with the best rank. The rank is related to
     * the number of coordinate elements contained by the neighbor for the
     * specified {@code coordinate}.
     * 
     * @param neighbors
     *            the neighbors to filter.
     * 
     * @param coordinate
     *            the coordinate used to filter the neighbors by rank.
     * 
     * @return a list of neighbors with the best rank.
     */
    private List<NeighborEntry<E>> neighborsWithBestRank(List<NeighborEntry<E>> neighbors,
                                                         Coordinate<E> coordinate) {
        @SuppressWarnings("unchecked")
        List<NeighborEntry<E>>[] ranks = new List[coordinate.size() + 1];
        int nbEltVerified = 0;

        for (int i = 0; i < neighbors.size(); i++) {
            nbEltVerified = 0;
            for (byte j = 0; j < coordinate.size(); j++) {
                if (neighbors.get(i).getZone().contains(
                        j, coordinate.getElement(j)) == 0) {
                    nbEltVerified++;
                }
            }

            if (ranks[nbEltVerified] == null) {
                ranks[nbEltVerified] = new ArrayList<NeighborEntry<E>>();
            }

            ranks[nbEltVerified].add(neighbors.get(i));
        }

        for (int i = ranks.length - 1; i >= 0; i--) {
            if (ranks[i] != null) {
                return ranks[i];
            }
        }

        return null;
    }

    /**
     * Returns the neighbors which validates the specified {@code coordinate} on
     * {@code d-1} dimensions where {@code d} is the given {@code dimension}.
     * 
     * @param neighbors
     *            the neighbors to filter.
     * 
     * @param coordinate
     *            the coordinate used for filtering.
     * 
     * @param dimension
     *            the dimension limit.
     * 
     * @return the neighbors which validates the specified {@code coordinate} on
     *         {@code d-1} dimensions where {@code d} is the given
     *         {@code dimension}.
     */
    public List<NeighborEntry<E>> neighborsVerifyingDimensions(Collection<NeighborEntry<E>> neighbors,
                                                               Coordinate<E> coordinate,
                                                               byte dimension) {
        List<NeighborEntry<E>> result = new ArrayList<NeighborEntry<E>>();
        boolean validatesPrecedingDimensions;

        for (NeighborEntry<E> entry : neighbors) {
            validatesPrecedingDimensions = true;
            for (byte dim = 0; dim < dimension; dim++) {
                if (entry.getZone().contains(dim, coordinate.getElement(dim)) != 0) {
                    validatesPrecedingDimensions = false;
                    break;
                }
            }

            if (validatesPrecedingDimensions) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Returns the neighbor table for the current zone managed.
     * 
     * @return the neighbors of the managed zone.
     */
    public NeighborTable<E> getNeighborTable() {
        return this.neighborTable;
    }

    /**
     * Gets a random dimension number. The lower dimension number is defined as
     * {@code 0}. The maximum dimension number is defined by
     * {@link P2PStructuredProperties#CAN_NB_DIMENSIONS}.
     * 
     * @return a random dimension number.
     */
    public static byte getRandomDimension() {
        return (byte) RandomUtils.nextInt(P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue());
    }

    /**
     * Gets a random direction number.
     * 
     * @return a random direction number.
     */
    public static byte getRandomDirection() {
        return (byte) RandomUtils.nextInt(2);
    }

    /**
     * Returns the history of the splits.
     * 
     * @return the history of the splits.
     */
    public LinkedList<SplitEntry> getSplitHistory() {
        return this.splitHistory;
    }

    /**
     * Returns the zone which is managed by the overlay.
     * 
     * @return the zone which is managed by the overlay.
     */
    public Zone<E> getZone() {
        return this.zone;
    }

    @Override
    public String dump() {
        StringBuilder buf = new StringBuilder();
        buf.append("Peer with id ");
        buf.append(super.id);
        buf.append(" manages zone ");
        buf.append(this);

        if (this.neighborTable.size() > 0) {
            buf.append(" and has the following neighbor(s):\n");

            for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
                for (byte direction = 0; direction < 2; direction++) {
                    for (NeighborEntry<E> neighbor : this.neighborTable.get(
                            dim, direction).values()) {
                        buf.append("  - ");
                        buf.append(neighbor.getZone());
                        buf.append(", id is ");
                        buf.append(neighbor.getId());
                        buf.append(", abuts in dim "
                                + neighbor.getZone().neighbors(this.zone)
                                + " and is in dim=" + dim + ", dir="
                                + direction);
                        buf.append('\n');
                    }
                }
            }
        } else {
            buf.append('\n');
        }

        return buf.toString();
    }

    /**
     * Handles the specified {@link JoinIntroduceOperation}. This operation is
     * performed by the peer which is already on the network (the landmark
     * node).
     * 
     * @param msg
     *            the message to handle.
     * 
     * @return a response associated to the initial message.
     */
    @SuppressWarnings("unchecked")
    public JoinIntroduceResponseOperation<E> handleJoinIntroduceMessage(JoinIntroduceOperation<E> msg) {
        byte dimension = 0;
        // TODO: choose the direction according to the number of triples to
        // transfer
        byte direction = getRandomDirection();
        byte directionInv = CanOverlay.getOppositeDirection(direction);

        // gets the next dimension to split into
        if (!this.splitHistory.isEmpty()) {
            dimension =
                    CanOverlay.getNextDimension(this.splitHistory.getLast()
                            .getDimension());
        }

        // splits the current peer zone to share it
        HomogenousPair<? extends Zone<E>> newZones = this.splitZones(dimension);

        // neighbors affected for the new peer which joins the network
        NeighborTable<E> pendingNewNeighborhood = new NeighborTable<E>();
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte dir = 0; dir < 2; dir++) {
                // the peer which is joining does not have the same neighbors as
                // the landmark peer in the dimension and direction of the
                // landmark peer
                if (dim != dimension || dir != direction) {
                    Iterator<NeighborEntry<E>> it =
                            this.neighborTable.get(dim, dir)
                                    .values()
                                    .iterator();
                    while (it.hasNext()) {
                        NeighborEntry<E> entry = it.next();
                        // adds to the new peer neighborhood iff the new peer
                        // zone neighbors the current neighbor
                        if (newZones.get(directionInv).neighbors(
                                entry.getZone()) != -1) {
                            pendingNewNeighborhood.add(entry, dim, dir);
                        }
                    }
                }
            }
        }
        // adds the landmark peer in the neighborhood of the peer which is
        // joining
        pendingNewNeighborhood.add(
                new NeighborEntry<E>(
                        super.id, super.stub, newZones.get(direction)),
                dimension, direction);

        LinkedList<SplitEntry> historyToTransfert = null;
        try {
            historyToTransfert =
                    (LinkedList<SplitEntry>) MakeDeepCopy.makeDeepCopy(this.splitHistory);
            historyToTransfert.add(new SplitEntry(dimension, directionInv));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // updates overlay information due to the split
        this.zone = newZones.get(direction);
        this.splitHistory.add(new SplitEntry(dimension, direction));

        // removes the current peer from the neighbors that are back
        // the new peer which join and updates the zone maintained by
        // the others neighbors
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte dir = 0; dir < 2; dir++) {
                Iterator<NeighborEntry<E>> it =
                        this.neighborTable.get(dim, dir).values().iterator();
                NeighborEntry<E> entry = null;
                while (it.hasNext()) {
                    entry = it.next();
                    // we get a neighbor reference which is back the new peer
                    // which joins
                    if (dim == dimension && dir == directionInv) {
                        CanOperations.removeNeighbor(
                                entry.getStub(), super.id, dim,
                                getOppositeDirection(dir));
                        it.remove();
                    } else if (entry.getZone().neighbors(this.zone) == -1) {
                        // the old neighbor does not neighbors us with the new
                        // zone affected
                        CanOperations.removeNeighbor(
                                entry.getStub(), super.id, dim,
                                getOppositeDirection(dir));
                        it.remove();
                    } else {
                        // the neighbors have to update the zone associated to
                        // our id
                        CanOperations.updateNeighborOperation(
                                entry.getStub(), this.getNeighborEntry(), dim,
                                getOppositeDirection(dir));
                    }
                }
            }
        }

        this.neighborTable.add(new NeighborEntry<E>(
                msg.getPeerID(), msg.getRemotePeer(),
                newZones.get(directionInv)), dimension, directionInv);

        return new JoinIntroduceResponseOperation<E>(
                super.id, newZones.get(directionInv), historyToTransfert,
                pendingNewNeighborhood,
                this.removeDataIn(newZones.get(directionInv)));
    }

    protected HomogenousPair<? extends Zone<E>> splitZones(byte dimension) {
        return this.zone.split(dimension);
    }

    /**
     * TODO: use this method when fault tolerance support is activated in order
     * to perform periodic refresh.
     */
    @SuppressWarnings("unused")
    private void createMaintenanceTask() {
        this.maintenanceTask = Executors.newSingleThreadScheduledExecutor();
        this.maintenanceTask.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        CanOverlay.this.update();
                    }
                }, P2PStructuredProperties.CAN_REFRESH_TASK_START.getValue(),
                P2PStructuredProperties.CAN_REFRESH_TASK_INTERVAL.getValue(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create() {
        this.zone = this.newZone();
    }

    protected abstract Zone<E> newZone();

    /**
     * Forces the current peer to join a peer that is already member of a
     * network.
     * 
     * @param landmarkPeer
     *            the landmark node to join.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void join(Peer landmarkPeer) {
        JoinIntroduceResponseOperation<E> response =
                (JoinIntroduceResponseOperation<E>) PAFuture.getFutureValue(landmarkPeer.receive(new JoinIntroduceOperation<E>(
                        super.id, super.stub)));

        this.assignDataReceived(response.getData());

        this.zone = response.getZone();
        this.splitHistory = response.getSplitHistory();
        this.neighborTable = response.getNeighbors();

        // notify the neighbors that the current peer has joined
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte dir = 0; dir < 2; dir++) {
                if (dim != this.splitHistory.getLast().getDimension()
                        || dir != getOppositeDirection(this.splitHistory.getLast()
                                .getDirection())) {
                    for (NeighborEntry<E> entry : this.neighborTable.get(
                            dim, dir).values()) {
                        CanOperations.insertNeighbor(
                                entry.getStub(), this.getNeighborEntry(), dim,
                                getOppositeDirection(dir));
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void leave() {
        if (this.neighborTable.size() == 0) {
            return;
        }

        this.leaveBasedOnSplitHistory();
    }

    private void leaveBasedOnSplitHistory() {
        SplitEntry lastSplitEntry = this.splitHistory.getLast();

        byte reassignmentDimension = lastSplitEntry.getDimension();
        byte reassignmentDirection =
                getOppositeDirection(lastSplitEntry.getDirection());

        E element =
                reassignmentDirection > 0
                        ? this.zone.getLowerBound(reassignmentDimension)
                        : this.zone.getUpperBound(reassignmentDimension);

        System.out.println("reassignmentDimension=" + reassignmentDimension
                + ", reassignmentDirection=" + reassignmentDirection + ", e="
                + element);

        for (NeighborEntry<E> entry : this.neighborTable.get(
                reassignmentDimension, reassignmentDirection).values()) {
            System.out.println("ENTRY=" + entry);
            // enlarges the local neighbors' zones that take over the leaving
            // zone such that we can update neighbors' pointer with local
            // knowledge
            entry.getZone().enlarge(reassignmentDimension,
            // opposite direction of reassignmentDirection
            lastSplitEntry.getDirection(), element);

            // enlarge the remote neighbor's zone
            PAFuture.waitFor(entry.getStub()
                    .receive(
                            new EnlargeZoneOperation<E>(
                                    this.splitHistory.size() - 1,
                                    reassignmentDimension,
                                    reassignmentDirection,
                                    element,
                                    ImmutableList.copyOf(this.neighborTable.get(
                                            reassignmentDimension,
                                            CanOverlay.getOppositeDirection(reassignmentDirection))
                                            .values()))));
        }

        // updates neighbor's pointers of each neighbor in the opposite
        // reassignment position that take over the leaving zone
        for (NeighborEntry<E> entry : this.neighborTable.get(
                reassignmentDimension,
                CanOverlay.getOppositeDirection(reassignmentDirection))
                .values()) {
            PAFuture.waitFor(entry.getStub().receive(
                    new UpdateNeighborOperation2<E>(
                            ImmutableList.copyOf(this.neighborTable.get(
                                    reassignmentDimension,
                                    reassignmentDirection).values()),
                            reassignmentDimension, reassignmentDirection)));
        }

        for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (byte direction = 0; direction < 2; direction++) {
                for (NeighborEntry<E> neighborEntry : this.neighborTable.get(
                        dimension, direction).values()) {
                    // removes the leaving peer from remote neighbors' tables
                    PAFuture.waitFor(neighborEntry.getStub()
                            .receive(
                                    new RemoveNeighborOperation<E>(
                                            this.getId(),
                                            dimension,
                                            CanOverlay.getOppositeDirection(direction))));

                    PAFuture.waitFor(neighborEntry.getStub().receive(
                            new RefreshNeighborOperation<E>()));
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void lazyLeave() {
        NeighborEntry<E> suitableNeighbor =
                this.neighborTable.getMergeableNeighbor(this.zone);

        if (suitableNeighbor == null) {
            this.retryLeave();
            return;
        } else {
            HomogenousPair<Byte> neighborDimDir =
                    this.neighborTable.findDimensionAndDirection(suitableNeighbor.getId());

            Set<NeighborEntry<E>> neighbors =
                    Sets.newHashSet(this.neighborTable.get(
                            neighborDimDir.getFirst(),
                            neighborDimDir.getSecond()).values());

            PAFuture.waitFor(suitableNeighbor.getStub().receive(
                    new LeaveOperation<E>(
                            super.id, this.zone, neighbors,
                            this.retrieveAllData())));

            // updates neighbors NeighborTable
            for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
                for (byte dir = 0; dir < 2; dir++) {
                    for (NeighborEntry<E> entry : this.neighborTable.get(
                            dim, dir).values()) {
                        if (dim != neighborDimDir.getFirst()
                                && dir != neighborDimDir.getSecond()) {
                            PAFuture.waitFor(entry.getStub().receive(
                                    new ReplaceNeighborOperation<E>(
                                            super.id, entry)));
                        }
                    }
                }
            }

            log.info(
                    "Peer {} has left the network and the zone has been taken over by {}",
                    this, suitableNeighbor.getZone());
        }
    }

    private void retryLeave() {
        int timeout =
                RandomUtils.nextInt(P2PStructuredProperties.CAN_LEAVE_RETRY_MAX.getValue()
                        - P2PStructuredProperties.CAN_LEAVE_RETRY_MIN.getValue())
                        + P2PStructuredProperties.CAN_LEAVE_RETRY_MIN.getValue();

        log.info(
                "Peer {} cannot leave at this time, retry in {} ms", this,
                timeout);

        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        this.leave();
    }

    public EmptyResponseOperation processLeave(LeaveOperation<E> operation) {
        byte dim =
                this.neighborTable.findDimension(operation.getPeerLeavingId());
        byte dir =
                this.neighborTable.findDirection(operation.getPeerLeavingId());

        this.zone = this.zone.merge(operation.getPeerLeavingZone());

        if (operation.getData() != null) {
            this.assignDataReceived(operation.getData());
        }

        this.neighborTable.removeAll(dim, dir);
        for (NeighborEntry<E> entry : operation.getNewNeighborsToSet()) {
            this.neighborTable.add(entry, dim, dir);
        }

        return new EmptyResponseOperation();
    }

    public void update() {
        this.removeOutdatedNeighbors();

        for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (byte direction = 0; direction < 2; direction++) {
                Iterator<NeighborEntry<E>> it =
                        this.neighborTable.get(dimension, direction)
                                .values()
                                .iterator();
                while (it.hasNext()) {
                    it.next().getStub().receive(
                            new UpdateNeighborOperation<E>(
                                    this.getNeighborEntry(), dimension,
                                    getOppositeDirection(direction)));
                }
            }
        }
    }

    /**
     * Returns the {@link NeighborEntry} associated to the current overlay.
     * 
     * @return the {@link NeighborEntry} associated to the current overlay.
     */
    private NeighborEntry<E> getNeighborEntry() {
        return new NeighborEntry<E>(super.id, super.stub, this.zone);
    }

    /**
     * Sets the new split history.
     * 
     * @param history
     *            the new split history to set.
     */
    public void setHistory(LinkedList<SplitEntry> history) {
        this.splitHistory = history;
    }

    /**
     * Sets the new zone covered.
     * 
     * @param zone
     *            the new zone covered.
     */
    public void setZone(Zone<E> zone) {
        this.zone = zone;
    }

    @Override
    public OverlayType getType() {
        return OverlayType.CAN;
    }

    public void dumpNeighbors() {
        log.debug("Peer managing {}", this.zone);
        NeighborTable<E> neighborTable = this.getNeighborTable();

        for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (byte direction = 0; direction < 2; direction++) {
                for (NeighborEntry<E> entry : neighborTable.get(
                        dimension, direction).values()) {
                    log.debug(
                            "  * {}, dimension={}, direction={}", new Object[] {
                                    entry.getZone(), dimension, direction});
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (this.zone == null) {
            return super.id.toString();
        } else {
            return this.zone.toString();
        }
    }

    /**
     * Returns the next dimension following the specified dimension.
     * 
     * @param dimension
     *            the specified dimension.
     * @return the next dimension following the specified dimension.
     */
    public static byte getNextDimension(byte dimension) {
        return (byte) ((dimension + 1) % P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue());
    }

    /**
     * Returns the opposite direction of the specified {@code direction}.
     * 
     * @param direction
     *            the specified direction.
     * 
     * @return the opposite direction.
     */
    public static byte getOppositeDirection(byte direction) {
        return (byte) ((direction + 1) % 2);
    }

    /**
     * Returns the previous dimension following the specified dimension.
     * 
     * @param dimension
     *            the specified dimension.
     * @return the previous dimension following the specified dimension.
     */
    public static byte getPreviousDimension(byte dimension) {
        byte dim = (byte) (dimension - 1);
        if (dim < 0) {
            dim = P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue();
        }
        return dim;
    }

    /*
     * NeighborTable shortcuts
     */

    /**
     * Indicates if the data structure contains the specified {@link Peer}
     * identifier as neighbor.
     * 
     * @param peerID
     *            the peer identifier to look for.
     * @return {@code true} if the {@link NeighborTable} contains the peer
     *         identifier as neighbor, {@code false} otherwise.
     */
    public boolean hasNeighbor(UUID peerID) {
        return this.neighborTable.contains(peerID);
    }

}
