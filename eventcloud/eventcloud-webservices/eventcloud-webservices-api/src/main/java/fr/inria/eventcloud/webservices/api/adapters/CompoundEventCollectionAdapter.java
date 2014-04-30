/**
 * Copyright (c) 2011-2014 INRIA.
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

import java.util.Collection;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ByteToObjectConverter;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ObjectToByteConverter;

import fr.inria.eventcloud.api.CompoundEvent;

/**
 * XML Adapter for {@link Collection} of {@link CompoundEvent} objects.
 * 
 * @author bsauvan
 */
public class CompoundEventCollectionAdapter extends
        XmlAdapter<byte[], Collection<CompoundEvent>> {

    public CompoundEventCollectionAdapter() {
    }

    /**
     * Converts the specified collection of compound events to its byte array
     * representation.
     * 
     * @param compoundEvents
     *            the collection of compound events to be converted.
     * 
     * @return the byte array representing the specified collection of compound
     *         events.
     */
    @Override
    public byte[] marshal(Collection<CompoundEvent> compoundEvents)
            throws Exception {
        return ObjectToByteConverter.convert(compoundEvents);
    }

    /**
     * Converts the specified byte array to its corresponding collection of
     * compound events.
     * 
     * @param compoundEventsArray
     *            the byte array to be converted.
     * 
     * @return the collection of compound events represented by the specified
     *         byte array.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Collection<CompoundEvent> unmarshal(byte[] compoundEventsArray)
            throws Exception {
        return (Collection<CompoundEvent>) ByteToObjectConverter.convert(compoundEventsArray);
    }

}
