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
package fr.inria.eventcloud.webservices;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.deployment.local.LocalNodeProvider;

import fr.inria.eventcloud.deployment.EventCloudComponentsManager;
import fr.inria.eventcloud.webservices.factories.WsEventCloudComponentsManagerFactory;

/**
 * Abstract class common to all web service tests.
 * 
 * @author bsauvan
 */
public abstract class WsTest {

    protected static final int WEBSERVICES_PORT = getWebservicesPort();

    public static final EventCloudComponentsManager createAndStartComponentsManager() {
        EventCloudComponentsManager componentsManager =
                WsEventCloudComponentsManagerFactory.newComponentsManager(
                        new LocalNodeProvider(), 1, 1, 0, 0, 0);
        componentsManager.start();

        return componentsManager;
    }

    public static final void stopAndTerminateComponentsManager(EventCloudComponentsManager componentsManager) {
        componentsManager.stop();
        PAActiveObject.terminateActiveObject(componentsManager, false);
    }

    private static final int getWebservicesPort() {
        String port = System.getProperty("eventcloud.webservices.port");

        if (port != null) {
            return Integer.parseInt(port);
        }

        return 42999;
    }

}
