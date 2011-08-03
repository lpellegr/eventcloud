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
package fr.inria.eventcloud.translators.wsnotif;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.translators.wsnotif.handlers.WsNotifNotificationToEventHandler;

/**
 * 
 * @author lpellegr
 */
public class WsNotificationTranslatorImpl implements WsNotificationTranslator {

    /**
     * {@inheritDoc}
     */
    @Override
    public Event translateWsNotifNotificationToEvent(InputStream xmlPayload,
                                                     InputStream xsdPayload,
                                                     URI eventId) {
        WsNotifNotificationToEventHandler handler =
                new WsNotifNotificationToEventHandler(eventId.toString());

        this.executeSaxParser(xmlPayload, handler);

        return handler.getEvent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String translateWsNotifSubscriptionToSparqlQuery(InputStream xmlPayload,
                                                            InputStream xsdPaylaod) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void translateEventToWsNotifNotification(OutputStream output,
                                                    Event event) {

    }

    private void executeSaxParser(InputStream in, DefaultHandler handler) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);

            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(in, handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
