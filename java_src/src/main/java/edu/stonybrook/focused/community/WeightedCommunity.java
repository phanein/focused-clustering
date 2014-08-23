package edu.stonybrook.focused.community;

import com.google.common.collect.Lists;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;

/**
 * Author: Bryan Perozzi
 *

 */
public class WeightedCommunity extends HashSet<Integer> implements ICommunity {

    public final double MIN_VERTEX_VOLUME = 0.0000001;

    Graph<Integer, DefaultWeightedEdge> graph;

    public WeightedCommunity(Graph<Integer, DefaultWeightedEdge> g) {
        graph = g;

        totalEdges = graph.edgeSet().size();

        // sum up total edge weights
        // TODO (bperozzi) find a more stable way to do this?
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            totalVolume += graph.getEdgeWeight(edge);
        }
        totalVolume *= 2;
    }

    protected double conductance = 1.0;

    protected double external_edges = 0;
    protected double internal_edges_sum = 0;
    protected double internal_edges_sum_squares = 0;
    protected int internal_edges_cnt = 0;
    protected double denominator = 0;
    protected double volume = 0;

    protected double totalEdges = 0;
    protected double totalVolume = 0;

    @Override
    public boolean add(Integer i) {
        Set<DefaultWeightedEdge> neighborSet = graph.edgesOf(i);

        // maintain unweighted volume and
        // count number of external edges
        for (DefaultWeightedEdge edge : neighborSet) {

            // no easy way to get 'other' out of JGraph's undirected graph edge traversal... ugh!
            Integer src = graph.getEdgeSource(edge);
            Integer dst = graph.getEdgeTarget(edge);

            Double weight = graph.getEdgeWeight(edge);
            Integer other = src.equals(i) ? dst : src;

            if (weight > MIN_VERTEX_VOLUME) {
                if (contains(other)) {
                    external_edges -= weight;
                    internal_edges_sum += weight;
                    internal_edges_sum_squares += Math.pow(weight, 2);
                    internal_edges_cnt++;
                } else {
                    external_edges += weight;
                }
                volume += weight;
            }
        }

        // did floating point mess up the math?
        if (external_edges < 0) {
            external_edges = 0.0;
        }

        // maintain correct denominator to compute conductance with
        denominator = Math.min(volume, totalVolume - volume);

        // what if we've added the entire graph?
        if ((totalVolume - volume) < MIN_VERTEX_VOLUME) {
            denominator = 1.0;
        }

        conductance = external_edges / (double) denominator;

        return super.add(i);
    }

    @Override
    public boolean remove(Object o) {
        Integer i = (Integer) o;

        Set<DefaultWeightedEdge> neighborSet = graph.edgesOf(i);

        double weightedDegree_U = 0;
        double delta_E = 0L;

        for (DefaultWeightedEdge edge : neighborSet) {

            // no easy way to get 'other' out of JGraph's undirected graph edge traversal... ugh!
            Integer src = graph.getEdgeSource(edge);
            Integer dst = graph.getEdgeTarget(edge);
            Integer other = src.equals(i) ? dst : src;
            Double weight = graph.getEdgeWeight(edge);

            if (contains(other)) {
                delta_E -= weight;
                internal_edges_sum -= weight;
                internal_edges_sum_squares -= Math.pow(weight, 2);
                internal_edges_cnt--;
            } else {
                delta_E += weight;
            }
            weightedDegree_U += weight;
        }

        // maintain weighted volume
        if (weightedDegree_U > MIN_VERTEX_VOLUME) {
            volume -= weightedDegree_U;
        }

        // maintain correct denominator to compute conductance with
        denominator = Math.min(volume, totalVolume - volume);

        // what if we've added the entire graph?
        if ((totalVolume - volume) < MIN_VERTEX_VOLUME) {
            denominator = 1.0;
        }

        boolean retValue = super.remove(i);

        // count how many external edges this node has with the community, and remove them (opposite of add)
        if (Math.abs(delta_E) > MIN_VERTEX_VOLUME) {
            external_edges -= delta_E;
        }

        // did floating point mess up the math?
        if (external_edges < 0) {
            external_edges = 0;
        }

        conductance = external_edges / (double) denominator;

        return retValue;
    }

    public double getExternalEdges() {
        return external_edges;
    }

    @Override
    public double getConductance() {
        return conductance;
    }

    @Override
    public double getVolume() {
        return volume;
    }

    /**
     * Return the change in conductance which would occur from adding a particular vertex to the set.
     * This is linear in the degree of the vertex. ie., O(degree(toAdd))
     */
    @Override
    public double getDeltaConductance(Integer vertex, boolean add) {

        Set<DefaultWeightedEdge> neighborSet = graph.edgesOf(vertex);

        double weightedDegree_U = 0;
        double delta_E = 0L;

        for (DefaultWeightedEdge edge : neighborSet) {

            // no easy way to get 'other' out of JGraph's undirected graph edge traversal... ugh!
            Integer src = graph.getEdgeSource(edge);
            Integer dst = graph.getEdgeTarget(edge);
            Integer other = src.equals(vertex) ? dst : src;
            Double weight = graph.getEdgeWeight(edge);

            if (contains(other)) {
                delta_E -= weight;
            } else {
                delta_E += weight;
            }
            weightedDegree_U += weight;
        }

        // what if the node has 0 volume?  ie, all edges about 0.0
        double absVolume = Math.abs(weightedDegree_U);
        if (absVolume < MIN_VERTEX_VOLUME) {
            return 0.0;
        }

        if (!add) {
            delta_E = -1 * delta_E;
            weightedDegree_U = -1 * weightedDegree_U;
        }

        double new_volume = volume + weightedDegree_U;
        double new_denom = Math.min(new_volume, totalVolume - new_volume);
        double rescaled_conductance = ((denominator) / (double) (new_denom)) * conductance;
        double final_conductance = rescaled_conductance + (delta_E) / (double) (new_denom);

        // this is the change in conductance if one were to add node u
        return final_conductance - conductance;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("WeightedCommunity:\n");
        buffer.append("\t outgoing edges: " + external_edges + "\n");
        buffer.append("\t volume: " + volume + "\n");
        buffer.append("\t conductance: " + conductance + "\n");
        buffer.append("\t members: " + size() + "\n");

        ArrayList<Integer> members = Lists.newArrayList(this);
        Collections.sort(members);

        if (size() < 5000) {
            buffer.append("\t {");
            for (Integer i : members) {
                buffer.append(i + ", ");
            }
            buffer.append(" }\n");
        }


        return buffer.toString();
    }
}
