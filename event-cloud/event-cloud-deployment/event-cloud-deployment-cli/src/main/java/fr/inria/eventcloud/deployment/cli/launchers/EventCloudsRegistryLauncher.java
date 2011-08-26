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
package fr.inria.eventcloud.deployment.cli.launchers;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.EventCloudsRegistryFactory;

/**
 * This launcher is used to deploy a new {@link EventCloudsRegistry}. Before to
 * return, the Java application print on the standard output the URL indicating
 * where the registry is binded.
 * 
 * @author lpellegr
 */
public final class EventCloudsRegistryLauncher {

    private EventCloudsRegistryLauncher() {
        super();
    }

    public static void main(String[] args) {
        new EventCloudsRegistryLauncher().run();
    }

    private void run() {
        EventCloudsRegistry registry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();

        try {
            System.out.println(PAActiveObject.registerByName(
                    registry, "eventclouds-registry"));
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

}