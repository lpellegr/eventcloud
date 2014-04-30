/**
P * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.proxies;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.extensions.p2p.structured.proxies.ProxyImpl;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.monitoring.ProxyMonitoringActions;
import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * Abstract class which is common to all proxies.
 * 
 * @author lpellegr
 */
public abstract class EventCloudProxy extends ProxyImpl implements
        BindingController {

    /**
     * Non functional interface name of the monitoring interface.
     */
    public static final String MONITORING_SERVICES_CONTROLLER_ITF =
            "monitoring-services-controller";

    protected EventCloudCache eventCloudCache;

    protected ProxyMonitoringActions monitoringManager;

    protected EventCloudProxy() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        this.configurationProperty = "eventcloud.configuration";
        this.propertiesClass = EventCloudProperties.class;
        super.initComponentActivity(body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticPeer selectPeer() {
        return (SemanticPeer) super.selectPeer();
    }

    public EventCloudCache getEventCloudCache() {
        return this.eventCloudCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bindFc(String clientItfName, Object serverItf)
            throws NoSuchInterfaceException {
        if (clientItfName.equals(MONITORING_SERVICES_CONTROLLER_ITF)) {
            this.monitoringManager = (ProxyMonitoringActions) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] listFc() {
        return new String[] {MONITORING_SERVICES_CONTROLLER_ITF};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object lookupFc(String clientItfName)
            throws NoSuchInterfaceException {
        if (clientItfName.equals(MONITORING_SERVICES_CONTROLLER_ITF)) {
            return this.monitoringManager;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException {
        if (clientItfName.equals(MONITORING_SERVICES_CONTROLLER_ITF)) {
            this.monitoringManager = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

}
