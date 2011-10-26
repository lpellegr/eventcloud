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
package fr.inria.eventcloud.webservices.services;

import javax.jws.WebService;

import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;

/**
 * 
 * @author lpellegr
 */
//@WebService(serviceName = "EventCloudSubscriber", portName = "EventCloudSubscriberPort", targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "EventCloudSubscriberPortType")
public interface ISubscriberService extends NotificationConsumer {

    public int getNbEventsReceived();

}
