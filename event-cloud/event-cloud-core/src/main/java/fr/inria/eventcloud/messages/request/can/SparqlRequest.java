package fr.inria.eventcloud.messages.request.can;

import java.util.concurrent.Callable;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.AnycastConstraintsValidator;

import fr.inria.eventcloud.config.EventCloudProperties;
import fr.inria.eventcloud.overlay.SparqlRequestResponseManager;
import fr.inria.eventcloud.overlay.can.SemanticCanOverlay;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;

/**
 * A SemanticRequest is a super type used to route a SPARQL query over a CAN
 * overlay network.
 * 
 * @author lpellegr
 */
public abstract class SparqlRequest extends AnycastRequest {

    private static final long serialVersionUID = 1L;

    private String sparqlConstructQuery;

    public SparqlRequest(
            AnycastConstraintsValidator<StringCoordinate> validator,
            String sparqlConstructQuery) {
        super(validator);
        this.sparqlConstructQuery = sparqlConstructQuery;
    }

    /**
     * Returns the sparql query to handle.
     * 
     * @return the sparql query to execute.
     */
    public String getSparqlConstructQuery() {
        return this.sparqlConstructQuery;
    }

    /**
     * Indicates if the key to reach has all its coordinate elements fixed with
     * a not <code>null</code> value or not.
     * 
     * @return <code>true</code> if the key to reach has all its coordinate
     *         elements fixed with a not <code>null</code> value,
     *         <code>false</code> otherwise.
     */
    public boolean keyToReachNotNullElements() {
        for (StringElement elt : super.getKey()) {
            if (elt == null) {
                return false;
            }
        }
        return true;
    }

    public ClosableIterableWrapper queryDatastore(AbstractCanOverlay overlay) {
        return new ClosableIterableWrapper(
                ((SemanticCanOverlay) overlay).getDatastore().sparqlConstruct(
                        EventCloudProperties.DEFAULT_CONTEXT,
                        this.sparqlConstructQuery));
    }

    public AnycastRequestRouter<SparqlRequest> getRouter() {
        return new AnycastRequestRouter<SparqlRequest>() {
            @Override
            public void onPeerValidatingKeyConstraints(final AbstractCanOverlay overlay,
                                                       final AnycastRequest request) {
                final SparqlRequestResponseManager messagingManager =
                        (SparqlRequestResponseManager) overlay.getRequestResponseManager();

                if (!messagingManager.hasReceivedRequest(request.getId())) {
                    // query the datastore at the same time as the query is
                    // propagated
                    messagingManager.getPendingRequestsResult().put(
                            request.getId(),
                            messagingManager.getThreadPool().submit(
                                    new Callable<ClosableIterableWrapper>() {
                                        public ClosableIterableWrapper call() {
                                            return ((SparqlRequest) request).queryDatastore(overlay);
                                        }
                                    }));
                }
            }
        };
    }

}
