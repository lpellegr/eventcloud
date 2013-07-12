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
package org.objectweb.proactive.extensions.p2p.structured.multiactivies;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.multiactivity.compatibility.StatefulCompatibilityMap;
import org.objectweb.proactive.multiactivity.policy.DefaultServingPolicy;

import com.google.common.collect.ImmutableList;

/**
 * Extends the default serving policy to have join and leave operations that
 * have higher priority over other requests when a join, leave or reassign
 * operation is running.
 * 
 * @author lpellegr
 */
public class PeerServingPolicy extends DefaultServingPolicy {

    /**
     * {@inheritDoc}
     */
    @Override
    public int runPolicyOnRequest(int requestIndexInRequestQueue,
                                  StatefulCompatibilityMap compatibility,
                                  List<Request> runnableRequests) {
        Collection<Request> executingRequests =
                compatibility.getExecutingRequests();

        Request requestOfInterest;

        // check if executing requests contain a join request
        if ((requestOfInterest = this.getReassignRequest(executingRequests)) != null) {
            Iterator<Request> it = compatibility.getQueueContents().iterator();

            // find the oldest join or leave operation in the request queue. If
            // one is found, it is returned for scheduling (and thus overtakes
            // all other requests that predate it)
            while (it.hasNext()) {
                Request request = it.next();

                // TODO should check operation ID
                if (this.isJoinOperation(request)
                        || this.isLeaveOperation(request)) {
                    it.remove();
                    runnableRequests.addAll(ImmutableList.of(request));
                    return --requestIndexInRequestQueue;
                }
            }
        } else if ((requestOfInterest = this.getJoinRequest(executingRequests)) != null) {
            Iterator<Request> it = compatibility.getQueueContents().iterator();

            // find the oldest join operation in the request queue. If one is
            // found, it is returned for scheduling (and thus overtakes all
            // other requests that predate it)
            while (it.hasNext()) {
                Request request = it.next();

                // TODO should check operation ID
                if (this.isJoinOperation(request)) {
                    it.remove();
                    runnableRequests.addAll(ImmutableList.of(request));
                    return --requestIndexInRequestQueue;
                }
            }
        } else if ((requestOfInterest = this.getLeaveRequest(executingRequests)) != null) {
            Iterator<Request> it = compatibility.getQueueContents().iterator();

            // find the oldest leave operation in the request queue. If one is
            // found, it is returned for scheduling (and thus overtakes all
            // other requests that predate it)
            while (it.hasNext()) {
                Request request = it.next();

                // TODO should check operation ID
                if (this.isLeaveOperation(request)) {
                    it.remove();
                    runnableRequests.addAll(ImmutableList.of(request));
                    return --requestIndexInRequestQueue;
                }
            }
        }

        return super.runPolicyOnRequest(
                requestIndexInRequestQueue, compatibility, runnableRequests);
    }

    private Request getJoinRequest(Collection<Request> requests) {
        for (Request request : requests) {
            if (this.isJoinRequest(request)) {
                return request;
            }
        }

        return null;
    }

    private Request getLeaveRequest(Collection<Request> requests) {
        for (Request request : requests) {
            if (this.isLeaveRequest(request)) {
                return request;
            }
        }

        return null;
    }

    private Request getReassignRequest(Collection<Request> requests) {
        for (Request request : requests) {
            if (this.isReassignRequest(request)) {
                return request;
            }
        }

        return null;
    }

    private boolean isJoinRequest(Request request) {
        return request.getMethodCall().getNumberOfParameter() == 1
                && request.getMethodName().equals("join");
    }

    private boolean isLeaveRequest(Request request) {
        return request.getMethodCall().getNumberOfParameter() == 0
                && request.getMethodName().equals("leave");
    }

    private boolean isJoinOperation(Request request) {
        return this.isOperation(request)
                && ((Operation) request.getParameter(0)).isJoinOperation();
    }

    private boolean isLeaveOperation(Request request) {
        return this.isOperation(request)
                && ((Operation) request.getParameter(0)).isLeaveOperation();
    }

    private boolean isOperation(Request request) {
        return request.getMethodCall().getNumberOfParameter() == 1
                && request.getMethodName().equals("receive")
                && (request.getParameter(0) instanceof Operation);
    }

    private boolean isReassignRequest(Request request) {
        return request.getMethodCall().getNumberOfParameter() == 1
                && request.getMethodName().equals("reassign");
    }

}
