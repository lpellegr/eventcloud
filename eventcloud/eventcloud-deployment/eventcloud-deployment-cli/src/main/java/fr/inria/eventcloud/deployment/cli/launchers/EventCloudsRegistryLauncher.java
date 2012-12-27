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
package fr.inria.eventcloud.deployment.cli.launchers;

import org.objectweb.proactive.core.ProActiveException;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;

/**
 * This launcher is used to deploy a new {@link EventCloudsRegistry}. Once the
 * registry is deployed an instance file is created. This instance file contains
 * an URL indicating where the registry is bind.
 * 
 * @author lpellegr
 */
public final class EventCloudsRegistryLauncher extends Launcher {

    public static void main(String[] args) {
        new EventCloudsRegistryLauncher().launch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String run() {
        EventCloudsRegistry registry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();

        try {
            return registry.register("eventclouds-registry");
        } catch (ProActiveException e) {
            e.printStackTrace();
            return null;
        }
    }

}
