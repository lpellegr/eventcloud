/**
 * Copyright (c) 2011-2012 INRIA.
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

    public static final PropertyBoolean ENABLE_BENCHMARKS_INFORMATION =
            new PropertyBoolean("enable.benchmarks.information", false);

    public static final PropertyByte CAN_NB_DIMENSIONS = new PropertyByte(
            "can.nb.dimensions", Byte.valueOf((byte) 3));

    /**
     * This property defines the minimum lower bound associated to a CAN
     * network. It is by default set to &#92;u0000 and <strong>should not be
     * edited</strong>.
     */
    public static final PropertyCharacter CAN_LOWER_BOUND =
            new PropertyCharacter("can.lower.bound", '\u0000');

    /**
     * This property defines the maximum upper bound associated to a CAN
     * network. It is by default set to &#92;uffff to support all UTF-16
     * characters. This is an open bound (i.e. no peer will manage it).
     */
    public static final PropertyCharacter CAN_UPPER_BOUND =
            new PropertyCharacter("can.upper.bound", '\uffff');

    public static final PropertyInteger CAN_LEAVE_RETRY_MIN =
            new PropertyInteger("can.leave.retry.min", 2000);

    public static final PropertyInteger CAN_LEAVE_RETRY_MAX =
            new PropertyInteger("can.leave.retry.max", 5000);

    public static final PropertyInteger CAN_REFRESH_TASK_START =
            new PropertyInteger("can.task.refresh.start", 0);

    public static final PropertyInteger CAN_REFRESH_TASK_INTERVAL =
            new PropertyInteger("can.task.refresh.interval", 500);

    /**
     * Indicates which representation to use for printing characters on the
     * standard output. The value of this property can be set to:
     * <ul>
     * <li>{@code codepoints} in order to display the code points values by
     * using the notation</li>
     * <li>{@code decimal} to see the coordinate as a decimal value.</li>
     * <li>{@code default} for a standard String representation</li>
     * </ul>
     */
    public static final PropertyString CAN_COORDINATE_DISPLAY =
            new PropertyString("can.coordinate.display", "codepoints");

    public static final PropertyDouble TRACKER_STORAGE_PROBABILITY =
            new PropertyDouble("tracker.storage.probability", 1.0);

    public static final PropertyInteger TRACKER_JOIN_RETRY_INTERVAL =
            new PropertyInteger("tracker.join.retry.interval", 500);

    static {
        ConfigurationParser.load(
                P2PStructuredProperties.class,
                "proactive.p2p.structured.configuration",
                System.getProperty("user.home")
                        + "/.proactive/p2p-structured.properties");
    }

}
