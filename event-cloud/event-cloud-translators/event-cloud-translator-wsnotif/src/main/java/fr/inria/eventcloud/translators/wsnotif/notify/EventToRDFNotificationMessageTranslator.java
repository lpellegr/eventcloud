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
package fr.inria.eventcloud.translators.wsnotif.notify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.parsers.RdfSerializer;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslatorConstants;

/**
 * Translator for {@link CompoundEvent events} to
 * {@link NotificationMessageHolderType notification messages} with the RDF
 * Message Payload.
 * 
 * @author ialshaba
 */
public class EventToRDFNotificationMessageTranslator {
    /**
     * Translates the specified event to its corresponding notification message
     * with RDF payload .
     * 
     * @param event
     *            the event to be translated.
     * @return the notification message corresponding to the specified event.
     */
    public NotificationMessageHolderType translate(CompoundEvent event) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Collection<Quadruple> quads = event.getQuadruples();
        RdfSerializer.triGWriter(out, quads);
        /*System.out.println("**************  Message");
        try {
            out.writeTo(System.out);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("**************  Message");*/
        Message message = new Message();
        message.setAny(out);
        NotificationMessageHolderType notificationMessage =
                new NotificationMessageHolderType();
        notificationMessage.setMessage(message);
        return notificationMessage;
    }

}
