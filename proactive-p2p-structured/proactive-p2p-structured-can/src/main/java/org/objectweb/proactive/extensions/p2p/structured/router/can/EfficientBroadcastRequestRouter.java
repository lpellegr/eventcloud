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
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.EfficientBroadcastRequest;
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
public class EfficientBroadcastRequestRouter<T extends AnycastRequest<E>, E extends Element>
extends Router<EfficientBroadcastRequest<E>, Coordinate<E>> {

	private static final Logger logger =
			LoggerFactory.getLogger(EfficientBroadcastRequestRouter.class);

	public EfficientBroadcastRequestRouter() {
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
			EfficientBroadcastRequest<E> request) {
		CanOverlay<E> canOverlay = ((CanOverlay<E>) overlay);
		CanRequestResponseManager messagingManager =
				(CanRequestResponseManager) canOverlay.getRequestResponseManager();
		// retrieves the hostname for debugging purpose
		String hostname = "";
		if (JobLogger.getBcastDebug()) {
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				logger.error("Cannot log broadcast algorithm : "
						+ "hostname couldn't be retrieved");
				e.printStackTrace();
			}
		}
		// the current overlay has already received the request
		if (!messagingManager.receiveRequest(request.getId())) {
			logger.debug(
					"Request {} reached peer {} which has already received it",
					request.getId(), canOverlay.getZone().toString());
			if (JobLogger.getBcastDebug()) {
				Date receiveTime = new Date();
				String timestamp = JobLogger.getDateFormat().format(receiveTime);
				JobLogger.logMessage(request.getId().toString() + "_"
						+ "EfficientBroadcast_" + hostname, "1 " + timestamp
						+ " " + canOverlay.getId() + " " + canOverlay
						.getNeighborTable().size() + JobLogger.getLineSeparator());
			}
			if (request.getResponseProvider() != null) {
				// send back an empty response
				request.getAnycastRoutingList()
				.removeLast()
				.getPeerStub()
				.route(
						request.getResponseProvider().get(
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
				if (JobLogger.getBcastDebug()) {
					Date receiveTime = new Date();
					String timestamp =
							JobLogger.getDateFormat().format(receiveTime);
					JobLogger.logMessage(request.getId().toString() + "_"
							+ "EfficientBroadcast_" + hostname, "0 " + timestamp 
							+ " " + canOverlay.getId() + " " + canOverlay
							.getNeighborTable().size() + JobLogger.getLineSeparator());
				}
				this.onPeerValidatingKeyConstraints(canOverlay, request);
				// sends the message to the other neighbors which validates the
				// constraints
			}
			else {
				// Log "-1" message to say that the overlay is only a router,
				// not a receiver
				if (JobLogger.getBcastDebug()) {
					Date receiveTime = new Date();
					String timestamp =
							JobLogger.getDateFormat().format(receiveTime);
					JobLogger.logMessage(request.getId().toString() + "_"
							+ "EfficientBroadcast_" + hostname, "-1 " + timestamp 
							+ " " + canOverlay.getId() + " " + canOverlay
							.getNeighborTable().size() + JobLogger.getLineSeparator());
				}
			}
			this.handle(overlay, request);
		}
	}

	/**
	 * When this method is called we can be sure that the specified
	 * {@code overlay} validates the constraints. The next step is to propagate
	 * the request to the neighbors which validates the constraints.
	 */
	@Override
	protected void handle(final StructuredOverlay overlay,
			final EfficientBroadcastRequest<E> request) {
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
					this.getNeighborsToSendTo(canOverlay, request);

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
							p.route(request);
						}
					}
				}
			}
		}
	}

	/**
	 * Apply the M-CAN broadcast algorithm to select the peers to forward the
	 * message.
	 * 
	 * @param overlay
	 * @param request
	 * 
	 * @return the neighbors to send the message to.
	 */
	private NeighborTableWrapper<E> getNeighborsToSendTo(final CanOverlay<E> overlay,
			final EfficientBroadcastRequest<E> request) {
		int dimensions = 
				P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue();
		NeighborTableWrapper<E> extendedNeighbors =
				new NeighborTableWrapper<E>();
		NeighborTableWrapper<E> neighborsToSendTo =
				new NeighborTableWrapper<E>();

		// The directions that the node has to cover.
		byte[][] directions;

		// If the request is received by the initiator(there are no given
		// directions) then it has to compute the directions and coordinates
		// that need to be contained by the neighbors who will receive the
		// broadcast request.
		if (request.getDirections() == null) {
			directions = this.getDirections();
		} else {
			directions = request.getDirections();
		}

		// Create an extended neighbor table(one that contains more
		// information regarding the neighbors then the ordinary neighbor
		// table) from the current neighbor table.
		for (byte dim = 0; dim < dimensions ; dim++) {
			for (byte direction = 0; direction < 2; direction++) {
				extendedNeighbors.addAll(
						dim, direction, overlay.getNeighborTable().get(
								dim, direction));
			}
		}
		for (byte dimension = (byte) (dimensions-1) ; dimension >= 0 ; dimension--) {

			for (byte direction = 0; direction < 2; direction++) {
				// A peer only needs to propagate the broadcast request only on
				// the directions that were given to it (in this case if the
				// directions are not equal to -1).
				if (directions[direction][dimension] != -1) {
					for (NeighborEntryWrapper<E> neighbor : extendedNeighbors.get(
							dimension, direction)) {
						boolean contains = true;
						// We need to check all the constraints that are given
						if (dimension == 0) {
							for (byte coordinate = 1; coordinate < dimensions; coordinate++) {
								// MCAN Strategy : If the dimension considered is
								// lower than the
								// dimension of the reception, then check the corner
								// constraint
								// to determine whether the message must be sent to
								// this neighbor.
								if (coordinate > dimension) {
									if (this.contains(
											overlay.getZone(),
											neighbor.getNeighborEntry().getZone(),
											coordinate) == false) {
										contains = false;
									}
								}
							}
						}
						// If the neighbor validates all the above constraints
						// it will receive the broadcast request.
						if (contains == true) {
							byte[][] newDirections =
									this.copyDirections(directions);
							// Remove from the directions array the peer's
							// opposite direction (if it is neighbor with the
							// sender on the x superior side, the x inferior
							// direction is removed, so that it won't return
							// the message to the sender).
							newDirections[(direction + 1) % 2][dimension] = -1;
							neighbor.setDirections(newDirections);
							neighborsToSendTo.add(
									dimension, direction, neighbor);
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
	protected void route(StructuredOverlay overlay,
			EfficientBroadcastRequest<E> request) {
		// This method is not used with this broadcast algorithm because a message
		// has to follow a unique path and no heuristics can be made to route
		// the message to peers validating the constraint. Hence, we always use
		// the EfficientBroadcastRequestRouter#handle method and never this one,
		// since the behavior should be the same for both methods.
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
		if (zone2.getLowerBound(dimension).isBetween(
				zone1.getLowerBound(dimension), zone1.getUpperBound(dimension))) {
			return true;
		}
		return false;
	}

	/**
	 * Initializes the dimensions array.
	 */
	protected byte[][] getDirections() {
		int dimensions = P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue();
		byte[][] directions = new byte[2][dimensions];
		for (byte direction = 0; direction < 2; direction++) {
			for (byte dimension = 0; dimension < dimensions ; dimension++) {
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
		int dimensions = P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue();
		byte[][] directions = new byte[2][dimensions];
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < dimensions ; j++) {
				directions[i][j] = initialDirs[i][j];
			}
		}
		return directions;
	}
}
