package fr.inria.eventcloud.translators.wsnotif;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class WsNotificationTranslatorTest {

    @Test
    public void test() {
        testWsNotifNotificationToEventTranslationWith("/notification-01.xml");
    }

    private void testWsNotifNotificationToEventTranslationWith(String file) {
        testWith(file, "from WS-Notification Notification to an Event");
    }

    private void testWith(String file, String comment) {
        InputStream inputStream =
                WsNotificationTranslatorTest.class.getResourceAsStream(file);

        System.out.println("[ Translating " + file + " " + comment + "... ]");
        System.out.println("> Output:");

        WsNotificationTranslator translator =
                new WsNotificationTranslatorImpl();
        try {
            System.out.println(translator.translateWsNotifNotificationToEvent(
                    inputStream, null, new URI(
                            "http://www.inria.fr/85asd7qw7f5cbj6tr23ja1ad7")));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
