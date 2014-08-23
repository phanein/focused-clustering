package edu.stonybrook.focused.community;

import java.util.Set;

/**
 * Author: Bryan Perozzi
 *

 */
public interface ICommunity extends Set<Integer> {
    double getConductance();

    double getVolume();

    double getDeltaConductance(Integer vertex, boolean toAdd);
}
