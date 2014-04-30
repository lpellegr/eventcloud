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

import fr.inria.eventcloud.api.Quadruple;

/**
 * XML Adapter for {@link Collection} of {@link Quadruple} objects.
 * 
 * @author bsauvan
 */
public class QuadrupleCollectionAdapter extends
        XmlAdapter<byte[], Collection<Quadruple>> {

    public QuadrupleCollectionAdapter() {
    }

    /**
     * Converts the specified collection of quadruples to its byte array
     * representation.
     * 
     * @param quads
     *            the collection of quadruples to be converted.
     * 
     * @return the byte array representing the specified collection of
     *         quadruples.
     */
    @Override
    public byte[] marshal(Collection<Quadruple> quads) throws Exception {
        return ObjectToByteConverter.convert(quads);
    }

    /**
     * Converts the specified byte array to its corresponding collection of
     * quadruples.
     * 
     * @param quadsArray
     *            the byte array to be converted.
     * 
     * @return the collection of quadruples represented by the specified byte
     *         array.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Collection<Quadruple> unmarshal(byte[] quadsArray) throws Exception {
        return (Collection<Quadruple>) ByteToObjectConverter.convert(quadsArray);
    }

}
