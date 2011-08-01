package fr.inria.eventcloud.pubsub;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;

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
    public void testRewriteWithQuadrupleAndBoundVariables() {
        String sparqlQuery =
                "SELECT ?s ?a1 WHERE { GRAPH ?g { ?s <http://v1> <http://v2> . ?s <http://v3> ?a1 . ?s <http://v4> ?a2 } }";

        Subscription subscription = new Subscription(source, sparqlQuery);

        Subscription rewrittenSubscription =
                SubscriptionRewriter.rewrite(subscription, defaultQuadruple);

        for (Subsubscription s : rewrittenSubscription.getSubSubscriptions()) {
            Assert.assertEquals(defaultQuadruple.getGraph(), s.getAtomicQuery()
                    .getGraph());
        }

        Assert.assertEquals(
                subscription.getId(), rewrittenSubscription.getParentId());
        Assert.assertEquals(
                2, rewrittenSubscription.getSubSubscriptions().length);
        Assert.assertEquals(source, rewrittenSubscription.getSource());

        Subsubscription[] subsubscriptions =
                rewrittenSubscription.getSubSubscriptions();

        Assert.assertEquals(defaultNode, subsubscriptions[0].getAtomicQuery()
                .getSubject());
        Assert.assertEquals(
                Node.createURI("http://v3"),
                subsubscriptions[0].getAtomicQuery().getPredicate());
        Assert.assertEquals("a1", subsubscriptions[0].getAtomicQuery()
                .getObject()
                .getName());

        Assert.assertEquals(defaultNode, subsubscriptions[1].getAtomicQuery()
                .getSubject());
        Assert.assertEquals(
                Node.createURI("http://v4"),
                subsubscriptions[1].getAtomicQuery().getPredicate());
        Assert.assertEquals("a2", subsubscriptions[1].getAtomicQuery()
                .getObject()
                .getName());
    }

    @Test
    public void testRewriteWithQuadrupleFreeVariables() {
        Node v3 = Node.createURI("http://v3");
        Node v4 = Node.createURI("http://v4");

        String sparqlQuery =
                "SELECT ?s ?o WHERE { GRAPH ?g { ?s <http://v1> <http://v2> . <http://v3> <http://v4> ?o } }";

        Subscription subscription = new Subscription(source, sparqlQuery);

        Subscription rewrittenSubscription =
                SubscriptionRewriter.rewrite(subscription, defaultQuadruple);

        for (Subsubscription s : rewrittenSubscription.getSubSubscriptions()) {
            Assert.assertEquals(defaultQuadruple.getGraph(), s.getAtomicQuery()
                    .getGraph());
        }

        Assert.assertEquals(
                subscription.getId(), rewrittenSubscription.getParentId());
        Assert.assertEquals(
                1, rewrittenSubscription.getSubSubscriptions().length);
        Assert.assertEquals(source, rewrittenSubscription.getSource());

        AtomicQuery aq =
                rewrittenSubscription.getSubSubscriptions()[0].getAtomicQuery();

        Assert.assertEquals(v3, aq.getSubject());
        Assert.assertEquals(v4, aq.getPredicate());
        Assert.assertTrue(aq.getObject().isVariable());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRewriteWithQuadrupleAndOnlyOneTriplePattern() {
        String sparqlQuery =
                "SELECT ?s ?o WHERE { GRAPH ?g { ?s <http://v1> ?o } }";

        Subscription subscription = new Subscription(source, sparqlQuery);

        SubscriptionRewriter.rewrite(subscription, defaultQuadruple);
    }

    @Test
    public void testRewriteWithBindingAndBoundVariables() {
        Node id = Node.createURI("http://www.inria.fr/member/6609");

        String sparqlQuery =
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?name ?mail WHERE { GRAPH ?g { ?id foaf:name ?name . ?id foaf:email ?email . ?id foaf:phone ?phone } }";
        Subscription subscription = new Subscription(source, sparqlQuery);

        Binding b = BindingFactory.create();
        b.add(Var.alloc("id"), id);
        b.add(Var.alloc("name"), Node.createLiteral("lpellegr"));

        Subscription rewrittenSubscription =
                SubscriptionRewriter.rewrite(subscription, b);

        Assert.assertEquals(
                2, rewrittenSubscription.getSubSubscriptions().length);

        Subsubscription[] subsubscriptions =
                rewrittenSubscription.getSubSubscriptions();

        Assert.assertEquals(id, subsubscriptions[0].getAtomicQuery()
                .getSubject());
        Assert.assertEquals(
                Node.createURI("http://xmlns.com/foaf/0.1/email"),
                subsubscriptions[0].getAtomicQuery().getPredicate());
        Assert.assertEquals("email", subsubscriptions[0].getAtomicQuery()
                .getObject()
                .getName());

        Assert.assertEquals(id, subsubscriptions[0].getAtomicQuery()
                .getSubject());
        Assert.assertEquals(
                Node.createURI("http://xmlns.com/foaf/0.1/phone"),
                subsubscriptions[1].getAtomicQuery().getPredicate());
        Assert.assertEquals("phone", subsubscriptions[1].getAtomicQuery()
                .getObject()
                .getName());
    }

    @Test
    public void testRewriteWithBindingAndFreeVariables() {
        String sparqlQuery =
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?name ?mail WHERE { GRAPH ?g { ?id1 foaf:name ?name . ?id2 foaf:email ?email } }";

        Binding b = BindingFactory.create();
        b.add(
                Var.alloc("id1"),
                Node.createURI("http://www.inria.fr/member/6609"));
        b.add(Var.alloc("name"), Node.createLiteral("lpellegr"));

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

        Assert.assertTrue(aq.getSubject().isVariable());
        Assert.assertEquals(
                Node.createURI("http://xmlns.com/foaf/0.1/email"),
                aq.getPredicate());
        Assert.assertTrue(aq.getObject().isVariable());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRewriteWithBindingAndOnlyOneTriplePattern() {
        String sparqlQuery =
                "SELECT ?s ?o WHERE { GRAPH ?g { ?s <http://v1> ?o } }";

        Subscription subscription = new Subscription(source, sparqlQuery);

        Binding b = BindingFactory.create();
        SubscriptionRewriter.rewrite(subscription, b);
    }

}
