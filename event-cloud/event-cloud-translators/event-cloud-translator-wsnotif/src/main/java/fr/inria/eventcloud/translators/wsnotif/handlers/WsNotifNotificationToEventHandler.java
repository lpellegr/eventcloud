package fr.inria.eventcloud.translators.wsnotif.handlers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslator;

/**
 * Translates a WS-Notification notification payload to an {@link Event}. The
 * handler translates only the leaves of the XML tree to {@link Quadruple}s.
 * Then, when all the leavea are translated, it is possible to retrieve an
 * {@link Event} associated to the set of Quadruples that have been translated.
 * 
 * @author lpellegr
 */
public class WsNotifNotificationToEventHandler extends DefaultHandler {

    private List<Quadruple> quadruples;

    private LinkedList<Element> elements;

    private String lastTextNodeRead;

    private Node graphNode;

    private Node topicFullQName;

    public WsNotifNotificationToEventHandler(String graphValue) {
        this.elements = new LinkedList<Element>();
        this.quadruples = new ArrayList<Quadruple>();
        this.graphNode = Node.createURI(graphValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {

        if (this.elements.size() > 1
                && this.elements.getLast().getFullQName().equals(
                        "http://docs.oasis-open.org/wsn/b-2/Message")) {
            this.topicFullQName = Node.createURI(uri + "/" + localName);
        }

        this.elements.add(new Element(uri, localName, qName, attributes));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (!this.lastTextNodeRead.trim().isEmpty()) {
            this.parseQuadruple(
                    this.elements.getLast().getFullQName(),
                    this.lastTextNodeRead);
        }
        this.elements.removeLast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        this.lastTextNodeRead = new String(ch, start, length);;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        // replaces null subjects by the topicFullQName
        for (int i = 0; i < this.quadruples.size(); i++) {
            Quadruple quad = this.quadruples.get(i);
            if (quad.getSubject() == null) {
                this.quadruples.set(i, new Quadruple(
                        quad.getGraph(), this.topicFullQName,
                        quad.getPredicate(), quad.getObject()));
            }
        }
    }

    private void parseQuadruple(String fullQName, String textNode) {
        StringBuilder predicate = new StringBuilder();
        for (int i = 0; i < this.elements.size(); i++) {
            predicate.append(this.elements.get(i).getFullQName());
            if (i < this.elements.size() - 1) {
                predicate.append(WsNotificationTranslator.URI_SEPARATOR);
            }
        }

        // TODO: annotate object with datatype from XSD

        this.quadruples.add(Quadruple.createWithoutTypeChecking(
                this.graphNode, this.topicFullQName,
                Node.createURI(predicate.toString()),
                Node.createLiteral(textNode)));
    }

    public List<Quadruple> getQuadruples() {
        return this.quadruples;
    }

    public Event getEvent() {
        return new Event(new Collection<Quadruple>(this.quadruples));
    }

    private static class Element {

        private final String uri;

        private final String localName;

        private final String qName;

        private final Attributes attributes;

        public Element(String uri, String localName, String qName,
                Attributes attributes) {
            super();
            this.uri = uri;
            this.localName = localName;
            this.qName = qName;
            this.attributes = attributes;
        }

        public String getFullQName() {
            return this.uri + "/" + this.localName;
        }

    }

}
