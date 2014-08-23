package edu.stonybrook.focused.io.ascii;

import edu.stonybrook.focused.community.Outlier;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

/**
 * Author: Bryan Perozzi
 *

 */
public class Outliers {
    public static void write(Writer writer, List<Outlier> clustering) throws IOException {

        PrintWriter printWriter = new PrintWriter(writer);

        int k = 1;
        for (Outlier o : clustering) {
            printWriter.append(Integer.toString(k));
            printWriter.append(" ");
            printWriter.append(Integer.toString(o.id));
            printWriter.append("\n");
            k++;
        }

        printWriter.close();
    }
}
