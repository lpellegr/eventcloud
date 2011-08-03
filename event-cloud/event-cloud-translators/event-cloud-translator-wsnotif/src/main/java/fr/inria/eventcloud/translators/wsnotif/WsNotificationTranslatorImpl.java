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
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.translators.wsnotif.handlers.WsNotifNotificationToEventHandler;
import fr.inria.eventcloud.translators.wsnotif.handlers.XsdHandler;

/**
 * A concrete implementation of {@link WsNotificationTranslator}.
 * 
 * @author lpellegr
 */
public class WsNotificationTranslatorImpl implements WsNotificationTranslator {

    /**
     * {@inheritDoc}
     */
    @Override
    public Event translateWsNotifNotificationToEvent(InputStream xmlPayload,
                                                     URI eventId) {
        return this.translateWsNotifNotificationToEvent(
                xmlPayload, null, eventId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Event translateWsNotifNotificationToEvent(InputStream xmlPayload,
                                                     InputStream xsdPayload,
                                                     URI eventId) {
        Map<String, XSDDatatype> elementDatatypes = null;
        if (xsdPayload != null) {
            XsdHandler xsdHandler = new XsdHandler();
            this.executeSaxParser(xsdPayload, null, xsdHandler);
            elementDatatypes = xsdHandler.getElementDatatypes();
        }

        WsNotifNotificationToEventHandler handler =
                new WsNotifNotificationToEventHandler(
                        eventId.toString(), elementDatatypes);
        this.executeSaxParser(xmlPayload, xsdPayload, handler);

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

    private void executeSaxParser(InputStream in, InputStream xsd,
                                  DefaultHandler handler) {
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
