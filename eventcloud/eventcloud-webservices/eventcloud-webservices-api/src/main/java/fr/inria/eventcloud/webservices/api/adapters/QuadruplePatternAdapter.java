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
package fr.inria.eventcloud.webservices.api.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ByteToObjectConverter;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ObjectToByteConverter;

import fr.inria.eventcloud.api.QuadruplePattern;

/**
 * XML Adapter for {@link QuadruplePattern} objects.
 * 
 * @author bsauvan
 */
public class QuadruplePatternAdapter extends
        XmlAdapter<byte[], QuadruplePattern> {

    public QuadruplePatternAdapter() {
    }

    /**
     * Converts the specified quadruple pattern to its byte array
     * representation.
     * 
     * @param quadPattern
     *            the quadruple pattern to be converted.
     * @return the byte array representing the specified quadruple pattern.
     */
    @Override
    public byte[] marshal(QuadruplePattern quadPattern) throws Exception {
        return ObjectToByteConverter.convert(quadPattern);
    }

    /**
     * Converts the specified byte array to its corresponding quadruple pattern.
     * 
     * @param quadPatternArray
     *            the byte array to be converted.
     * @return the quadruple pattern represented by the specified byte array.
     */
    @Override
    public QuadruplePattern unmarshal(byte[] quadPatternArray) throws Exception {
        return (QuadruplePattern) ByteToObjectConverter.convert(quadPatternArray);
    }

}
