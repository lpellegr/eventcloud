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
package fr.inria.eventcloud.deployment.cli.converters;

import com.beust.jcommander.IStringConverter;

import fr.inria.eventcloud.api.EventCloudId;

/**
 * JCommander converter to convert a String representing a stream URL to an
 * EventCloudId.
 * 
 * @author lpellegr
 */
public class EventCloudIdConverter implements IStringConverter<EventCloudId> {

    /**
     * {@inheritDoc}
     */
    @Override
    public EventCloudId convert(String value) {
        return new EventCloudId(value);
    }

}
