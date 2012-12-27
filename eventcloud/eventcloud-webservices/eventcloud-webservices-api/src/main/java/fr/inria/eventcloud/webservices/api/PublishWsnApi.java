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
package fr.inria.eventcloud.webservices.api;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;

/**
 * Defines a publish web service as defined by the WS-Notification
 * specification.
 * 
 * @author bsauvan
 */
@WebService(serviceName = "EventCloudPublishWsn", portName = "EventCloudPublishWsnPort", name = "EventCloudPublishWsnPortType", targetNamespace = "http://docs.oasis-open.org/wsn/bw-2")
@XmlSeeAlso(value = {
        org.oasis_open.docs.wsn.br_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.rp_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.r_2.ObjectFactory.class,
        org.oasis_open.docs.wsn.t_1.ObjectFactory.class,
        org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface PublishWsnApi extends NotificationConsumer {

}
