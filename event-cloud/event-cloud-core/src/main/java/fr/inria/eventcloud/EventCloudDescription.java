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
package fr.inria.eventcloud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;

/**
 * Defines a list properties that do not change over time for an EventCloud.
 * 
 * @author lpellegr
 */
public class EventCloudDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    private final EventCloudId id;

    private final long creationTime;

    private List<UnalterableElaProperty> elaProperties;

    public EventCloudDescription() {
        this(new EventCloudId());
    }
    
    public EventCloudDescription(String streamUrl,
            UnalterableElaProperty... elaProperties) {
        this(new EventCloudId(streamUrl), elaProperties);
    }

    public EventCloudDescription(EventCloudId eventCloudId,
            UnalterableElaProperty... elaProperties) {
        this.id = eventCloudId;
        this.creationTime = System.currentTimeMillis();

        if (elaProperties.length > 0) {
            this.elaProperties =
                    new ArrayList<UnalterableElaProperty>(elaProperties.length);

            for (UnalterableElaProperty prop : elaProperties) {
                this.elaProperties.add(prop);
            }
        }
    }

    /**
     * Returns the {@link EventCloudId} that uniquely identifies the
     * Event-Cloud.
     * 
     * @return the {@link EventCloudId} that uniquely identifies the
     *         Event-Cloud.
     */
    public EventCloudId getId() {
        return this.id;
    }

    /**
     * Returns the Event-Cloud creation date.
     * 
     * @return the Event-Cloud creation date.
     */
    public long getCreationTime() {
        return this.creationTime;
    }

    /**
     * Returns the list of ELA properties that have been set when the
     * Event-Cloud has been created. These ELA properties are not modifiable.
     * 
     * @return the list of ELA properties that have been set when the
     *         Event-Cloud has been created.
     */
    public List<UnalterableElaProperty> getElaProperties() {
        return this.elaProperties;
    }

}
