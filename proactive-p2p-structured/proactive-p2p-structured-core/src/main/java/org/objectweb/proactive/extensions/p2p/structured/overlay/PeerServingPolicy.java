package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.util.Iterator;
import java.util.List;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.operations.MaintenanceOperation;
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

    private final PeerImpl peer;

    public PeerServingPolicy(PeerImpl peer) {
        this.peer = peer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int runPolicyOnRequest(int requestIndexInRequestQueue,
                                  StatefulCompatibilityMap compatibility,
                                  List<Request> runnableRequests) {
        if (this.peer.overlay != null) {
            MaintenanceId maintenanceId = this.peer.overlay.maintenanceId;

            // checks whether a join, leave or reassign request is executing
            if (maintenanceId != null) {
                Iterator<Request> it =
                        compatibility.getQueueContents().iterator();

                // finds the oldest maintenance operation with the same
                // maintenance id as the one being executed. If one is found, it
                // is returned for scheduling (and thus overtakes all other
                // requests that predate it)
                while (it.hasNext()) {
                    Request request = it.next();

                    if (this.compatible(request, maintenanceId)) {
                        it.remove();
                        runnableRequests.add(request);

                        return --requestIndexInRequestQueue;
                    }
                }
            }
        }

        return super.runPolicyOnRequest(
                requestIndexInRequestQueue, compatibility, runnableRequests);
    }

    private boolean compatible(Request request, MaintenanceId maintenanceId) {
        if (this.isMaintenanceOperation(request)) {
            return ((MaintenanceOperation) request.getParameter(0)).getMaintenanceId()
                    .equals(maintenanceId);
        }

        if (request.getMethodCall().getNumberOfParameter() == 2
                && request.getMethodName().equals("inject")
                && (((MaintenanceId) request.getParameter(1)).equals(maintenanceId))) {
            return true;
        }

        return false;
    }

    private boolean isMaintenanceOperation(Request request) {
        return request.getMethodCall().getNumberOfParameter() == 1
                && request.getMethodName().equals("receive")
                && (request.getParameter(0) instanceof MaintenanceOperation);
    }

    // private boolean isJoinRequest(Request request) {
    // return request.getMethodCall().getNumberOfParameter() == 1
    // && request.getMethodName().equals("join");
    // }
    //
    // private boolean isLeaveRequest(Request request) {
    // return request.getMethodCall().getNumberOfParameter() == 0
    // && request.getMethodName().equals("leave");
    // }
    //
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
