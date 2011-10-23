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

import org.oasis_open.docs.wsn.b_2.SubscribeResponse;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.utils.LongLong;
import fr.inria.eventcloud.utils.MurmurHash;

/**
 * XML Adapter for {@link SubscriptionId} objects.
 * 
 * @author lpellegr
 */
public class SubscriptionIdAdapter extends
        XmlAdapter<SubscribeResponse, SubscriptionId> {

    public SubscriptionIdAdapter() {
    }

    /**
     * Converts the specified SubscriptionId to its string representation.
     * 
     * @param id
     *            the SubscriptionId to be converted.
     * @return the string representing the specified SubscriptionId.
     */
    @Override
    public SubscribeResponse marshal(SubscriptionId id) throws Exception {
        return new SubscribeResponse();
    }

    /**
     * Converts the specified string to its corresponding SubscriptionId.
     * 
     * @param id
     *            the string containing the SubscriptionId representation to be
     *            converted.
     * @return the SubscriptionId represented by the specified string.
     */
    @Override
    public SubscriptionId unmarshal(SubscribeResponse id) throws Exception {
        return new SubscriptionId(new LongLong(
                MurmurHash.hash128(id.toString())));
    }

}
