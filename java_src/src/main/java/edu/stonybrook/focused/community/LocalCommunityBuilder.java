package edu.stonybrook.focused.community;

import java.util.HashSet;

/**
 * User: hubris
 *

 */
public interface LocalCommunityBuilder {

    public UnweightedCommunity getStructuralCommunity();

    public WeightedCommunity getFocusedCommunity();

    public HashSet<Integer> getOutliers();

    public HashSet<Integer> getInliers();
}
