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

import javax.xml.namespace.QName;

import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.play_commons.constants.Stream;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.Translator;
import fr.inria.eventcloud.translators.wsn.WsnHelper;
import fr.inria.eventcloud.translators.wsn.WsnTranslatorConstants;

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

    private static TopicSubscriptionTranslator instance;

    private TopicSubscriptionTranslator() {

    }

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
    public String translate(Subscribe subscription) throws TranslationException {
        QName topic = null;
        try {
            topic = WsnHelper.getTopic(subscription);
        } catch (IllegalArgumentException e) {
            throw new TranslationException(e);
        }

        String topicNamespace = topic.getNamespaceURI();

        if ((topicNamespace == null) || (topicNamespace.equals(""))) {
            // FIXME: a TranslationException should be thrown but
            // first the issue #43 has to be fixed
            log.warn("No namespace declared for prefix '"
                    + topic.getPrefix()
                    + "' associated to topic "
                    + topic
                    + " the default topic namespace will be used 'http://streams.event-processing.org/ids/'");

            topicNamespace = "http://streams.event-processing.org/ids/";
        }

        return "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s <"
                + WsnTranslatorConstants.TOPIC_TEXT + "> <" + topicNamespace
                + topic.getLocalPart() + Stream.STREAM_ID_SUFFIX + "> . } }";
    }

    public static synchronized TopicSubscriptionTranslator getInstance() {
        if (instance == null) {
            instance = new TopicSubscriptionTranslator();
        }

        return instance;
    }

}
