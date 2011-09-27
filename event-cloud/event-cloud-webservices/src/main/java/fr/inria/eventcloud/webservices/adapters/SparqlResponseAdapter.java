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
package fr.inria.eventcloud.webservices.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ByteToObjectConverter;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ObjectToByteConverter;

import fr.inria.eventcloud.api.responses.SparqlResponse;

/**
 * XML Adapter for {@link SparqlResponse} objects.
 * 
 * @author bsauvan
 */
public class SparqlResponseAdapter extends
        XmlAdapter<byte[], SparqlResponse<?>> {

    public SparqlResponseAdapter() {
    }

    /**
     * Converts the specified SPARQL response to its byte array representation.
     * 
     * @param sparqlResponse
     *            the SPARQL response to be converted.
     * @return the byte array representing the specified SPARQL response.
     */
    @Override
    public byte[] marshal(SparqlResponse<?> sparqlResponse) throws Exception {
        return ObjectToByteConverter.convert(sparqlResponse);
    }

    /**
     * Converts the specified byte array to its corresponding SPARQL response.
     * 
     * @param sparqlResponseArray
     *            the byte array to be converted.
     * @return the SPARQL response represented by the specified byte array.
     */
    @Override
    public SparqlResponse<?> unmarshal(byte[] sparqlResponseArray)
            throws Exception {
        return (SparqlResponse<?>) ByteToObjectConverter.convert(sparqlResponseArray);
    }

}
