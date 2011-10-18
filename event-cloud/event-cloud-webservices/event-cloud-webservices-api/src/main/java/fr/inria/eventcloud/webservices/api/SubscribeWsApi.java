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
package fr.inria.eventcloud.webservices.api;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.webservices.api.adapters.SubscribeInfosAdapter;
import fr.inria.eventcloud.webservices.api.adapters.SubscriptionIdAdapter;

/**
 * Defines the subscribe operations that can be executed on an Event-Cloud and
 * can be exposed as web services by a subscribe proxy component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
@WebService(serviceName = "EventCloudSubscribe", portName = "EventCloudSubscribePort", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", name = "EventCloudSubscribePortType")
@XmlSeeAlso({
        org.oasis_open.docs.wsn.t_1.ObjectFactory.class,
        org.oasis_open.docs.wsn.b_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.r_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.rp_2.ObjectFactory.class})
public interface SubscribeWsApi {

    /**
     * Subscribes for notifications with the specified {@link SubscribeInfos}.
     * 
     * @param subscribeInfos
     * 
     * @return the subscription identifier.
     */
    @WebResult(name = "SubscribeResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "SubscribeResponse")
    @WebMethod(operationName = "Subscribe", action = "http://www.petalslink.com/wsn/service/WsnProducer/Subscribe")
    @XmlJavaTypeAdapter(SubscriptionIdAdapter.class)
    public SubscriptionId subscribe(@WebParam(partName = "SubscribeRequest", name = "Subscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") @XmlJavaTypeAdapter(SubscribeInfosAdapter.class) SubscribeInfos subscribeInfos);

}
