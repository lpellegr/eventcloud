/**
 * Copyright (c) 2011 INRIA.
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
package fr.inria.eventcloud;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

/**
 * This class is used to create and to deploy an {@link EventCloudsRegistry} as
 * an active object.
 * 
 * @author lpellegr
 */
public class EventCloudsRegistryFactory {

    private EventCloudsRegistryFactory() {

    }

    public static EventCloudsRegistry newEventCloudsRegistry() {
        return newEventCloudsRegistry((Node) null);
    }

    public static EventCloudsRegistry newEventCloudsRegistry(Node node) {
        try {
            return PAActiveObject.newActive(
                    EventCloudsRegistry.class, new Object[0], node);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static EventCloudsRegistry newEventCloudsRegistry(GCMVirtualNode vn) {
        if (vn != null) {
            return newEventCloudsRegistry(vn.getANode());
        } else {
            return newEventCloudsRegistry((Node) null);
        }
    }

}
