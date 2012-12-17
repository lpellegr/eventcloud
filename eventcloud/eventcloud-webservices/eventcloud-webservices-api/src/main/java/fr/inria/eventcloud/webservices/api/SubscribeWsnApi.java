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
package fr.inria.eventcloud.webservices.api;

import javax.jws.WebService;

import org.oasis_open.docs.wsn.bw_2.NotificationProducer;
import org.oasis_open.docs.wsn.bw_2.SubscriptionManager;

/**
 * Defines a subscribe web service as defined by the WS-Notification
 * specification.
 * 
 * @author bsauvan
 */
@WebService(serviceName = "EventCloudSubscribeWsn", portName = "EventCloudSubscribeWsnPort", name = "EventCloudSubscribeWsnPortType", targetNamespace = "http://docs.oasis-open.org/wsn/bw-2")
@javax.xml.bind.annotation.XmlSeeAlso(value = {
        org.oasis_open.docs.wsn.br_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.rp_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.r_2.ObjectFactory.class,
        org.oasis_open.docs.wsn.t_1.ObjectFactory.class,
        org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
@javax.jws.soap.SOAPBinding(parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
public interface SubscribeWsnApi extends NotificationProducer,
        SubscriptionManager {

}
