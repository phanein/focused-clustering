/*
 * Copyright (c) 2012, Søren Atmakuri Davidsen
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.stonybrook.focused.io.graphml;

import com.google.common.collect.Lists;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.*;

/**
 * Class for exporting to GraphML. This exporter supports additional attributes of edges and
 * vertices to be exported.
 * See {@link "http://graphml.graphdrawing.org/primer/graphml-primer.html"} for
 * more information on GraphML.
 * <ol>
 * <li>Create the exporter. Default is to use "n0..n10" for node IDs, and "e0..e10" for edge IDs.</li>
 * <li>To make more meaningful IDs use {@link GraphMLExporter#edgeIDProvider(org.jgrapht.ext.EdgeNameProvider)} and {@link GraphMLExporter#vertexIDProvider}</li>
 * <li>To map GraphML supported attributes, use {@link GraphMLExporter#edgeAttributeProvider(AttributeProvider)} and {@link GraphMLExporter#vertexAttributeProvider(AttributeProvider)}</li>
 * </ol>
 *
 * @author Soren <soren@tanesha.net>
 * @see org.jgrapht.ext.GraphMLExporter
 */
public class GraphMLExporter<V extends Comparable, E> {

    public static class DummyAttributeProvider<T> implements AttributeProvider<T> {
        @Override
        public void provide(T obj, AttributeSetter setter) {
            // do nothing
        }
    }

    private static String PREFIX_NODE = "node+";
    private static String PREFIX_EDGE = "edge+";

    public static String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns";
    public static String GRAPHML_SCHEMALOCATION = "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";

    private VertexNameProvider<V> vertexIDProvider = new ContinousNumericIDProviders.ContinousNumericVertexNameProvider<V>();
    private EdgeNameProvider<E> edgeIDProvider = new ContinousNumericIDProviders.ContinousNumericEdgeNameProvider<E>();

    private AttributeProvider<V> vertexAttributeProvider = new DummyAttributeProvider<V>();
    private AttributeProvider<E> edgeAttributeProvider = new DummyAttributeProvider<E>();

    private Map<String, String> dMap = new HashMap<String, String>();

    /**
     * Specify a provider for the "id" attribute of the "node" tag. Perform mapping between the node and the ID representing
     * the node in the GraphML XML format.
     *
     * @param vertexIDProvider
     * @return
     * @see ContinousNumericIDProviders.ContinousNumericVertexNameProvider
     * @see org.jgrapht.ext.StringNameProvider
     */
    public GraphMLExporter<V, E> vertexIDProvider(VertexNameProvider<V> vertexIDProvider) {
        this.vertexIDProvider = vertexIDProvider;
        return this;
    }

    /**
     * Specify a provider for the "id" attribute of the "edge" tag. Performs mapping between edge and the ID representing the edge
     * in the GraphML XML format.
     *
     * @param edgeIDProvider
     * @return
     * @see ContinousNumericIDProviders.ContinousNumericEdgeNameProvider
     * @see EdgeNameProvider
     */
    public GraphMLExporter<V, E> edgeIDProvider(EdgeNameProvider<E> edgeIDProvider) {
        this.edgeIDProvider = edgeIDProvider;
        return this;
    }

    public GraphMLExporter<V, E> vertexAttributeProvider(AttributeProvider<V> vertexAttributeProvider) {
        this.vertexAttributeProvider = vertexAttributeProvider;
        return this;
    }

    public GraphMLExporter<V, E> edgeAttributeProvider(AttributeProvider<E> edgeAttributeProvider) {
        this.edgeAttributeProvider = edgeAttributeProvider;
        return this;
    }

    private String mapGraphMLType(Class clazz) {
        if (clazz.isAssignableFrom(Boolean.class))
            return "boolean";
        else if (clazz.isAssignableFrom(Double.class))
            return "double";
        else if (clazz.isAssignableFrom(Integer.class))
            return "int";
        else if (clazz.isAssignableFrom(Long.class))
            return "long";
        else if (clazz.isAssignableFrom(Float.class))
            return "float";
        else if (clazz.isAssignableFrom(String.class))
            return "string";
        else
            throw new RuntimeException("Unsupported attribute type: " + clazz + " (supported: Boolean, Double, Integer, Long, Float, String)");
    }

    private void declareKey(XMLStreamWriter xmlw, String id, String keyFor, String attrName, Class attrType) throws XMLStreamException {

        // declare the weight attribute
        xmlw.writeStartElement("key");
        xmlw.writeAttribute("id", id);
        xmlw.writeAttribute("for", keyFor);
        xmlw.writeAttribute("attr.name", attrName);
        xmlw.writeAttribute("attr.type", mapGraphMLType(attrType));
        xmlw.writeEndElement();
    }

    private void declareData(XMLStreamWriter xmlw, String prefix, Map<String, String> dMap, Map<String, String> values) throws XMLStreamException {

        for (Map.Entry<String, String> value : values.entrySet()) {
            xmlw.writeStartElement("data");
            xmlw.writeAttribute("key", dMap.get(prefix + value.getKey()));
            xmlw.writeCharacters(value.getValue());
            xmlw.writeEndElement();
        }

    }

    private void declareVertex(XMLStreamWriter xmlw, V v, Map<String, String> dMap, Map<String, String> values) throws XMLStreamException {

        String id = vertexIDProvider.getVertexName(v);

        xmlw.writeStartElement("node");
        xmlw.writeAttribute("id", id);
        declareData(xmlw, PREFIX_NODE, dMap, values);
        xmlw.writeEndElement();
    }

    private void declareEdge(XMLStreamWriter xmlw, Graph<V, E> graph, E e, Map<String, String> dMap, Map<String, String> values) throws XMLStreamException {

        String id = edgeIDProvider.getEdgeName(e);

        V src = graph.getEdgeSource(e);
        V dst = graph.getEdgeTarget(e);

        String srcID = vertexIDProvider.getVertexName(src);
        String dstID = vertexIDProvider.getVertexName(dst);

        xmlw.writeStartElement("edge");
        xmlw.writeAttribute("id", id);
        xmlw.writeAttribute("source", srcID);
        xmlw.writeAttribute("target", dstID);

        declareData(xmlw, PREFIX_EDGE, dMap, values);

        xmlw.writeEndElement();
    }

    /**
     * Export a graph using a writer to represent the output.
     *
     * @param w     the writer where output goes into
     * @param graph the graph to export
     */
    public void export(OutputStream w, Graph<V, E> graph) {

        // Create an output factory
        XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        // Set namespace prefix defaulting for all created writers
        // xmlof.setProperty("javax.xml.stream.isPrefixDefaulting", Boolean.TRUE);

        String edgeDefault = (graph instanceof DirectedGraph) ? "directed" : "undirected";

        // process nodes and attribute mappings
        final Map<String, Class> nodeTypeMap = new HashMap<String, Class>();
        final Map<V, Map<String, String>> nodeXml = new HashMap<V, Map<String, String>>();
        for (V v : graph.vertexSet()) {
            final Map<String, String> thisNodeXml = new HashMap<String, String>();
            this.vertexAttributeProvider.provide(v, new AttributeSetter() {
                @Override
                public <T> void set(Class<T> clazz, String key, T value) {
                    thisNodeXml.put(key, value.toString());
                    if (!nodeTypeMap.containsKey(key)) nodeTypeMap.put(key, clazz);
                }
            });
            nodeXml.put(v, thisNodeXml);
        }

        // process edges and attribute mappings
        final Map<String, Class> edgeTypeMap = new HashMap<String, Class>();
        final Map<E, Map<String, String>> edgeXml = new HashMap<E, Map<String, String>>();
        for (E e : graph.edgeSet()) {
            final Map<String, String> thisEdgeXml = new HashMap<String, String>();
            this.edgeAttributeProvider.provide(e, new AttributeSetter() {
                @Override
                public <T> void set(Class<T> clazz, String key, T value) {
                    thisEdgeXml.put(key, value.toString());
                    if (!edgeTypeMap.containsKey(key)) edgeTypeMap.put(key, clazz);
                }
            });
            edgeXml.put(e, thisEdgeXml);
        }

        try {

            // Create an XML stream writer
            XMLStreamWriter xmlwP = xmlof.createXMLStreamWriter(w, "UTF-8");
//            IndentingXMLStreamWriter xmlw = new IndentingXMLStreamWriter(xmlwP);
            XMLStreamWriter xmlw = xmlwP;

            // Write XML prologue
            xmlw.writeStartDocument("utf-8", "1.0");

            // Now start with root element
            xmlw.writeStartElement("graphml");
            xmlw.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xmlw.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xmlw.writeDefaultNamespace(GRAPHML_NS);
            xmlw.writeAttribute("xsi:schemaLocation", GRAPHML_SCHEMALOCATION);

            // d-idx
            int d = 0;
            for (String attributeName : edgeTypeMap.keySet()) {
                String key = PREFIX_EDGE + attributeName;
                if (!dMap.containsKey(key))
                    dMap.put(key, "d" + (d++));
                String id = dMap.get(key);
                declareKey(xmlw, id, "edge", attributeName, edgeTypeMap.get(attributeName));
            }

            for (String attributeName : nodeTypeMap.keySet()) {
                String key = PREFIX_NODE + attributeName;
                if (!dMap.containsKey(key))
                    dMap.put(key, "d" + (d++));
                String id = dMap.get(key);
                declareKey(xmlw, id, "node", attributeName, nodeTypeMap.get(attributeName));
            }

            // start the graph
            xmlw.writeStartElement("graph");
            xmlw.writeAttribute("id", "G");
            xmlw.writeAttribute("edgedefault", edgeDefault);

            // write vertices
            // write vertices in sorted order
            ArrayList<V> vertexList = Lists.newArrayList(graph.vertexSet());
            Collections.sort(vertexList, new Comparator<V>() {
                @Override
                public int compare(V o1, V o2) {
                    return o1.compareTo(o2);
                }
            });

            for (V v : vertexList)
                declareVertex(xmlw, v, dMap, nodeXml.get(v));

            for (E e : graph.edgeSet()) {
                declareEdge(xmlw, graph, e, dMap, edgeXml.get(e));
            }

            // Write document end. This closes all open structures
            xmlw.writeEndDocument();
            // Close the writer to flush the output
            xmlw.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Error writing: " + e.getMessage(), e);
        }

    }


}