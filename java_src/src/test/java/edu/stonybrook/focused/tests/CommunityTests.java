package edu.stonybrook.focused.tests;

import edu.stonybrook.focused.community.UnweightedCommunity;
import edu.stonybrook.focused.community.WeightedCommunity;
import edu.stonybrook.focused.community.WeightedMeansCommunity;
import junit.framework.Assert;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.Test;
import umontreal.iro.lecuyer.probdist.NormalDistQuick;

/**
 * Author: Bryan Perozzi
 *

 */
public class CommunityTests {

    public static Graph<Integer, DefaultWeightedEdge> getTwoTriangles(Double weight) {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        // triangle 1
        addTriangle(graph, 0, weight);
        addTriangle(graph, 3, weight);

        // bridge
        Graphs.addEdgeWithVertices(graph, 0, 3, weight);

        return graph;
    }

//    public static void addTriangle(Graph<Integer, DefaultWeightedEdge> graph, Integer startId) {
//        // triangle 2
//        Graphs.addEdgeWithVertices(graph, startId, startId + 1);
//        Graphs.addEdgeWithVertices(graph, startId + 1, startId + 2);
//        Graphs.addEdgeWithVertices(graph, startId + 2, startId);
//    }

    public static void addTriangle(Graph<Integer, DefaultWeightedEdge> graph, Integer startId, Double weight) {
        // triangle 2
        Graphs.addEdgeWithVertices(graph, startId, startId + 1, weight);
        Graphs.addEdgeWithVertices(graph, startId + 1, startId + 2, weight);
        Graphs.addEdgeWithVertices(graph, startId + 2, startId, weight);
    }

    @Test
    public void TestStructuralConductance1() {
        Graph<Integer, DefaultWeightedEdge> graph = getTwoTriangles(1.0);

        UnweightedCommunity community = new UnweightedCommunity(graph);
        community.add(0);

        Assert.assertEquals("External edges equality", 3, community.getExternalEdges());
        Assert.assertEquals("Conductance", 1.0, community.getConductance());
    }

    @Test
    public void TestStructuralConductance2() {
        Graph<Integer, DefaultWeightedEdge> graph = getTwoTriangles(1.0);

        UnweightedCommunity community = new UnweightedCommunity(graph);
        community.add(0);
        community.add(1);

        Assert.assertEquals("External edges equality", 3, community.getExternalEdges());
        Assert.assertEquals("Conductance", 3.0/5, community.getConductance());
    }

    @Test
    public void TestStructuralConductance3() {
        Graph<Integer, DefaultWeightedEdge> graph = getTwoTriangles(1.0);

        UnweightedCommunity community = new UnweightedCommunity(graph);
        community.add(0);
        community.add(1);
        community.add(2);

        Assert.assertEquals("External edges equality", 1, community.getExternalEdges());
        Assert.assertEquals("Conductance", 1.0/7, community.getConductance());
    }

    @Test
    public void TestStructuralConductance4() {
        // test when community is larger than Vol(G)/2
        Graph<Integer, DefaultWeightedEdge> graph = getTwoTriangles(1.0);

        UnweightedCommunity community = new UnweightedCommunity(graph);
        community.add(0);
        community.add(1);
        community.add(2);
        community.add(3);

        Assert.assertEquals("External edges equality", 2, community.getExternalEdges());
        // note that the conductance should not be 2/10!!
        Assert.assertEquals("Conductance", 2.0/4, community.getConductance());
    }

    @Test
    public void TestStructuralConductance5() {
        Graph<Integer, DefaultWeightedEdge> graph = getTwoTriangles(1.0);

        UnweightedCommunity community = new UnweightedCommunity(graph);
        community.add(0);
        community.add(1);
        community.add(2);
        community.remove(2);

        Assert.assertEquals("External edges equality", 3, community.getExternalEdges());
        Assert.assertEquals("Conductance", 3.0/5, community.getConductance());
    }

    @Test
    public void TestStructuralConductance6() {
        Graph<Integer, DefaultWeightedEdge> graph = getTwoTriangles(1.0);

        UnweightedCommunity community = new UnweightedCommunity(graph);
        community.add(0);
        community.add(1);
        community.add(2);
        community.remove(2);
        community.remove(1);

        Assert.assertEquals("External edges equality", 3, community.getExternalEdges());
        Assert.assertEquals("Conductance", 1.0, community.getConductance());
    }

    @Test
    public void TestStructuralConductanceDelta1() {
        // test when community is larger than Vol(G)/2
        Graph<Integer, DefaultWeightedEdge> graph = getTwoTriangles(1.0);

        UnweightedCommunity community = new UnweightedCommunity(graph);
        community.add(0);
        community.add(1);

        Double delta = community.getDeltaConductance(2, true);

        Assert.assertEquals("Conductance change by adding 2", (1.0/7) - (3.0/5), delta);
    }

    @Test
    public void TestStructuralConductanceDelta2() {
        // test when community is larger than Vol(G)/2
        Graph<Integer, DefaultWeightedEdge> graph = getTwoTriangles(1.0);

        UnweightedCommunity community = new UnweightedCommunity(graph);
        community.add(0);
        community.add(1);
        community.add(2);

        Double delta = community.getDeltaConductance(2, false);

        // removing 2 would cause this to happen
        Assert.assertEquals("Conductance change by removing 2", (3.0/5)-(1.0/7), delta);

        // no actual change should be done
        Assert.assertEquals("External edges equality", 1, community.getExternalEdges());
        Assert.assertEquals("Conductance", 1.0/7, community.getConductance());
    }

    @Test
    public void TestWeightedConductance1() {
        Graph<Integer, DefaultWeightedEdge> graph = getTwoTriangles(0.3);

        WeightedCommunity community = new WeightedCommunity(graph);
        community.add(0);

        Assert.assertEquals("External edges weight", 0.9, community.getExternalEdges(), 0.01);
        Assert.assertEquals("Conductance", 1.0, community.getConductance());
    }

    @Test
    public void TestWeightedConductance2() {
        Graph<Integer, DefaultWeightedEdge> graph = getTwoTriangles(0.3);

        WeightedCommunity community = new WeightedCommunity(graph);
        community.add(0);
        community.add(1);

        Assert.assertEquals("External edges weight", 0.9, community.getExternalEdges(), 0.01);
        Assert.assertEquals("Conductance", 0.6, community.getConductance(), 0.01);
    }

    @Test
    public void TestWeightedConductance3() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        addTriangle(graph, 0, 0.5);
        addTriangle(graph, 3, 0.1);
        Graphs.addEdgeWithVertices(graph, 0, 3, 0.1);


        WeightedCommunity community = new WeightedCommunity(graph);
        community.add(0);
        community.add(1);

        // total volume in this graph is 3.8
        // community has vol() 2.1, so denominator should be 1.7
        Assert.assertEquals("External edges weight", 1.1, community.getExternalEdges(), 0.01);
        Assert.assertEquals("Conductance", 1.1/1.7, community.getConductance(), 0.01);
    }

    @Test
    public void TestWeightedConductance4() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        addTriangle(graph, 0, 0.5);
        addTriangle(graph, 3, 0.1);
        Graphs.addEdgeWithVertices(graph, 0, 3, 0.1);


        WeightedCommunity community = new WeightedCommunity(graph);
        community.add(0);
        community.add(1);
        community.add(2);

        Assert.assertEquals("External edges weight", 0.10, community.getExternalEdges(), 0.01);
        Assert.assertEquals("Conductance", 1.0/7, community.getConductance(), 0.01);
    }

    @Test
    public void TestWeightedConductance5() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        addTriangle(graph, 0, 0.5);
        addTriangle(graph, 3, 0.1);
        Graphs.addEdgeWithVertices(graph, 0, 3, 0.1);


        WeightedCommunity community = new WeightedCommunity(graph);
        community.add(0);
        community.add(1);
        community.add(2);

        Assert.assertEquals("External edges weight", 0.10, community.getExternalEdges(), 0.01);
        Assert.assertEquals("Conductance", 1.0/7, community.getConductance(), 0.01);

        community.remove(2);

        // total volume in this graph is 3.8
        // community has vol() 2.1, so denominator should be 1.7
        Assert.assertEquals("External edges weight", 1.1, community.getExternalEdges(), 0.01);
        Assert.assertEquals("Conductance", 1.1/1.7, community.getConductance(), 0.01);
    }

    @Test
    public void TestDeltaWeightedConductance1() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        addTriangle(graph, 0, 0.5);
        addTriangle(graph, 3, 0.5);
        Graphs.addEdgeWithVertices(graph, 0, 3, 0.5);


        WeightedCommunity community = new WeightedCommunity(graph);
        community.add(0);
        community.add(1);

        Double delta = community.getDeltaConductance(2, true);
        Assert.assertEquals("Conductance change by adding 2", (1.0/7) - (3.0/5), delta);
    }

    @Test
    public void TestDeltaWeightedConductance2() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        addTriangle(graph, 0, 0.5);
        addTriangle(graph, 3, 0.1);
        Graphs.addEdgeWithVertices(graph, 0, 3, 0.1);


        WeightedCommunity community = new WeightedCommunity(graph);
        community.add(0);
        community.add(1);
        community.add(2);

        Double delta = community.getDeltaConductance(2, false);

        Assert.assertEquals("Conductance change by removing 2", (1.1/1.7)-(1.0/7), delta, 0.01);
        Assert.assertEquals("External edges weight", 0.10, community.getExternalEdges(), 0.01);
        Assert.assertEquals("Conductance", 1.0/7, community.getConductance(), 0.01);
    }

    @Test
    public void TestDeltaWeightedConductance3() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        addTriangle(graph, 0, 0.5);
        addTriangle(graph, 3, 0.0);
        Graphs.addEdgeWithVertices(graph, 0, 3, 0.0);


        WeightedCommunity community = new WeightedCommunity(graph);
        community.add(0);
        community.add(1);
        community.add(2);

        Double delta = community.getDeltaConductance(3, true);

        Assert.assertEquals("Conductance change by adding 3", 0.0, delta, 0.01);
        Assert.assertEquals("External edges weight", 0.0, community.getExternalEdges(), 0.01);
        Assert.assertEquals("Conductance", 0.0, community.getConductance(), 0.01);
    }

    @Test
    public void TestWeightedMeansCommunity1() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        addTriangle(graph, 0, 1.0);
        addTriangle(graph, 3, 0.0);
        Graphs.addEdgeWithVertices(graph, 0, 3, 0.0);

        WeightedMeansCommunity c = new WeightedMeansCommunity(graph, 0.1);

        c.add(0);
        Assert.assertEquals("p values", NormalDistQuick.cdf(0, c.CLUSTER_VARIANCE, 0.5), c.getPValue(0.5), 0.01);

        c.add(1);
        Assert.assertEquals("p values", NormalDistQuick.cdf(1.0, c.CLUSTER_VARIANCE, 0.5), c.getPValue(0.5), 0.01);

        c.add(2);
        Assert.assertEquals("p values", NormalDistQuick.cdf(1.0, c.CLUSTER_VARIANCE, 0.5), c.getPValue(0.5), 0.01);

        c.remove(2);
        Assert.assertEquals("p values", NormalDistQuick.cdf(1.0, c.CLUSTER_VARIANCE, 0.5), c.getPValue(0.5), 0.01);

        c.add(2);
        Assert.assertEquals("p values", NormalDistQuick.cdf(1.0, c.CLUSTER_VARIANCE, 0.5), c.getPValue(0.5), 0.01);

        // would this reject a 0 edge?
        Assert.assertTrue("p values", c.getPValue(0.0) < 0.05);

        // would this reject a 0.25 edge?
        Assert.assertTrue("p values", c.getPValue(0.25) < 0.05);
    }

    @Test
    public void TestDeltaWeightedMeansConductance1() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        addTriangle(graph, 0, 0.5);
        addTriangle(graph, 3, 0.5);
        Graphs.addEdgeWithVertices(graph, 0, 3, 0.5);


        WeightedMeansCommunity community = new WeightedMeansCommunity(graph, 0.1);
        community.add(0);
        community.add(1);

        Double delta = community.getDeltaConductance(2, true);
        Assert.assertEquals("Conductance change by adding 2", (1.0/7) - (3.0/5), delta);
    }

    @Test
    public void TestDeltaWeightedMeansConductance2() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        addTriangle(graph, 0, 0.5);
        addTriangle(graph, 3, 0.0);
        Graphs.addEdgeWithVertices(graph, 0, 3, 0.0);


        WeightedMeansCommunity community = new WeightedMeansCommunity(graph, 0.1);
        community.add(0);
        community.add(1);
        community.add(2);

        Double delta = community.getDeltaConductance(3, true);

        Assert.assertEquals("Conductance change by adding 3", 0.0, delta, 0.01);
        Assert.assertEquals("External edges weight", 0.0, community.getExternalEdges(), 0.01);
        Assert.assertEquals("Conductance", 0.0, community.getConductance(), 0.01);
    }

    @Test
    public void TestDeltaWeightedMeansConductance3() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        addTriangle(graph, 0, 0.5);
        addTriangle(graph, 3, 0.0);
        Graphs.addEdgeWithVertices(graph, 0, 3, 0.4);


        WeightedMeansCommunity community = new WeightedMeansCommunity(graph, 0.1);
        community.add(0);
        community.add(1);
        community.add(2);

        Assert.assertEquals("p values", NormalDistQuick.cdf(0.5, community.CLUSTER_VARIANCE, 0.4), community.getPValue(0.4), 0.01);

        Double delta = community.getDeltaConductance(3, true);

        Assert.assertEquals("Conductance change by removing 3", -1.0, delta, 0.01);
        Assert.assertEquals("External edges weight", 0.4, community.getExternalEdges(), 0.01);
        Assert.assertEquals("Conductance", 0.133, community.getConductance(), 0.01);

        community.add(3);

        delta = community.getDeltaConductance(3, false);

        Assert.assertEquals("Conductance change by removing 3", 1.0, delta, 0.01);
        Assert.assertEquals("External edges weight", 0.0, community.getExternalEdges(), 0.01);
        Assert.assertEquals("Conductance", 0.0, community.getConductance(), 0.01);
    }

    @Test
    public void TestDeltaWeightedMeansConductance4() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        addTriangle(graph, 0, 0.5);
        addTriangle(graph, 3, 0.0);
        Graphs.addEdgeWithVertices(graph, 0, 3, 0.4);


        WeightedMeansCommunity community = new WeightedMeansCommunity(graph, 0.1);
        community.add(0);
        community.add(1);
        community.add(2);


    }
}
