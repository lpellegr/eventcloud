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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;

import org.junit.Test;

import fr.inria.eventcloud.api.Event;

/**
 * A simple test that output to the standard output the translation result of
 * some predefined WS-Notification payloads.
 * 
 * @author lpellegr
 */
public class WsNotificationTranslatorTest {

    @Test
    public void test() {
        WsNotificationTranslator translator =
                new WsNotificationTranslatorImpl();

        Event event;

        System.out.println("[ Output for the translation of /notification-01.xml from WS-Notification notification to an Event without using XSD information ]");
        // a first translation by using only a WS-Notification notification
        // payload
        event =
                translator.translateWsNotifNotificationToEvent(
                        inputStreamFrom("/notification-01.xml"),
                        generateRandomUri());
        System.out.println(event);

        System.out.println("[ Output for the translation of /notification-01.xml from WS-Notification notification to an Event by using XSD information ]");
        // a second translation by using the WS-Notification notification
        // payload and an associated XSD file (in that case we can see that the
        // value "90" is annotated with http://www.w3.org/2001/XMLSchema#int,
        // whereas in the previous case it was not done)
        event =
                translator.translateWsNotifNotificationToEvent(
                        inputStreamFrom("/notification-01.xml"),
                        inputStreamFrom("/xsd-01.xml"), generateRandomUri());
        System.out.println(event);

        System.out.println("[ Output for the translation of an Event to a WS-Notification notification for the Event which has been previously created ]");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        translator.translateEventToWsNotifNotification(baos, event);
        System.out.println(new String(baos.toByteArray()));

    }

    private InputStream inputStreamFrom(String file) {
        InputStream is = null;

        if (file != null) {
            is = WsNotificationTranslatorTest.class.getResourceAsStream(file);
        }

        return is;
    }

    private URI generateRandomUri() {
        String legalChars =
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        StringBuilder result = new StringBuilder("http://www.inria.fr/");
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 20; i++) {
            result.append(random.nextInt(legalChars.length()));
        }

        try {
            return new URI(result.toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

}
