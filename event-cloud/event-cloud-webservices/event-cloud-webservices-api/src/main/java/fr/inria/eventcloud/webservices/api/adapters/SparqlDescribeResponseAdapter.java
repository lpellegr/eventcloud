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
package fr.inria.eventcloud.webservices.api.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ByteToObjectConverter;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ObjectToByteConverter;

import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;

/**
 * XML Adapter for {@link SparqlDescribeResponse} objects.
 * 
 * @author bsauvan
 */
public class SparqlDescribeResponseAdapter extends
        XmlAdapter<byte[], SparqlDescribeResponse> {

    public SparqlDescribeResponseAdapter() {
    }

    /**
     * Converts the specified SPARQL Describe response to its byte array
     * representation.
     * 
     * @param sparqlDescribeResponse
     *            the SPARQL Describe response to be converted.
     * @return the byte array representing the specified SPARQL Describe
     *         response.
     */
    @Override
    public byte[] marshal(SparqlDescribeResponse sparqlDescribeResponse)
            throws Exception {
        return ObjectToByteConverter.convert(sparqlDescribeResponse);
    }

    /**
     * Converts the specified byte array to its corresponding SPARQL Describe
     * response.
     * 
     * @param sparqlDescribeResponseArray
     *            the byte array to be converted.
     * @return the SPARQL Describe response represented by the specified byte
     *         array.
     */
    @Override
    public SparqlDescribeResponse unmarshal(byte[] sparqlDescribeResponseArray)
            throws Exception {
        return (SparqlDescribeResponse) ByteToObjectConverter.convert(sparqlDescribeResponseArray);
    }

}
