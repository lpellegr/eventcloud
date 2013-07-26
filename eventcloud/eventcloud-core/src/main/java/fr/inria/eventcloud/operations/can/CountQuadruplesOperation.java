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
package fr.inria.eventcloud.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.GenericResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;

import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * Simple operation to count the number of quadruples contained on a datastore
 * managed by a {@link SemanticPeer}.
 * 
 * @author lpellegr
 */
public class CountQuadruplesOperation extends CallableOperation {

    private static final long serialVersionUID = 160L;

    private final boolean useSubscriptionDatastore;

    public CountQuadruplesOperation() {
        this.useSubscriptionDatastore = false;
    }

    public CountQuadruplesOperation(boolean useSubscriptionDatastore) {
        this.useSubscriptionDatastore = useSubscriptionDatastore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperation handle(StructuredOverlay overlay) {

        SemanticCanOverlay semanticOverlay = ((SemanticCanOverlay) overlay);

        TransactionalTdbDatastore datastore =
                semanticOverlay.getMiscDatastore();

        if (this.useSubscriptionDatastore) {
            datastore = semanticOverlay.getSubscriptionsDatastore();
        }

        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);

        int result = 0;

        try {
            QueryExecution qExec =
                    QueryExecutionFactory.create(
                            "SELECT (COUNT(*) as ?count) { GRAPH ?g { ?s ?p ?o } } ",
                            txnGraph.getUnderlyingDataset());
            ResultSet rs = qExec.execSelect();
            try {
                result =
                        (Integer) rs.nextBinding()
                                .get(Var.alloc("count"))
                                .getLiteralValue();
            } finally {
                qExec.close();
            }

        } finally {
            txnGraph.end();
        }

        return new GenericResponseOperation<Integer>(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWithRouting() {
        return true;
    }

}
