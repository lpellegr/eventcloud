package fr.inria.eventcloud.translators.wsn.subscribe;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.WsNotificationMessageBuilder;

/**
 * Test cases associated to {@link TopicSubscriptionTranslator}.
 * 
 * @author lpellegr
 */
@RunWith(value = Parameterized.class)
public class TopicSubscriptionTranslatorTest {

    private static Logger log =
            LoggerFactory.getLogger(TopicSubscriptionTranslatorTest.class);

    private TopicSubscriptionTranslator translator;

    private String topic;

    public TopicSubscriptionTranslatorTest(String topic) {
        this.topic = topic;
    }

    @Before
    public void setUp() {
        this.translator = new TopicSubscriptionTranslator();
    }

    @Parameters
    public static List<Object[]> data() {
        Object[][] data = new Object[][] { {"ex:topicName"}, {"topicName"}};
        return Arrays.asList(data);
    }

    @Test
    public void testTopicTranslationToSparql() throws TranslationException {
        Subscribe subscribeMessage =
                WsNotificationMessageBuilder.createSubscribeMessage(
                        "http://example.org/subscribers/s12",
                        "http://example.org/namespace", "ns", this.topic);

        String sparqlQuery = this.translator.translate(subscribeMessage);

        Assert.assertNotNull(sparqlQuery);

        log.info("Translation output:\n" + sparqlQuery);
    }
}
