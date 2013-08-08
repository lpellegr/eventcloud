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
package fr.inria.eventcloud.proxies;

import org.objectweb.fractal.api.control.AttributeController;
import org.objectweb.proactive.extensions.p2p.structured.CommonAttributeController;

import fr.inria.eventcloud.api.properties.AlterableElaProperty;

/**
 * {@link AttributeController} for {@link SubscribeProxy} components.
 * 
 * @author bsauvan
 */
public interface SubscribeProxyAttributeController extends
        CommonAttributeController {

    /**
     * Sets the attributes of the subscribe proxy.
     * 
     * @param proxy
     *            the EventCloud proxy instance to set to the subscribe proxy.
     * @param componentUri
     *            the URI at which the component is bind.
     * @param properties
     *            a set of {@link AlterableElaProperty} properties to use for
     *            initializing the {@link SubscribeProxy}.
     */
    void setAttributes(EventCloudCache proxy, String componentUri,
                       AlterableElaProperty[] properties);

}
