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
package fr.inria.eventcloud.api.listeners;

import org.objectweb.proactive.extensions.p2p.structured.utils.EnumConverter;
import org.objectweb.proactive.extensions.p2p.structured.utils.ReverseEnumMap;

/**
 * Defines the different types of listeners which are available.
 * 
 * @author lpellegr
 */
public enum NotificationListenerType
        implements
        EnumConverter<NotificationListenerType> {

    BINDING((short) 0), COMPOUND_EVENT((short) 1), SIGNAL((short) 2);

    private static ReverseEnumMap<NotificationListenerType> map =
            new ReverseEnumMap<NotificationListenerType>(
                    NotificationListenerType.class);

    private final short value;

    NotificationListenerType(short value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short convert() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationListenerType convert(short val) {
        return map.get(val);
    }

}
