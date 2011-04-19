package fr.inria.eventcloud.overlay.can;

import java.util.Set;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.ontoware.rdf2go.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import fr.inria.eventcloud.api.SemanticFactory;
import fr.inria.eventcloud.config.EventCloudProperties;
import fr.inria.eventcloud.initializers.EventCloudInitializer;
import fr.inria.eventcloud.operations.can.SparqlConstructOperation;
import fr.inria.eventcloud.operations.can.SparqlConstructResponseOperation;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.util.RDF2GoBuilder;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Test the data transfert during a join operation for
 * {@link SemanticSpaceCANOverlay}.
 * 
 * @author lpellegr
 */
public class DataTransfertTest {

    private final static Logger logger =
            LoggerFactory.getLogger(DataTransfertTest.class);

    private static EventCloudInitializer initializer =
            new EventCloudInitializer();

    private static String[][] statementsToAdd = {
            {"http://A", "http://A", "http://A"},
            {"http://U", "http://U", "http://U"},
            {"http://Z", "http://Z", "http://Z"}};

    @BeforeClass
    public static void setUp() {
        initializer.setUpNetworkOnLocalMachine(1);
    }

    @Test
    public void testDataTransfert() {
        for (String[] stmt : statementsToAdd) {
            initializer.getRandomPeer().addStatement(
                    EventCloudProperties.DEFAULT_CONTEXT,
                    RDF2GoBuilder.createStatementInternal(
                            stmt[0], stmt[1], stmt[2]));
        }

        SemanticPeer newPeer = SemanticFactory.newActiveSemanticCanPeer();
        SemanticPeer oldPeer = initializer.getRandomPeer();

        initializer.getRandomTracker().addOnNetwork(newPeer);

        if (logger.isInfoEnabled()) {
            logger.info("Initial peer manages "
                    + initializer.getRandomTracker().getRandomPeer());
            logger.info("New peer manages " + newPeer);
        }

        String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";

        Set<Statement> resultOldPeer =
                SemanticHelper.asSet(((SparqlConstructResponseOperation) PAFuture.getFutureValue(oldPeer.receiveImmediateService(new SparqlConstructOperation(
                        EventCloudProperties.DEFAULT_CONTEXT, query)))).getResult()
                        .toRDF2Go());

        Set<Statement> resultNewPeer =
                SemanticHelper.asSet(((SparqlConstructResponseOperation) PAFuture.getFutureValue(newPeer.receiveImmediateService(new SparqlConstructOperation(
                        EventCloudProperties.DEFAULT_CONTEXT, query)))).getResult()
                        .toRDF2Go());

        Set<Statement> newSet = Sets.intersection(resultOldPeer, resultNewPeer);

        if (logger.isInfoEnabled()) {
            logger.info("Initial peer contains " + resultOldPeer.size()
                    + " data");
            logger.info("New peer contains " + resultNewPeer.size() + " data");
        }

        Assert.assertEquals(0, newSet.size());
        Assert.assertEquals(statementsToAdd.length, resultOldPeer.size()
                + resultNewPeer.size());
        Assert.assertTrue(resultOldPeer.size() < statementsToAdd.length);
    }

    @AfterClass
    public static void tearDown() {
        initializer.tearDownNetwork();
    }

}
