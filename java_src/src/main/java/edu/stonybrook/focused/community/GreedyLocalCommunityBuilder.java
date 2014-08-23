package edu.stonybrook.focused.community;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * User: hubris (Bryan Perozzi)
 *

 */
public class GreedyLocalCommunityBuilder implements LocalCommunityBuilder {

    Logger logger = Logger.getLogger(GreedyLocalCommunityBuilder.class.getName());

    public int GREEDY_STRUCTUAL_NODES_REMOVED_CNT = 0;
    public int GREEDY_FOCUSED_NODES_REMOVED_CNT = 0;

    final public int MAX_MISTEPS = 0;

    final public double MIN_DELTA = -0.000001;

    final public int MAX_ITER = 5;

    // inputs
    Graph<Integer, DefaultWeightedEdge> structuralGraph;
    Graph<Integer, DefaultWeightedEdge> focusedGraph;
    Iterable<Integer> seedSet;

    // outputs
    UnweightedCommunity structuralCommunity;
    WeightedCommunity focusedCommunity;

    // types of outliers
    HashSet<Integer> outliers = new HashSet<Integer>();
    HashSet<Integer> inliers = new HashSet<Integer>();

    public GreedyLocalCommunityBuilder(Graph structural, Graph focused, Iterable<Integer> seeds, double EDGE_VARIANCE) {
        structuralGraph = structural;
        focusedGraph = focused;
        seedSet = seeds;

        structuralCommunity = new UnweightedCommunity(structural);
        focusedCommunity = new WeightedMeansCommunity(focused, EDGE_VARIANCE);

        buildCommunities();
    }

    protected void buildCommunities() {
        HashSet<Integer> bestStructuralNodes = new HashSet<Integer>();

        // Greedy Focused UnweightedCommunity Algorithm:
        // 1. initialize community with seed set
        // 2. iterate over neighbors to expand set
        // 2.a. get candidate nodes (neighbors of community for greedy)
        // 2.b. find best structural node and best focused node to add
        // 2.c. add best nodes if they exist
        // 3. do the time warp (check to see if removing any node decrease conductance, and remove it)
        // 4. record outliers

        // 1. initialize community with seed set
        for (Integer i : seedSet) {
            structuralCommunity.add(i);
            focusedCommunity.add(i);
        }

        // 2. iterate over neighbors to expand set
        ArrayList<Integer> candidateNodes = new ArrayList<Integer>();
        Integer bestNode = null;
        Integer bestStructuralNode = null;

        // for backtracking greedy
        double minConductanceSeenSoFar = 1.0;
        ArrayList<Integer> backtrackList = new ArrayList<Integer>();

        boolean any_focused_community_change = false;

        // found a case where it infitinately adds/removes?
        int iter = 0;

        do {
            any_focused_community_change = false;

            int missteps = 0;

            do {
                bestNode = null;
                bestStructuralNode = null;
                double bestDeltaPhi = Double.POSITIVE_INFINITY;
                double bestDeltaPhi_s = Double.POSITIVE_INFINITY;

                // 2.a. get candidate nodes (neighbors of community for greedy)
                neighbors(focusedCommunity, candidateNodes);

                // 2.b. find best structural node and best focused node to add
                for (Integer n : candidateNodes) {
                    // check whether 'n' decreases conductance for either of the sets
                    double deltaPhi_s = structuralCommunity.getDeltaConductance(n, true);
                    double deltaPhi = focusedCommunity.getDeltaConductance(n, true);

                    if (deltaPhi_s < bestDeltaPhi_s) {
                        bestStructuralNode = n;
                        bestDeltaPhi_s = deltaPhi_s;
                    }

                    if (deltaPhi < bestDeltaPhi) {
                        bestNode = n;
                        bestDeltaPhi = deltaPhi;
                    }
                }

                // 2.c. add best nodes if they exist
                if (bestNode != null) {

                    // add node, if its good, or if we have backtracking steps left
                    if (bestDeltaPhi > MIN_DELTA && missteps < MAX_MISTEPS) {
                        focusedCommunity.add(bestNode);
                        structuralCommunity.add(bestNode);
                        missteps++;
                        backtrackList.add(bestNode);
                    } else if (bestDeltaPhi <= MIN_DELTA) {
                        focusedCommunity.add(bestNode);
                        structuralCommunity.add(bestNode);
                        if (missteps > 0) {
                            backtrackList.add(bestNode);
                        }
                        any_focused_community_change = true;
                    } else {
                        // go back to minimum
                        for (Integer i : backtrackList) {
                            focusedCommunity.remove(i);
                        }
                        bestNode = null;
                    }

                    // if the backtrack made things better in the long run, reset it
                    if (focusedCommunity.getConductance() < minConductanceSeenSoFar) {
                        missteps = 0;
                        minConductanceSeenSoFar = focusedCommunity.getConductance();
                        backtrackList.clear();
                    }

                }
                if (bestStructuralNode != null) {
                    bestStructuralNodes.add(bestStructuralNode);
                }

//                logger.info("best: [" + bestNode + " : " + bestDeltaPhi + " , " + bestStructuralNode + " : " + bestDeltaPhi_s + "]");
            } while (bestNode != null);

//            System.err.println("Done Adding.  Current conductance: " + focusedCommunity.getConductance() + "size: " + focusedCommunity.size());

            // 3. do the time warp (check to see if removing any node decreases conductance, and remove it)
            boolean removed = false;

            do {
                removed = false;
                bestNode = null;
                double bestDeltaPhi = 0.0;

                for (Integer n : focusedCommunity) {
                    // check whether removing n decreases conductance.  if so, do it
                    double deltaPhi = focusedCommunity.getDeltaConductance(n, false);
                    if (deltaPhi < bestDeltaPhi) {
                        bestNode = n;
                        bestDeltaPhi = deltaPhi;
                    }
                }

                if (bestNode != null) {
//                    logger.info("best: [" + bestNode + " : " + bestDeltaPhi + "]");
                    removed = true;
                    focusedCommunity.remove(bestNode);
                    any_focused_community_change = true;
                    GREEDY_FOCUSED_NODES_REMOVED_CNT++;
                }

            } while (removed);

//            System.err.println("Done Removing.  Current conductance: " + focusedCommunity.getConductance() + "size: " + focusedCommunity.size());
            iter++;

        } while (any_focused_community_change && iter < MAX_ITER);

        // 4. record outliers.
        // we define outliers to be nodes that were in the focused community, but were not in the structural community
        bestStructuralNodes.removeAll(focusedCommunity);
        outliers.addAll(bestStructuralNodes);

        // we define an inlier here as something that was added to the focused community, but then later removed
        inliers.addAll(structuralCommunity);
        inliers.removeAll(focusedCommunity);
    }

    protected void neighbors(HashSet<Integer> input, ArrayList<Integer> output) {
        output.clear();

        HashSet<Integer> added = new HashSet<Integer>();

        // TODO (bperozzi) perhaps this should sort by edge weight
        for (Integer i : input) {
            for (DefaultWeightedEdge e : structuralGraph.edgesOf(i)) {
                Integer target = Graphs.getOppositeVertex(structuralGraph, e, i);

                if (!input.contains(target) && !added.contains(target)) {
                    output.add(target);
                    added.add(target);
                }
            }
        }
    }

    @Override
    public UnweightedCommunity getStructuralCommunity() {
        return structuralCommunity;
    }

    @Override
    public WeightedCommunity getFocusedCommunity() {
        return focusedCommunity;
    }

    @Override
    public HashSet<Integer> getOutliers() {
        return outliers;
    }

    @Override
    public HashSet<Integer> getInliers() {
        return inliers;
    }
}
