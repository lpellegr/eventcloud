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

import fr.inria.eventcloud.api.wrappers.BindingWrapper;

/**
 * XML Adapter for {@link BindingWrapper} objects.
 * 
 * @author bsauvan
 */
public class BindingWrapperAdapter extends XmlAdapter<byte[], BindingWrapper> {

    public BindingWrapperAdapter() {
    }

    /**
     * Converts the specified binding to its byte array representation.
     * 
     * @param binding
     *            the binding to be converted.
     * 
     * @return the byte array representing the specified binding.
     */
    @Override
    public byte[] marshal(BindingWrapper binding) throws Exception {
        return ObjectToByteConverter.convert(binding);
    }

    /**
     * Converts the specified byte array to its corresponding binding.
     * 
     * @param bindingArray
     *            the byte array to be converted.
     * 
     * @return the binding represented by the specified byte array.
     */
    @Override
    public BindingWrapper unmarshal(byte[] bindingArray) throws Exception {
        return (BindingWrapper) ByteToObjectConverter.convert(bindingArray);
    }

}
