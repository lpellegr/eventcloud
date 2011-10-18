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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.TransformBase;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;

import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslator;
import fr.inria.eventcloud.utils.ReflectionUtils;
import fr.inria.eventcloud.webservices.api.SubscribeInfos;

/**
 * XML Adapter for {@link SubscribeInfos} objects.
 * 
 * @author lpellegr
 */
public class SubscribeInfosAdapter extends
        XmlAdapter<Subscribe, SubscribeInfos> {

    private WsNotificationTranslator translator;

    public SubscribeInfosAdapter() {
        this.translator = new WsNotificationTranslator();
    }

    /**
     * Converts the specified SubscribeInfos to its string representation.
     * 
     * @param subscribeInfos
     *            the SubscribeInfos to be converted.
     * 
     * @return the string representing the specified SubscribeInfos.
     */
    @Override
    public Subscribe marshal(SubscribeInfos subscribeInfos) throws Exception {
        Subscribe subscribeRequest = new Subscribe();
        FilterType filterType = new FilterType();
        TopicExpressionType tet = new TopicExpressionType();

        // retrieve topic name from SPARQL query
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

        tet.getContent().add(topicName.toString());
        filterType.getAny().add(tet);
        subscribeRequest.setFilter(filterType);

        W3CEndpointReferenceBuilder endPointReferenceBuilder =
                new W3CEndpointReferenceBuilder();
        endPointReferenceBuilder.address(subscribeInfos.getSubscriberWsUrl());
        subscribeRequest.setConsumerReference(endPointReferenceBuilder.build());

        return subscribeRequest;
    }

    /**
     * Converts the specified {@link Subscribe} request to its corresponding
     * {@link SubscribeInfos}.
     * 
     * @param subscribeRequest
     *            the Subscribe representation to be converted.
     * 
     * @return the SubscribeInfos represented by the specified Subscribe object.
     */
    @Override
    public SubscribeInfos unmarshal(Subscribe subscribeRequest)
            throws Exception {
        String subscriberUrl =
                (String) ReflectionUtils.getFieldValue(
                        ReflectionUtils.getFieldValue(
                                subscribeRequest.getConsumerReference(),
                                "address"), "uri");

        return new SubscribeInfos(
                this.translator.translateSubscribeToSparqlQuery(subscribeRequest),
                subscriberUrl);
    }

}
