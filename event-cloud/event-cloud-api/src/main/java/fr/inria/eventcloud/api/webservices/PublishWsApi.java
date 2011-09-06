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
package fr.inria.eventcloud.api.webservices;

import java.net.URI;

/**
 * Defines the publish operations that can be executed on an Event Cloud and can
 * be exposed as web services by a publish proxy component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public interface PublishWsApi {

    /**
     * Publishes an event represented by the specified WS-Notification
     * notification XML payload.
     * 
     * @param xmlPayload
     *            the WS-Notification notification XML payload representing the
     *            event.
     * @param eventId
     *            the identifier associated to the event.
     */
    public void publishEvent(String xmlPayload, URI eventId);

    /**
     * Publishes an event represented by the specified WS-Notification
     * notification XML payload where the literal values associated to the
     * quadruple contained by the event are annotated by using the specified XSD
     * payload.
     * 
     * @param xmlPayload
     *            the WS-Notification notification XML payload representing the
     *            event.
     * @param xsdPayload
     *            the XSD payload associated to the event.
     * @param eventId
     *            the identifier associated to the event.
     */
    public void publishEventWithXsd(String xmlPayload, String xsdPayload,
                                    URI eventId);

}
