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

import java.util.UUID;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.utils.Base64LongLong;
import fr.inria.eventcloud.utils.UniqueId;

/**
 * Uniquely identify an Event Cloud.
 * 
 * @author lpellegr
 */
public class EventCloudId extends UniqueId {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a unique eventcloud id .
     */
    public EventCloudId() {
        super();
    }

    private EventCloudId(UUID uuid) {
        super(uuid);
    }

    /**
     * Constructs an URL from the current event cloud identifier. The prefix
     * used to create the URL is defined by
     * {@link EventCloudProperties#EVENT_CLOUD_ID_PREFIX}.
     * 
     * @return an URL from the current Event Cloud identifier.
     */
    public String toUrl() {
        return EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue().concat(
                super.toString());
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
    public static EventCloudId parseEventCloudIdUrl(String eventCloudIdUrl) {
        if (!eventCloudIdUrl.startsWith(EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue())) {
            throw new IllegalArgumentException("URI must start with "
                    + EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                    + " but was:" + eventCloudIdUrl);
        }

        return new EventCloudId(
                Base64LongLong.decodeUUID(eventCloudIdUrl.substring(EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                        .length())));
    }

}
