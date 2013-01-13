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
package org.objectweb.proactive.extensions.p2p.structured.router.can;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.logger.JobLogger;
import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.OptimalBroadcastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanRequestResponseManager;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntryWrapper;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTableWrapper;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This router is used to route the messages of type {@link AnycastRequest}. The
 * request is supposed to reach one or more peers depending of the key
 * associated to the request to route.
 * 
 * @param <T>
 *            the request type to route.
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author jrochas
 */
public class OptimalBroadcastRequestRouter<T extends AnycastRequest<E>, E extends Element>
extends Router<OptimalBroadcastRequest<E>, Coordinate<E>> {

	private static final Logger logger =
			LoggerFactory.getLogger(OptimalBroadcastRequestRouter.class);

	public OptimalBroadcastRequestRouter() {
		super();
	}

	/**
	 * This method is called just before the next routing step when the request
	 * reach a peer which validates the routing constraints. By default the
	 * implementation is empty. This method must be overridden if necessary.
	 * 
	 * @param overlay
	 *            the {@link CanOverlay} of the peer which validates
	 *            constraints.
	 * 
	 * @param request
	 *            the message which is handled.
	 */
	public void onPeerValidatingKeyConstraints(CanOverlay<E> overlay,
			AnycastRequest<E> request) {
		// to be override if necessary
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void makeDecision(StructuredOverlay overlay,
			OptimalBroadcastRequest<E> request) {
		CanOverlay<E> canOverlay = ((CanOverlay<E>) overlay);
		CanRequestResponseManager messagingManager =
				(CanRequestResponseManager) canOverlay.getRequestResponseManager();
		// retrieves the hostname for debugging purpose
		String hostname = "";
		if (JobLogger.bcastDebugMode) {
			try {
				hostname = InetAddress.getLocalHost().getHostName() ;
			} catch (UnknownHostException e) {
				logger.error("Cannot log broadcast algorithm : " + 
							"hostname couldn't be retrieved");
				e.printStackTrace();
			}
		}
		// the current overlay has already received the request
		if (!messagingManager.receiveRequest(request.getId())) {
			logger.debug(
					"Request {} reached peer {} which has already received it",
					request.getId(), canOverlay.getZone().toString());
			if (JobLogger.bcastDebugMode) {
				Date receiveTime = new Date();
				String timestamp = JobLogger.DATE_FORMAT.format(receiveTime);
				JobLogger.logMessage(request.getId().toString() + "_" + 
						"OptimalBroadcast_" + hostname, 
						"1 " + timestamp + JobLogger.RETURN);
			}
			if (request.getResponseProvider() != null) {
				// send back an empty response
				request.getAnycastRoutingList()
				.removeLast()
				.getPeerStub()
				.route(request.getResponseProvider().get(
								request, overlay));
			}
		} else {
			// the current overlay validates the constraints
			if (request.validatesKeyConstraints(canOverlay)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Request " + request.getId() + " is on peer "
							+ overlay + " which validates constraints "
							+ request.getKey());
				}
				if (JobLogger.bcastDebugMode) {
					Date receiveTime = new Date();
					String timestamp = JobLogger.DATE_FORMAT.format(receiveTime);
					JobLogger.logMessage(request.getId().toString() + "_" +
							"OptimalBroadcast_" + hostname, 
							"0 " + timestamp + JobLogger.RETURN);
				}
				this.onPeerValidatingKeyConstraints(canOverlay, request);
				// sends the message to the other neighbors which validates the
				// constraints
				this.handle(overlay, request);
			} else {
				// We should only be in this case if the broadcast is not total
				this.route(overlay, request);
			}
		}
	}

	/**
	 * When this method is called we can be sure that the specified
	 * {@code overlay} validates the constraints. The next step is to propagate
	 * the request to the neighbors which validates the constraints.
	 */
	@Override
	protected void handle(final StructuredOverlay overlay,
			final OptimalBroadcastRequest<E> request) {
		@SuppressWarnings("unchecked")
		CanOverlay<E> canOverlay = (CanOverlay<E>) overlay;

		// the current peer has no neighbor: this means that the query can
		// only be handled by itself
		if (canOverlay.getNeighborTable().size() == 0) {
			super.onDestinationReached(overlay, request);

			if (request.getResponseProvider() != null) {
				overlay.getResponseEntries().put(
						request.getId(), new ResponseEntry(1));
				AnycastResponse<E> response =
						(AnycastResponse<E>) request.getResponseProvider().get(
								request, overlay);
				response.incrementHopCount(1);
				response.route(overlay);
			}
		} else {
			NeighborTableWrapper<E> neighborsToSendTo =
					this.getNeighborsToSendTo(canOverlay, (OptimalBroadcastRequest<E>) request);

			// neighborsToSendTo equals 0 means that we don't have to route the
			// query anymore: we are on a leaf and the response must be
			// returned;
			if (neighborsToSendTo.size() == 0) {
				super.onDestinationReached(overlay, request);

				if (request.getResponseProvider() != null) {
					AnycastResponse<E> response =
							(AnycastResponse<E>) request.getResponseProvider()
							.get(request, overlay);
					response.incrementHopCount(1);
					overlay.getResponseEntries().put(
							response.getId(), new ResponseEntry(1));
					response.route(overlay);
				}
			}
			// neighborsToSendTo > 0 means we have to perform many send
			// operation and the current peer must await for the number
			// of responses sent.
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Sending request " + request.getId() + " to "
							+ neighborsToSendTo.size() + " neighbor(s) from "
							+ overlay);
				}

				// adds entry containing the number of responses awaited
				final ResponseEntry entry =
						new ResponseEntry(neighborsToSendTo.size());

				if (request.getResponseProvider() != null) {
					overlay.getResponseEntries().put(request.getId(), entry);

					// constructs the routing list used by responses for routing
					// back
					request.getAnycastRoutingList().add(
							new AnycastRoutingEntry(
									overlay.getId(), overlay.getStub()));
				}

				for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
					for (byte direction = 0; direction < 2; direction++) {
						Iterator<NeighborEntryWrapper<E>> it =
								neighborsToSendTo.get(dim, direction)
								.iterator();
						while (it.hasNext()) {
							NeighborEntryWrapper<E> neighborEntry = it.next();
							Peer p = neighborEntry.getNeighborEntry().getStub();
							if (logger.isDebugEnabled()) {
								logger.debug("Sending request "
										+ request.getId() + " from " + overlay
										+ " -> " + p);
							}
							request.setDirections(neighborEntry.getDirections());
							request.setSplitPlans(neighborEntry.getSplitPlans());
							p.route(request);
						}
					}
				}
			}
		}
	}

	private NeighborTableWrapper<E> getNeighborsToSendTo(final CanOverlay<E> overlay,
			final OptimalBroadcastRequest<E> request) {
		NeighborTableWrapper<E> extendedNeighbors = new NeighborTableWrapper<E>();
		NeighborTableWrapper<E> neighborsToSendTo = new NeighborTableWrapper<E>();
		// The directions that the node has to cover.
		byte[][] directions;
		// The constraints that the neighbors have to validate in order to
		// receive the message
		Element[] plane;

		// If the request is received by the initiator(there are no given
		// directions) then it has to compute the directions and coordinates
		// that need to be contained by the neighbors who will receive the
		// broadcast request.
		if (request.getDirections() == null) {
			directions = getDirections();
			plane = getPlanToBeContained(overlay);
		} else {
			directions = request.getDirections();
			plane = request.getSplitPlans();
		}

		// Create an extended neighbor table(one that contains more
		// information regarding the neighbors than the ordinary neighbor
		// table) from the current neighbor table.
		for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS
				.getValue(); dim++) {
			for (byte direction = 0; direction < 2; direction++) {
				extendedNeighbors.addAll(dim, direction, overlay.getNeighborTable()
						.get(dim, direction));
			}
		}

		for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS
				.getValue(); dimension++) {
			// We don't need to verify if the neighbor contains the sender's
			// coordinate on the dimension on which they are neighbors.
			plane[dimension] = null;

			for (byte direction = 0; direction < 2; direction++) {
				// A peer only needs to propagate the broadcast request only on
				// the directions that were given to it (in this case if the
				// directions are not equal to -1).
				if (directions[direction][dimension] != -1) {
					for (NeighborEntryWrapper<E> neighbor : extendedNeighbors
							.get(dimension, direction)) {
						boolean contains = true;
						// We need to check all the constraints that are given
						// by the plane array.
						for (byte coordinate = 0; coordinate < P2PStructuredProperties.CAN_NB_DIMENSIONS
								.getValue(); coordinate++) {
							// If plane has the null value on a certain
							// dimension( that is not the dimension on which the
							// sender and peer are neighbors) then in order for
							// the neighbor to receive the broadcast message its
							// zone has to verify a certain inequality: the	
							// sender's lower bound coordinate on the given
							// dimension has to be smaller or equal to the
							// neighbor's lower bound coordinate on the same
							// dimension.
							if (plane[coordinate] == null
									&& coordinate != dimension) {
								if (contains(overlay.getZone(), neighbor
										.getNeighborEntry().getZone(),
										coordinate) == false) {
									contains = false;
								}
							}
							// If the plan has on a certain dimension a value
							// that is not null, then in order for the peer to
							// receive the broadcast message it has to contain
							// that value in its zone (on the given dimension).
							else if (containsCoordinate(neighbor
									.getNeighborEntry().getZone(),
									plane[coordinate], coordinate) == false) {
								contains = false;
							}
						}

						// If the neighbor validates all the above constraints
						// it will receive the broadcast request.
						if (contains == true) {
							byte[][] newDirections = copyDirections(directions);
							Element[] newPlan = copyPlan(plane);
							// Remove from the directions array the peer's
							// opposite direction (if it is neighbor with the
							// sender on the x superior side, the x inferior
							// direction is removed, so that it won't return
							// the message to the sender).
							newDirections[(direction + 1) % 2][dimension] = -1;
							neighbor.setDirections(newDirections);
							neighbor.setSplitPlans(newPlan);
							neighborsToSendTo.add(dimension, direction,
									neighbor);
						}
					}
				}
				// Remove from the directions array the directions that have
				// already been given (even though there were no peers
				// validating the above constraints, we don't need to cover
				// these directions anymore).
				directions[direction][dimension] = -1;
			}
		}

		return neighborsToSendTo;
	}

	/**
	 * Forward the request to the neighbors until a {@link Peer} which validates
	 * the constraints is found.
	 * 
	 * @param overlay
	 *            the overlay used to route the message.
	 * 
	 * @param request
	 *            the message to route.
	 */
	@Override
	protected void route(StructuredOverlay overlay, OptimalBroadcastRequest<E> request) {
		@SuppressWarnings("unchecked")
		NeighborTableWrapper<E> neighborsToSendTo =
				this.getNeighborsToSendTo((CanOverlay<E>)overlay, (OptimalBroadcastRequest<E>) request);
		
		for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
			for (byte direction = 0; direction < 2; direction++) {
				Iterator<NeighborEntryWrapper<E>> it =
						neighborsToSendTo.get(dim, direction)
						.iterator();
				while (it.hasNext()) {
					NeighborEntryWrapper<E> neighborEntry = it.next();
					Peer p = neighborEntry.getNeighborEntry().getStub();
					if (logger.isDebugEnabled()) {
						logger.debug("Sending request "
								+ request.getId() + " from " + overlay
								+ " -> " + p);
					}
					request.setDirections(neighborEntry.getDirections());
					request.setSplitPlans(neighborEntry.getSplitPlans());
					p.route(request);
				}
			}
		}
	}

	/**
	 * Verifies if zone1 contains a certain corner of zone2.
	 * 
	 * @param zone1
	 *            the current peer
	 * @param zone2
	 *            the neighbor
	 * @param dimension
	 *            the dimension on which we do the checking
	 * @return true if zone1 contains the corner of zone2, false otherwise.
	 */
	protected boolean contains(Zone<E> zone1, Zone<E> zone2, byte dimension) {
		if (Element.min(zone1.getLowerBound(dimension),
				zone2.getLowerBound(dimension)) == zone1
				.getLowerBound(dimension)
				|| zone1.getLowerBound(dimension).equals(
						zone2.getLowerBound(dimension))) {
			return true;
		}

		return false;
	}

	/**
	 * Computes the coordinates that need to be validated by the peers receiving
	 * the message.
	 * 
	 * @param can
	 *            the overlay
	 * @return the coordinates to be contained by the peers receiving the
	 *         message.
	 */
	protected Element[] getPlanToBeContained(CanOverlay<E> can) {
		Element[] plan = new Element[P2PStructuredProperties.CAN_NB_DIMENSIONS
		                             .getValue()];

		// In this case the values of the constraints are equal to the lower
		// bound of the initiator.
		// We can choose other constraints too as long as the values are in the
		// initiator's zone.
		for (byte i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS
				.getValue(); i++) {
			plan[i] = can.getZone().getLowerBound(i);
		}

		return plan;
	}

	/**
	 * Checks if a value is included in the zone on a given dimension.
	 * 
	 * @param zone
	 *            the zone that has to contain a given value
	 * @param value
	 *            the value that needs to be contained by the zone
	 * @param dimension
	 *            the dimension on which the zone has to contain the given
	 *            value.
	 * @return true if zone contains the value on the given dimension; false,
	 *         otherwise.
	 */
	protected boolean containsCoordinate(Zone<E> zone, Element value,
			byte dimension) {
		if (value != null) {
			if (value.isBetween(zone.getLowerBound(dimension),
					zone.getUpperBound(dimension)) == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Initializes the dimensions array.
	 */
	protected byte[][] getDirections() {
		byte[][] directions = new byte[2][P2PStructuredProperties.CAN_NB_DIMENSIONS
		                                  .getValue()];
		for (byte direction = 0; direction < 2; direction++) {
			for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS
					.getValue(); dimension++) {
				directions[direction][dimension] = 1;
			}
		}
		return directions;
	}

	/**
	 * Create a new byte array from an existing one
	 * 
	 * @param initialDirs
	 *            initial array
	 * @return the new byte array
	 */
	protected byte[][] copyDirections(byte[][] initialDirs) {
		byte[][] directions = new byte[2][P2PStructuredProperties.CAN_NB_DIMENSIONS
		                                  .getValue()];
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < P2PStructuredProperties.CAN_NB_DIMENSIONS
					.getValue(); j++) {
				directions[i][j] = initialDirs[i][j];
			}
		}
		return directions;
	}

	/**
	 * Create a new StringElement array from an existing one.
	 * 
	 * @param initialPlan
	 *            initial StringElement array
	 * @return the new StringElement array
	 */
	protected Element[] copyPlan(Element[] initialPlan) {
		Element[] plan = new Element[P2PStructuredProperties.CAN_NB_DIMENSIONS
		                             .getValue()];
		for (int j = 0; j < P2PStructuredProperties.CAN_NB_DIMENSIONS
				.getValue(); j++) {
			plan[j] = initialPlan[j];
		}
		return plan;
	}
}
