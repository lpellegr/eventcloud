package fr.inria.eventcloud.benchmarks.putget;

import java.io.*;

import org.w3c.dom.*;

import javax.xml.parsers.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

public class XmlWriter {
	
	private DocumentBuilderFactory dbfac;
	private DocumentBuilder docBuilder;
	private Document doc;
	private Element root;

    public XmlWriter(int nbPeers, int nbQuadruples, long insertTime, long timeToReceiveFinalResult, String datastoreType) {
        try {
            dbfac = DocumentBuilderFactory.newInstance();
            docBuilder = dbfac.newDocumentBuilder();
            doc = docBuilder.newDocument();
            root = doc.createElement("Test");
            root.setAttribute("nbPeers", ""+nbPeers);
            root.setAttribute("nbQuadruples", ""+nbQuadruples);
            root.setAttribute("insertTimeInMillis", ""+insertTime);
            root.setAttribute("timeToReceiveFinalResultInMillis", ""+timeToReceiveFinalResult);
            root.setAttribute("datastoreType", datastoreType);
            doc.appendChild(root);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    public Element addQuery(int queryNumber, long elapsedTime, long queryDatastoreTime, long latency)
    {
    	Element child = doc.createElement("Query");
        child.setAttribute("number", ""+queryNumber);
        child.setAttribute("elapsedTimeInMillis", ""+elapsedTime);
        child.setAttribute("queryDatastoreTime", ""+queryDatastoreTime);
        child.setAttribute("latency", ""+latency);
        root.appendChild(child);
        return child;
    }
    
    public Element addElement(Element elem, String elementName, String value)
    {
    	Element child = doc.createElement(elementName);
        elem.appendChild(child);
        Text text = doc.createTextNode(value);
        child.appendChild(text);
        return child;
    }
    
    public void end()
    {
    	try
    	{
    	TransformerFactory transfac = TransformerFactory.newInstance();
    	Transformer trans = transfac.newTransformer();
    	trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    	trans.setOutputProperty(OutputKeys.INDENT, "yes");
    	StringWriter sw = new StringWriter();
    	StreamResult result = new StreamResult(sw);
    	DOMSource source = new DOMSource(doc);
    	trans.transform(source, result);
    	String xmlString = sw.toString();
    	System.out.println("\n\n" + xmlString);
    	}
    	catch (Exception e) {
            System.out.println(e);
        }
    
    }
    
    public void writeXmlFile(String fileName) {
        try {
            Source source = new DOMSource(doc);
            File file = new File(fileName);
            Result result = new StreamResult(file);
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
        } catch (TransformerException e) {
        }
    }
}
