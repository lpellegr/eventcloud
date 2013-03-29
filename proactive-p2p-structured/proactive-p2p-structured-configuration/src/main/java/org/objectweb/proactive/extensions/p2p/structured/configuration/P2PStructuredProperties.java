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
package org.objectweb.proactive.extensions.p2p.structured.configuration;

/**
 * Defines properties for the p2p-structured modules.
 * 
 * @author lpellegr
 */
public class P2PStructuredProperties {

    private P2PStructuredProperties() {
    }

    public static final PropertyString NODE_PROVIDER_CONFIG_FILE =
            new PropertyString("node.provider.config.file", null);

    public static final PropertyString GCM_PROVIDER = new PropertyString(
            "gcm.provider", "org.objectweb.proactive.core.component.Fractive");

    public static final PropertyClass APFLOAT_DEFAULT_BUILDER_FACTORY =
            new PropertyClass(
                    "apfloat.default.builder.factory",
                    "org.apfloat.internal.LongBuilderFactory");

    public static final PropertyBoolean ENABLE_BENCHMARKS_INFORMATION =
            new PropertyBoolean("enable.benchmarks.information", false);

    public static final PropertyByte CAN_NB_DIMENSIONS = new PropertyByte(
            "can.nb.dimensions", Byte.valueOf((byte) 3));

    /**
     * This property defines the minimum lower bound associated to a CAN
     * network. It is by default set to &#92;u0000 and <strong>should not be
     * edited</strong>.
     */
    public static final PropertyCharacterUnicode CAN_LOWER_BOUND =
            new PropertyCharacterUnicode("can.lower.bound", "\u0000");

    /**
     * This property defines the maximum upper bound associated to a CAN
     * network. It is by default set to &#92;u10FFFF (=&#92;uDBFF&#92;uDFFF) to
     * support all UTF-16 characters. This is an open bound (i.e. no peer will
     * manage it).
     */
    public static final PropertyCharacterUnicode CAN_UPPER_BOUND =
            new PropertyCharacterUnicode("can.upper.bound", "\uDBFF\uDFFF");

    public static final PropertyInteger CAN_LEAVE_RETRY_MIN =
            new PropertyInteger("can.leave.retry.min", 2000);

    public static final PropertyInteger CAN_LEAVE_RETRY_MAX =
            new PropertyInteger("can.leave.retry.max", 5000);

    public static final PropertyInteger CAN_REFRESH_TASK_START =
            new PropertyInteger("can.task.refresh.start", 0);

    public static final PropertyInteger CAN_REFRESH_TASK_INTERVAL =
            new PropertyInteger("can.task.refresh.interval", 500);

    /**
     * Indicates which representation to use for printing characters contained
     * by a coordinate element on the standard output. The value of this
     * property can be set to:
     * <ul>
     * <li>{@code codepoints} to display the integer values associated to each
     * code point from a coordinate element.</li>
     * <li>{@code decimal} to display coordinate element as decimal values.</li>
     * <li>{@code string} to display a coordinate element as a standard unicode
     * String. Warning, some unicode characters are not printable or are not
     * displayed nicely.</li>
     * <li>{@code utf16} to display coordinate elements based on the unicode
     * notation for UTF-16 (&#92;uXXXX or &#92;uXXXX&#92;uXXXX if the character
     * is a supplementary character).</li>
     * <li>{@code utf32} to display coordinate elements based on the unicode
     * notation for UTF-32 (&#92;uXXXXX).</li>
     * </ul>
     * The property is set by default to {@code decimal}.
     */
    public static final PropertyString CAN_ELEMENT_DISPLAY =
            new PropertyString("can.element.display", "codepoints");

    /**
     * Defines the soft limit used by each peer that runs with multi active
     * serving.
     */
    public static final PropertyInteger MAO_SOFT_LIMIT_PEERS =
            new PropertyInteger("mao.soft.limit.peers", Runtime.getRuntime()
                    .availableProcessors() + 1);

    /**
     * Defines the soft limit used by each tracker that runs with multi active
     * serving.
     */
    public static final PropertyInteger MAO_SOFT_LIMIT_TRACKERS =
            new PropertyInteger("mao.soft.limit.trackers", Runtime.getRuntime()
                    .availableProcessors() + 1);

    public static final PropertyDouble TRACKER_STORAGE_PROBABILITY =
            new PropertyDouble("tracker.storage.probability", 1.0);

    public static final PropertyInteger TRACKER_JOIN_RETRY_INTERVAL =
            new PropertyInteger("tracker.join.retry.interval", 500);

    private static boolean configurationLoaded = false;

    static {
        loadConfiguration();
    }

    public static synchronized void loadConfiguration() {
        if (!configurationLoaded) {
            ConfigurationParser.load(
                    P2PStructuredProperties.class,
                    "proactive.p2p.structured.configuration",
                    System.getProperty("user.home")
                            + "/.proactive/p2p-structured.properties");

            configurationLoaded = true;
        }
    }

}
