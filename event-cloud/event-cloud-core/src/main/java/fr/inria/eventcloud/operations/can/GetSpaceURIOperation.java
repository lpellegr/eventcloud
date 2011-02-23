package fr.inria.eventcloud.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.GenericResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;

/**
 * Message used to retrieve the {@link SemanticSpaceOverlayKernel} remote
 * reference from a targeted {@link Peer}.
 * 
 * @author lpellegr
 */
public class GetSpaceURIOperation implements Operation {

    private static final long serialVersionUID = 1L;

    public ResponseOperation handle(StructuredOverlay overlay) {
        return new GenericResponseOperation<URI>(
        				((SemanticSpaceCanOverlay) overlay).getSpaceURI());
    }

}
