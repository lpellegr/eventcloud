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

import static com.google.common.base.Preconditions.checkNotNull;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.messages.response.can.RemoveStatementsResponse;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Removes all statements that validates the specified constraints.
 * 
 * @author lpellegr
 */
public class RemoveStatementsRequest extends AnycastRequest {

    private static final long serialVersionUID = 1L;

    private URI context;

    private Statement statement;

    public RemoveStatementsRequest(final URI context, final Statement statement) {
        super(new DefaultAnycastConstraintsValidator(
                SemanticHelper.createCoordinateWithNullValues(statement)));
        this.context = checkNotNull(context);
        this.statement = statement;
    }

    public AnycastRequestRouter<AnycastRequest> getRouter() {
        return new AnycastRequestRouter<AnycastRequest>() {
            @Override
            public void onPeerValidatingKeyConstraints(CanOverlay overlay,
                                                       AnycastRequest msg) {
                ((SemanticDatastore) overlay.getDatastore()).removeStatement(
                        context, statement);
            }
        };
    }

    public RemoveStatementsResponse createResponse() {
        return new RemoveStatementsResponse(this);
    }

}
