package fr.inria.eventcloud.overlay.can;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.ontoware.rdf2go.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.SemanticFactory;
import fr.inria.eventcloud.initializers.SpaceNetworkInitializer;
import fr.inria.eventcloud.kernel.operations.datastore.DatastoreResponseOperation;
import fr.inria.eventcloud.kernel.operations.datastore.SparqlConstructOperation;
import fr.inria.eventcloud.operations.can.GetOverlayKernelOperation;
import fr.inria.eventcloud.operations.can.GetOverlayKernelResponseOperation;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;
import fr.inria.eventcloud.util.RDF2GoBuilder;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Test the data transfert during a join operation for {@link SemanticSpaceCANOverlay}.
 * 
 * @author lpellegr
 */
public class DataTransfertTest {

    private final static Logger logger = LoggerFactory.getLogger(DataTransfertTest.class);

    private static SpaceNetworkInitializer initializer =
            new SpaceNetworkInitializer(
                    RDF2GoBuilder.createURI("http://www.inria.fr"));

    private static String[][] statementsToAdd = {
            { "http://A", "http://A", "http://A" },
            { "http://U", "http://U", "http://U" },
            { "http://Z", "http://Z", "http://Z" } };

    @BeforeClass
    public static void setUp() {
        initializer.setUpNetworkOnLocalMachine(1);
    }

    @Test
    public void testDataTransfert() {
        StringBuffer query = new StringBuffer();
        for (String[] stmt : statementsToAdd) {
            initializer.addStatement(stmt[0], stmt[1], stmt[2]);
        }

        query.append("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH<");
        query.append(initializer.getSpaceURI().toString());
        query.append("> { ?s ?p ?o }. }");

        Set<Statement> response = initializer.sparqlConstruct(query.toString());
        Assert.assertEquals(statementsToAdd.length, response.size());

        SemanticPeer newPeer = null;
        try {
            newPeer = SemanticFactory.newActiveSemanticPeer(
                    Arrays.asList(initializer.getTrackers()),
                    new SemanticSpaceCanOverlay(
                            initializer.getSpaceURI(),
                            SemanticFactory.newActiveSemanticSpaceOverlayKernel(
                                    initializer.getTrackers(), true)));
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        SemanticPeer oldPeer = (SemanticPeer) PAFuture.getFutureValue(
                                initializer.getRandomTracker().getRandomPeer());
        
		initializer.getRandomTracker().addOnNetwork(newPeer);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Initial peer manages "
                    + initializer.getRandomTracker().getRandomPeer());
            logger.debug("New peer manages " + newPeer);
        }

        SparqlConstructOperation retrieveAllOp = 
            new SparqlConstructOperation(initializer.getSpaceURI(),
                                         "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH<" + initializer.getSpaceURI() + "> { ?s ?p ?o } . } ");
        
        @SuppressWarnings("unchecked")
        Set<Statement> resultFromPeerA = 
            SemanticHelper.asSet(
                ((DatastoreResponseOperation<ClosableIterableWrapper>)
                        ((GetOverlayKernelResponseOperation) PAFuture.getFutureValue(
                            oldPeer.receiveOperationIS(new GetOverlayKernelOperation())))
                                .getSemanticSpaceOverlayKernel().send(retrieveAllOp)).getValue().toRDF2Go());
                

        @SuppressWarnings("unchecked")
        Set<Statement> resultFromPeerB = 
            SemanticHelper.asSet(
                    ((DatastoreResponseOperation<ClosableIterableWrapper>)
                            ((GetOverlayKernelResponseOperation) PAFuture.getFutureValue(
                                newPeer.receiveOperationIS(new GetOverlayKernelOperation())))
                                    .getSemanticSpaceOverlayKernel().send(retrieveAllOp)).getValue().toRDF2Go());
                    

        // performs intersection between the previous results
        Set<Statement> newSet = new HashSet<Statement>(resultFromPeerA);
        newSet.retainAll(resultFromPeerB);

        if (logger.isDebugEnabled()) {
            logger.debug("Initial peer contains " + resultFromPeerA.size() + " data");
            logger.debug("New peer contains " + resultFromPeerB.size() + " data");
        }

        Assert.assertEquals(0, newSet.size());
        Assert.assertEquals(statementsToAdd.length, resultFromPeerA.size() + resultFromPeerB.size());
        Assert.assertTrue(resultFromPeerA.size() < statementsToAdd.length);
    }

    @AfterClass
    public static void tearDown() {
        initializer.tearDownNetwork();
    }

}
