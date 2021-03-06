/**
 * Copyright (c) 2011-2014 INRIA.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.MulticastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import fr.inria.eventcloud.messages.request.StatefulQuadruplePatternRequest;
import fr.inria.eventcloud.messages.request.StatefulRequestAction;
import fr.inria.eventcloud.overlay.SemanticRequestResponseManager;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * Response associated to {@link StatefulQuadruplePatternRequest}.
 * 
 * @author lpellegr
 */
public abstract class StatefulQuadruplePatternResponse<T> extends
        StatelessQuadruplePatternResponse {

    private static final long serialVersionUID = 160L;

    // time to execute the action associated to the stateful request
    private long actionTime;

    protected List<SerializedValue<T>> intermediateResults;

    public StatefulQuadruplePatternResponse() {
        super();
        this.actionTime = 0;
        this.intermediateResults = new ArrayList<SerializedValue<T>>();
    }

    public T getResult() {
        return this.merge(this.intermediateResults);
    }

    /**
     * Defines how to merge the intermediate results to get one final result.
     * The intermediate results correspond to the results return on each peer
     * validating the constraints.
     * 
     * @param intermediateResults
     *            the results return on each peer validating the constraints.
     * 
     * @return a final result.
     */
    public abstract T merge(List<SerializedValue<T>> intermediateResults);

    /**
     * Returns the time (in nanoseconds) taken to execute the action on the
     * peers which have been visited and that are matching the constraints.
     * 
     * @return the time (in nanoseconds) taken to execute the action on the
     *         peers which have been visited and that are matching the
     *         constraints.
     */
    public long getActionTime() {
        return this.actionTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeSendingBackResponse(StructuredOverlay overlay) {
        if (this.validatesRequestKeyConstraints(overlay)) {
            @SuppressWarnings("unchecked")
            Future<StatefulRequestAction<T>> result =
                    (Future<StatefulRequestAction<T>>) ((SemanticRequestResponseManager) overlay.getRequestResponseManager()).getPendingResults()
                            .remove(super.getId());

            if (result != null) {
                try {
                    this.intermediateResults.add(SerializedValue.create(result.get().result));
                    this.actionTime += result.get().duration;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void mergeAttributes(MulticastResponse<SemanticCoordinate> responseReceived) {
        super.mergeAttributes(responseReceived);

        this.intermediateResults.addAll(((StatefulQuadruplePatternResponse<T>) responseReceived).intermediateResults);
        this.actionTime +=
                ((StatefulQuadruplePatternResponse<T>) responseReceived).actionTime;
    }

}
