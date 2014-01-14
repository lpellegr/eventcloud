package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.util.List;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.multiactivity.compatibility.StatefulCompatibilityMap;
import org.objectweb.proactive.multiactivity.policy.DefaultServingPolicy;

/**
 * Extends the default serving policy to have join and leave operations that
 * have higher priority over other requests when a join, leave or reassign
 * operation is running.
 * 
 * @author lpellegr
 */
public class PeerServingPolicy extends DefaultServingPolicy {

    // private final PeerImpl peer;

    public PeerServingPolicy(PeerImpl peer) {
        super();
        // this.peer = peer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int runPolicyOnRequest(int requestIndexInRequestQueue,
                                        StatefulCompatibilityMap compatibility,
                                        List<Request> runnableRequests) {
        /* 
         * We should manage the following cases:
         * 
         * 1) join or JoinIntroduce should pass in front of routing requests
         * 2) a routing request should not be executed if a mutual exclusion section has been requested
         */

        // Iterator<Request> it = compatibility.getQueueContents().iterator();
        //
        // boolean isSomethingExecuting =
        // compatibility.getExecutingRequests().isEmpty();
        //
        // MaintenanceId requestMaintenanceId = null;
        //
        // while (it.hasNext()) {
        // Request request = it.next();
        // // if (isStructureRequest(request)) {
        // // if (isSomethingExecuting) {
        // // return requestIndexInRequestQueue;
        // // } else {
        // //
        // // }
        // // }
        //
        // if ((containMaintenanceRequest = getMaintenanceId(request)) != null)
        // {
        // break;
        // }
        // }
        //
        // if ()
        //
        // if (compatibility.getExecutingRequests().isEmpty()) {
        //
        // // finds the oldest maintenance operation with the same
        // // maintenance id as the one being executed. If one is found, it
        // // is returned for scheduling (and thus overtakes all other
        // // requests that predate it)
        // while (it.hasNext()) {
        // Request request = it.next();
        //
        // MaintenanceId maintenanceIdFromRequest;
        //
        //
        //
        // // handle join/leave/reassign related requests in priority
        // if (isStructureRequest(request)) {
        // // execute the request
        // it.remove();
        // runnableRequests.add(request);
        // return --requestIndexInRequestQueue;
        //
        // } else if ((maintenanceIdFromRequest = getMaintenanceId(request)) !=
        // null) {
        // if (peer.overlay.maintenanceId != null) {
        // if (maintenanceIdFromRequest.equals(peer.overlay.maintenanceId)) {
        // if (compatibility.getExecutingRequests().isEmpty()) {
        // System.out.println("PeerServingPolicy.runPolicyOnRequest() CASE 3");
        // // execute the request
        // it.remove();
        // runnableRequests.add(request);
        // return --requestIndexInRequestQueue;
        // } else {
        // // wait for the termination of requests that are
        // // executing
        // System.out.println("PeerServingPolicy.runPolicyOnRequest() CASE 4");
        // return requestIndexInRequestQueue;
        // }
        // }
        // } else {
        // if (compatibility.getExecutingRequests().isEmpty()) {
        // System.out.println("PeerServingPolicy.runPolicyOnRequest() CASE 5");
        // // execute the request
        // it.remove();
        // runnableRequests.add(request);
        // return --requestIndexInRequestQueue;
        // } else {
        // // wait for the termination of requests that are
        // // executing
        // System.out.println("PeerServingPolicy.runPolicyOnRequest() CASE 6");
        // return requestIndexInRequestQueue;
        // }
        // }
        // }
        // }

        return super.runPolicyOnRequest(
                requestIndexInRequestQueue, compatibility, runnableRequests);
    }

    // private boolean compatible(Request request, MaintenanceId maintenanceId)
    // {
    // if (getMaintenanceId(request) != null) {
    // return ((MaintenanceOperation)
    // request.getParameter(0)).getMaintenanceId()
    // .equals(maintenanceId);
    // }
    //
    // if (request.getMethodCall().getNumberOfParameter() == 2
    // && request.getMethodName().equals("inject")
    // && (((MaintenanceId) request.getParameter(1)).equals(maintenanceId))) {
    // return true;
    // }
    //
    // return false;
    // }

    // private static MaintenanceId getMaintenanceId(Request request) {
    // if (request.getMethodCall().getNumberOfParameter() == 1
    // && request.getMethodName().equals("receive")
    // && (request.getParameter(0) instanceof MaintenanceOperation)) {
    // return ((MaintenanceOperation)
    // request.getParameter(0)).getMaintenanceId();
    // }
    //
    // return null;
    // }

    // private static boolean isStructureRequest(Request request) {
    // int numberOfParameters = request.getMethodCall().getNumberOfParameter();
    //
    // if (numberOfParameters == 1) {
    // String methodName = request.getMethodName();
    // return methodName.equals("join") || methodName.equals("reassign");
    // }
    //
    // if (numberOfParameters == 0) {
    // return request.getMethodName().equals("leave");
    // }
    //
    // return false;
    // }

    // private static boolean isJoinRequest(Request request) {
    // return request.getMethodCall().getNumberOfParameter() == 1
    // && request.getMethodName().equals("join");
    // }
    //
    // private static boolean isLeaveRequest(Request request) {
    // return request.getMethodCall().getNumberOfParameter() == 0
    // && request.getMethodName().equals("leave");
    // }
    //
    // private static boolean isReassignRequest(Request request) {
    // return request.getMethodCall().getNumberOfParameter() == 1
    // && request.getMethodName().equals("reassign");
    // }

    // private boolean isJoinOperation(Request request) {
    // return this.isOperation(request)
    // && (request.getClass() == JoinIntroduceOperation.class ||
    // request.getClass() == JoinWelcomeOperation.class);
    // }
    //
    // private boolean isLeaveOperation(Request request) {
    // return this.isOperation(request)
    // && request.getClass() == LeaveEnlargeZoneOperation.class;
    // }
    //
    // private boolean isOperation(Request request) {
    // return request.getMethodCall().getNumberOfParameter() == 1
    // && request.getMethodName().equals("receive")
    // && (request.getParameter(0) instanceof Operation);
    // }
    //
    // private boolean isReassignRequest(Request request) {
    // return request.getMethodCall().getNumberOfParameter() == 1
    // && request.getMethodName().equals("reassign");
    // }

}
