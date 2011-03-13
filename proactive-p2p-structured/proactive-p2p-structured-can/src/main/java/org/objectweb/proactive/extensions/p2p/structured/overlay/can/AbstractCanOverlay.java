package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.converter.MakeDeepCopy;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.configuration.DefaultProperties;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.ConcurrentJoinException;
import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.JoinIntroduceOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.JoinIntroduceResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.JoinWelcomeOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.LeaveOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.MergeOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.UpdateNeighborOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.RequestResponseManager;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractCANOverlay is an implementation of Content-Addressable Network (CAN)
 * protocol. Each peer manages neighbors from a {@link NeighborTable} and is
 * composed of a {@link Zone} which indicates the space associated to resources
 * to manage.
 * 
 * @author lpellegr
 */
public abstract class AbstractCanOverlay extends StructuredOverlay {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AbstractCanOverlay.class);

    private ScheduledExecutorService maintenanceTask;
    
    private NeighborTable neighborTable = new NeighborTable();

    private LinkedList<SplitEntry> splitHistory = new LinkedList<SplitEntry>();

    private Zone zone;

    private AtomicBoolean joinInProgress = new AtomicBoolean(false);
    
    private JoinInformation tmpJoinInformation;
    
    public AbstractCanOverlay() {
        super();
    }

    /**
     * Constructs a new overlay with the specified <code>localPeer</code> 
     * and <code>queryManager</code>.
     * 
     * @param localPeer
     *            the local peer reference to associate to this overlay.
     * 
     * @param queryManager
     *            the {@link RequestResponseManager} to use.
     */
    public AbstractCanOverlay(Peer localPeer, RequestResponseManager queryManager) {
        super(localPeer, queryManager);
    }

    /**
     * Constructs a new overlay with the specified <code>queryManager</code>.
     * 
     * @param queryManager
     *            the {@link RequestResponseManager} to use.
     */
    public AbstractCanOverlay(RequestResponseManager queryManager) {
        super(queryManager);
    }

    /**
     * Implements a behavior to execute with the specified 
     * <code>dataReceived</code> (e.g. to store the data in 
     * the local datastore).
     * 
     * @param dataReceived 
     * 			the data which have been received when the peer
     * 			has join a network from a landmark node.
     * 
     * @see AbstractCanOverlay#join(Peer)
     */
    protected abstract void affectDataReceived(Object dataReceived);

    protected abstract void mergeDataReceived(MergeOperation msg);

    protected abstract Object retrieveAllData();

    /**
     * Iterates on the {@link NeighborTable} in order to check whether each neighbor
     * neighbors the current peer or not. When the neighbor is outdated, it is removed
     * from the neighbor table.
     */
    public void removeOutdatedNeighbors() {
    	Iterator<NeighborEntry> it = null;
		for (int dim = 0; dim < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
			for (int direction = 0; direction < 2; direction++) {
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
     * Returns a boolean indicating whether the specified 
     * <code>coordinate</code> is managed by the {@link Zone} 
     * affected to the overlay, or not.
     * 
     * @param coordinate
     *            the coordinate to check.
     * 
     * @return <code>true</code> if the specified <code>coordinate</code> 
     * 		   is managed by the {@link Zone} affected to the overlay,
     *         <code>false</code> otherwise.
     */
    public boolean contains(Coordinate coordinate) {
        return this.zone.contains(coordinate);
    }

    /**
     * Returns the {@link NeighborEntry} which is the nearest from the specified 
     * <code>coordinate</code> and which contains the specified coordinate on 
     * <code>dimension-1</code> dimensions.
     * <p>
     * Currently, no metric has been chosen to evaluate the nearest peer. Therefore,
     * a peer is randomly selected from the set of peers verifying the second property
     * as previously explained. 
     * 
     * @param coordinate the coordinate to compare with.
     * 
     * @param dimension the dimension.
     * 
     * @param direction the direction.
     * 
     * @return the {@link NeighborEntry} which is the nearest from the specified 
     *         <code>coordinate</code> and which contains the specified coordinate 
     *         on <code>dimension-1</code> dimensions.
     *         
     * @see AbstractCanOverlay#neighborsVerifyingDimensions(Collection, Coordinate, int)
     */
    public NeighborEntry nearestNeighbor(Coordinate coordinate, int dimension, int direction) {
        List<NeighborEntry> neighbors = 
                this.neighborsVerifyingDimensions(
                        this.neighborTable.get(dimension, direction).values(), coordinate, dimension);
        
        // no neighbors satisfying the coordinate on the specified dimension AND direction
        if (neighbors.isEmpty() 
        		&& logger.isDebugEnabled()) {
        	logger.debug(
        			"No neighbors satisfying the coordinate on the specified dimension " 
                    + dimension + " AND direction " + direction + ", " + this.dump());
        } 
        
		// from neighbors which verify the dimensions get those which
		// have the best rank!
        if (neighbors.size() > 1) {
        	neighbors = this.neighborsWithBestRank(neighbors, coordinate);
        }

        if (neighbors.size() == 0) {
        	logger.error("No neighbor to route to, dump is " + this.dump());
        }
        
        // TODO: choose a metric to evaluate the nearest peer
		NeighborEntry entry = neighbors.get(ProActiveRandom.nextInt(neighbors.size()));
		if (logger.isDebugEnabled()) {
			if (this.zone.neighbors(entry.getZone()) == -1) {
				logger.error("Neighbor chosen to route the message is " + entry.getZone() 
							 + ". However it does not neighbor the current peer.");
			} else {
				logger.debug("Neighbor chosen to route the message is " + entry.getZone());
			}
		}
		return entry;
    }
    
    /**
     * Returns a list of neighbors with the best rank. The rank is 
     * related to the number of coordinate elements contained by 
     * the neighbor for the specified <code>coordinate</code>.
     * 
     * @param neighbors the neighbors to filter.
     * 
     * @param coordinate the coordinate used to filter the neighbors by rank.  
     * 
     * @return a list of neighbors with the best rank
     */
	private List<NeighborEntry> neighborsWithBestRank(List<NeighborEntry> neighbors, Coordinate coordinate) {
    	@SuppressWarnings("unchecked")
		List<NeighborEntry>[] ranks = new List[coordinate.size() + 1];
    	int nbEltVerified = 0;
    	
    	for (int i=0; i<neighbors.size(); i++) {
    		nbEltVerified = 0;
    		for (int j=0; j<coordinate.size(); j++) {
    			if (neighbors.get(i).getZone().contains(j, coordinate.getElement(j)) == 0) {
    				nbEltVerified++;
    			}    			
    		}
    		
    		if (ranks[nbEltVerified] == null) {
    			ranks[nbEltVerified] = new ArrayList<NeighborEntry>();
    		}
    		
    		ranks[nbEltVerified].add(neighbors.get(i));
    	}
    	
    	for (int i=ranks.length-1; i>=0; i--) {
    		if (ranks[i] != null) {
    			return ranks[i];
    		}
    	}
    	
    	return null;
    }
    
	/**
	 * Returns the neighbors which validates the specified <code>coordinate</code>
	 * on <code>d-1</code> dimensions where <code>d</code> is the given 
	 * <code>dimension</code>.
	 * 
	 * @param neighbors the neighbors to filter.
	 * 
	 * @param coordinate the coordinate used for filtering.
	 * 
	 * @param dimension the dimension limit.
	 * 
	 * @return the neighbors which validates the specified <code>coordinate</code>
	 * 		   on <code>d-1</code> dimensions where <code>d</code> is the given 
	 * 		   <code>dimension</code>.
	 */
    public List<NeighborEntry> neighborsVerifyingDimensions(Collection<NeighborEntry> neighbors, Coordinate coordinate, int dimension) {
        List<NeighborEntry> result = new ArrayList<NeighborEntry>();
        boolean validatesPrecedingDimensions;

        for (NeighborEntry entry : neighbors) {
            validatesPrecedingDimensions = true;
            for (int dim = 0; dim < dimension; dim++) {
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
     * Indicates if the dimension index of the current zone contains the
     * specified coordinate.
     * 
     * @param dimension
     *            the dimension index used for the check.
     * 
     * @param coordinate
     *            the coordinate to check.
     * 
     * @return <code>0</code> if the coordinate is contained by the zone on the
     *         specified axe, <code>-1</code> if the coordinate is smaller than
     *         the line which is managed by the specified dimension,
     *         <code>1</code> otherwise.
     */
    public short contains(int dimension, Element coordinate) {
        return this.getZone().contains(dimension, coordinate);
    }

    /**
     * Returns the neighbor table for the current zone managed.
     * 
     * @return the neighbors of the managed zone.
     */
    public NeighborTable getNeighborTable() {
        return this.neighborTable;
    }

    /**
     * Gets a random dimension number. The minimum dimension number is
     * <code>0</code>. The maximum dimension number is defined by
     * {@link DefaultProperties#CAN_NB_DIMENSIONS}.
     * 
     * @return a random dimension number.
     */
    public int getRandomDimension() {
        return ProActiveRandom.nextInt(DefaultProperties.CAN_NB_DIMENSIONS.getValue());
    }

    /**
     * Gets a random direction number.
     * 
     * @return a random direction number.
     */
    public int getRandomDirection() {
        return ProActiveRandom.nextInt(2);
    }

    /**
     * Returns the history of the splits.
     * 
     * @return the history of the splits.
     */
    public List<SplitEntry> getSplitHistory() {
        return this.splitHistory;
    }

    /**
     * Returns the zone which is managed by the overlay.
     * 
     * @return the zone which is managed by the overlay.
     */
    public Zone getZone() {
        return this.zone;
    }

    /**
     * Returns the data which are contained in the specified <code>zone</code>
     * from the current peer.
     * 
     * @param zone the zone delineating the space of data to retrieve.
     * 
     * @return the data which are contained in the specified <code>zone</code>
     * 		   from the current peer.
     */
    protected abstract Object getDataIn(Zone zone);

    /**
     * Removes the data managed in the specified <code>zone</code>
     * from the current overlay.
     * 
     * @param zone the zone delineating the space of data to remove.
     */
    protected abstract void removeDataIn(Zone zone);
    
    public String dump() {
        StringBuilder buf = new StringBuilder();
        buf.append("Peer managing ");
        buf.append(this);
        buf.append(" with ID=");
        buf.append(this.getId());
        buf.append(" has neighbor(s):\n");

        
        for (int dim=0; dim<DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (int direction=0; direction<2; direction++) {
                for (NeighborEntry neighbor : this.neighborTable.get(dim, direction).values()) {
                    buf.append("  - ");
                    buf.append(neighbor.getZone());
                    buf.append(", ID=");
                    buf.append(neighbor.getId());
                    buf.append(", abuts in dim " + neighbor.getZone().neighbors(zone) + " and is in dim=" + dim + ", dir=" + direction);
                    buf.append("\n");
                }
            }
        }
        return buf.toString();
    }

    /**
     * Handles the specified {@link JoinIntroduceOperation}. This operation is 
     * performed by the peer which is already on the network (the landmark node).
     * 
     * @param msg
     *            the message to handle.
     *            
     * @return a response associated to the initial message.
     */
    @SuppressWarnings("unchecked")
	public JoinIntroduceResponseOperation handleJoinIntroduceMessage(JoinIntroduceOperation msg) {
    	if (!this.joinInProgress.compareAndSet(false, true)) {
    		throw new ConcurrentJoinException();
    	}
    	
        int dimension = 0;
        // TODO: choose the direction according to the number of triples to transfer
        int direction = this.getRandomDirection();
        int directionInv = AbstractCanOverlay.getOppositeDirection(direction);

        // gets the next dimension to split onto
        if (!this.splitHistory.isEmpty()) {
            dimension = AbstractCanOverlay.getNextDimension(
                    		this.splitHistory.removeLast().getDimension());
        }

        // splits the current peer zone to share it
        Zone[] newZones = null;
        try {
            newZones = this.zone.split(dimension);
        } catch (ZoneException e) {
            logger.error("An error occured during the split of the zone from peer"
                         + this.toString(), e);
        }

        // neighbors affected for the new peer which joins the network
        NeighborTable pendingNewNeighborhood = new NeighborTable();
        for (int dim=0; dim<DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
        	for (int dir=0; dir<2; dir++) {
        		// the peer which is joining don't have the same neighbors as the
        		// landmark peer in the dimension and direction of the landmark peer
        		if (dim != dimension || dir != direction) {
					Iterator<NeighborEntry> it = this.neighborTable.get(dim, dir).values().iterator();
					while (it.hasNext()) {
						NeighborEntry entry = it.next();
						// adds to the new peer neighborhood iff the new peer zone 
						// neighbors the current neighbor
						if (newZones[directionInv].neighbors(entry.getZone()) != -1) {
							pendingNewNeighborhood.add(entry, dim, dir);
						}
					}
        		}
        	}
        }
        // adds the landmark peer in the neighborhood of the peer which is joining
        pendingNewNeighborhood.add(
                new NeighborEntry(
                        this.getId(), 
                        this.getRemotePeer(), 
                        newZones[direction]), 
                dimension, direction);

        LinkedList<SplitEntry> historyToTransfert = null;
        try {
            historyToTransfert = 
                (LinkedList<SplitEntry>) 
                    MakeDeepCopy.WithObjectStream
                        .makeDeepCopy(this.splitHistory);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        historyToTransfert.add(new SplitEntry(dimension, directionInv));

        this.tmpJoinInformation = 
        	new JoinInformation(
        			dimension, direction, newZones[direction], 
        			new NeighborEntry(msg.getPeerID(), msg.getRemotePeer(), newZones[directionInv]));
        
        return new JoinIntroduceResponseOperation(
        				newZones[directionInv],
        				historyToTransfert, 
        				pendingNewNeighborhood, 
        				this.getDataIn(newZones[directionInv]));
    }

    public EmptyResponseOperation handleJoinWelcomeMessage(JoinWelcomeOperation operation) {
    	int directionInv =  getOppositeDirection(this.tmpJoinInformation.getDirection());
    	
    	// updates overlay information due to split
    	this.zone = this.tmpJoinInformation.getZone();
    	this.splitHistory.add(
    			new SplitEntry(
    					this.tmpJoinInformation.getDimension(), 
    					this.tmpJoinInformation.getDirection()));
    	this.removeDataIn(this.tmpJoinInformation.getEntry().getZone());
    	
    	// removes the current peer from the neighbors that are back
    	// the new peer which join and updates the zone maintained by 
    	// the others neighbors
    	for (int dim=0; dim<DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
        	for (int dir=0; dir<2; dir++) {
        		Iterator<NeighborEntry> it = this.neighborTable.get(dim, dir).values().iterator();
        		NeighborEntry entry = null;
				while(it.hasNext()) {
					entry = it.next();
					// we get a neighbor reference which is back the new peer which join
					if (dim == this.tmpJoinInformation.getDimension() && dir == directionInv) {
						CanOperations.removeNeighbor(
								entry.getStub(), this.getId(), dim, getOppositeDirection(dir));
						it.remove();
					} else if (entry.getZone().neighbors(this.zone) == -1) {
						// the old neighbor does not neighbors us with the new zone affected
						CanOperations.removeNeighbor(
								entry.getStub(), this.getId(), dim, getOppositeDirection(dir));
						it.remove();
					} else {
						// the neighbor have to update the zone associated to our id
						CanOperations.updateNeighborOperation(
								entry.getStub(), this.getNeighborEntry(), dim, getOppositeDirection(dir));
					}
				}
        	}
        }
    	this.neighborTable.add(this.tmpJoinInformation.getEntry(), this.tmpJoinInformation.getDimension(), directionInv);

    	this.tmpJoinInformation = null;
    	this.joinInProgress.set(false);
    	
    	return new EmptyResponseOperation();
    }
    
    /**
     * TODO: use this method when fault tolerance support is activated 
     * in order to perform periodic refresh.
     */
    @SuppressWarnings("unused")
	private void createMaintenanceTask() {
        this.maintenanceTask = Executors.newSingleThreadScheduledExecutor();
        this.maintenanceTask.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, DefaultProperties.CAN_REFRESH_TASK_START.getValue(), 
           DefaultProperties.CAN_REFRESH_TASK_INTERVAL.getValue(), 
           TimeUnit.MILLISECONDS);
    }
    
    public boolean create() {
        this.zone = new Zone();
    	return true;
    }
    
    /**
     * Returns <code>true</code> when the join operation succeed 
     * and <code>false</code> when the peer receiving the operation
     * is already handling an another operation.
     * 
     * @param landmarkPeer the landmark node to join.
     * 
     * @return <code>true</code> when the join operation succeed 
     * 		   and <code>false</code> when the peer receiving the 
     *         operation is already handling an another join operation.
     */
    public boolean join(Peer landmarkPeer) {
    	JoinIntroduceResponseOperation response;
    	try {
    		response = 
    			(JoinIntroduceResponseOperation) PAFuture.getFutureValue(
                    landmarkPeer.receiveOperationIS(
                            new JoinIntroduceOperation(this.getId(), this.getRemotePeer())));
    	} catch (ConcurrentJoinException e) {
    		e.printStackTrace();
    		return false;
    	}
    	
        this.zone = response.getZone();
        this.splitHistory = response.getSplitHistory();
        this.neighborTable = response.getNeighbors();
        this.affectDataReceived(response.getData());
        
        PAFuture.waitFor(
        		landmarkPeer.receiveOperationIS(
        				new JoinWelcomeOperation()));
        
        // notify the neighbors that the current peer has joined
        for (int dim=0; dim<DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
        	for (int dir=0; dir<2; dir++) {
        		if (dim != this.splitHistory.getLast().getDimension()
        				|| dir != getOppositeDirection(this.splitHistory.getLast().getDirection())) {
					for (NeighborEntry entry : this.neighborTable.get(dim, dir).values()) {
						CanOperations.insertNeighbor(entry.getStub(), this.getNeighborEntry(), dim, getOppositeDirection(dir));
					}
				}
        	}
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Peer " + this + " has joined the network from " + landmarkPeer);
        }
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean leave() {
        /*
         * The current peer associated to this overlay is the only peer on the
         * network.
         */
        if (this.neighborTable.size() == 0) {
            return true;
        }

        SplitEntry lastSplitEntry = this.splitHistory.pop();
        int lastDimension = lastSplitEntry.getDimension();
        int lastDirection = lastSplitEntry.getDirection();

        Map<UUID, NeighborEntry> neighborsToMergeWith = this.neighborTable.get(lastDimension,
                AbstractCanOverlay.getOppositeDirection(lastDirection));

//        /*
//         * Notify all the neighbors the current peer is preparing to leave. That
//         * force the neighbors to wait the current peer has finished to leave
//         * before to handle new requests.
//         */
//        for (NeighborEntry entry : this.getNeighborTable()) {
//            entry.getStub().notifyNeighborStartLeave(this.getIdentifier());
//        }

        /*
         * Terminate the current body in order to notify all the neighbors that
         * they can't send message to the current remote peer. If they try they
         * will receive a BlockingRequestReceiverException : this exception is
         * used by the tracker in order to detect a peer which is preparing to
         * leave when it have to add a peer on the network.
         */

        // TODO BlockingRequestReceiver
        // ((BlockingRequestReceiver) ((MigratableBody)
        // super.getLocalPeer().getBody())
        // .getRequestReceiver()).blockReception();

        switch (neighborsToMergeWith.size()) {
            case 0:
                /* We are alone on this pitiless world : nothing to do */
                break;
            case 1:
                neighborsToMergeWith.values().iterator().next().getStub().receiveOperationIS(
                        new MergeOperation(lastDimension, lastDirection, this.getId(), this
                                .getZone(), new NeighborTable(), this.retrieveAllData()));
                break;
            default:
                Zone zoneToSplit = this.getZone();
                Zone[] newZones = null;

                /*
                 * For the last dimension and direction of the split, we split
                 * N-1 times, where N is the number of neighbors in the last
                 * dimension and last reverse direction from the current peer.
                 */
                for (NeighborEntry entry : neighborsToMergeWith.values()) {
                    try {
                        newZones = zoneToSplit.split(
                                        AbstractCanOverlay.getNextDimension(lastDimension), 
                                        this.neighborTable.getNeighborEntry(
                                                entry.getId()).getZone().getUpperBound(
                                                        AbstractCanOverlay.getNextDimension(lastDimension)));
                    } catch (ZoneException e) {
                        e.printStackTrace();
                    }

                    NeighborTable neighborsOfCurrentNeighbor = new NeighborTable();
                    neighborsOfCurrentNeighbor.addAll(this.neighborTable);

                    zoneToSplit = newZones[1];

                    /*
                     * Merge the new zones obtained with the suitable neighbors.
                     */
                    // FIXME The given Data are not good : we give all the
                    // resources to each
                    // neighbors to merge with
                    entry.getStub().receiveOperationIS(
                                    new MergeOperation(lastDimension,
                                            lastDirection, this.getId(), newZones[0],
                                            neighborsOfCurrentNeighbor,
                                            this.retrieveAllData()));
                }
                break;
        }

        // this.neighborsDataStructure.removeAll(lastDimension,
        // CANOverlay.getOppositeDirection(lastDirection));

        /*
         * Send LeaveOperation in order to update the neighbors list.
         */
        for (int dim = 0; dim < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (int direction = 0; direction < 2; direction++) {
                for (NeighborEntry entry : this.neighborTable.get(dim, direction).values()) {
                    if (!neighborsToMergeWith.containsKey(entry.getId())) {
                        entry.getStub().receiveOperationIS(
                                new LeaveOperation(this.getId(), new ArrayList<NeighborEntry>(neighborsToMergeWith.
                                        values()), dim, AbstractCanOverlay
                                        .getOppositeDirection(direction)));
                    }
                }
            }
        }

        // TODO BlockingRequestReceiver
        // ((BlockingRequestReceiver) ((MigratableBody)
        // super.getLocalPeer().getBody())
        // .getRequestReceiver()).acceptReception();

        /*
         * Notify all the old neighbors of the peer which is leaving that it has
         * terminated the leave operation.
         */

//        for (NeighborEntry entry : this.getNeighborTable()) {
//            entry.getStub().notifyNeighborEndLeave(this.getIdentifier());
//        }
//        for (NeighborEntry entry : neighborsToMergeWith.values()) {
//            entry.getStub().notifyNeighborEndLeave(this.getIdentifier());
//        }

        /*
         * Set all neighbors reference to null.
         */
        this.neighborTable.removeAll();

        return true;
    }
    
    public void update() {
    	this.removeOutdatedNeighbors();

    	for (int dimension=0; dimension<DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
			for (int direction = 0; direction < 2; direction++) {
				Iterator<NeighborEntry> it = this.neighborTable.get(dimension, direction).values().iterator();
				while (it.hasNext()) {
					it.next().getStub().receiveOperationIS(
		                    new UpdateNeighborOperation(
		                    		this.getNeighborEntry(), dimension, getOppositeDirection(direction)));
				}
			}
    	}
    }
    
    /**
     * Returns the {@link NeighborEntry} associated to the current overlay.
     * 
     * @return the {@link NeighborEntry} associated to the current overlay.
     */
    private NeighborEntry getNeighborEntry() {
    	return new NeighborEntry(this.getId(), this.getRemotePeer(), this.zone);
    }

    /**
     * Send a {@link Operation} to a list {@link Peer}.
     * 
     * @param remotePeers
     *            the list of peers to which to send the message.
     * @param msg
     *            the message to send.
     * 
     * @return the list of responses in agreement with the type of message sent.
     * @throws Exception
     *             this exception appears when a message cannot be send to a
     *             peer.
     */
    public List<ResponseOperation> sendTo(List<Peer> remotePeers, Operation msg)
            throws Exception {
        List<ResponseOperation> responses = new ArrayList<ResponseOperation>(
                remotePeers.size());

        for (Peer remotePeer : remotePeers) {
            responses.add(remotePeer.receiveOperationIS(msg));
        }

        return responses;
    }

    /**
     * Send a {@link Operation} to a peers that are in the specified
     * {@link NeighborTable}.
     * 
     * @param table
     *            the peers managed by the data structure to which to send the
     *            message.
     * @param msg
     *            the message to send.
     * 
     * @return the list of responses in agreement with the type of message sent.
     * @throws Exception
     *             this exception appears when a message cannot be send to a
     *             peer.
     */
    public List<ResponseOperation> sendTo(NeighborTable table,
            Operation msg) throws Exception {
        List<ResponseOperation> responses = new ArrayList<ResponseOperation>();

        for (int dimension = 0; dimension < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (int direction = 0; direction < 2; direction++) {
                for (NeighborEntry entry : table.get(dimension, direction).values()) {
                    responses.add(entry.getStub().receiveOperationIS(msg));
                }
            }
        }

        return responses;
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
    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public OverlayType getType() {
        return OverlayType.CAN;
    }

    public void dumpNeighbors() {
        logger.debug("Peer managing " + this.zone);
        NeighborTable neighborTable = this.getNeighborTable();

        for (int dimension = 0; dimension < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (int direction = 0; direction < 2; direction++) {
                for (NeighborEntry entry : neighborTable.get(dimension, direction).values()) {
                    logger.debug("  * " + entry.getZone() + ", dimension=" + dimension
                            + ", direction="
                            + direction);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
    	if (this.zone == null) {
    		return this.getId().toString();
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
    public static int getNextDimension(int dimension) {
        return (dimension + 1) % DefaultProperties.CAN_NB_DIMENSIONS.getValue();
    }

    /**
     * Returns the opposite direction of the specified direction.
     * 
     * @param direction
     *            the specified direction.
     * 
     * @return the opposite direction.
     */
    public static int getOppositeDirection(int direction) {
        return (direction + 1) % 2;
    }

    /**
     * Returns the previous dimension following the specified dimension.
     * 
     * @param dimension
     *            the specified dimension.
     * @return the previous dimension following the specified dimension.
     */
    public static int getPreviousDimension(int dimension) {
        int dim = dimension - 1;
        if (dim < 0) {
            dim = DefaultProperties.CAN_NB_DIMENSIONS.getValue();
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
     * @return <code>true</code> if the {@link NeighborTable} contains the peer
     *         identifier as neighbor, <code>false</code> otherwise.
     */
    public boolean hasNeighbor(UUID peerID) {
        return this.neighborTable.contains(peerID);
    }

    /*
     * Messages handling
     */

    public EmptyResponseOperation handleLeaveMessage(LeaveOperation msg) {
        this.neighborTable.remove(msg.getPeerHavingLeft());

        for (NeighborEntry entry : msg.getNeighborsToMergeWith()) {
            if (!this.getNeighborTable().contains(entry.getId())) {
                this.neighborTable.add(entry, msg.getDimension(), msg.getDirection());
            }
        }
        this.removeOutdatedNeighbors();

        return new EmptyResponseOperation();
    }

    public EmptyResponseOperation handleMergeMessage(MergeOperation msg) {
        try {
            this.setZone(this.zone.merge(msg.getZoneToReallocate()));
            this.mergeDataReceived(msg);
        } catch (ZoneException e) {
            e.printStackTrace();
        }

        int dimension = msg.getDimension();
        int direction = msg.getDirection();

        int index = -1, t = 0;
        for (SplitEntry entry : this.splitHistory) {
            if (entry.getDimension() == dimension && entry.getDirection() == direction) {
                index = t;
            }
            t++;
        }

        if (index >= 0) {
            this.splitHistory.remove(index);
        }

        this.neighborTable.remove(msg.getPeerToMergeWith(), dimension, direction);
        this.neighborTable.addAll(msg.getNeighborsToReallocate());
        this.removeOutdatedNeighbors();

        return new EmptyResponseOperation();
    }

    /*
     * Specific implementation of ProActive concepts.
     */
    @Override
    public void initActivity(Body body) {
        // The following methods have to be served as immediate
        // services in order to allow concurrent queries
        PAActiveObject.setImmediateService("send");
        PAActiveObject.setImmediateService("route");
    }
    
    @Override
    public void endActivity(Body body) {
    	
    }

    public void runActivity(Body body) {
        Service service = new Service(body);

        while (body.isActive()) {
//            Request request = null;
//            if (super.getLocalPeer().getPeersWhichArePreparingToLeave().size() > 0) {
//                /*
//                 * boolean receiveFromLeaver = false; request =
//                 * service.blockingRemoveOldest();
//                 * 
//                 * for (Peer peer : this.peersWhichAreLeaving) { if
//                 * (peer.getBody().getID().equals(request.getSender().getID()))
//                 * { System.out.println("OK FIND !!!"); receiveFromLeaver =
//                 * true; service.serve(request); break; } }
//                 * 
//                 * if (!receiveFromLeaver) { System.out.println("NT FIND");
//                 */
//                service.serveOldest("notifyNeighborEndLeave");
//                // }
//            } else {
//                request = service.blockingRemoveOldest(this.getUpdateIntervalTime());
//
//                if (request == null) {
//                    this.update();
//                } else {
//                    service.serve(request);
//                }
//            }
        	service.serveOldest();
        }
    }

}
