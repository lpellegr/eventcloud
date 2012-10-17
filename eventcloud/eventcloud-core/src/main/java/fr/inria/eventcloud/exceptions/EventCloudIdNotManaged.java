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
package fr.inria.eventcloud.exceptions;

import fr.inria.eventcloud.api.EventCloudId;

/**
 * Exception thrown when an {@link EventCloudId} is not managed by the system.
 * 
 * @author lpellegr
 */
public class EventCloudIdNotManaged extends Exception {

    private static final long serialVersionUID = 1L;

    public EventCloudIdNotManaged() {
        super();
    }

    public EventCloudIdNotManaged(String id, String registryUrl) {
        super("EventCloud identifier " + id
                + " not managed by EventClouds registry " + registryUrl);
    }

    public EventCloudIdNotManaged(String message, Throwable cause) {
        super(message, cause);
    }

    public EventCloudIdNotManaged(Throwable cause) {
        super(cause);
    }

}
