/**
 * Copyright (c) 2011-2013 INRIA.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.w3c.dom.Element;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.providers.SemanticOverlayProvider;
import fr.inria.eventcloud.reasoner.SparqlReasoner;
import fr.inria.eventcloud.utils.Callback;
import fr.inria.eventcloud.utils.RDFReader;

/**
 * Launcher for running benchmarks with historical queries.
 * 
 * @author mantoine
 */
public class BenchmarkLauncher {

    private EventCloudId eventCloudId;

    private JunitEventCloudInfrastructureDeployer deployer;

    private PutGetApi putGetProxy;

    private final Callback<Quadruple> callback;

    private List<Quadruple> quadruples;

    private static String query1 =
            "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/> "
                    + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> "
                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "SELECT DISTINCT ?product ?label "
                    + "WHERE { GRAPH ?g  { "
                    + " ?product rdfs:label ?label ."
                    + " FILTER (?label > \"http://aaaaa\")"
                    + " ?product a bsbm-inst:ProductType145 ."
                    + " ?product bsbm:productFeature bsbm-inst:ProductFeature4504 . "
                    + " ?product bsbm:productFeature bsbm-inst:ProductFeature4511 . "
                    + " ?product bsbm:productPropertyNumeric1 ?value1  "
                    + " FILTER (?value1 > 15) "
                    + " } } "
                    + "ORDER BY ?label "
                    + " LIMIT 10 ";

    private static String query5 =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> "
                    + "PREFIX dataFromProducer1: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer1/> "
                    + "SELECT DISTINCT ?product ?productLabel "
                    + "WHERE { GRAPH ?g  {"
                    + " ?product rdfs:label ?productLabel ."
                    + " FILTER (dataFromProducer1:Product1 != ?product)"
                    + " dataFromProducer1:Product1 bsbm:productFeature ?prodFeature ."
                    + " ?product bsbm:productFeature ?prodFeature ."
                    + " dataFromProducer1:Product1 bsbm:productPropertyNumeric1 ?origProperty1 ."
                    + " ?product bsbm:productPropertyNumeric1 ?simProperty1 ."
                    + " FILTER (?simProperty1 < (?origProperty1 + 300) && ?simProperty1 > (?origProperty1 - 300))"
                    + " dataFromProducer1:Product1 bsbm:productPropertyNumeric2 ?origProperty2 ."
                    + " ?product bsbm:productPropertyNumeric2 ?simProperty2 ."
                    + " FILTER (?simProperty2 < (?origProperty2 + 300) && ?simProperty2 > (?origProperty2 - 300))"
                    + "}}" + "ORDER BY ?productLabel " + "LIMIT 5";

    public static String query10 =
            "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> "
                    + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                    + "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
                    + "PREFIX dataFromProducer1: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer1/> "
                    + "SELECT DISTINCT ?offer ?price "
                    + "WHERE { GRAPH ?g  { "
                    + " ?offer bsbm:product dataFromProducer1:Product12 ."
                    + " ?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#GB> ."
                    + " ?offer bsbm:vendor ?vendor ."
                    + " ?offer dc:publisher ?vendor ."
                    + " ?offer bsbm:deliveryDays ?deliveryDays ."
                    + " FILTER (?deliveryDays <= 6)"
                    + " ?offer bsbm:price ?price ."
                    + " ?offer bsbm:validTo ?date "
                    + " FILTER (?date > \"2000-01-06T00:00:00\"^^xsd:dateTime)"
                    + "}}" + "ORDER BY xsd:double(str(?price)) " + "LIMIT 10";

    public static String query11 =
            "PREFIX dataFromVendor1: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/> "
                    + "SELECT ?property ?hasValue ?isValueOf "
                    + "WHERE { GRAPH ?g  {"
                    + "  { dataFromVendor1:Offer3 ?property ?hasValue }"
                    + "  UNION"
                    + "  { ?isValueOf ?property dataFromVendor1:Offer3 }"
                    + " } }";

    private List<SparqlSelectResponse> responses;
    private List<String> queries;
    private int nbPeers, nbQuadruplesAdded = 0;
    private long startTime, elapsedTime, testTime, timeToInsertQuads,
            sizeOfQuadsInsertedInBytes;
    private String fileToParse, datastoreType;

    public BenchmarkLauncher(int nbPeers, String fileName, String storage)
            throws EventCloudIdNotManaged {
        P2PStructuredProperties.ENABLE_BENCHMARKS_INFORMATION.setValue(true);
        this.nbPeers = nbPeers;
        this.fileToParse = fileName;
        if (storage.equals("p")) {
            this.datastoreType = "persistent";
        } else if (storage.equals("m")) {
            this.datastoreType = "memory";
        }
        this.sizeOfQuadsInsertedInBytes = 0;
        this.callback = new Callback<Quadruple>() {
            @Override
            public void execute(Quadruple quad) {
                BenchmarkLauncher.this.sizeOfQuadsInsertedInBytes +=
                        BenchmarkLauncher.this.quadToBytes(quad);
                BenchmarkLauncher.this.quadruples.add(quad);
                BenchmarkLauncher.this.nbQuadruplesAdded++;
            }
        };

        this.quadruples = new ArrayList<Quadruple>();
        try {
            RDFReader.read(
                    new FileInputStream(new File(this.fileToParse)),
                    SerializationFormat.TriG, this.callback);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.deployer = new JunitEventCloudInfrastructureDeployer();

        SerializableProvider<? extends SemanticCanOverlay> overlayProvider =
                null;
        if (this.datastoreType.equals("persistent")) {
            overlayProvider = new SemanticOverlayProvider(false);
        } else if (this.datastoreType.equals("memory")) {
            overlayProvider = new SemanticOverlayProvider(true);
        }

        this.eventCloudId =
                this.deployer.newEventCloud(new EventCloudDeploymentDescriptor(
                        overlayProvider), 1, this.nbPeers);

        this.putGetProxy =
                ProxyFactory.newPutGetProxy(
                        this.deployer.getEventCloudsRegistryUrl(),
                        this.eventCloudId);

        this.responses = new ArrayList<SparqlSelectResponse>();
        this.queries = new ArrayList<String>();
        this.queries.add(query1);
        this.queries.add(query5);
        this.queries.add(query10);
        this.queries.add(query11);

        this.timeToInsertQuads = System.currentTimeMillis();
        this.putGetProxy.add(this.quadruples);
        this.timeToInsertQuads =
                System.currentTimeMillis() - this.timeToInsertQuads;

        for (int i = 0; i < this.queries.size(); i++) {
            this.startTime = System.currentTimeMillis();

            try {
                this.responses.add(this.putGetProxy.executeSparqlSelect(this.queries.get(i)));
            } catch (MalformedSparqlQueryException e) {
                throw new IllegalStateException(e);
            }

            this.elapsedTime = System.currentTimeMillis() - this.startTime;
            this.responses.get(i).setTimeToGetResult(this.elapsedTime);
            this.testTime += this.elapsedTime;

            try {
                this.responses.get(i).setNbSubQueries(
                        SparqlReasoner.parse(this.queries.get(i)).size());
            } catch (MalformedSparqlQueryException e) {
                throw new IllegalStateException(e);
            }
        }

        XmlWriter xmlWriter =
                new XmlWriter(
                        nbPeers, this.nbQuadruplesAdded,
                        this.timeToInsertQuads, this.testTime,
                        this.datastoreType, this.sizeOfQuadsInsertedInBytes);
        for (int j = 0; j < this.responses.size(); j++) {
            int nbResults = 0;
            SparqlSelectResponse resp = this.responses.get(j);
            while (resp.getResult().hasNext()) {
                resp.getResult().next();
                nbResults++;
            }
            Element query =
                    xmlWriter.addQuery(
                            j + 1, resp.getTimeToGetResult(), resp.getStats()
                                    .getCumulativeTimeToQueryDatastores(),
                            resp.getStats()
                                    .getCumulativeTimeToExecuteSubQueries(),
                            resp.getStats().getCumulativeInboundHopCount());
            xmlWriter.addElement(query, "finalResults", "" + nbResults);
            xmlWriter.addElement(query, "intermediateResults", ""
                    + resp.getNbIntermediateResults());
            xmlWriter.addElement(query, "intermediateResultsSizeInBytes", ""
                    + resp.getSizeOfIntermediateResultsInBytes());
            xmlWriter.addElement(query, "subQueries", ""
                    + resp.getNbSubQueries());
            Map<String, Integer> nbResultsForEachSubquery =
                    resp.getMapSubQueryNbResults();
            for (String subQuery : nbResultsForEachSubquery.keySet()) {
                xmlWriter.addSubQueryResults(
                        query, subQuery, nbResultsForEachSubquery.get(subQuery));
            }
        }
        xmlWriter.end();
        xmlWriter.writeXmlFile("test_storage_" + this.datastoreType + "_peers_"
                + nbPeers + "_quads_" + this.nbQuadruplesAdded + ".xml");
        System.exit(0);
    }

    public int quadToBytes(Object quad) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(quad);
            oos.flush();
            oos.close();
            bos.close();
            bytes = bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return bytes.length;
    }

    public static void main(String[] args) throws NumberFormatException,
            EventCloudIdNotManaged, ArrayIndexOutOfBoundsException {
        if (args.length != 3) {
            throw new ArrayIndexOutOfBoundsException(
                    "Enter 3 parameters : number of peers, datafile location in your filesystem (absolute path), "
                            + "m or p for in memory or persistent storage");
        }
        new BenchmarkLauncher(Integer.parseInt(args[0]), args[1], args[2]);
    }

}
