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
 * Translator for {@link NotificationMessageHolderType notification messages}
 * with the RDF message payload to{@link CompoundEvent events}
 * 
 * @author ialshaba
 * @author lpellegr
 */
public class SemanticNotificationTranslator extends
        Translator<NotificationMessageHolderType, CompoundEvent> {

    private static SemanticNotificationTranslator instance;

    private SemanticNotificationTranslator() {

    }

    /**
     * Translates a message which is in XML escaped characters to the
     * corresponding CompoundEvent
     * 
     * @param notificationMessage
     *            The message to be translated
     * 
     * @return the Compound Event
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

    public static synchronized SemanticNotificationTranslator getInstance() {
        if (instance == null) {
            instance = new SemanticNotificationTranslator();
        }

        return instance;
    }

}
