/**
 * Copyright (c) 2011-2014 INRIA.
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
package org.objectweb.proactive.extensions.p2p.structured.tracker;

import org.objectweb.fractal.api.control.AttributeController;
import org.objectweb.proactive.extensions.p2p.structured.CommonAttributeController;

/**
 * {@link AttributeController} for {@link Tracker} components.
 * 
 * @author bsauvan
 */
public interface TrackerAttributeController extends CommonAttributeController {

    /**
     * Initializes all the attributes of the tracker.
     * 
     * @param stub
     *            the tracker remote reference.
     * @param networkName
     *            the network name to use.
     */
    void initAttributes(Tracker stub, String networkName);

}
