package fr.inria.eventcloud.pubsub;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.reasoner.AtomicQuery;

/**
 * Test cases associated to {@link SubscriptionRewriter}
 * 
 * @author lpellegr
 */
public class SubscriptionRewriterTest {

    private static final String source =
            "rmi://oops.inria.fr:1099/32bf1d5d-131240b729f--7f6e--6f9e1f1e514bea7c-32bf1d5d-131240b729f--8000";

    private static final Node defaultNode =
            Node.createURI("http://www.inria.fr");

    private static final Quadruple defaultQuadruple = new Quadruple(
            defaultNode, defaultNode, defaultNode, defaultNode);

    @Test
    public void testRewriteWithBoundVariables() {
        String sparqlQuery =
                "SELECT ?s ?o WHERE { GRAPH ?g { ?s <http://v1> <http://v2> . ?s <http://v3> ?o } }";

        Subscription subscription = new Subscription(source, sparqlQuery);

        Subscription rewrittenSubscription =
                SubscriptionRewriter.rewrite(subscription, defaultQuadruple);

        Assert.assertEquals(
                subscription.getId(), rewrittenSubscription.getParentId());
        Assert.assertEquals(
                1, rewrittenSubscription.getSubSubscriptions().length);
        Assert.assertEquals(source, rewrittenSubscription.getSource());

        AtomicQuery aq =
                rewrittenSubscription.getSubSubscriptions()[0].getAtomicQuery();

        Assert.assertEquals(defaultNode, aq.getSubject());
        Assert.assertEquals(Node.createURI("http://v3"), aq.getPredicate());
        Assert.assertEquals(Node.ANY, aq.getObject());
    }

    @Test
    public void testRewriteWithFreeVariables() {
        String sparqlQuery =
                "SELECT ?s ?o WHERE { GRAPH ?g { ?s <http://v1> <http://v2> . <http://v3> <http://v4> ?o } }";

        Subscription subscription = new Subscription(source, sparqlQuery);

        Subscription rewrittenSubscription =
                SubscriptionRewriter.rewrite(subscription, defaultQuadruple);

        Assert.assertEquals(
                subscription.getId(), rewrittenSubscription.getParentId());
        Assert.assertEquals(
                1, rewrittenSubscription.getSubSubscriptions().length);
        Assert.assertEquals(source, rewrittenSubscription.getSource());

        AtomicQuery aq =
                rewrittenSubscription.getSubSubscriptions()[0].getAtomicQuery();

        Assert.assertEquals(Node.createURI("http://v3"), aq.getSubject());
        Assert.assertEquals(Node.createURI("http://v4"), aq.getPredicate());
        Assert.assertEquals(Node.ANY, aq.getObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRewriteWithOnlyOneTriplePattern() {
        String sparqlQuery =
                "SELECT ?s ?o WHERE { GRAPH ?g { ?s <http://v1> ?o } }";

        Subscription subscription = new Subscription(source, sparqlQuery);

        SubscriptionRewriter.rewrite(subscription, defaultQuadruple);
    }

}
