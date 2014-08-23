package edu.stonybrook.focused.main;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.stonybrook.focused.community.*;
import edu.stonybrook.focused.io.ascii.Clustering;
import edu.stonybrook.focused.io.ascii.Outliers;
import edu.stonybrook.focused.io.ascii.WeightedEdgeList;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.*;

/**
 * Author: Bryan Perozzi
 *

 */
public class CommunityClusterer {

    public final static double OUTLIER_DETECTION_THRESHOLD = 1.0;

    public static double OVERLAP_THRESHOLD = 0.5;

    public static List<Set<Integer>> GreedyCommunityMerge(List<ICommunity> communities, Graph<Integer, DefaultWeightedEdge> graph, ArrayList<Outlier> outliers) {
        HashSet<Integer> used = new HashSet<Integer>();
        ArrayList<Set<Integer>> ret = new ArrayList<Set<Integer>>();
        int CONFLICTING_VERTEX_CNT = 0;

        ArrayList<ICommunity> sortedCommunities = new ArrayList<ICommunity>(communities);
        Collections.sort(sortedCommunities, new Comparator<ICommunity>() {
            @Override
            public int compare(ICommunity o1, ICommunity o2) {
                return Double.compare(o1.getConductance(), o2.getConductance());
            }
        });

        // add outliers
        HashSet<Integer> outlierSet = new HashSet<Integer>();
        for (Outlier i : outliers) {
            outlierSet.add(i.id);
            used.add(i.id);
        }
        ret.add(outlierSet);

        // now sorted from lowest to highest conductance
        for (ICommunity c : sortedCommunities) {
            HashSet<Integer> temp = new HashSet<Integer>();
            for (Integer i : c) {
                if (!used.contains(i)) {
                    temp.add(i);
                    used.add(i);
                } else {
                    CONFLICTING_VERTEX_CNT++;
                }
            }
            if (temp.size() > 0) {
                ret.add(temp);
            }
        }

        HashSet<Integer> unusedNodes = new HashSet<Integer>(graph.vertexSet());
        unusedNodes.removeAll(used);

        for (Integer i : unusedNodes) {
            HashSet<Integer> temp = new HashSet<Integer>();
            temp.add(i);
            ret.add(temp);
        }

        System.err.println("Conflicting vertex cnt: " + CONFLICTING_VERTEX_CNT);
        System.err.println("Orphan vertex cnt: " + unusedNodes.size());

        return ret;
    }

    public static List<ICommunity> RemoveOverlappingCommunity(List<ICommunity> communities) {
        ArrayList<ICommunity> sortedCommunities = new ArrayList<ICommunity>(communities);
        Collections.sort(sortedCommunities, new Comparator<ICommunity>() {
            @Override
            public int compare(ICommunity o1, ICommunity o2) {
                return Double.compare(o1.getConductance(), o2.getConductance());
            }
        });

        HashSet<Integer> toRemove = new HashSet<Integer>();
        HashSet<Integer> toKeep = new HashSet<Integer>();
        for (int i = 0; i < sortedCommunities.size(); i++) {
            if (!toRemove.contains(i)) {
                for (int j = i + 1; j < sortedCommunities.size(); j++) {
                    HashSet x = Sets.newHashSet(sortedCommunities.get(i));
                    x.retainAll(sortedCommunities.get(j));

                    if ((x.size() / (sortedCommunities.get(i).size() + sortedCommunities.get(j).size() - x.size())) > OVERLAP_THRESHOLD) {
                        toRemove.add(j);
                    }
                }
                toKeep.add(i);
            }
        }

        ArrayList<ICommunity> toReturn = new ArrayList<ICommunity>();
        for (Integer x : toKeep) {
            toReturn.add(sortedCommunities.get(x));
        }

        return toReturn;
    }

//    public static void PrintOverlappingCommunities(List<ICommunity> communities) {
//        ArrayList<ICommunity> sortedCommunities = new ArrayList<ICommunity>(communities);
//        Collections.sort(sortedCommunities, new Comparator<ICommunity>() {
//            @Override
//            public int compare(ICommunity o1, ICommunity o2) {
//                return Double.compare(o1.getConductance(), o2.getConductance());
//            }
//        });
//
//        System.err.print("Scanning for overlapping communities....");
//
//        HashSet<Integer> toRemove = new HashSet<Integer>();
//        for (int i = 0; i < sortedCommunities.size(); i++) {
//            if (!toRemove.contains(i)) {
//                for (int j = i + 1; j < sortedCommunities.size(); j++) {
//                    HashSet x = Sets.newHashSet(sortedCommunities.get(i));
//                    x.retainAll(sortedCommunities.get(j));
//
//                    if (x.size() > 0) {
//                        System.out.println("Community " + i + " overlaps with community " + j);
//                        System.out.println(sortedCommunities.get(i));
//                        System.out.println(sortedCommunities.get(j));
//                    }
//                }
//            }
//        }
//    }


    public static String[] getDirectories(String path) {
        File file = new File(path);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        });
        return directories;
    }

    public void processOneFile(String input_file, String output_dir, boolean treatNonCommunityAsOutlier, String outputPostfix, boolean debug_Grapml) throws Exception {

        System.err.println("Performing task: " + input_file);

        BookkeepingWeightedGraph graph = (BookkeepingWeightedGraph) WeightedEdgeList.read(new FileReader(new File(input_file)));

        Date startTime = new Date();

        String outputFilePrefix = new File(output_dir, "task.").toString();
        CommunityHolder communityHolder = new CommunityHolder(outputFilePrefix, graph, debug_Grapml, OPTIONS).invoke();

        HashMap<Integer, Outlier> outlierHashMap = new HashMap<Integer, Outlier>();
        HashSet<Integer> verticesInCommunities = new HashSet<Integer>();
        for (LocalCommunityBuilder builder : communityHolder.getCommunityBuildiers()) {
            for (Integer i : builder.getOutliers()) {
                if (!outlierHashMap.containsKey(i)) {
                    outlierHashMap.put(i, new Outlier(i));
                }
                outlierHashMap.get(i).votesOutlier++;
            }
        }

        for (LocalCommunityBuilder builder : communityHolder.getCommunityBuildiers()) {
            for (Integer i : builder.getFocusedCommunity()) {
                if (outlierHashMap.containsKey(i)) {
                    outlierHashMap.get(i).votes++;
                }
            }
        }

        System.err.println("Focused communities found: " + communityHolder.getFocusedCommunties().size());

        // print communities that overlap
//            PrintOverlappingCommunities(communityHolder.getFocusedCommunties());


        // remove communities that overlap too much
        List<ICommunity> communities = RemoveOverlappingCommunity(communityHolder.getFocusedCommunties());
        System.err.println("Non-overlapping focused communities found: " + communities.size());

        for (ICommunity community : communities) {
//                System.err.println("Focused community found:\n" + community);
            verticesInCommunities.addAll(community);
        }

        // did any vertices not make it into communities?
        if (treatNonCommunityAsOutlier) {
            for (Integer i : graph.vertexSet()) {
                if (!verticesInCommunities.contains(i)) {
                    if (!outlierHashMap.containsKey(i)) {
                        outlierHashMap.put(i, new Outlier(i));
                        outlierHashMap.get(i).votesOutlier++;
                    }
                    outlierHashMap.get(i).notInCommunity++;
                }
            }
        } else {
            UnweightedCommunity catchAllCommunity = new UnweightedCommunity(graph);
            for (Integer i : graph.vertexSet()) {
                if (!verticesInCommunities.contains(i)) {
                    if (outlierHashMap.containsKey(i)) {
                        outlierHashMap.get(i).notInCommunity++;
                    }
                    catchAllCommunity.add(i);
                }
            }
            communities.add(catchAllCommunity);
        }


        ArrayList<Outlier> outliers = Lists.newArrayList(outlierHashMap.values());
        Collections.sort(outliers, new Comparator<Outlier>() {
            @Override
            public int compare(Outlier o1, Outlier o2) {
                return -1 * Double.compare(o1.outlierRatio(), o2.outlierRatio());
            }
        });
        System.err.println("Outliers: " + outliers);

        // remove outliers from communities if their outlier RATIO is greater than OUTLIER_DETECTION_THRESHOLD
        for (ICommunity community : communities) {
            ArrayList<Integer> toRemove = new ArrayList<Integer>();
            for (Integer i : community) {
                if (outlierHashMap.containsKey(i)) {
                    if (outlierHashMap.get(i).outlierRatio() >= OUTLIER_DETECTION_THRESHOLD) {
                        toRemove.add(i);
                    }
                }
            }
            for (Integer i : toRemove) {
                community.remove(i);
            }
        }

        // remove outliers that don't meet the threshold
        ArrayList<Outlier> toKeep = new ArrayList<Outlier>();
        for (Outlier i : outliers) {
            if (i.outlierRatio() >= OUTLIER_DETECTION_THRESHOLD) {
                toKeep.add(i);
            }
        }
        outliers = toKeep;

        List<Set<Integer>> clustering = GreedyCommunityMerge(communities, graph, outliers);

        Date stopTime = new Date();
        System.err.println("time:" + (stopTime.getTime() - startTime.getTime()));
        System.out.println((stopTime.getTime() - startTime.getTime()));

        System.err.println("Clustering: " + clustering);
        Clustering.write(new FileWriter(outputFilePrefix + "guess.clustering" + outputPostfix), clustering);
        Outliers.write(new FileWriter(outputFilePrefix + "guess.outliers" + outputPostfix), outliers);
    }

    // Container to hold all options
    FocuscoOptions OPTIONS = new FocuscoOptions();

    public static void main(String[] args) throws Exception {
        new CommunityClusterer().doMain(args);
    }

    public void doMain(String[] args) throws Exception {


        CmdLineParser parser = new CmdLineParser(OPTIONS);

        parser.setUsageWidth(80);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java CommunityClusterer [options...]");

            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();
            return;
        }

        processOneFile(OPTIONS.inputGraph, OPTIONS.outputDirectory, false, OPTIONS.outputPostfix, OPTIONS.debug_graphml);
    }
}
