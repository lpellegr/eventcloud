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
package fr.inria.eventcloud.messages.request.can;

import java.io.IOException;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.messages.response.can.StatelessQuadruplePatternResponse;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * This request is used to send a shutdown message to all the peers that belong
 * to the network. This shutdown message will stop the execution of the
 * datastore and remove the repository on each peer. As response, a
 * {@link StatelessQuadruplePatternResponse} is returned.
 * 
 * @author lpellegr
 */
public class ShutdownRequest extends StatelessQuadruplePatternRequest {

    private static final long serialVersionUID = 130L;

    public ShutdownRequest() {
        super(QuadruplePattern.ANY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerValidatingKeyConstraints(CanOverlay<SemanticElement> overlay,
                                               QuadruplePattern quadruplePattern) {
        // close the datastore associated to the peer
        ((SemanticCanOverlay) overlay).getMiscDatastore().close();
        ((SemanticCanOverlay) overlay).getSubscriptionsDatastore().close();

        try {
            overlay.getRequestResponseManager().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
