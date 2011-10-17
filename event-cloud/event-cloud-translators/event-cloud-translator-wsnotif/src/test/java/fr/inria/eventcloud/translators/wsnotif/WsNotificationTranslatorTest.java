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
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.Assert;

import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangRIOT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.hp.hpl.jena.sparql.core.Quad;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;

/**
 * Tests cases associated to {@link WsNotificationTranslator}.
 * 
 * @author lpellegr
 */
public class WsNotificationTranslatorTest {

    private static final Logger log =
            LoggerFactory.getLogger(WsNotificationTranslatorTest.class);

    private WsNotificationTranslator translator;

    public WsNotificationTranslatorTest() {
        this.translator = new WsNotificationTranslator();
    }

    @Test
    public void testTranslation() {
        // creates an event from a notification example
        Event initialEvent = new Event(read("/notification-01.trig"));

        log.info("Initial quadruples are:");
        logInfo(initialEvent);

        Event event = initialEvent;
        NotificationMessageHolderType message;
        for (int i = 0; i < 2; i++) {
            message =
                    this.translator.translateEventToNotificationMessage(event);

            log.info(
                    "Message payload:\n{}",
                    asString((Element) message.getMessage().getAny()));

            // TODO: add assertions about the message which is issued from the
            // translation

            Assert.assertEquals(1, message.getTopic().getContent().size());
            Assert.assertEquals(
                    "fireman_event:cardiacRythmFiremanTopic",
                    message.getTopic().getContent().get(0));

            event =
                    this.translator.translateNotificationMessageToEvent(message);

            // TODO: add assertions about the event which is issued from the
            // translation

            log.info("Event issued from translation {}:", i + 1);
            logInfo(event);

            Assert.assertEquals(
                    // +1 for the metadata containing the event id
                    initialEvent.getQuadruples().size() + 1,
                    event.getQuadruples().size());
        }
    }

    private static String asString(Element elt) {
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = null;
        try {
            trans = transfac.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "4");

        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(elt);
        try {
            trans.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return sw.toString();
    }

    private static void logInfo(Event event) {
        for (Quadruple quad : event) {
            log.info(quad.toString());
        }
    }

    private static InputStream inputStreamFrom(String file) {
        InputStream is = null;

        if (file != null) {
            is = WsNotificationTranslatorTest.class.getResourceAsStream(file);
        }

        return is;
    }

    private static Collection<Quadruple> read(String file) {
        final Collection<Quadruple> quadruples = new Collection<Quadruple>();

        Sink<Quad> sink = new Sink<Quad>() {
            @Override
            public void send(final Quad quad) {
                quadruples.add(new Quadruple(
                        quad.getGraph(), quad.getSubject(),
                        quad.getPredicate(), quad.getObject()));
            }

            @Override
            public void close() {
            }

            @Override
            public void flush() {
            }

        };

        LangRIOT parser =
                RiotReader.createParserTriG(inputStreamFrom(file), null, sink);

        parser.parse();
        sink.close();

        return quadruples;
    }

}
