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
 * Concrete implementation for {@link ProxyMonitoringManager}.
 * 
 * @author lpellegr
 */
public class ProxyMonitoringManagerImpl implements ProxyMonitoringActions,
        ProxyMonitoringManager {

    private boolean inputOutputMonitoringEnabled = false;

    private String consumerEndpoint;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enableInputOutputMonitoring(String consumerEndpoint) {
        if (!this.inputOutputMonitoringEnabled) {
            this.consumerEndpoint = consumerEndpoint;
            this.inputOutputMonitoringEnabled = true;
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean disableInputOutputMonitoring() {
        if (this.inputOutputMonitoringEnabled) {
            this.inputOutputMonitoringEnabled = false;
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInputOutputMonitoringEnabled() {
        return this.inputOutputMonitoringEnabled;
    }

    public String getConsumerEndpoint() {
        return this.consumerEndpoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendInputOutputMonitoringReport(String source) {
        // TODO send monitoring report (put in cache the cxf client which is
        // used to send the report)
    }

}
