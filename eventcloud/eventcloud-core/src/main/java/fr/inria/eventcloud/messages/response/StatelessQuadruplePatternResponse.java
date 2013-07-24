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
package fr.inria.eventcloud.messages.response;

import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.MulticastResponse;
import org.objectweb.proactive.extensions.p2p.structured.router.can.BroadcastResponseRouter;

import fr.inria.eventcloud.messages.request.StatelessQuadruplePatternRequest;
import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * Response associated to {@link StatelessQuadruplePatternRequest}.
 * 
 * @author lpellegr
 */
public class StatelessQuadruplePatternResponse extends
        MulticastResponse<SemanticElement> {

    private static final long serialVersionUID = 160L;

    public StatelessQuadruplePatternResponse() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BroadcastResponseRouter<MulticastResponse<SemanticElement>, SemanticElement> getRouter() {
        return new BroadcastResponseRouter<MulticastResponse<SemanticElement>, SemanticElement>();
    }

}
