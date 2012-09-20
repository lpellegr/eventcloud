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
package fr.inria.eventcloud.operations.can;

import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

import com.google.common.collect.Lists;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Operation used to query the datastore managed by <strong>one peer</strong>
 * with a call to {@link TransactionalDatasetGraph#find(QuadruplePattern)}.
 * 
 * @author lpellegr
 */
public final class FindQuadruplesOperation implements CallableOperation {

    private static final long serialVersionUID = 1L;

    private final QuadruplePattern quadruplePattern;

    private final boolean useSubscriptionsDatastore;

    /**
     * The quadruple pattern to execute against the misc datastore by default.
     * 
     * @param quadruplePattern
     *            the quadruple pattern to resolve.
     */
    public FindQuadruplesOperation(QuadruplePattern quadruplePattern) {
        this(quadruplePattern, false);
    }

    public FindQuadruplesOperation(QuadruplePattern quadruplePattern,
            boolean useSubscriptionsDatastore) {
        this.quadruplePattern = quadruplePattern;
        this.useSubscriptionsDatastore = useSubscriptionsDatastore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperation handle(StructuredOverlay overlay) {
        List<Quadruple> result = null;

        TransactionalTdbDatastore datastore;

        if (this.useSubscriptionsDatastore) {
            datastore =
                    ((SemanticCanOverlay) overlay).getSubscriptionsDatastore();
        } else {
            datastore = ((SemanticCanOverlay) overlay).getMiscDatastore();
        }

        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);

        try {
            result = Lists.newArrayList(txnGraph.find(this.quadruplePattern));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        return new FindQuadruplesResponseOperation(result);
    }

}
