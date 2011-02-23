package fr.inria.eventcloud.overlay;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

import fr.inria.eventcloud.api.SemanticSpaceOperations;
import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;

/**
 * SemanticStructuredOverlay is used in order to have a super type for
 * delegating several operations which are specific to SemanticSpace from a
 * {@link SemanticPeer} to a concrete implementation of a {@link StructuredOverlay} like
 * {@link SemanticSpaceCanOverlay} or {@link SemanticSpaceChordOverlay}.
 * 
 * @author lpellegr
 */
public interface SemanticStructuredOverlay extends SemanticSpaceOperations {

}
