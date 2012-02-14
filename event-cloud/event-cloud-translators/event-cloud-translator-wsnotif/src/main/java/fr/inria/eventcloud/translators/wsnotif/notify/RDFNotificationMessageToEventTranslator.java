package fr.inria.eventcloud.translators.wsnotif.notify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.utils.Callback;

public class RDFNotificationMessageToEventTranslator {

    public CompoundEvent translate(NotificationMessageHolderType notificationMessage) {

        Message message = notificationMessage.getMessage();
        ByteArrayOutputStream baos = (ByteArrayOutputStream) message.getAny();
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
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
