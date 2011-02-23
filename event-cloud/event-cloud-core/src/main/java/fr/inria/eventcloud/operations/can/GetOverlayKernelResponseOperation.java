package fr.inria.eventcloud.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;

import fr.inria.eventcloud.kernel.SemanticSpaceOverlayKernel;

/**
 * @author lpellegr
 */
@SuppressWarnings("serial")
public class GetOverlayKernelResponseOperation implements ResponseOperation {

    private SemanticSpaceOverlayKernel stub;

    public GetOverlayKernelResponseOperation(SemanticSpaceOverlayKernel kernel) {
        this.stub = kernel;
    }

    public SemanticSpaceOverlayKernel getSemanticSpaceOverlayKernel() {
        return this.stub;
    }

}
