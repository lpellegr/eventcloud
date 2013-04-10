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

import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.apache.jena.riot.WebContent;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.w3c.dom.Element;

import eu.play_project.play_commons.eventformat.EventFormatHelpers;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.parsers.RdfSerializer;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.Translator;

/**
 * Translator for {@link CompoundEvent compound events} to
 * {@link NotificationMessageHolderType WS-Notification messages} with the RDF
 * Message Payload.
 * 
 * @author ialshaba
 * @author lpellegr
 */
public class SemanticCompoundEventTranslator extends
        Translator<CompoundEvent, NotificationMessageHolderType> {

    /**
     * Creates a {@link SemanticCompoundEventTranslator}.
     */
    public SemanticCompoundEventTranslator() {

    }

    /**
     * Translates the specified {@link CompoundEvent compound event} to its
     * corresponding {@link NotificationMessageHolderType WS-Notification
     * message} with RDF payload with XML escaped characters.
     * 
     * @param event
     *            the compound event to be translated.
     * 
     * @return the WS-Notification message corresponding to the specified
     *         compound event.
     */
    @Override
    public NotificationMessageHolderType translate(CompoundEvent event)
            throws TranslationException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        RdfSerializer.triGWriter(out, event);

        Element any =
                EventFormatHelpers.wrapWithDomNativeMessageElement(new String(
                        out.toByteArray()), WebContent.contentTypeTriGAlt1);

        Message message = new Message();
        message.setAny(any);

        NotificationMessageHolderType notificationMessage =
                new NotificationMessageHolderType();
        if (event.size() > 0) {
            String publicationSource = event.get(0).getPublicationSource();

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

}
