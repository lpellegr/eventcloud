package fr.inria.eventcloud.messages.request.can;


import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCANOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.messages.request.SparqlQuery;
import fr.inria.eventcloud.messages.reply.can.AnycastReply;
import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;
import fr.inria.eventcloud.router.can.AnycastRequestRouter;
import fr.inria.eventcloud.validator.AnycastConstraintsValidator;

/**
 * 
 * @author lpellegr
 */
public class TriplePatternRequest extends SemanticRequest {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = 
            LoggerFactory.getLogger(TriplePatternRequest.class);

    public TriplePatternRequest(SparqlQuery query, Coordinate coordinateToFind) {
        super(query, coordinateToFind);

        if (logger.isDebugEnabled()) {
        	StringBuffer buf = new StringBuffer();
        	buf.append("New TriplePatternQueryMessage created (");
        	buf.append(super.getKeyToReach());
        	buf.append(")");
        	logger.debug(buf.toString());
        }
    }

    public AnycastReply<?> createResponseMessage() {
        return super.getSparqlQuery().createResponseMessage(this);
    }

    public AnycastRequestRouter<TriplePatternRequest> getRouter() {
        ConstraintsValidator<Coordinate> validator = new AnycastConstraintsValidator<Coordinate>() {
            public boolean validatesKeyConstraints(StructuredOverlay overlay, Coordinate key) {
                return this.validatesKeyConstraints(
                			((SemanticSpaceCanOverlay) overlay).getZone(), key);
            }

            public boolean validatesKeyConstraints(Zone zone, Coordinate key) {
                for (int i = 0; i < key.size(); i++) {
                    // if coordinate is null we skip the test
                    if (key.getElement(i) != null) {
                        // the specified overlay does not contains the key
                        if (zone.contains(i, key.getElement(i)) != 0) {
                            return false;
                        }
                    }
                }
                return true;
            }
        };
        
        return new AnycastRequestRouter<TriplePatternRequest>(validator) {
            public void onPeerWhichValidatesKeyConstraints(
                    AbstractCANOverlay overlay, AnycastRequest msg) {
            };
        };
    }

}
