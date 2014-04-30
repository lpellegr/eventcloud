/**
 * Copyright (c) 2011-2014 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.benchmarks.putget;

import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * 
 * @author mantoine
 */
public class XmlWriter {

    private DocumentBuilderFactory dbfac;
    private DocumentBuilder docBuilder;
    private Document doc;
    private Element root;

    public XmlWriter(int nbPeers, int nbQuadruples, long insertTime,
            long timeToReceiveFinalResult, String datastoreType,
            long sizeOfQuadsInsertedInBytes) {
        try {
            this.dbfac = DocumentBuilderFactory.newInstance();
            this.docBuilder = this.dbfac.newDocumentBuilder();
            this.doc = this.docBuilder.newDocument();
            this.root = this.doc.createElement("Test");
            this.root.setAttribute("nbPeers", "" + nbPeers);
            this.root.setAttribute("nbQuadruples", "" + nbQuadruples);
            this.root.setAttribute("insertTimeInMillis", "" + insertTime);
            this.root.setAttribute("timeToReceiveFinalResultInMillis", ""
                    + timeToReceiveFinalResult);
            this.root.setAttribute("datastoreType", datastoreType);
            this.root.setAttribute("sizeOfQuadsInsertedInBytes", ""
                    + sizeOfQuadsInsertedInBytes);
            this.doc.appendChild(this.root);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public Element addQuery(int queryNumber, long elapsedTime,
                            long queryDatastoreTime, long latency,
                            long inboundHopCount) {
        Element child = this.doc.createElement("Query");
        child.setAttribute("number", "" + queryNumber);
        child.setAttribute("elapsedTimeInMillis", "" + elapsedTime);
        child.setAttribute("queryDatastoreTime", "" + queryDatastoreTime);
        child.setAttribute("latency", "" + latency);
        child.setAttribute("inboundHopCount", "" + inboundHopCount);
        this.root.appendChild(child);
        return child;
    }

    public Element addElement(Element elem, String elementName, String value) {
        Element child = this.doc.createElement(elementName);
        elem.appendChild(child);
        Text text = this.doc.createTextNode(value);
        child.appendChild(text);
        return child;
    }

    public Element addSubQueryResults(Element elem, String subQuery,
                                      int nbResults) {
        Element child = this.doc.createElement("subQuery");
        elem.appendChild(child);
        Text text = this.doc.createTextNode(subQuery);
        child.appendChild(text);
        child.setAttribute("nbResults", "" + nbResults);
        return child;
    }

    public void end() {
        try {
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(this.doc);
            trans.transform(source, result);
            String xmlString = sw.toString();
            System.out.println("\n\n" + xmlString);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void writeXmlFile(String fileName) {
        try {
            Source source = new DOMSource(this.doc);
            File file = new File(fileName);
            Result result = new StreamResult(file);
            Transformer xformer =
                    TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
        } catch (TransformerException e) {
        }
    }
}
