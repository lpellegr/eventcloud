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
//package fr.inria.eventcloud.messages.request.can;
//
//import java.util.List;
//
//import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
//import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
//import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;
//import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
//import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import fr.inria.eventcloud.api.messages.request.SparqlQuery;
//import fr.inria.eventcloud.messages.reply.can.AnycastReply;
//import fr.inria.eventcloud.reasoner.RangeQueryCondition;
//import fr.inria.eventcloud.reasoner.RangeQueryOperator;
//import fr.inria.eventcloud.router.can.AnycastRequestRouter;
//import fr.inria.eventcloud.validator.AnycastConstraintsValidator;
//
///**
// * @author lpellegr
// */
//public class RangeQueryRequest extends SparqlRequest {
//
//    private final static Logger logger = LoggerFactory.getLogger(RangeQueryRequest.class);
//
//    private static final long serialVersionUID = 130L;
//
//    private List<RangeQueryCondition> conditions;
//
//    public RangeQueryRequest(SparqlQuery query, final List<RangeQueryCondition> conditions) {
//        super(query, new Coordinate(null, null, null));
//        this.conditions = conditions;
//
//        /*
//         * Extract information in order to set keyToReach for routing on peers
//         * which don't validate constraints
//         */
//        Coordinate keyToReach = new Coordinate(null, null, null);
//
//        for (RangeQueryCondition condition : conditions) {
//            if (condition.getOperator() == RangeQueryOperator.GREATER
//                    || condition.getOperator() == RangeQueryOperator.GREATER_EQUALS) {
//                keyToReach.setElement(condition.getIndex(), condition.getCoordinate());
//            }
//        }
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("Set keyToReach to (" + keyToReach.getElement(0) + ", " + keyToReach.getElement(1) + ", "
//                    + keyToReach.getElement(2) + ")");
//        }
//        super.setKeyToReach(keyToReach);
//        logger.debug("New RangeQueryMessage created");
//    }
//
//    /**
//     * @return the conditions
//     */
//    public List<RangeQueryCondition> getConditions() {
//        return this.conditions;
//    }
//
//    public AnycastRequestRouter<RangeQueryRequest> getRouter() {
//        ConstraintsValidator<Coordinate> validator =  new AnycastConstraintsValidator<Coordinate>() {
//            public boolean validatesKeyConstraints(StructuredOverlay overlay, Coordinate key) {
//                return this.validatesKeyConstraints(((AbstractCanOverlay) overlay).getZone(), key);
//            }
//
//            public boolean validatesKeyConstraints(Zone zone, Coordinate key) {
//                for (RangeQueryCondition condition : conditions) {
//                    if (condition.getOperator() == RangeQueryOperator.LESS
//                            && zone.contains(condition.getIndex(), condition.getCoordinate()) > 0) {
//                        return false;
//                    }
//                    if (condition.getOperator() == RangeQueryOperator.LESS_EQUALS
//                            && zone.contains(condition.getIndex(), condition.getCoordinate()) > 0) {
//                        return false;
//                    }
//                    if (condition.getOperator() == RangeQueryOperator.GREATER
//                            && zone.contains(condition.getIndex(), condition.getCoordinate()) < 0) {
//                        return false;
//                    }
//                    if (condition.getOperator() == RangeQueryOperator.GREATER_EQUALS
//                            && zone.contains(condition.getIndex(), condition.getCoordinate()) <= 0) {
//                        return false;
//                    }
//                }
//                return true;
//            }
//        };
//        
//        return new AnycastRequestRouter<RangeQueryRequest>(validator) {
//            public void onPeerValidatingKeyConstraints(AbstractCanOverlay overlay,
//                    AnycastRequest msg) {
//
//            }
//        };
//    }
//
//    public AnycastReply<?> createReply() {
//        return super.getSparqlConstructQuery().createResponseMessage(this);
//    }
//
//    public String toString() {
//        StringBuffer buf = new StringBuffer();
//
//        for (int i = 0; i < this.conditions.size(); i++) {
//            buf.append(this.conditions.get(i));
//            if (i < this.conditions.size() - 1) {
//                buf.append(" && ");
//            }
//        }
//
//        return buf.toString();
//    }
//
// }
