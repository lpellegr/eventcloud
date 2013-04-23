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
package fr.inria.eventcloud.api;

import java.io.Serializable;

import fr.inria.eventcloud.api.generators.UuidGenerator;
import fr.inria.eventcloud.configuration.EventCloudProperties;

/**
 * This class defines an identifier (also named stream identifier) that can be
 * used to uniquely identify an EventCloud among an organization.
 * 
 * @author lpellegr
 */
public class EventCloudId implements Serializable {

    private static final long serialVersionUID = 150L;

    private final String streamUrl;

    /**
     * Creates a new EventCloud identifier with randomly generated stream URL.
     */
    public EventCloudId() {
        this(EventCloudProperties.EVENTCLOUD_ID_PREFIX.getValue()
                + UuidGenerator.randomUuid());
    }

    /**
     * Creates a new EventCloud identifier from the specified {@code streamUrl}
     * . This URL is assumed to identify a unique EventCloud among an
     * organization.
     * 
     * @param streamUrl
     *            the unique URL that identifies an EventCloud among an
     *            organization.
     */
    public EventCloudId(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    /**
     * Returns an URL representation for this identifier.
     * 
     * @return an URL representation for this identifier.
     */
    public String getStreamUrl() {
        return this.streamUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.streamUrl.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof EventCloudId
                && this.streamUrl.equals(((EventCloudId) obj).streamUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.streamUrl;
    }

}
