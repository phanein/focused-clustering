package edu.stonybrook.focused.main;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Bryan Perozzi
 *

 */
public class FocuscoOptions {

    /*
    Program level options
     */
    @Option(name = "-output_postfix")
    public String outputPostfix = "";

    @Option(name = "-output_directory", usage = "directory path to output result files in")
    public String outputDirectory = new File("").toString();

    @Option(name = "-input", required = true, usage = "weighted input edge list")
    public String inputGraph = "task.edges";

    @Option(name = "-emit_graphml")
    public Boolean debug_graphml = false;

    /*
        FocusCO algorithm options.
     */

    @Option(name = "-seed_set", usage = "comma separeted list of nodeids to include in a manually entered seed set")
    public String SEED_SET_CSV = "";

    public double PERCENT_OF_EDGES_TO_KEEP = 5.0;

    public double MIN_CONDUCTANCE_CUTOFF = 1.0;

    public int TOP_K_EDGES_TO_KEEP = 1000;

    @Option(name = "-intra_cluster_variance", usage = "focused cluster variance")
    public double INTRACLUSTER_EDGE_VARIANCE = 0.1;

    @Option(name = "-initial_variance", usage = "initial set variance")
    public double INITIAL_SET_EDGE_VARIANCE = 0.001;

    @Option(name = "-initial_bootstrap_size", usage = "initial set bootstrap size (in # edges)")
    public int INITIAL_SET_BOOTSTRAP_SIZE = 5;

    @Option(name = "-initial_p_val", usage = "initial set p-value")
    public double INITIAL_SET_P_VALUE = 0.05;


    // receives other command line parameters
    @Argument
    public List<String> arguments = new ArrayList<String>();
}
