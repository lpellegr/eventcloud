/**
 * Copyright (c) 2011-2012 INRIA.
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
package fr.inria.eventcloud.translators.wsn.notify;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.WsNotificationTranslator;
import fr.inria.eventcloud.utils.Callback;

/**
 * Tests cases associated to {@link WsNotificationTranslator}.
 * 
 * @author ialshaba
 */
public class SemanticNotificationTranslatorTest {

    private static final Logger log =
            LoggerFactory.getLogger(SemanticNotificationTranslatorTest.class);

    private WsNotificationTranslator translator;

    public SemanticNotificationTranslatorTest() {
        this.translator = new WsNotificationTranslator();
    }

    @Test
    public void testTranslation() throws TranslationException {
        CompoundEvent initialEvent =
                new CompoundEvent(read(
                        "/example.trig", SerializationFormat.TriG));
        // final List<Quadruple> quads = new List<Quadruple>();
        /*InputStream is =RdfParser.class.getResourceAsStream("/example.trig");
        // System.out.println("le fichier Trig : \n"+convertStreamToString(is));
        System.out.println("******************************* \t ********************\t**************************");
        String instr = convertStreamToString(is);
        System.out.println("le fichier Trig avant : \n"+instr);
        System.out.println("******************************* \t ********************\t**************************");
        String outstr = StringEscapeUtils.unescapeXml(instr);
        System.out.println("le fichier Trig apres unescape : \n "+outstr);
        
        *//*InputStream iis = new  ByteArrayInputStream(StringEscapeUtils.escapeXml(convertStreamToString(is)).getBytes());
          
          //InputStream is = new  ByteArrayInputStream(StringEscapeUtils.escapeXml(inputPayload).getBytes());
          RdfParser.parse(iis, SerializationFormat.TriG, new Callback<Quadruple>() {
              @Override
              public void execute(Quadruple quad) {
                  quads.add(quad);
                  //counter++;
              }
          } );
          */
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
                    initialEvent.getQuadruples().size(), event.getQuadruples()
                            .size());
        }

    }

    @Test
    public void testTranslationWithBlankNodes() throws TranslationException {
        CompoundEvent event =
                new CompoundEvent(read(
                        "/example-blanknodes.trig", SerializationFormat.TriG));

        CompoundEvent translatedEvent =
                this.translator.translateSemanticNotification(this.translator.translateSemanticCompoundEvent(event));

        Assert.assertFalse(containsBlankNodes(translatedEvent.getQuadruples()));

        Assert.assertEquals(
                translatedEvent,
                this.translator.translateSemanticNotification(this.translator.translateSemanticCompoundEvent(translatedEvent)));
    }

    private static boolean containsBlankNodes(List<Quadruple> quadruples) {
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

    private static InputStream inputStreamFrom(String file) {
        InputStream is = null;

        if (file != null) {
            is =
                    SemanticNotificationTranslatorTest.class.getResourceAsStream(file);
        }

        return is;
    }

    private static List<Quadruple> read(String file, SerializationFormat format) {
        final List<Quadruple> quadruples = new ArrayList<Quadruple>();

        RdfParser.parse(
                inputStreamFrom(file), format, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        quadruples.add(quad);
                    }

                }, false);

        return quadruples;
    }

    private static String readFileAsString(String filePath)
            throws java.io.IOException {
        byte[] buffer = new byte[(int) new File(filePath).length()];
        BufferedInputStream f = null;
        try {
            f = new BufferedInputStream(new FileInputStream(filePath));
            f.read(buffer);
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException ignored) {
                }
            }
        }
        return new String(buffer);
    }

    private String convertStreamToString(InputStream is) throws IOException {
        /*BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
          sb.append(line+"\n");
        }
        is.close();
        return sb.toString();*/

        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader r1 =
                        new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = r1.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }

    }

}
