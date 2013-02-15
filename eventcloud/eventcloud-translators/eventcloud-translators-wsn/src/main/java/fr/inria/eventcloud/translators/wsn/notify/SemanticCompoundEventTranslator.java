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
package fr.inria.eventcloud.translators.wsn.notify;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.openjena.riot.WebContent;
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
 * @author lpellegr
 */
public class SemanticCompoundEventTranslator extends
        Translator<CompoundEvent, NotificationMessageHolderType> {

    private static SemanticCompoundEventTranslator instance;

    private SemanticCompoundEventTranslator() {

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

        Element any =
                EventFormatHelpers.wrapWithDomNativeMessageElement(new String(
                        out.toByteArray()), WebContent.contentTypeTriGAlt);

        Message message = new Message();
        message.setAny(any);

        NotificationMessageHolderType notificationMessage =
                new NotificationMessageHolderType();
        if (event.size() > 0) {
            String publicationSource =
                    event.getQuadruples().get(0).getPublicationSource();

            if (publicationSource != null) {
                W3CEndpointReferenceBuilder endPointReferenceBuilder =
                        new W3CEndpointReferenceBuilder();
                endPointReferenceBuilder.address(publicationSource);
                notificationMessage.setProducerReference(endPointReferenceBuilder.build());
            }
        }
        notificationMessage.setMessage(message);

        return notificationMessage;
    }

    public static synchronized SemanticCompoundEventTranslator getInstance() {
        if (instance == null) {
            instance = new SemanticCompoundEventTranslator();
        }

        return instance;
    }

}
