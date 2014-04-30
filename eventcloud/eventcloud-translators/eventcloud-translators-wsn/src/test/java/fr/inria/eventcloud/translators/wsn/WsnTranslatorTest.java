/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.translators.wsn;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.utils.RDFReader;

/**
 * Tests cases associated to {@link WsnTranslator}.
 * 
 * @author lpellegr
 */
public class WsnTranslatorTest {

    private static final Logger log =
            LoggerFactory.getLogger(WsnTranslatorTest.class);

    private WsnTranslator translator;

    public WsnTranslatorTest() {
        this.translator = new WsnTranslator();
    }

    @Test
    public void testTranslation() throws TranslationException {
        // creates an event from a notification example
        CompoundEvent initialEvent =
                new CompoundEvent(RDFReader.read(
                        this.getClass().getResourceAsStream(
                                "/notification-01.trig"),
                        SerializationFormat.TriG));

        log.info("Initial quadruples are:");
        logInfo(initialEvent);

        CompoundEvent event = initialEvent;
        NotificationMessageHolderType message;
        for (int i = 0; i < 2; i++) {
            message = this.translator.translateXmlCompoundEvent(event);

            log.info(
                    "Message payload:\n{}",
                    asString((Element) message.getMessage().getAny()));

            // TODO: add assertions about the message which is issued from the
            // translation

            Assert.assertEquals(1, message.getTopic().getContent().size());
            Assert.assertEquals("cardiacRythmFiremanTopic", message.getTopic()
                    .getContent()
                    .get(0));

            event = this.translator.translateXmlNotification(message);

            // TODO: add assertions about the event which is issued from the
            // translation

            log.info("Event issued from translation {}:", i + 1);
            logInfo(event);

            Assert.assertEquals(
            // +1 for the metadata containing the event id
                    initialEvent.size() + 1, event.size());
        }
    }

    private static String asString(Element elt) {
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = null;

        try {
            trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(elt);

            trans.transform(source, result);

            return sw.toString();
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void logInfo(CompoundEvent event) {
        for (Quadruple quad : event) {
            log.info(quad.toString());
        }
    }

}
