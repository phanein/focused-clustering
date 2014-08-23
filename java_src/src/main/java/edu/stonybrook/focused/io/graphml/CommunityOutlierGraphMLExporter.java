package edu.stonybrook.focused.io.graphml;

import edu.stonybrook.focused.community.ICommunity;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.HashSet;

/**
 * Author: Bryan Perozzi
 *

 */
public class CommunityOutlierGraphMLExporter extends GraphMLExporter<Integer, DefaultWeightedEdge> {

    public CommunityOutlierGraphMLExporter(final Graph<Integer, DefaultWeightedEdge> graph, final ICommunity community, final HashSet<Integer> outliers, final HashSet<Integer> inliers) {
        this.edgeAttributeProvider(new AttributeProvider<DefaultWeightedEdge>() {
            @Override
            public void provide(DefaultWeightedEdge obj, AttributeSetter setter) {
                setter.set(Double.class, "weight", graph.getEdgeWeight(obj));
            }
        });

        this.vertexAttributeProvider(new AttributeProvider<Integer>() {
            @Override
            public void provide(Integer obj, AttributeSetter setter) {

                setter.set(String.class, "Label", obj.toString());

                if (community.contains(obj)) {
                    setter.set(String.class, "Community", "Member");
                } else if(outliers.contains(obj)){
                    setter.set(String.class, "Community", "Outlier");
                }
                else if(inliers.contains(obj)){
                    setter.set(String.class, "Community", "Other Outlier");
                }
                else {
                    setter.set(String.class, "Community", "Unassigned");
                }
            }
        });

    }
}
