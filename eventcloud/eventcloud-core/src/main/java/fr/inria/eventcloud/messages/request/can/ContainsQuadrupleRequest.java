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
package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.ForwardResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.messages.response.can.BooleanForwardResponse;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * A ContainsQuadrupleRequest is a request that is used to know if there is a
 * peer that contains the quadruple that is specified when the request is
 * constructed.
 * 
 * @author lpellegr
 */
public class ContainsQuadrupleRequest extends QuadrupleRequest {

    private static final long serialVersionUID = 150L;

    public ContainsQuadrupleRequest(final Quadruple quad) {
        super(
                quad,
                new ResponseProvider<ForwardResponse<SemanticElement>, Coordinate<SemanticElement>>() {
                    private static final long serialVersionUID = 150L;

                    @Override
                    public ForwardResponse<SemanticElement> get() {
                        return new BooleanForwardResponse() {
                            private static final long serialVersionUID = 150L;

                            @Override
                            public void setAttributes(Request<Coordinate<SemanticElement>> request,
                                                      StructuredOverlay overlay) {
                                super.setAttributes(request, overlay);

                                TransactionalDatasetGraph txnGraph =
                                        ((SemanticCanOverlay) overlay).getMiscDatastore()
                                                .begin(AccessMode.READ_ONLY);

                                try {
                                    this.setResult(txnGraph.contains(quad));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    txnGraph.end();
                                }
                            }
                        };
                    }
                });
    }

}
