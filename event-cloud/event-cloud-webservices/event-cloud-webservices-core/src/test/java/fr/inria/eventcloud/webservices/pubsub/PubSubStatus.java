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
package fr.inria.eventcloud.webservices.pubsub;

import fr.inria.eventcloud.api.Event;

/**
 * This interface defines a method which may be called to know if an
 * {@link Event} has been received.
 * 
 * @author bsauvan
 */
public interface PubSubStatus {

    /**
     * Indicates whether an event has been received or not.
     * 
     * @return true if an event has been received, false otherwise.
     */
    public boolean hasReceivedEvent();

}
