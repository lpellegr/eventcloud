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
package fr.inria.eventcloud.webservices;

import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.deployment.local.LocalNodeProvider;

import fr.inria.eventcloud.deployment.ComponentPoolManager;
import fr.inria.eventcloud.webservices.factories.WsComponentPoolManagerFactory;

/**
 * Abstract class common to all web service tests.
 * 
 * @author bsauvan
 */
public abstract class WsTest {

    protected static final ComponentPoolManager COMPONENT_POOL_MANAGER =
            WsTest.getComponentPoolManager();

    protected static final int WEBSERVICES_PORT = getWebservicesPort();

    private static final ComponentPoolManager getComponentPoolManager() {
        NodeProvider nodeProvider = new LocalNodeProvider();
        nodeProvider.start();

        ComponentPoolManager componentPoolManager =
                WsComponentPoolManagerFactory.newComponentPoolManager(nodeProvider);

        return componentPoolManager;
    }

    private static final int getWebservicesPort() {
        String port = System.getProperty("eventcloud.webservices.port");

        if (port != null) {
            return Integer.parseInt(port);
        }

        return 42999;
    }

}
