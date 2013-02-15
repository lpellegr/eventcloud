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
package fr.inria.eventcloud.adapters.rdf2go.listeners;

import fr.inria.eventcloud.api.SubscriptionId;

/**
 * This interface is a marker for all notification listeners that are adapted to
 * handle RDF2Go objects.
 * 
 * @author lpellegr
 */
public interface Rdf2goNotificationListener<T> {

    /**
     * This method is called each time a new notification is received by the
     * subscriber.
     * 
     * @param id
     *            the subscription identifier.
     * 
     * @param solution
     *            the solution received.
     */
    public void handle(SubscriptionId id, T solution);

}
