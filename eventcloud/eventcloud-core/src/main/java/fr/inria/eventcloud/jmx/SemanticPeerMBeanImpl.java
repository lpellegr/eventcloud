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
package fr.inria.eventcloud.jmx;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Simple JMX MBean implementation for {@link SemanticPeerMBean}.
 * 
 * @author lpellegr
 */
public class SemanticPeerMBeanImpl extends StandardMBean implements
        SemanticPeerMBean {

    // TODO: could be improved by using a StandardEmitterMBean to push updates
    // when required only

    private final SemanticCanOverlay overlay;

    public SemanticPeerMBeanImpl(SemanticCanOverlay semanticCanOverlay)
            throws NotCompliantMBeanException {
        super(SemanticPeerMBean.class);
        this.overlay = semanticCanOverlay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDynamicLoadBalancingEnabled() {
        return EventCloudProperties.isDynamicLoadBalancingEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStaticLoadBalancingEnabled() {
        return EventCloudProperties.isStaticLoadBalancingEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLocalLoad() {
        if (this.isDynamicLoadBalancingEnabled()) {
            return this.overlay.getLoadBalancingManager().getLocalLoad();
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAverageSystemLoad() {
        if (this.isDynamicLoadBalancingEnabled()) {
            return this.overlay.getLoadBalancingManager().getSystemLoad();
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getJoinTime() {
        return this.overlay.getJoinTime();
    }

}
