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
package fr.inria.eventcloud.webservices.api.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ByteToObjectConverter;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ObjectToByteConverter;

import fr.inria.eventcloud.api.CompoundEvent;

/**
 * XML Adapter for {@link CompoundEvent} objects.
 * 
 * @author bsauvan
 */
public class CompoundEventAdapter extends XmlAdapter<byte[], CompoundEvent> {

    public CompoundEventAdapter() {
    }

    /**
     * Converts the specified compound event to its byte array representation.
     * 
     * @param compoundEvent
     *            the compound event to be converted.
     * 
     * @return the byte array representing the specified compound event.
     */
    @Override
    public byte[] marshal(CompoundEvent compoundEvent) throws Exception {
        return ObjectToByteConverter.convert(compoundEvent);
    }

    /**
     * Converts the specified byte array to its corresponding compound event.
     * 
     * @param compoundEventArray
     *            the byte array to be converted.
     * 
     * @return the compound event represented by the specified byte array.
     */
    @Override
    public CompoundEvent unmarshal(byte[] compoundEventArray) throws Exception {
        return (CompoundEvent) ByteToObjectConverter.convert(compoundEventArray);
    }

}
