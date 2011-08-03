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
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

/**
 * A simple test that output to the standard output the translation result of
 * some predefined WS-Notification payloads.
 * 
 * @author lpellegr
 */
public class WsNotificationTranslatorTest {

    @Test
    public void test() {
        // a first translation by using only a WS-Notification notification
        // payload
        testWsNotifNotificationToEventTranslationWith("/notification-01.xml");
        // a second translation by using the WS-Notification notification
        // payload and an associated XSD file (in that case we can see that the
        // value "90" is annotated with http://www.w3.org/2001/XMLSchema#int,
        // whereas in the previous case it was not done)
        testWsNotifNotificationToEventTranslationWith(
                "/notification-01.xml", "/xsd-01.xml");
    }

    private void testWsNotifNotificationToEventTranslationWith(String dataFile) {
        testWith(
                dataFile,
                null,
                "from WS-Notification notification to an Event without using XSD information",
                new WsNotificationTranslatorLambda() {

                    @Override
                    public void executeMethodCall(WsNotificationTranslator translator,
                                                  InputStream dataIS,
                                                  InputStream xsdIS) {
                        try {
                            System.out.println(translator.translateWsNotifNotificationToEvent(
                                    dataIS,
                                    xsdIS,
                                    new URI(
                                            "http://www.inria.fr/85asd7qw7f5cbj6tr23ja1ad7")));
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void testWsNotifNotificationToEventTranslationWith(String dataFile,
                                                               String xsdFile) {
        testWith(
                dataFile,
                xsdFile,
                "from WS-Notification notification to an Event by using XSD information",
                new WsNotificationTranslatorLambda() {

                    @Override
                    public void executeMethodCall(WsNotificationTranslator translator,
                                                  InputStream dataIS,
                                                  InputStream xsdIS) {
                        try {
                            System.out.println(translator.translateWsNotifNotificationToEvent(
                                    dataIS,
                                    xsdIS,
                                    new URI(
                                            "http://www.inria.fr/85asd7qw7f5cbj6tr23ja1ad7")));
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void testWith(String dataFile, String xsdFile, String comment,
                          WsNotificationTranslatorLambda lambda) {
        InputStream dataIS =
                WsNotificationTranslatorTest.class.getResourceAsStream(dataFile);

        InputStream xsdIS = null;
        if (xsdFile != null) {
            xsdIS =
                    WsNotificationTranslatorTest.class.getResourceAsStream(xsdFile);
        }

        System.out.println("[ Output for the translation of " + dataFile + " "
                + comment + " ]");

        WsNotificationTranslator translator =
                new WsNotificationTranslatorImpl();

        lambda.executeMethodCall(translator, dataIS, xsdIS);
    }

    private static interface WsNotificationTranslatorLambda {

        public void executeMethodCall(WsNotificationTranslator translator,
                                      InputStream dataIS, InputStream xsdIS);

    }

}
