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

import org.oasis_open.docs.wsn.b_2.SubscribeResponse;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.webservices.utils.WsnHelper;

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
     * Converts the specified SubscriptionId to its subscribe response
     * representation.
     * 
     * @param id
     *            the SubscriptionId to be converted.
     * @return the subscribe response representing the specified SubscriptionId.
     */
    @Override
    public SubscribeResponse marshal(SubscriptionId id) throws Exception {
        // FIXME: put a correct value for the subscription reference endpoint
        return WsnHelper.createSubscribeResponse("http://eventcloud.inria.fr/notification:NotificationService@Endpoint");
    }

    /**
     * Converts the specified subscribe response to its corresponding
     * SubscriptionId.
     * 
     * @param subscribeResponse
     *            the subscribe response containing the SubscriptionId
     *            representation to be converted.
     * @return the SubscriptionId represented by the specified subscribe
     *         response.
     */
    @Override
    public SubscriptionId unmarshal(SubscribeResponse subscribeResponse)
            throws Exception {
        return new SubscriptionId();
    }

}
