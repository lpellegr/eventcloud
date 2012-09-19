package fr.inria.eventcloud.datastore.stats;

import org.apfloat.Apfloat;

import com.hp.hpl.jena.graph.Node;

public class SimpleStatsRecorder extends StatsRecorder {

    @Override
    protected void quadrupleAddedComputeStats(Node g, Node s, Node p, Node o) {
    }

    @Override
    protected void quadrupleRemovedComputeStats(Node g, Node s, Node p, Node o) {
    }

    @Override
    public Apfloat computeGraphEstimation() {
        return null;
    }

    @Override
    public Apfloat computeSubjectEstimation() {
        return null;
    }

    @Override
    public Apfloat computePredicateEstimation() {
        return null;
    }

    @Override
    public Apfloat computeObjectEstimation() {
        return null;
    }

}
