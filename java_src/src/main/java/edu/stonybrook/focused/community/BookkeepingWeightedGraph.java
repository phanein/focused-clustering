package edu.stonybrook.focused.community;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.HashMap;

/**
 * Author: Bryan Perozzi
 *

 */
public class BookkeepingWeightedGraph extends SimpleWeightedGraph<Integer, DefaultWeightedEdge> {

    protected HashMap<Integer, Double> weightedOutDegree = new HashMap<Integer, Double>();

    double totalWeightedVolume = 0.0;

    public BookkeepingWeightedGraph() {
        super(DefaultWeightedEdge.class);
    }

    @Override
    public DefaultWeightedEdge addEdge(Integer v, Integer v1) {
        double weight = DEFAULT_EDGE_WEIGHT;
        weightedOutDegree.put(v, weightedOutDegree.get(v) + weight);
        weightedOutDegree.put(v1, weightedOutDegree.get(v1) + weight);
        totalWeightedVolume += 2 * weight;

        return super.addEdge(v, v1);
    }

    @Override
    public boolean addVertex(Integer v){
        if (!weightedOutDegree.containsKey(v)){
            weightedOutDegree.put(v, 0.0);
        }
        return super.addVertex(v);
    }

    @Override
    public boolean addEdge(Integer v, Integer v1, DefaultWeightedEdge e) {
        double weight = getEdgeWeight(e);
        weightedOutDegree.put(v, weightedOutDegree.get(v) + weight);
        weightedOutDegree.put(v1, weightedOutDegree.get(v1) + weight);
        totalWeightedVolume += 2 * weight;

        return super.addEdge(v, v1, e);
    }

    @Override
    public void setEdgeWeight(DefaultWeightedEdge e, double val) {
        double weight = getEdgeWeight(e);

        Integer v1 = getEdgeSource(e);
        Integer v2 = getEdgeTarget(e);

        if (v1 != null || v2 != null){
            weightedOutDegree.put(v1, weightedOutDegree.get(v1) - weight);
            weightedOutDegree.put(v2, weightedOutDegree.get(v2) - weight);
            totalWeightedVolume -= 2 * weight;

            weightedOutDegree.put(v1, weightedOutDegree.get(v1) + val);
            weightedOutDegree.put(v2, weightedOutDegree.get(v2) + val);
            totalWeightedVolume += 2 * val;
        }

        super.setEdgeWeight(e, val);
    }

    public double getWeightedVolume() {
        return totalWeightedVolume;
    }

    public double getWeightedOutDegreeOf(int v) {
        return weightedOutDegree.get(v);
    }
}
