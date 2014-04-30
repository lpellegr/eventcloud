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

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ByteToObjectConverter;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ObjectToByteConverter;

import fr.inria.eventcloud.api.Quadruple;

/**
 * XML Adapter for {@link Quadruple} objects.
 * 
 * @author bsauvan
 */
public class QuadrupleAdapter extends XmlAdapter<byte[], Quadruple> {

    public QuadrupleAdapter() {
    }

    /**
     * Converts the specified quadruple to its byte array representation.
     * 
     * @param quad
     *            the quadruple to be converted.
     * 
     * @return the byte array representing the specified quadruple.
     */
    @Override
    public byte[] marshal(Quadruple quad) throws Exception {
        return ObjectToByteConverter.convert(quad);
    }

    /**
     * Converts the specified byte array to its corresponding quadruple.
     * 
     * @param quadArray
     *            the byte array to be converted.
     * 
     * @return the quadruple represented by the specified byte array.
     */
    @Override
    public Quadruple unmarshal(byte[] quadArray) throws Exception {
        return (Quadruple) ByteToObjectConverter.convert(quadArray);
    }

}
