package edu.stonybrook.focused.community;

import com.google.common.collect.Lists;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Bryan Perozzi
 *

 */
public class UnweightedCommunity extends HashSet<Integer> implements ICommunity {

    Graph<Integer, DefaultWeightedEdge> graph;

    public UnweightedCommunity(Graph<Integer, DefaultWeightedEdge> g) {
        graph = g;

        totalEdges = graph.edgeSet().size();
        totalVolume = 2 * totalEdges;
    }

    protected double conductance = 1.0;

    protected long external_edges = 0;
    protected long denominator = 0;
    protected long volume = 0;

    protected long totalEdges = 0;
    protected long totalVolume = 0;

    public long countExternalEdges(Integer i, Set<DefaultWeightedEdge> neighborSet) {

        int numberExternalEdges = 0;

        // count number of external edges
        for (DefaultWeightedEdge edge : neighborSet) {

            // no easy way to get 'other' out of JGraph's undirected graph edge traversal... ugh!
            Integer src = graph.getEdgeSource(edge);
            Integer dst = graph.getEdgeTarget(edge);
            Integer other = src.equals(i) ? dst : src;

            if (contains(other)) {
                numberExternalEdges -= 1;
            } else {
                numberExternalEdges += 1;
            }
        }

        return numberExternalEdges;
    }

    @Override
    public boolean add(Integer i) {
        // maintain unweighted volume
        Set<DefaultWeightedEdge> neighborSet = graph.edgesOf(i);
        volume += neighborSet.size();

        // maintain correct denominator to compute conductance with
        denominator = Math.min(volume, totalVolume - volume);

        // count how many external edges this node has with the community
        external_edges += countExternalEdges(i, neighborSet);

        conductance = external_edges / (double) denominator;

        return super.add(i);
    }

    @Override
    public boolean remove(Object o) {
        Integer i = (Integer) o;
        // maintain unweighted volume
        Set<DefaultWeightedEdge> neighborSet = graph.edgesOf(i);
        volume -= neighborSet.size();

        // maintain correct denominator to compute conductance with
        denominator = Math.min(volume, totalVolume - volume);

        boolean retValue = super.remove(i);

        // count how many external edges this node has with the community, and remove them (opposite of add)
        external_edges -= countExternalEdges(i, neighborSet);

        conductance = external_edges / (double) denominator;

        return retValue;
    }

    public long getExternalEdges() {
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

        int degree_U = neighborSet.size();
        long delta_E = 0;

        delta_E = countExternalEdges(vertex, neighborSet);

        if (!add) {
            delta_E = -1 * delta_E;
            degree_U = -1 * degree_U;
        }

        long new_volume = volume + degree_U;
        long new_denom = Math.min(new_volume, totalVolume - new_volume);
        double rescaled_conductance = ((denominator) / (double) (new_denom)) * conductance;
        double final_conductance = rescaled_conductance + (delta_E) / (double) (new_denom);

        // this is the change in conductance if one were to add node u
        return final_conductance - conductance;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("UnweightedCommunity:\n");
        buffer.append("\t outgoing edges: " + external_edges + "\n");
        buffer.append("\t volume: " + volume + "\n");
        buffer.append("\t conductance: " + conductance + "\n");
        buffer.append("\t members: " + size() + "\n");

        ArrayList<Integer> members = Lists.newArrayList(this);
        Collections.sort(members);

        if (size() < 50) {
            buffer.append("\t {");
            for (Integer i : members) {
                buffer.append(i + ", ");
            }
            buffer.append(" }\n");
        }

        return buffer.toString();
    }
}
