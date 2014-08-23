package edu.stonybrook.focused.io.ascii;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

/**
 * Author: Bryan Perozzi
 *

 */
public class Clustering {

    public static void write(Writer writer, List<Set<Integer>> clustering) throws IOException {
        HashMap<Integer, Integer> key = new HashMap<Integer, Integer>();

        // assign each cluster a label
        for(int i=0; i<clustering.size(); i++){
            Set<Integer> cluster = clustering.get(i);
            for(Integer v : cluster){
                key.put(v, i+1);
            }
        }

        ArrayList<Integer> vertices = new ArrayList<Integer>(key.keySet());
        Collections.sort(vertices);

        PrintWriter printWriter = new PrintWriter(writer);

        for(Integer v : vertices){
            printWriter.append(v.toString());
            printWriter.append(" ");
            printWriter.append(key.get(v).toString());
            printWriter.append("\n");
        }

        printWriter.close();
    }
}
