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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.configuration;

/**
 * Defines properties for the p2p-structured modules.
 * 
 * @author lpellegr
 */
public class P2PStructuredProperties {

    private P2PStructuredProperties() {
    }

    public static final PropertyString GCM_PROVIDER = new PropertyString(
            "gcm.provider", "org.objectweb.proactive.core.component.Fractive");

    public static final PropertyString PEER_ADL = new PropertyString(
            "peer.adl",
            "org.objectweb.proactive.extensions.p2p.structured.overlay.Peer");

    public static final PropertyString PEER_SERVICES_ITF = new PropertyString(
            "peer.services.itf", "peer-services");

    public static final PropertyString TRACKER_ADL =
            new PropertyString(
                    "tracker.adl",
                    "org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker");

    public static final PropertyString TRACKER_SERVICES_ITF =
            new PropertyString("tracker.services.itf", "tracker-services");

    public static final PropertyByte CAN_NB_DIMENSIONS = new PropertyByte(
            "can.nb.dimensions", Byte.valueOf((byte) 3));

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

    public static final PropertyDouble TRACKER_STORAGE_PROBABILITY =
            new PropertyDouble("tracker.storage.probability", 1.0);

    public static final PropertyInteger TRACKER_JOIN_RETRY_INTERVAL =
            new PropertyInteger("tracker.join.retry.interval", 500);

    static {
        ConfigurationParser.load(
                P2PStructuredProperties.class,
                "proactive.p2p.structured.configuration",
                System.getProperty("user.home") + "/.proactive/p2p-structured.properties");
    }

}
