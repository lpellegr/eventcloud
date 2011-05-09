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

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;

import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.messages.response.can.ShutdownResponse;

/**
 * Request used to shutdown the datastores associated to each peer over the
 * overlay.
 * 
 * @author lpellegr
 */
public class ShutdownRequest extends AnycastRequest {

    private static final long serialVersionUID = 1L;

    public ShutdownRequest() {
        super(new DefaultAnycastConstraintsValidator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Router<? extends RequestResponseMessage<StringCoordinate>, StringCoordinate> getRouter() {
        return new AnycastRequestRouter<AnycastRequest>() {
            @Override
            public void onPeerValidatingKeyConstraints(CanOverlay overlay,
                                                       AnycastRequest request) {
                ((SemanticDatastore) overlay.getDatastore()).close(true);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnycastResponse createResponse() {
        return new ShutdownResponse(this);
    }

}
