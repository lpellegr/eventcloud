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
package fr.inria.eventcloud.monitoring;

/**
 * Defines methods for managing the monitoring service for proxies (e.g.
 * enabling/disabling sensors).
 * 
 * @author lpellegr
 */
public interface ProxyMonitoringService {

    /**
     * Enables input output monitoring. The reports which are generated are sent
     * to the specified {@code consumerEndpoint} which is supposed to be
     * WS-Notification compliant.
     * 
     * @param consumerEndpoint
     *            the consumer endpoint to contact in order to send reports.
     * 
     * @return {@code true} if the activation has succeeded, {@code false} when
     *         the input/output monitoring is already enabled.
     */
    boolean enableInputOutputMonitoring(String consumerEndpoint);

    /**
     * Disables input output monitoring on the current proxy.
     * 
     * @return {@code true} when the deactivation has succeeded, {@code false}
     *         if input/output monitoring was not enabled.
     */
    boolean disableInputOutputMonitoring();

    /**
     * Returns {@code true} if input/output monitoring is enabled, {@code false}
     * otherwise.
     * 
     * @return {@code true} if input/output monitoring is enabled, {@code false}
     *         otherwise.
     */
    boolean isInputOutputMonitoringEnabled();

}
