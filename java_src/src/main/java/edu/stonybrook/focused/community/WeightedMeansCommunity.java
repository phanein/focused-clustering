package edu.stonybrook.focused.community;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import umontreal.iro.lecuyer.probdist.NormalDistQuick;

import java.util.Set;

/**
 * Author: Bryan Perozzi
 *

 */
public class WeightedMeansCommunity extends WeightedCommunity {

    public final double P_VALUE = 0.05;
    public final double CLUSTER_VARIANCE;

    public final double MAP_BETA = 5;

    public WeightedMeansCommunity(Graph<Integer, DefaultWeightedEdge> g, double variance) {
        super(g);
        CLUSTER_VARIANCE = variance;
    }

    public double getPValue(double x) {
        if (internal_edges_cnt == 0) {
            return 1.0;
        }

        double mean = internal_edges_sum / internal_edges_cnt;

//        double varianceMLE = (internal_edges_sum_squares - (Math.pow(internal_edges_sum, 2)/internal_edges_cnt))/internal_edges_cnt;
//        double varianceMAP = CLUSTER_VARIANCE / (1 + internal_edges_cnt)  + (internal_edges_cnt * varianceMLE) / (internal_edges_cnt + 1);
//        return NormalDistQuick.cdf(mean, varianceMAP, x);

        return NormalDistQuick.cdf(mean, CLUSTER_VARIANCE, x);
    }

//    /**
//     * We want this to return the weighted means conductance here, so the sorting will perhaps be more meaningful?
//     *
//     * @return
//     */
//    @Override
//    public double getConductance() {
//
//        double internal = 0;
//        double external = 0;
//
//        if (internal_edges_cnt > 0) {
//            for (Integer vertex : this) {
//                Set<DefaultWeightedEdge> neighborSet = graph.edgesOf(vertex);
//
//                for (DefaultWeightedEdge edge : neighborSet) {
//                    // no easy way to get 'other' out of JGraph's undirected graph edge traversal... ugh!
//                    Integer src = graph.getEdgeSource(edge);
//                    Integer dst = graph.getEdgeTarget(edge);
//                    Integer other = src.equals(vertex) ? dst : src;
//                    Double weight = graph.getEdgeWeight(edge);
//
//                    if (contains(other)) {
//                        // does this edge fit in the distribution?  (p < 0.05)
//                        // if not, don't give credit for including it
//                        if (getPValue(weight) > P_VALUE) {
//                            internal += weight;
//                        }
//                    } else {
//                        if (getPValue(weight) > P_VALUE) {
//                            external += weight;
//                        }
//                    }
//                }
//            }
//
////            internal = Math.min(internal, totalVolume - internal);
//
//            if (internal < MIN_VERTEX_VOLUME)
//                return 0.0;
//
//            return external / internal;
//        }
//
//        return 0;
//    }

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
                // does this edge fit in the distribution?  (p < 0.05)
                // if not, don't give credit for including it
                if (getPValue(weight) > P_VALUE) {
                    delta_E -= weight;
                }
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

        // what if we've added the entire graph?
        if ((totalVolume - new_volume) < MIN_VERTEX_VOLUME) {
            new_denom = 1.0;
        }

        double rescaled_conductance = ((denominator) / (double) (new_denom)) * conductance;
        double final_conductance = rescaled_conductance + (delta_E) / (double) (new_denom);

        // this is the change in conductance if one were to add node u
        return final_conductance - conductance;
    }
}
