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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.openjena.riot.RiotException;
import org.w3c.dom.Element;

import eu.play_project.play_commons.eventformat.EventFormatHelpers;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.Skolemizator;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.Translator;
import fr.inria.eventcloud.utils.Callback;
import fr.inria.eventcloud.utils.ReflectionUtils;

/**
 * Translator for {@link NotificationMessageHolderType WS-Notification messages}
 * with the RDF message payload to {@link CompoundEvent compound events}.
 * 
 * @author ialshaba
 * @author lpellegr
 */
public class SemanticNotificationTranslator extends
        Translator<NotificationMessageHolderType, CompoundEvent> {

    public SemanticNotificationTranslator() {

    }

    /**
     * Translates a {@link NotificationMessageHolderType WS-Notification
     * message} which is in XML escaped characters to the corresponding
     * {@link CompoundEvent compound event}.
     * 
     * @param notificationMessage
     *            the WS-Notification message to be translated.
     * 
     * @return the compound event corresponding to the specified WS-Notification
     *         message.
     */
    @Override
    public CompoundEvent translate(NotificationMessageHolderType notificationMessage)
            throws TranslationException {
        InputStream is =
                new ByteArrayInputStream(
                        EventFormatHelpers.unwrapFromDomNativeMessageElement(
                                (Element) notificationMessage.getMessage()
                                        .getAny()).getBytes());

        final String publicationSource;
        W3CEndpointReference producerReference =
                notificationMessage.getProducerReference();
        if (producerReference != null) {
            Object address =
                    ReflectionUtils.getFieldValue(
                            notificationMessage.getProducerReference(),
                            "address");
            if (address != null) {
                publicationSource =
                        ReflectionUtils.getFieldValue(address, "uri")
                                .toString();
            } else {
                publicationSource = null;
            }
        } else {
            publicationSource = null;
        }

        final List<Quadruple> quads = new ArrayList<Quadruple>();

        try {
            RdfParser.parse(
                    is, SerializationFormat.TriG, new Callback<Quadruple>() {
                        @Override
                        public void execute(Quadruple quad) {
                            if (publicationSource != null) {
                                quad.setPublicationSource(publicationSource);
                            }
                            quads.add(quad);
                        }

                    }, false);
        } catch (RiotException e) {
            throw new TranslationException(e);
        }

        return new CompoundEvent(Skolemizator.skolemize(quads));
    }

}
