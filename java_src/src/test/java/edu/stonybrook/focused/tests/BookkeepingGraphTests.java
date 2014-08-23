package edu.stonybrook.focused.tests;

import edu.stonybrook.focused.community.BookkeepingWeightedGraph;
import junit.framework.Assert;
import org.jgrapht.Graphs;
import org.junit.Test;

import static edu.stonybrook.focused.tests.CommunityTests.addTriangle;

/**
 * Author: Bryan Perozzi
 *

 */
public class BookkeepingGraphTests {

    @Test
    public void TestBasicBookkeeping1() {
        BookkeepingWeightedGraph graph = new BookkeepingWeightedGraph();

        // triangle 1
        addTriangle(graph, 0, 1.0);
        addTriangle(graph, 3, 1.0);

        // bridge
        Graphs.addEdgeWithVertices(graph, 0, 3, 1.0);

        Assert.assertEquals("Weighted volume", 14.0, graph.getWeightedVolume(), 0.1);
        Assert.assertEquals("Outdegree of 0", 3, graph.getWeightedOutDegreeOf(0), 0.1);
    }
}
