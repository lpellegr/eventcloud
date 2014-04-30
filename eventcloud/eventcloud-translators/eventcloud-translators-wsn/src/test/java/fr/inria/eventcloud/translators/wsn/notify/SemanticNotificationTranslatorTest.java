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
package fr.inria.eventcloud.translators.wsn.notify;

import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.WsnTranslator;
import fr.inria.eventcloud.utils.RDFReader;

/**
 * Tests cases associated to {@link WsnTranslator}.
 * 
 * @author ialshaba
 */
public class SemanticNotificationTranslatorTest {

    private static final Logger log =
            LoggerFactory.getLogger(SemanticNotificationTranslatorTest.class);

    private WsnTranslator translator;

    public SemanticNotificationTranslatorTest() {
        this.translator = new WsnTranslator();
    }

    @Test
    public void testTranslation() throws TranslationException {
        CompoundEvent initialEvent =
                new CompoundEvent(RDFReader.read(
                        this.getClass().getResourceAsStream("/example.trig"),
                        SerializationFormat.TriG));

        log.info("Initial quadruples are:");
        logInfo(initialEvent);
        // printQuadruples(initialEvent.getQuadruples());

        CompoundEvent event = initialEvent;

        NotificationMessageHolderType message;
        for (int i = 0; i < 2; i++) {
            message = this.translator.translateSemanticCompoundEvent(event);

            log.info("Message payload:\n{}", message.getMessage().getAny());
            event =
            // this.translator.translateNotificationMessageToEvent(message);
                    this.translator.translateSemanticNotification(message);
            // TODO: add assertions about the event which is issued from the
            // translation

            log.info("Event issued from translation {}:", i + 1);
            logInfo(event);

            Assert.assertEquals(
            // +1 for the metadata containing the event id
                    initialEvent.size(), event.size());
        }

    }

    @Test
    public void testTranslationWithBlankNodes() throws TranslationException {
        CompoundEvent event =
                new CompoundEvent(RDFReader.read(
                        this.getClass().getResourceAsStream(
                                "/example-blanknodes.trig"),
                        SerializationFormat.TriG, false, false));

        CompoundEvent translatedEvent =
                this.translator.translateSemanticNotification(this.translator.translateSemanticCompoundEvent(event));

        Assert.assertFalse(containsBlankNodes(translatedEvent));

        Assert.assertEquals(
                translatedEvent,
                this.translator.translateSemanticNotification(this.translator.translateSemanticCompoundEvent(translatedEvent)));
    }

    private static boolean containsBlankNodes(Iterable<Quadruple> quadruples) {
        for (Quadruple q : quadruples) {
            if (q.getSubject().isBlank() || q.getObject().isBlank()) {
                return true;
            }
        }

        return false;
    }

    private static void logInfo(CompoundEvent event) {
        for (Quadruple quad : event) {
            log.info(quad.toString());
        }
    }

}
