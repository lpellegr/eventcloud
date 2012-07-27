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

/**
 * Service API for EventCloudManagement Webservice.
 * 
 * @author lpellegr
 */
@WebService(serviceName = "EventCloudManagement", portName = "EventCloudManagementPort", targetNamespace = "http://webservices.eventcloud.inria.fr/", name = "EventCloudManagementPortType")
public interface EventCloudManagementWsServiceApi extends
        EventCloudManagementWsApi, NotificationProducer {

}
