package fr.inria.eventcloud.translators.wsn.subscribe;

import javax.xml.namespace.QName;

import org.junit.Assert;
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
import fr.inria.eventcloud.translators.wsn.WsnHelper;

/**
 * Test cases associated to {@link TopicSubscriptionTranslator}.
 * 
 * @author lpellegr
 */
public class TopicSubscriptionTranslatorTest {

    private static Logger log =
            LoggerFactory.getLogger(TopicSubscriptionTranslatorTest.class);

    private static final String TOPIC_NAMESPACE =
            "http://example.org/topic/namespace/";

    @BeforeClass
    public static void initContext() {
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 4);
    }

    @Test
    public void testTopicTranslation() throws TranslationException {
        this.testTopicTranslationToSparql(new QName(
                TOPIC_NAMESPACE, "myTopic", "t"));
        this.testTopicTranslationToSparql(new QName(
                TOPIC_NAMESPACE, "topicName:myTopic", "t"));
    }

    private void testTopicTranslationToSparql(QName topic)
            throws TranslationException {
        Subscribe subscribeMessage =
                WsnHelper.createSubscribeMessage(
                        "http://example.org/subscriber/s1", topic);

        String sparqlQuery =
                TopicSubscriptionTranslator.getInstance().translate(
                        subscribeMessage);

        Assert.assertNotNull(sparqlQuery);

        QuadruplePattern quadruplePattern =
                new SparqlReasoner().parseSparql(sparqlQuery)
                        .get(0)
                        .getQuadruplePattern()
                        .getValue();

        Assert.assertEquals(TOPIC_NAMESPACE + topic.getLocalPart()
                + Stream.STREAM_ID_SUFFIX, quadruplePattern.getObject()
                .getURI());

        log.info("Translation output:\n" + sparqlQuery);
    }

}
