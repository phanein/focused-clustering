package edu.stonybrook.focused.main;

import com.google.common.collect.Sets;
import edu.stonybrook.focused.community.BookkeepingWeightedGraph;
import edu.stonybrook.focused.community.GreedyLocalCommunityBuilder;
import edu.stonybrook.focused.community.ICommunity;
import edu.stonybrook.focused.community.LocalCommunityBuilder;
import edu.stonybrook.focused.io.graphml.CommunityOutlierGraphMLExporter;
import edu.stonybrook.focused.io.graphml.ContinousNumericIDProviders;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import umontreal.iro.lecuyer.probdist.NormalDistQuick;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Author: Bryan Perozzi
 *

 */
public class CommunityHolder {

    public boolean from_same_distribution(double testValue, double mean, double variance, double p_value) {
        return NormalDistQuick.cdf(mean, variance, testValue) > p_value;
    }

    public static UndirectedGraph<Integer, DefaultWeightedEdge> getSubgraph(Graph<Integer, DefaultWeightedEdge> graph, List<DefaultWeightedEdge> highestWeightedEdges) {
        UndirectedGraph<Integer, DefaultWeightedEdge> seedGraph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        for (DefaultWeightedEdge edge : highestWeightedEdges) {
            Graphs.addEdgeWithVertices(seedGraph, graph.getEdgeSource(edge), graph.getEdgeTarget(edge), graph.getEdgeWeight(edge));
        }

        return seedGraph;
    }

    public List<DefaultWeightedEdge> getTopEdges(final Graph<Integer, DefaultWeightedEdge> graph, double percent) {

        int top_k = (int) (graph.edgeSet().size() * (percent / 100)) + 1;

        return getTopKEdges(graph, top_k);
    }

    public List<DefaultWeightedEdge> getTopKEdges(final Graph<Integer, DefaultWeightedEdge> graph, int top_k) {

        ArrayList<DefaultWeightedEdge> edgeList = new ArrayList<DefaultWeightedEdge>(graph.edgeSet());
        Collections.sort(edgeList, new Comparator<DefaultWeightedEdge>() {
            @Override
            public int compare(DefaultWeightedEdge o1, DefaultWeightedEdge o2) {
                Double e1 = graph.getEdgeWeight(o1);
                Double e2 = graph.getEdgeWeight(o2);

                return Double.compare(e1, e2);
            }
        });

        return edgeList.subList(edgeList.size() - top_k, edgeList.size());
    }

    public List<DefaultWeightedEdge> minEdgesToKeep(final Graph<Integer, DefaultWeightedEdge> graph) {
        int top_k = (int) (graph.edgeSet().size() * (OPTIONS.PERCENT_OF_EDGES_TO_KEEP / 100)) + 1;

        System.err.println(OPTIONS.PERCENT_OF_EDGES_TO_KEEP + "% of edges: " + top_k);

        return getTopKEdges(graph, Math.min(top_k, OPTIONS.TOP_K_EDGES_TO_KEEP));
    }

    public List<DefaultWeightedEdge> getTopEdgesByDistributionFitting(final Graph<Integer, DefaultWeightedEdge> graph) {

        ArrayList<DefaultWeightedEdge> edgeList = new ArrayList<DefaultWeightedEdge>(graph.edgeSet());
        Collections.sort(edgeList, new Comparator<DefaultWeightedEdge>() {
            @Override
            public int compare(DefaultWeightedEdge o1, DefaultWeightedEdge o2) {
                Double e1 = graph.getEdgeWeight(o1);
                Double e2 = graph.getEdgeWeight(o2);

                return -1 * Double.compare(e1, e2);
            }
        });

        System.err.println("Initial edge bootstrap size: " + OPTIONS.INITIAL_SET_BOOTSTRAP_SIZE);

        double initial_set_sum = 0;
        double initial_set_mean = 0;
        int initial_set_cutoff = 0;
        for (int x = 0; x < edgeList.size(); x++) {
            double weight = graph.getEdgeWeight(edgeList.get(x));
            if (x < OPTIONS.INITIAL_SET_BOOTSTRAP_SIZE) {
                initial_set_sum += weight;
                initial_set_mean = initial_set_sum / (x + 1);
                initial_set_cutoff = x;
            } else {
                if (from_same_distribution(weight, initial_set_mean, OPTIONS.INITIAL_SET_EDGE_VARIANCE, OPTIONS.INITIAL_SET_P_VALUE)) {
                    initial_set_sum += weight;
                    initial_set_mean = initial_set_sum / (x + 1);
                    initial_set_cutoff = x;
                } else {
                    break;
                }
            }
        }

        System.err.println("Starting edge cutoff weight: " + graph.getEdgeWeight(edgeList.get(0)));
        System.err.println("Ending edge cutoff weight: " + graph.getEdgeWeight(edgeList.get(initial_set_cutoff)));

        return edgeList.subList(0, initial_set_cutoff);
    }

    private String outputFilePrefix;
    private BookkeepingWeightedGraph graph;
    private ArrayList<ICommunity> structuralCommunities;
    private ArrayList<ICommunity> focusedCommunties;
    private boolean debug_Graphml = false;
    ArrayList<LocalCommunityBuilder> builders = new ArrayList<LocalCommunityBuilder>();

    FocuscoOptions OPTIONS;

    public CommunityHolder(String outputFilePrefix, BookkeepingWeightedGraph graph, boolean debug_Graphml, FocuscoOptions myOptions) {
        this.outputFilePrefix = outputFilePrefix;
        this.graph = graph;
        this.debug_Graphml = debug_Graphml;
        this.OPTIONS = myOptions;
    }

    public ArrayList<ICommunity> getStructuralCommunities() {
        return structuralCommunities;
    }

    public ArrayList<ICommunity> getFocusedCommunties() {
        return focusedCommunties;
    }

    public ArrayList<LocalCommunityBuilder> getCommunityBuildiers() {
        return builders;
    }

    public CommunityHolder invoke() throws IOException {
        // initialize the seed sets to perform expansion around

        List<Set<Integer>> connectedSets = new ArrayList<Set<Integer>>();

        if (OPTIONS.SEED_SET_CSV.isEmpty()) {
            System.err.println("Finding candidate seed sets by distribution fitting.");

            // get top edges by fitting a distribution to the top ones and grabbing them
            List<DefaultWeightedEdge> highestWeightedEdges = getTopEdgesByDistributionFitting(graph);

            System.err.println("Top edges saved: " + highestWeightedEdges.size());

            // make a lil graph from G and these edges
            UndirectedGraph<Integer, DefaultWeightedEdge> seedGraph = getSubgraph(graph, highestWeightedEdges);

            // get connected components
            ConnectivityInspector<Integer, DefaultWeightedEdge> inspector = new ConnectivityInspector<Integer, DefaultWeightedEdge>(seedGraph);

            connectedSets = inspector.connectedSets();
        } else {
            System.err.println("Using manually entered seed set.");

            // a seed set has been manually specified on the command line
            HashSet<Integer> manual_seed_set = Sets.newHashSet();
            for (String seed_node : OPTIONS.SEED_SET_CSV.split(",")) {
                manual_seed_set.add(Integer.parseInt(seed_node));
            }

            connectedSets.add(manual_seed_set);
        }

        System.err.println("Number of connected components: " + connectedSets.size());

        // for each connected component, try to build a community
        structuralCommunities = new ArrayList<ICommunity>();
        focusedCommunties = new ArrayList<ICommunity>();

        long community_size = 0;

        for (Set<Integer> seedSet : connectedSets) {
            GreedyLocalCommunityBuilder builder = new GreedyLocalCommunityBuilder(graph, graph, seedSet, OPTIONS.INTRACLUSTER_EDGE_VARIANCE);

            System.err.println("Conductance:" + builder.getFocusedCommunity().getConductance() + "; size: " + builder.getFocusedCommunity().size());

            // XXX:  beware min conductance cutoff in dense graphs
            if (builder.getFocusedCommunity().getConductance() <= OPTIONS.MIN_CONDUCTANCE_CUTOFF) {
                if (debug_Graphml) {
                    CommunityOutlierGraphMLExporter exporter = new CommunityOutlierGraphMLExporter(graph, builder.getFocusedCommunity(), builder.getOutliers(), builder.getInliers());
                    exporter.vertexIDProvider(new ContinousNumericIDProviders.IntegerVertexNameProvider());
                    exporter.export(new BufferedOutputStream(new FileOutputStream(outputFilePrefix + "debug.focused.community-" + builders.size() + ".size-" + builder.getFocusedCommunity().size() + ".graphml")), graph);
                }

                builders.add(builder);
                structuralCommunities.add(builder.getStructuralCommunity());
                focusedCommunties.add(builder.getFocusedCommunity());
                community_size += builder.getFocusedCommunity().size();
            }
        }

        System.err.println("Number of Communities Found" + focusedCommunties.size());
        System.out.print(focusedCommunties.size() + ",");

        double average_size = ((double) community_size) / focusedCommunties.size();

        System.err.println("Average commmunity size" + average_size);
        System.out.print(average_size + ",");

        return this;
    }
}