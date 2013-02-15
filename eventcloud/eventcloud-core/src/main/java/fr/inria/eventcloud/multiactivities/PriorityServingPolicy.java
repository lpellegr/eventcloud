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
package fr.inria.eventcloud.multiactivities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.multiactivity.ServingPolicy;
import org.objectweb.proactive.multiactivity.compatibility.StatefulCompatibilityMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * Priority serving policy for forcing desired methods to be executed in
 * priority over some others.
 * 
 * @author lpellegr
 */
public class PriorityServingPolicy implements ServingPolicy {

    private static final Logger log =
            LoggerFactory.getLogger(PriorityServingPolicy.class);

    private final Multimap<String, RequestPriorityConstraint> priorityConstraints;

    private final TreeMap<Integer, PriorityGroup> priorityGroups;

    public PriorityServingPolicy(
            RequestPriorityConstraint... priorityConstraints) {
        this(Lists.newArrayList(priorityConstraints));
    }

    public PriorityServingPolicy(
            List<RequestPriorityConstraint> priorityConstraints) {
        Builder<String, RequestPriorityConstraint> priorityConstraintsBuilder =
                ImmutableMultimap.<String, RequestPriorityConstraint> builder();

        this.priorityGroups = new TreeMap<Integer, PriorityGroup>();
        // priority group for methods without priority
        this.priorityGroups.put(0, new PriorityGroup(0));

        for (RequestPriorityConstraint constraint : priorityConstraints) {
            priorityConstraintsBuilder.put(
                    constraint.getMethodName(), constraint);

            if (!this.priorityGroups.containsKey(constraint.getPriorityLevel())) {
                this.priorityGroups.put(
                        constraint.getPriorityLevel(), new PriorityGroup(
                                constraint.getPriorityLevel()));
            }
        }

        this.priorityConstraints = priorityConstraintsBuilder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Request> runPolicy(StatefulCompatibilityMap compatibility) {
        // to be declared as runnable a request must respect constraints
        // c1: the request is compatible with all running requests
        // c2: the request is compatible with all the requests preceding it
        // in the queue
        List<Request> queue = compatibility.getQueueContents();

        List<Request> runnableRequests = new ArrayList<Request>();
        List<Integer> runnableRequestsIndexes = new ArrayList<Integer>();

        if (queue.isEmpty()) {
            return runnableRequests;
        }

        log.trace("Taking serving decision");
        log.trace("Compatible requests");

        for (int i = 0; i < queue.size(); i++) {
            Request request = queue.get(i);

            List<Request> precedingRequests = queue.subList(0, i);

            // the request satisfies c1 and c2
            if (compatibility.isCompatibleWithExecuting(request)
                    && compatibility.isCompatibleWithRequests(
                            request, precedingRequests)) {
                runnableRequests.add(request);
                runnableRequestsIndexes.add(i);

                log.trace("  {}", toString(request));
            }
        }

        // keep only requests with the highest and same priority level
        // others we be selected during the next round
        List<Request> result =
                this.applyPriorities(
                        compatibility, runnableRequests,
                        runnableRequestsIndexes);

        return result;
    }

    private List<Request> applyPriorities(StatefulCompatibilityMap compatibility,
                                          List<Request> runnableRequests,
                                          List<Integer> runnableRequestsIndexes) {
        this.clearPriorityGroups();

        for (int i = 0; i < runnableRequests.size(); i++) {
            Request request = runnableRequests.get(i);
            // request index in request queue
            int requestIndex = runnableRequestsIndexes.get(i);

            Collection<RequestPriorityConstraint> possibleConstraintsFulfilled =
                    this.priorityConstraints.get(request.getMethodName());

            if (possibleConstraintsFulfilled.isEmpty()) {
                this.addToDefaultPriorityGroup(request, requestIndex);
                continue;
            }

            for (RequestPriorityConstraint priorityConstraint : possibleConstraintsFulfilled) {
                if (this.satisfies(request, priorityConstraint)) {
                    this.priorityGroups.get(
                            priorityConstraint.getPriorityLevel()).add(
                            request, requestIndex);
                } else {
                    this.addToDefaultPriorityGroup(request, requestIndex);
                }
            }
        }

        log.trace("Highest priority is {}", this.priorityGroups.lastKey());

        // find highest priority group
        for (PriorityGroup priorityGroup : this.priorityGroups.descendingMap()
                .values()) {
            if (priorityGroup.size() > 0) {
                if (log.isTraceEnabled()) {

                    log.trace(
                            "Highest valid priority group is {}",
                            priorityGroup.getPriorityLevel());
                    for (Request request : priorityGroup.getRequests()) {
                        log.trace("  {}", toString(request));
                    }
                }

                // removes entries from the queue according to indexes memorized
                Iterator<Request> it =
                        compatibility.getQueueContents().iterator();

                int i = 0;
                while (it.hasNext()) {
                    it.next();

                    if (priorityGroup.getRequestPositions().contains(i)) {
                        it.remove();
                    }

                    i++;
                }

                return priorityGroup.getRequests();
            }
        }

        return new ArrayList<Request>();
    }

    private boolean satisfies(Request request,
                              RequestPriorityConstraint priorityConstraint) {
        boolean sameNames =
                request.getMethodCall().getName().equals(
                        priorityConstraint.getMethodName());
        boolean sameParameters = true;

        if (priorityConstraint.getParameterTypes() != null) {
            for (int i = 0; i < priorityConstraint.getParameterTypes().size(); i++) {
                Class<?> parameterClazz =
                        priorityConstraint.getParameterTypes().get(i);

                if (i >= request.getMethodCall().getNumberOfParameter()) {
                    sameParameters = false;
                    break;
                } else {
                    sameParameters &=
                            request.getMethodCall()
                                    .getParameter(i)
                                    .getClass()
                                    .equals(parameterClazz);
                }
            }
        }

        return sameNames && sameParameters;
    }

    private void addToDefaultPriorityGroup(Request request, int requestPosition) {
        this.priorityGroups.get(0).add(request, requestPosition);
    }

    private void clearPriorityGroups() {
        for (PriorityGroup priorityGroup : this.priorityGroups.values()) {
            priorityGroup.clear();
        }
    }

    private static String toString(Request request) {
        StringBuilder result = new StringBuilder();

        result.append("methodCallName=[");
        result.append(request.getMethodCall().getName());
        result.append("]");
        result.append(" ");

        for (int i = 0; i < request.getMethodCall().getNumberOfParameter(); i++) {
            result.append(request.getMethodCall().getParameter(i).getClass());

            if (i < request.getMethodCall().getNumberOfParameter() - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }

}
