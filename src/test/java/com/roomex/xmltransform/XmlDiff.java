package com.roomex.xmltransform;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class XmlDiff {
    private boolean nodeTypeDiff = true;
    private boolean nodeValueDiff = true;

    private List<String> nodesIgnoredNodeValues=new ArrayList<>();

    private List<String> nodesIgnoredNodes=new ArrayList<>();
    public void ignoreValueForNode(String nodeName) {
        nodesIgnoredNodeValues.add(nodeName);
    }

    public void ignoreNode(String nodeName) {
        nodesIgnoredNodes.add(nodeName);
    }
    public boolean diff(String xml1, String xml2, List<String> diffs) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc1 = db.parse(new ByteArrayInputStream(xml1.getBytes()));
        Document doc2 = db.parse(new ByteArrayInputStream(xml2.getBytes()));

        doc1.normalizeDocument();
        doc2.normalizeDocument();

        return diff(doc1, doc2, diffs);
    }

    /**
     * Diff 2 nodes and put the diffs in the list
     */
    private boolean diff(Node node1, Node node2, List<String> diffs) throws Exception {
        if (diffNodeExists(node1, node2, diffs)) {
            return true;
        }

        if (nodeTypeDiff) {
            diffNodeType(node1, node2, diffs);
        }

        if (nodeValueDiff) {
            diffNodeValue(node1, node2, diffs);
        }
        diffAttributes(node1, node2, diffs);
        diffNodes(node1, node2, diffs);
        return diffs.size() > 0;
    }

    /**
     * Diff the nodes
     */
    private boolean diffNodes(Node node1, Node node2, List<String> diffs) throws Exception {
        //Sort by Name
        Map<String, Node> children1 = new LinkedHashMap<String, Node>();
        for (Node child1 = node1.getFirstChild(); child1 != null; child1 = child1.getNextSibling()) {
            children1.put(child1.getNodeName(), child1);
        }

        //Sort by Name
        Map<String, Node> children2 = new LinkedHashMap<String, Node>();
        for (Node child2 = node2.getFirstChild(); child2 != null; child2 = child2.getNextSibling()) {
            children2.put(child2.getNodeName(), child2);
        }

        //Diff all the children1
        for (Node child1 : children1.values()) {
            Node child2 = children2.remove(child1.getNodeName());
            diff(child1, child2, diffs);
        }

        //Diff all the children2 left over
        for (Node child2 : children2.values()) {
            Node child1 = children1.get(child2.getNodeName());
            diff(child1, child2, diffs);
        }

        return diffs.size() > 0;
    }


    /**
     * Diff the nodes
     */
    private boolean diffAttributes(Node node1, Node node2, List<String> diffs) throws Exception {
        if ( node1!=null && nodesIgnoredNodes.contains(node1.getNodeName())
                || node2!=null && nodesIgnoredNodes.contains(node2.getNodeName())) {
            return false;
        }
        //Sort by Name
        NamedNodeMap nodeMap1 = node1.getAttributes();
        Map<String, Node> attributes1 = new LinkedHashMap<String, Node>();
        for (int index = 0; nodeMap1 != null && index < nodeMap1.getLength(); index++) {
            attributes1.put(nodeMap1.item(index).getNodeName(), nodeMap1.item(index));
        }

        //Sort by Name
        NamedNodeMap nodeMap2 = node2.getAttributes();
        Map<String, Node> attributes2 = new LinkedHashMap<String, Node>();
        for (int index = 0; nodeMap2 != null && index < nodeMap2.getLength(); index++) {
            attributes2.put(nodeMap2.item(index).getNodeName(), nodeMap2.item(index));

        }

        //Diff all the attributes1
        for (Node attribute1 : attributes1.values()) {
            Node attribute2 = attributes2.remove(attribute1.getNodeName());
            diff(attribute1, attribute2, diffs);
        }

        //Diff all the attributes2 left over
        for (Node attribute2 : attributes2.values()) {
            Node attribute1 = attributes1.get(attribute2.getNodeName());
            diff(attribute1, attribute2, diffs);
        }

        return diffs.size() > 0;
    }

    /**
     * Check that the nodes exist
     */
    private boolean diffNodeExists(Node node1, Node node2, List<String> diffs) throws Exception {
        if (node1!=null && nodesIgnoredNodes.contains(node1.getNodeName()) ||
                node2!=null && nodesIgnoredNodes.contains(node2.getNodeName())) {
            return false;
        }
        if (node1 == null && node2 == null) {
            diffs.add(getPath(node2) + ":node " + node1 + "!=" + node2 + "\n");
            return true;
        }

        if (node1 == null && node2 != null) {
            if (node2.getNodeValue()!=null && node2.getNodeValue().trim().length()>0) {
                diffs.add(getPath(node2) + ":node " + node1 + " !=" + node2);
            }
            return true;
        }

        if (node1 != null && node2 == null) {
            diffs.add(getPath(node1) + ":node " + node1  + "!= "+ node2);
            return true;
        }

        return false;
    }

    /**
     * Diff the Node Type
     */
    private boolean diffNodeType(Node node1, Node node2, List<String> diffs) throws Exception {
        if ( node1.getParentNode()!=null && nodesIgnoredNodes.contains(node1.getParentNode().getNodeName())
                || node2.getParentNode()!=null && nodesIgnoredNodes.contains(node2.getParentNode().getNodeName())) {
            return false;
        }
        if (node1.getNodeType() != node2.getNodeType()) {
            diffs.add(getPath(node1) + ":type " + node1.getNodeType() + "!=" + node2.getNodeType());
            return true;
        }

        return false;
    }

    /**
     * Diff the Node Value
     */
    private boolean diffNodeValue(Node node1, Node node2, List<String> diffs) throws Exception {
        if ( node1.getParentNode()!=null &&
                (nodesIgnoredNodeValues.contains(node1.getParentNode().getNodeName()) ||
                        nodesIgnoredNodes.contains(node1.getParentNode().getNodeName()))
            || node2.getParentNode()!=null &&
                (nodesIgnoredNodeValues.contains(node2.getParentNode().getNodeName()) ||
                nodesIgnoredNodes.contains(node2.getParentNode().getNodeName()))) {
            return false;
        }
        if (node1.getNodeValue() == null && node2.getNodeValue() == null) {
            return false;
        }

        if (node1.getNodeValue() == null && node2.getNodeValue() != null) {
            diffs.add("null-value "+getPath(node1) + ":type " + node1 + "!=" + node2.getNodeValue());
            return true;
        }

        if (node1.getNodeValue() != null && node2.getNodeValue() == null) {
            diffs.add("value-null "+getPath(node1) + ":type " + node1.getNodeValue() + "!=" + node2);
            return true;
        }

        if (!node1.getNodeValue().trim().equals(node2.getNodeValue().trim())) {
            diffs.add("value-value "+getPath(node1) + ":type "
                    + node1.getNodeValue() + "!=" + node2.getNodeValue());
            return true;
        }


        return false;
    }


    /**
     * Get the node path
     */
    public String getPath(Node node) {
        StringBuilder path = new StringBuilder();

        do {
            path.insert(0, node.getNodeName());
            path.insert(0, "/");
        }
        while ((node = node.getParentNode()) != null);

        return path.toString();
    }
}
