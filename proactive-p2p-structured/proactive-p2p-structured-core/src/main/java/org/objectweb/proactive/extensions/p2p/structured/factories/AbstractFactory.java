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
package org.objectweb.proactive.extensions.p2p.structured.factories;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

/**
 * Abstract class which is common to all factories in order to ease the
 * termination of GCM components.
 * 
 * @author bsauvan
 */
public class AbstractFactory {

    /**
     * Terminates the component represented by the given stub.
     * 
     * @param stub
     *            the stub of the component to terminate.
     */
    public static void terminateComponent(Object stub) {
        try {
            Component component = ((Interface) stub).getFcItfOwner();
            GCM.getGCMLifeCycleController(component).stopFc();
            GCM.getGCMLifeCycleController(component).terminateGCMComponent();
        } catch (IllegalLifeCycleException e) {
            e.printStackTrace();
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        }
    }

}
