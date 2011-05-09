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

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.datastore.SemanticDatastore;

/**
 * Used to remove a statement from the overlay network.
 * 
 * @author lpellegr
 */
public class RemoveStatementRequest extends StatementRequest {

    private static final long serialVersionUID = 1L;

    private final static Logger logger =
            LoggerFactory.getLogger(RemoveStatementRequest.class);

    public RemoveStatementRequest(URI context, Statement stmt) {
        super(context, stmt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestinationReached(StructuredOverlay overlay, URI context,
                                     Statement stmt) {
        ((SemanticDatastore) overlay.getDatastore()).removeStatement(
                context, stmt);

        if (logger.isDebugEnabled()) {
            logger.debug("Statement (" + context + ", " + stmt
                    + ") removed from " + overlay);
        }
    }

}
