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
package fr.inria.eventcloud.api;

import java.io.Serializable;

import fr.inria.eventcloud.configuration.EventCloudProperties;

/**
 * Uniquely identify an Event Cloud.
 * 
 * @author lpellegr
 */
public class EventCloudId implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long value;

    public EventCloudId(long value) {
        this.value = value;
    }

    /**
     * Returns the EventCloudId as an URL.
     * 
     * @return the EventCloudId as an URL.
     */
    public String asUrl() {
        return EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                + this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof EventCloudId
                && this.value == ((EventCloudId) obj).value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Long.valueOf(this.value).hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Long.toString(this.value);
    }

}