/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.messages.request.can;

import java.util.concurrent.Callable;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.AnycastConstraintsValidator;

import fr.inria.eventcloud.config.EventCloudProperties;
import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.overlay.SparqlRequestResponseManager;
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

    public ClosableIterableWrapper queryDatastore(CanOverlay overlay) {
        return new ClosableIterableWrapper(
                ((SemanticDatastore) overlay.getDatastore()).sparqlConstruct(
                        EventCloudProperties.DEFAULT_CONTEXT,
                        this.sparqlConstructQuery));
    }

    public AnycastRequestRouter<SparqlRequest> getRouter() {
        return new AnycastRequestRouter<SparqlRequest>() {
            @Override
            public void onPeerValidatingKeyConstraints(final CanOverlay overlay,
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
