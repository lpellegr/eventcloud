package fr.inria.eventcloud.benchmarks.putget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.w3c.dom.Element;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.providers.SemanticInMemoryOverlayProvider;
import fr.inria.eventcloud.providers.SemanticPersistentOverlayProvider;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.reasoner.SparqlReasoner;
import fr.inria.eventcloud.utils.Callback;

public class BenchmarkLauncher {

    private EventCloudId eventCloudId;

    private JunitEventCloudInfrastructureDeployer deployer;

    private PutGetProxy putGetProxy;

    private final Callback<Quadruple> callback;

    private List<Quadruple> quadruples;

    private SparqlReasoner reasoner;

    private static String query1 =
            "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/> "
                    + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> "
                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "SELECT DISTINCT ?product ?label "
                    + "WHERE { GRAPH ?g  { "
                    + " ?product rdfs:label ?label ."
                    + " ?product a bsbm-inst:ProductType1 ."
                    + " ?product bsbm:productFeature bsbm-inst:ProductFeature25 . "
                    + " ?product bsbm:productFeature bsbm-inst:ProductFeature29 . "
                    + " ?product bsbm:productPropertyNumeric1 ?value1 . "
                    + " FILTER (?value1 > 15) }}"
                    + " ORDER BY ?label "
                    + " LIMIT 10 ";

    private static String query5 =
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> "
                    + "PREFIX dataFromProducer1: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer1/> "
                    + "SELECT DISTINCT ?product ?productLabel "
                    + "WHERE { GRAPH ?g  {"
                    + "	?product rdfs:label ?productLabel ."
                    + " FILTER (dataFromProducer1:Product1 != ?product)"
                    + "	dataFromProducer1:Product1 bsbm:productFeature ?prodFeature ."
                    + "	?product bsbm:productFeature ?prodFeature ."
                    + "	dataFromProducer1:Product1 bsbm:productPropertyNumeric1 ?origProperty1 ."
                    + "	?product bsbm:productPropertyNumeric1 ?simProperty1 ."
                    + "	FILTER (?simProperty1 < (?origProperty1 + 300) && ?simProperty1 > (?origProperty1 - 300))"
                    + "	dataFromProducer1:Product1 bsbm:productPropertyNumeric2 ?origProperty2 ."
                    + "	?product bsbm:productPropertyNumeric2 ?simProperty2 ."
                    + "	FILTER (?simProperty2 < (?origProperty2 + 300) && ?simProperty2 > (?origProperty2 - 300))"
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
                    + "	?offer bsbm:vendor ?vendor ."
                    + " ?offer dc:publisher ?vendor ."
                    + "	?offer bsbm:deliveryDays ?deliveryDays ."
                    + "	FILTER (?deliveryDays <= 6)"
                    + "	?offer bsbm:price ?price ."
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
    private int nbPeers, nbQuadruplesAdded;
    private long startTime, elapsedTime, testTime, timeToInsertQuads;
    private String fileToParse, datastoreType;

    public BenchmarkLauncher(int nbPeers, String fileName, String storage)
            throws EventCloudIdNotManaged {
        P2PStructuredProperties.ENABLE_BENCHMARKS_INFORMATION.setValue(true);
        this.nbPeers = nbPeers;
        this.fileToParse = fileName;
        if (storage.equals("p"))
            this.datastoreType = "persistent";
        else if (storage.equals("m"))
            this.datastoreType = "memory";
        this.callback = new Callback<Quadruple>() {
            @Override
            public void execute(Quadruple quad) {
                quadruples.add(quad);
                nbQuadruplesAdded++;
            }
        };

        quadruples = new ArrayList<Quadruple>();
        try {
            RdfParser.parse(
                    new FileInputStream(new File(fileToParse)),
                    SerializationFormat.TriG, this.callback);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.deployer = new JunitEventCloudInfrastructureDeployer();

        SerializableProvider<? extends SemanticCanOverlay> overlayProvider =
                null;
        if (datastoreType.equals("persistent"))
            overlayProvider = new SemanticPersistentOverlayProvider();
        else if (datastoreType.equals("memory"))
            overlayProvider = new SemanticInMemoryOverlayProvider();

        this.eventCloudId =
                deployer.newEventCloud(new EventCloudDeploymentDescriptor(
                        overlayProvider), 1, this.nbPeers);

        this.putGetProxy =
                ProxyFactory.newPutGetProxy(
                        deployer.getEventCloudsRegistryUrl(), this.eventCloudId);

        responses = new ArrayList<SparqlSelectResponse>();
        queries = new ArrayList<String>();
        queries.add(query1);
        queries.add(query5);
        queries.add(query10);
        queries.add(query11);

        timeToInsertQuads = System.currentTimeMillis();
        this.putGetProxy.add(quadruples);
        timeToInsertQuads = System.currentTimeMillis() - timeToInsertQuads;

        for (int i = 0; i < queries.size(); i++) {
            startTime = System.currentTimeMillis();
            responses.add(this.putGetProxy.executeSparqlSelect(queries.get(i)));
            elapsedTime = System.currentTimeMillis() - startTime;
            responses.get(i).setTimeToGetResult(elapsedTime);
            testTime += elapsedTime;
            reasoner = new SparqlReasoner();
            responses.get(i).setNbSubQueries(
                    reasoner.parseSparql(queries.get(i)).size());
        }

        XmlWriter xmlWriter =
                new XmlWriter(
                        nbPeers, nbQuadruplesAdded, timeToInsertQuads,
                        testTime, datastoreType);
        for (int j = 0; j < responses.size(); j++) {
            int nbResults = 0;
            while (responses.get(j).getResult().hasNext()) {
                responses.get(j).getResult().next();
                nbResults++;
            }
            Element query =
                    xmlWriter.addQuery(j + 1, responses.get(j)
                            .getTimeToGetResult(), responses.get(j)
                            .getQueryDatastoreTime(), responses.get(j)
                            .getLatency());
            xmlWriter.addElement(query, "finalResults", "" + nbResults);
            xmlWriter.addElement(query, "intermediateResults", ""
                    + responses.get(j).getNbIntermediateResults());
            xmlWriter.addElement(query, "intermediateResultsSizeInBytes", ""
                    + responses.get(j).getSizeOfIntermediateResultsInBytes());
            xmlWriter.addElement(query, "subQueries", ""
                    + responses.get(j).getNbSubQueries());
        }

        xmlWriter.end();
        xmlWriter.writeXmlFile("test_storage_" + datastoreType + "_peers_"
                + nbPeers + "_quads_" + nbQuadruplesAdded + ".xml");

        System.exit(0);
    }

    public static void main(String[] args) throws NumberFormatException,
            EventCloudIdNotManaged, ArrayIndexOutOfBoundsException {
        if (args.length != 3)
            throw new ArrayIndexOutOfBoundsException(
                    "Enter 3 parameters : number of peers, datafile location in your filesystem (absolute path), "
                            + "m or p for in memory or persistent storage");
        new BenchmarkLauncher(Integer.parseInt(args[0]), args[1], args[2]);
    }

}
