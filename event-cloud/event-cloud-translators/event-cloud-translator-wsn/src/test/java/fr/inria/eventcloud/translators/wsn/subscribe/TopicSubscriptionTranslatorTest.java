package fr.inria.eventcloud.translators.wsn.subscribe;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.play_commons.constants.Stream;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.reasoner.SparqlReasoner;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.WsNotificationMessageBuilder;

/**
 * Test cases associated to {@link TopicSubscriptionTranslator}.
 * 
 * @author lpellegr
 */
public class TopicSubscriptionTranslatorTest {

    private static Logger log =
            LoggerFactory.getLogger(TopicSubscriptionTranslatorTest.class);

    private TopicSubscriptionTranslator translator;

    private static final String topicNamespace =
            "http://example.org/topic/namespace/";

    @BeforeClass
    public static void initContext() {
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 4);
    }

    @Before
    public void setUp() {
        this.translator = new TopicSubscriptionTranslator();
    }

    @Test
    public void testTopicTranslation() throws TranslationException {
        this.testTopicTranslationToSparql(topicNamespace, "t", "myTopic");
        this.testTopicTranslationToSparql(
                topicNamespace, "t", "topicName:myTopic");
    }

    private void testTopicTranslationToSparql(String topicNamespace,
                                              String topicNsPrefix,
                                              String topicLocalPart)
            throws TranslationException {
        Subscribe subscribeMessage =
                WsNotificationMessageBuilder.createSubscribeMessage(
                        "http://example.org/subscriber/s1", topicNamespace,
                        topicNsPrefix, topicLocalPart);

        String sparqlQuery = this.translator.translate(subscribeMessage);

        Assert.assertNotNull(sparqlQuery);

        QuadruplePattern quadruplePattern =
                new SparqlReasoner().parseSparql(sparqlQuery)
                        .get(0)
                        .getQuadruplePattern()
                        .getValue();

        Assert.assertEquals(topicNamespace + topicLocalPart
                + Stream.STREAM_ID_SUFFIX, quadruplePattern.getObject()
                .getURI());

        log.info("Translation output:\n" + sparqlQuery);
    }

}
