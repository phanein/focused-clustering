package edu.stonybrook.focused.tests;

import edu.stonybrook.focused.io.ascii.WeightedEdgeList;
import junit.framework.Assert;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

/**
 * Author: Bryan Perozzi
 *

 */
public class GraphIOTests {
    @Test
    public void TestEdgelistReader() throws IOException {
        StringReader reader = new StringReader("1 2 0.5\n1 3 0.1\n");

        Graph<Integer, DefaultWeightedEdge> graph = WeightedEdgeList.read(reader);

        Assert.assertEquals(2, graph.edgeSet().size());
    }
}
