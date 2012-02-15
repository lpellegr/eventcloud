package fr.inria.eventcloud.translators.wsnotif.notify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.utils.Callback;

/**
 * Translator for {@link NotificationMessageHolderType notification messages}
 * with the RDF message payload to{@link CompoundEvent events}
 * 
 * @author ialshaba
 */
public class RDFNotificationMessageToEventTranslator {
    /**
     * Translates a message which is in XML escaped characters to the
     * corresponding CompoundEvent
     * 
     * @param notificationMessage
     *            The message to be translated
     * @return the Compound Event
     */
    public CompoundEvent translate(NotificationMessageHolderType notificationMessage) {

        Message message = notificationMessage.getMessage();
        ByteArrayOutputStream baos = (ByteArrayOutputStream) message.getAny();
        // TODO UnEscaping XML tags before translation !
        String messageAsString = baos.toString();
        InputStream is =
                new ByteArrayInputStream(StringEscapeUtils.unescapeXml(
                        messageAsString).getBytes());
        final List<Quadruple> quads = new ArrayList<Quadruple>();
        RdfParser.parse(
                is, SerializationFormat.TriG, new Callback<Quadruple>() {
                    public void execute(Quadruple quad) {
                        quads.add(quad);
                    }

                });

        return new CompoundEvent(new Collection<Quadruple>(quads));
    }

}
