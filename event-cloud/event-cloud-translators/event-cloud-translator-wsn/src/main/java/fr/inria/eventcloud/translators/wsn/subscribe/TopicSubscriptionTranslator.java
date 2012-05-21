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
package fr.inria.eventcloud.translators.wsn.subscribe;

import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.play_commons.eventformat.Namespace;
import eu.play_project.play_commons.eventformat.Stream;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.Translator;

/**
 * This class defines a method to translate a WS-Notification {@link Subscribe}
 * message, which is supposed to be a topic based subscription, to a SPARQL
 * subscription. This translator makes the assumption that events which are
 * published comply with the event format defined with FZI (see
 * http://km.aifb.kit.edu/sites/lodstream/).
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class TopicSubscriptionTranslator extends Translator<Subscribe, String> {

    private static Logger log =
            LoggerFactory.getLogger(TopicSubscriptionTranslator.class);

    /**
     * Translates a WS-Notification {@link Subscribe} message, which is supposed
     * to be a topic based subscription, to a SPARQL subscription.
     * 
     * @param subscription
     *            the subscribe message to be translated.
     * 
     * @return a SPARQL query corresponding to the topic-based subscription.
     */
    @Override
    @SuppressWarnings("unchecked")
    public String translate(Subscribe subscription) throws TranslationException {
        FilterType filterType = subscription.getFilter();
        if (filterType != null) {
            List<Object> any = filterType.getAny();
            if (any.size() > 0) {
                TopicExpressionType topicExpressionType =
                        ((JAXBElement<TopicExpressionType>) any.get(0)).getValue();

                List<Object> content = topicExpressionType.getContent();
                if (content.size() > 0) {
                    String topic =
                            ((String) content.get(0)).trim().replaceAll(
                                    "\n", "");

                    String topicLocalPart =
                            org.apache.xml.utils.QName.getLocalPart(topic);
                    String topicNamespace = null;

                    for (Entry<QName, String> entry : topicExpressionType.getOtherAttributes()
                            .entrySet()) {
                        // TODO: compare by using prefix declaration and not
                        // local parts. It is possible to have two local part
                        // values that are the same but each one is using a
                        // different prefix. In such a case, the namespace
                        // extracted may be wrong
                        if (entry.getKey()
                                .getLocalPart()
                                .equals(topicLocalPart)) {
                            topicNamespace = entry.getValue();
                            break;
                        }
                    }

                    if (topicNamespace == null) {
                        // FIXME: a TranslationException should be thrown but
                        // first the #43 has to be fixed
                        log.warn("No namespace declared for prefix '"
                                + org.apache.xml.utils.QName.getPrefixPart(topic)
                                + "' associated to topic "
                                + topic
                                + " the default topic namespace will be used 'http://streams.event-processing.org/ids/'");

                        topicNamespace =
                                "http://streams.event-processing.org/ids/";
                    }

                    return "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s <"
                            + Namespace.TYPES.getUri() + "stream> <"
                            + topicNamespace + topicLocalPart
                            + Stream.STREAM_ID_SUFFIX + "> . } }";
                } else {
                    throw new TranslationException(
                            "No topic content set in the subscribe message");
                }
            } else {
                throw new TranslationException(
                        "No any object set in the subscribe message");
            }
        } else {
            throw new TranslationException(
                    "No filter set in the subscribe message");
        }
    }
}
