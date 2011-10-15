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

import java.util.Arrays;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslator;

/**
 * XML Adapter for subscription SPARQL queries.
 * 
 * @author bsauvan
 */
public class SubscriptionAdapter extends XmlAdapter<String[], String> {

    private WsNotificationTranslator translator;

    public SubscriptionAdapter() {
        this.translator = new WsNotificationTranslator();
    }

    /**
     * Converts the specified SPARQL query to its WS-Notification notification
     * XML payload representation.
     * 
     * @param sparqlQuery
     *            the SPARQL query to be converted.
     * 
     * @return the WS-Notification notification XML payload representing the
     *         specified SPARQL query.
     */
    @Override
    public String[] marshal(String sparqlQuery) throws Exception {
        return null;
    }

    /**
     * Converts the specified WS-Notification notification XML payload to its
     * corresponding SPARQL query.
     * 
     * @param xmlPayload
     *            the WS-Notification notification XML payload to be converted.
     * 
     * @return the SPARQL query represented by the specified WS-Notification
     *         notification XML payload.
     */
    @Override
    public String unmarshal(String[] xmlPayload) throws Exception {
        if (xmlPayload.length >= 3) {
            return this.translator.translateWsNotifSubscriptionToSparqlQuery(
                    xmlPayload[0], xmlPayload[1], Arrays.copyOfRange(
                            xmlPayload, 2, xmlPayload.length));
        } else {
            return null;
        }
    }
}
