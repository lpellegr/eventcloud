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
package fr.inria.eventcloud.translators.wsn.notify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.openjena.riot.WebContent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.play_project.play_commons.eventformat.EventFormatHelpers;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.parsers.RdfSerializer;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.Translator;

/**
 * Translator for {@link CompoundEvent events} to
 * {@link NotificationMessageHolderType notification messages} with the RDF
 * Message Payload.
 * 
 * @author ialshaba
 */
public class SemanticCompoundEventTranslator extends
        Translator<CompoundEvent, NotificationMessageHolderType> {

    // used to have the possibility to create DOM elements
    private static Document document;

    static {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = dbfac.newDocumentBuilder();
            document = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Translates the specified event to its corresponding notification message
     * with RDF payload with XML escaped characters.
     * 
     * @param event
     *            the Compound Event to be translated.
     * @return the notification message corresponding to the specified event.
     */
    @Override
    public NotificationMessageHolderType translate(CompoundEvent event)
            throws TranslationException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Quadruple> quads = event.getQuadruples();
        RdfSerializer.triGWriter(out, quads);
        Message message = new Message();

        // escape XML characters before sending message
        byte[] buf = StringEscapeUtils.escapeXml(out.toString()).getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Element any =
                document.createElementNS(
                        EventFormatHelpers.WSN_MSG_ELEMENT.getNamespaceURI(),
                        EventFormatHelpers.WSN_MSG_ELEMENT.getPrefix()
                                + ":"
                                + EventFormatHelpers.WSN_MSG_ELEMENT.getLocalPart());
        any.setTextContent(baos.toString());
        any.setAttributeNS(
                EventFormatHelpers.WSN_MSG_ELEMENT.getNamespaceURI(),
                EventFormatHelpers.WSN_MSG_ELEMENT.getPrefix() + ":"
                        + EventFormatHelpers.WSN_MSG_SYNTAX_ATTRIBUTE,
                WebContent.contentTypeTriGAlt);
        message.setAny(any);
        NotificationMessageHolderType notificationMessage =
                new NotificationMessageHolderType();
        notificationMessage.setMessage(message);

        return notificationMessage;
    }

}
