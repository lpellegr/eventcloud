package fr.inria.eventcloud.translators.wsn.notify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.w3c.dom.Element;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.Skolemizator;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.Translator;
import fr.inria.eventcloud.utils.Callback;

/**
 * Translator for {@link NotificationMessageHolderType notification messages}
 * with the RDF message payload to{@link CompoundEvent events}
 * 
 * @author ialshaba
 */
public class SemanticNotificationTranslator extends
        Translator<NotificationMessageHolderType, CompoundEvent> {
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

        Message message = notificationMessage.getMessage();
        Element any = (Element) message.getAny();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(any.getTextContent().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String messageAsString = baos.toString();
        InputStream is =
                new ByteArrayInputStream(StringEscapeUtils.unescapeXml(
                        messageAsString).getBytes());
        final List<Quadruple> quads = new ArrayList<Quadruple>();
        RdfParser.parse(
                is, SerializationFormat.TriG, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        quads.add(quad);
                    }

                }, false);

        return new CompoundEvent(Skolemizator.skolemize(quads));
    }

}
