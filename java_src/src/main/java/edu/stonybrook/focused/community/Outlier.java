package edu.stonybrook.focused.community;

/**
 * Author: Bryan Perozzi
 *

 */
public class Outlier {
    public int id = -1;
    public int votesOutlier = 1;
    public int votes = 1;
    public int notInCommunity = 0;

    public Outlier(int id) {
        this.id = id;
    }

    public double outlierRatio() {
        return (votesOutlier / (double) votes);
    }

    @Override
    public boolean equals(Object o) {
        Outlier other = (Outlier) o;
        if (other != null) {
            return other.id == id;
        }
        return false;
    }

    @Override
    // TODO this probably needs to be better
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "[" + id + "," + outlierRatio() + "," + notInCommunity + "]";
    }
}