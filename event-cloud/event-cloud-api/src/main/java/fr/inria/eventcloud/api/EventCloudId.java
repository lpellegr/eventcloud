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
import fr.inria.eventcloud.utils.LongLong;

/**
 * Uniquely identify an Event Cloud.
 * 
 * @author lpellegr
 */
public class EventCloudId implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LongLong hashValue;

    /**
     * Constructs an event cloud identifier from the specified hash value. The
     * hash value is supposed to be a 128 bits hash value.
     * 
     * @param hashValue
     *            the hash value to use in order to create the identifier.
     * 
     * @throws IllegalArgumentException
     *             if the specified hash value is not a 128 bits hash value.
     */
    public EventCloudId(LongLong hashValue) {
        this.hashValue = hashValue;
    }

    /**
     * Constructs an URL from the current Event Cloud identifier. The prefix
     * used to create the URL is defined by
     * {@link EventCloudProperties#EVENT_CLOUD_ID_PREFIX}.
     * 
     * @return an URL from the current Event Cloud identifier.
     */
    public String toUrl() {
        return EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue().concat(
                this.hashValue.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventCloudId) {
            return this.hashValue.equals(((EventCloudId) obj).hashValue);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.hashValue.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.hashValue.toString();
    }

    /**
     * Parses a {@link EventCloudId} from the specified URL. The URL is supposed
     * to start with the prefix
     * {@link EventCloudProperties#EVENT_CLOUD_ID_PREFIX}.
     * 
     * @param eventCloudIdUrl
     *            the URL to parse.
     * 
     * @return a event cloud identifier which identifies uniquely an event
     *         cloud.
     * 
     * @throws IllegalArgumentException
     *             if the specified URL does no start with the prefix
     *             {@link EventCloudProperties#EVENT_CLOUD_ID_PREFIX}.
     */
    public static EventCloudId fromUrl(String eventCloudIdUrl) {
        if (!eventCloudIdUrl.startsWith(EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue())) {
            throw new IllegalArgumentException("URI must start with "
                    + EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                    + " but was:" + eventCloudIdUrl);
        }

        return new EventCloudId(
                LongLong.fromString(eventCloudIdUrl.substring(EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                        .length())));
    }
}
