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
package fr.inria.eventcloud.webservices.api.adapters;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.TransformBase;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;

import fr.inria.eventcloud.translators.wsn.WsNotificationTranslator;
import fr.inria.eventcloud.utils.ReflectionUtils;
import fr.inria.eventcloud.webservices.api.SubscribeInfos;

/**
 * XML Adapter for {@link SubscribeInfos} objects.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class SubscribeInfosAdapter extends
        XmlAdapter<Subscribe, SubscribeInfos> {

    private static Logger log =
            LoggerFactory.getLogger(SubscribeInfosAdapter.class);

    private WsNotificationTranslator translator;

    public SubscribeInfosAdapter() {
        this.translator = new WsNotificationTranslator();
    }

    /**
     * Converts the specified subscribe infos to its subscribe object
     * representation.
     * 
     * @param subscribeInfos
     *            the subscribe infos to be converted.
     * 
     * @return the subscribe object representing the specified subscribe infos.
     */
    @Override
    public Subscribe marshal(SubscribeInfos subscribeInfos) throws Exception {
        Subscribe subscribe = new Subscribe();
        FilterType filterType = new FilterType();
        TopicExpressionType topicExpressionType = new TopicExpressionType();

        // Retrieve topic name from SPARQL query
        final StringBuilder topicName = new StringBuilder();
        OpAsQuery.asQuery(Transformer.transform(
                new TransformBase() {
                    @Override
                    public Op transform(OpBGP opBGP) {
                        topicName.append(opBGP.getPattern()
                                .get(0)
                                .getSubject()
                                .getURI());
                        return super.transform(opBGP);
                    }

                },
                Algebra.compile(QueryFactory.create(subscribeInfos.getSparqlQuery()))));
        topicExpressionType.getContent().add(topicName.toString());
        JAXBElement<TopicExpressionType> jaxbElement =
                new JAXBElement<TopicExpressionType>(
                        new QName(
                                "http://docs.oasis-open.org/wsn/b-2",
                                "TopicExpression"), TopicExpressionType.class,
                        topicExpressionType);
        filterType.getAny().add(jaxbElement);
        subscribe.setFilter(filterType);

        W3CEndpointReferenceBuilder endPointReferenceBuilder =
                new W3CEndpointReferenceBuilder();
        endPointReferenceBuilder.address(subscribeInfos.getSubscriberWsUrl());
        subscribe.setConsumerReference(endPointReferenceBuilder.build());

        return subscribe;
    }

    /**
     * Converts the specified subscribe object to its corresponding subscribe
     * infos.
     * 
     * @param subscribe
     *            the subscribe object to be converted.
     * 
     * @return the subscribe infos represented by the specified subscribe
     *         object.
     */
    @Override
    public SubscribeInfos unmarshal(Subscribe subscribe) throws Exception {
        W3CEndpointReference consumerReference =
                subscribe.getConsumerReference();

        if (consumerReference != null) {
            Object address =
                    ReflectionUtils.getFieldValue(consumerReference, "address");
            if (address != null) {
                String subscriberUrl =
                        (String) ReflectionUtils.getFieldValue(address, "uri");
                if (subscriberUrl != null) {
                    String sparqlQuery =
                            this.translator.translateTopicSubscription(subscribe);
                    if (sparqlQuery != null) {
                        return new SubscribeInfos(sparqlQuery, subscriberUrl);
                    } else {
                        log.error("SPARQL query cannot be extracted from subscribe notification");
                    }
                }
            }

            log.error("Consumer reference cannot be extracted from subscribe notification");
        } else {
            log.error("Subscribe notification does not contain consumer reference");
        }

        return null;
    }
}
