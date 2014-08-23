package edu.stonybrook.focused.community;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Author: Bryan Perozzi
 *

 */
public class PrecomputedCommunity implements ICommunity{

    List<Integer> community;
    double conductance;
    double volume;

    public PrecomputedCommunity(List<Integer> community, double conductance, double volume){
        this.community = community;
        this.conductance = conductance;
        this.volume = volume;
    }

    @Override
    public double getConductance() {
        return conductance;
    }

    @Override
    public double getVolume() {
        return volume;
    }

    @Override
    public double getDeltaConductance(Integer vertex, boolean toAdd) {
        throw new UnsupportedOperationException ();
    }

    @Override
    public int size() {
        return community.size();
    }

    @Override
    public boolean isEmpty() {
        return community.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException ();
    }

    @Override
    public Iterator<Integer> iterator() {
        return community.iterator();
    }

    @Override
    public Object[] toArray() {
        return community.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return community.toArray(a);
    }

    @Override
    public boolean add(Integer integer) {
        throw new UnsupportedOperationException ();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException ();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException ();
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        throw new UnsupportedOperationException ();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException ();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException ();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException ();
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("GenericCommunity Container:\n");
        buffer.append("\t volume: " + volume + "\n");
        buffer.append("\t conductance: " + conductance + "\n");
        buffer.append("\t members: " + size() + "\n");

        if (size() < 50) {
            buffer.append("\t {");
            for (Integer i : this) {
                buffer.append(i + ", ");
            }
            buffer.append(" }\n");
        }


        return buffer.toString();
    }
}
