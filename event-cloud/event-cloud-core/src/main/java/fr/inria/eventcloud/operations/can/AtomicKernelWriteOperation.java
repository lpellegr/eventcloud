package fr.inria.eventcloud.operations.can;

import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.ontoware.rdf2go.model.Statement;

import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;

/**
 * Operation used to add triples on the {@link SemanticSpaceOverlayKernel}
 * associated to the {@link Peer} which receives this message.
 * <p>
 * Note that the triples are added without any verification. Therefore, the
 * triples have to be managed by the peer which receives the message or the
 * whole network consistency will be broken.
 * 
 * @author lpellegr
 */
public class AtomicKernelWriteOperation implements Operation {

    private static final long serialVersionUID = 1L;
    
    private List<Statement> triples;


    public AtomicKernelWriteOperation(List<Statement> triples) {
		super();
		this.triples = triples;
	}

	public ResponseOperation handle(StructuredOverlay overlay) {
    	((SemanticSpaceCanOverlay) overlay).getLocalSemanticSpaceOverlayKernel()
    		.atomicAddAll(((SemanticSpaceCanOverlay) overlay).getSpaceURI(), this.triples.iterator());
    	return new EmptyResponseOperation();
    }

}
