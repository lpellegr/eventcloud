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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.configuration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.extensions.p2p.structured.configuration.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser used to retrieve Event-Cloud properties from a preferences file.
 * 
 * @author lpellegr
 */
public class ConfigurationParser {

    protected static Logger logger =
            LoggerFactory.getLogger(ConfigurationParser.class);

    private static final String JAXP_SCHEMA_LANGUAGE =
            "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    private static final String JAXP_SCHEMA_SOURCE =
            "http://java.sun.com/xml/jaxp/properties/schemaSource";

    private static final String W3C_XML_SCHEMA =
            "http://www.w3.org/2001/XMLSchema";

    private static final String XPATH_PROPS = "//properties/property";

    private static final String ATTR_KEY = "key";

    private static final String ATTR_VALUE = "value";

    private static Map<String, Property> getProperties() {
        Field[] fields = EventCloudProperties.class.getDeclaredFields();
        Map<String, Property> properties =
                new HashMap<String, Property>(fields.length);
        for (Field field : fields) {
            if (Property.class.isAssignableFrom(field.getType())
                    && Modifier.isStatic(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    Property prop = Property.class.cast(field.get(null));
                    properties.put(prop.getName(), prop);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }

    public static void parse(String filename) {
        InputSource source = null;
        try {
            source = new org.xml.sax.InputSource(filename);

            DocumentBuilderFactory domFactory;
            DocumentBuilder builder;
            Document document;
            XPath xpath;
            domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false);
            domFactory.setValidating(true);
            domFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            domFactory.setAttribute(
                    JAXP_SCHEMA_SOURCE, EventCloudProperties.class.getClass()
                            .getResource(
                                    "/config/xml/EventCloudConfiguration.xsd")
                            .toString());
            domFactory.setValidating(true);

            XPathFactory factory = XPathFactory.newInstance();

            xpath = factory.newXPath();
            xpath.setNamespaceContext(new MyNamespaceContext());
            builder = domFactory.newDocumentBuilder();
            builder.setErrorHandler(new MyDefaultHandler());

            document = builder.parse(source);

            NodeList nodes;

            nodes =
                    (NodeList) xpath.evaluate(
                            XPATH_PROPS, document, XPathConstants.NODESET);

            boolean unknownProperty = false;
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String key = getAttributeValue(node, ATTR_KEY);
                String value = getAttributeValue(node, ATTR_VALUE);

                Map<String, Property> properties = getProperties();

                Property prop = properties.get(key);
                if (prop != null) {
                    if (prop.isValid(value)) {
                        System.setProperty(key, value);
                    } else {
                        logger.warn("Invalid value, " + value + " for key "
                                + key + ". Must be a "
                                + prop.getType().toString());
                    }
                } else {
                    logger.warn("Skipped unknown property: " + key);
                    unknownProperty = true;
                }
            }
            if (unknownProperty) {
                logger.warn("All supported Event-Cloud properties are declared inside "
                        + PAProperties.class.getName()
                        + ". Please check your Event-Cloud Configuration file: "
                        + filename);
            }
        } catch (SAXException e) {
            logger.warn("Invalid Event-Cloud Configuration file: " + source, e);
        } catch (ParserConfigurationException e) {
            logger.warn("Invalid Event-Cloud Configuration file: " + source, e);
        } catch (XPathExpressionException e) {
            logger.warn("Invalid Event-Cloud Configuration file: " + source, e);
        } catch (IOException e) {
            logger.error(
                    "Error while parsing Event-Cloud Configuration file", e);
        }
    }

    private static String getAttributeValue(Node node, String attributeName) {
        Node namedItem = node.getAttributes().getNamedItem(attributeName);
        return (namedItem != null)
                ? namedItem.getNodeValue() : null;
    }

    private static class MyNamespaceContext implements NamespaceContext {
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new NullPointerException("Null prefix");
            }
            return XMLConstants.DEFAULT_NS_PREFIX;
        }

        // This method isn't necessary for XPath processing.
        public String getPrefix(String uri) {
            throw new UnsupportedOperationException();
        }

        // This method isn't necessary for XPath processing either.
        public Iterator<String> getPrefixes(String uri) {
            throw new UnsupportedOperationException();
        }
    }

    private static class MyDefaultHandler extends DefaultHandler {
        @Override
        public void warning(SAXParseException e) {
            logger.warn("Warning Line " + e.getLineNumber() + ": "
                    + e.getMessage() + "\n");
        }

        @Override
        public void error(SAXParseException e) throws SAXParseException {
            logger.error("Error Line " + e.getLineNumber() + ": "
                    + e.getMessage() + "\n");
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXParseException {
            logger.error("Error Line " + e.getLineNumber() + ": "
                    + e.getMessage() + "\n");
            throw e;
        }
    }

}
