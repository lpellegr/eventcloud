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
package org.objectweb.proactive.extensions.p2p.structured.configuration;

import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

/**
 * Defines properties for the p2p-structured module.
 * 
 * @author lpellegr
 */
public class P2PStructuredProperties {

    public static final PropertyString GCM_PROVIDER = new PropertyString(
            "gcm.provider", Fractive.class.getName());

    public static final PropertyString PEER_ADL = new PropertyString(
            "peer.adl", Peer.class.getName());

    public static final PropertyString PEER_SERVICES_ITF = new PropertyString(
            "peer.services.itf", "peer-services");

    public static final PropertyString TRACKER_ADL = new PropertyString(
            "tracker.adl", Tracker.class.getName());

    public static final PropertyString TRACKER_SERVICES_ITF =
            new PropertyString("tracker.services.itf", "tracker-services");

    public static final PropertyByte CAN_NB_DIMENSIONS = new PropertyByte(
            "can.nb.dimensions", (byte) 3);

    public static final PropertyString CAN_LOWER_BOUND = new PropertyString(
            "can.lower.bound", "0");

    public static final PropertyString CAN_UPPER_BOUND = new PropertyString(
            "can.upper.bound", "{");

    public static final PropertyInteger CAN_LEAVE_RETRY_MIN =
            new PropertyInteger("can.leave.retry.min", 2000);

    public static final PropertyInteger CAN_LEAVE_RETRY_MAX =
            new PropertyInteger("can.leave.retry.max", 5000);

    public static final PropertyInteger CAN_REFRESH_TASK_START =
            new PropertyInteger("can.task.refresh.start", 0);

    public static final PropertyInteger CAN_REFRESH_TASK_INTERVAL =
            new PropertyInteger("can.task.refresh.interval", 500);

    /**
     * Indicates which representation to use for coordinates when they are
     * printed on the standard output. The values can be {@code alpha} for a
     * standard String representation or {@code codepoints} in order to display
     * the code points values.
     */
    public static final PropertyString CAN_COORDINATE_DISPLAY =
            new PropertyString("can.coordinate.display", "alpha");

    public static final PropertyString CHORD_ID_REPRESENTATION =
            new PropertyString("chord.id.representation", "hexadecimal");

    public static final PropertyInteger CHORD_ID_DISPLAYED_BYTES =
            new PropertyInteger("chord.id.displayed.bytes", 4);

    public static final PropertyInteger CHORD_NB_TOLERANT_FAILURES =
            new PropertyInteger("chord.nb.tolerant.failures", 0);

    public static final PropertyInteger CHORD_CHECK_PREDECESSOR_TASK_START =
            new PropertyInteger("chord.check.predecessor.task.start", 6000);

    public static final PropertyInteger CHORD_CHECK_PREDECESSOR_TASK_INTERVAL =
            new PropertyInteger("chord.check.predecessor.task.interval", 12000);

    public static final PropertyInteger CHORD_FIX_FINGER_TASK_START =
            new PropertyInteger("chord.fix.finger.task.start", 0);

    public static final PropertyInteger CHORD_FIX_FINGER_TASK_INTERVAL =
            new PropertyInteger("chord.fix.finger.task.interval", 12000);

    public static final PropertyInteger CHORD_STABILIZE_TASK_START =
            new PropertyInteger("chord.stabilize.task.start", 12000);

    public static final PropertyInteger CHORD_STABILIZE_TASK_INTERVAL =
            new PropertyInteger("chord.stabilize.task.interval", 12000);

    public static final PropertyDouble TRACKER_STORAGE_PROBABILITY =
            new PropertyDouble("tracker.storage.probability", 1.0);

    public static final PropertyInteger TRACKER_JOIN_RETRY_INTERVAL =
            new PropertyInteger("tracker.join.retry.interval", 500);

}
