package fr.inria.eventcloud.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.RunnableOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

import fr.inria.eventcloud.load_balancing.LoadReport;
import fr.inria.eventcloud.load_balancing.services.RelativeLoadBalancingService;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Operation used to disseminate load information to peers whose immediate
 * reference is known.
 * 
 * @author lpellegr
 */
public class RegisterLoadReportOperation extends RunnableOperation {

    private static final long serialVersionUID = 160L;

    private final LoadReport loadReport;

    public RegisterLoadReportOperation(LoadReport loadReport) {
        super();
        this.loadReport = loadReport;
    }

    @Override
    public void handle(StructuredOverlay overlay) {
        ((RelativeLoadBalancingService) ((SemanticCanOverlay) overlay).getLoadBalancingManager()
                .getLoadBalancingService()).register(this.loadReport);
    }

}
