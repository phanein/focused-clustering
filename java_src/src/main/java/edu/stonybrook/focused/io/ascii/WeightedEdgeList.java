package edu.stonybrook.focused.io.ascii;

import edu.stonybrook.focused.community.BookkeepingWeightedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Author: Bryan Perozzi
 *

 */
public class WeightedEdgeList {
    public static Graph<Integer, DefaultWeightedEdge> read(Reader reader) throws IOException {

        int SELF_LOOPS = 0;

        Graph<Integer, DefaultWeightedEdge> graph = new BookkeepingWeightedGraph();

        BufferedReader in = new BufferedReader(reader);
        String line = null;
        while ((line = in.readLine()) != null) {
            String[] entries = line.split("\\s+");

            Integer src = Integer.parseInt(entries[0]);
            Integer dst = Integer.parseInt(entries[1]);

            if (src.equals(dst)) {
                SELF_LOOPS++;
                continue;
            }

            Graphs.addEdgeWithVertices(graph, src, dst, Double.parseDouble(entries[2]));
        }

        System.err.println("Loaded " + graph.edgeSet().size() + " edges.  Self loops removed: " + SELF_LOOPS);

        return graph;
    }
}
