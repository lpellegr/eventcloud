package fr.inria.eventcloud.translators.wsn;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.cxf.wsn.util.WSNHelper;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

/**
 * This class provides static methods to create some basic WS-Notification
 * messages.
 * 
 * @author lpellegr
 */
public class WsNotificationMessageBuilder {

    public static Subscribe createSubscribeMessage(String subscriberEndpoint,
                                                   String topic) {
        Subscribe subscribeRequest = new Subscribe();
        FilterType filterType = new FilterType();
        TopicExpressionType topicExpressionType = new TopicExpressionType();
        topicExpressionType.getContent().add(topic);

        JAXBElement<TopicExpressionType> jaxbElement =
                new JAXBElement<TopicExpressionType>(
                        new QName(
                                "http://docs.oasis-open.org/wsn/b-2",
                                "TopicExpression"), TopicExpressionType.class,
                        topicExpressionType);
        filterType.getAny().add(jaxbElement);
        subscribeRequest.setFilter(filterType);

        subscribeRequest.setConsumerReference(WSNHelper.createWSA(subscriberEndpoint));

        return subscribeRequest;
    }

}
