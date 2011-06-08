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
package fr.inria.eventcloud.messages.response.can;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastResponseRouter;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import fr.inria.eventcloud.messages.request.can.StatefulQuadruplePatternRequest;
import fr.inria.eventcloud.overlay.SparqlRequestResponseManager;

/**
 * Response associated to {@link StatefulQuadruplePatternRequest}.
 * 
 * @author lpellegr
 */
public abstract class StatefulQuadruplePatternResponse<T> extends
        AnycastResponse {

    private static final long serialVersionUID = 1L;

    // TODO compute the value
    private long stateActionsTime;

    protected List<SerializedValue<T>> subResults;

    public StatefulQuadruplePatternResponse(
            StatefulQuadruplePatternRequest<T> request) {
        super(request);
        this.subResults = new ArrayList<SerializedValue<T>>();
    }

    public T getResult() {
        return this.mergeSubResults(this.subResults);
    }

    public abstract T mergeSubResults(List<SerializedValue<T>> subResults);

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized void addSubResult(AnycastResponse subResponse) {
        this.subResults.addAll(((StatefulQuadruplePatternResponse<T>) subResponse).subResults);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnycastResponseRouter<AnycastResponse> getRouter() {
        return new AnycastResponseRouter<AnycastResponse>() {
            @Override
            @SuppressWarnings("unchecked")
            public void makeDecision(StructuredOverlay overlay,
                                     AnycastResponse response) {
                Future<T> result =
                        (Future<T>) ((SparqlRequestResponseManager) overlay.getRequestResponseManager()).getPendingResults()
                                .remove(response.getId());
                if (result != null) {
                    // ensure that the query operation has terminated
                    // before to send back the request
                    try {
                        ((StatefulQuadruplePatternResponse<T>) response).subResults.add(new SerializedValue<T>(
                                result.get()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                super.makeDecision(overlay, response);
            }
        };
    }

}
