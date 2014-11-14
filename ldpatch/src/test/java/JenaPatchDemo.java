package org.w3.banana.ldpatch;

import org.w3.banana.*;
import org.w3.banana.jena.Jena;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import org.w3.banana.ldpatch.model.LDPatch;
import java.io.*;

/**
 * Simple Demo for using LDPatch with Jena
 **/
public class JenaPatchDemo {

    public static String baseUri = "http://example.com/timbl";

    public static Graph fromTurtle(File file) throws Exception {
        Model model = ModelFactory.createDefaultModel();
        model.read(new FileInputStream(file), baseUri, "Turtle");
        return model.getGraph();
    }

    public static void dumpTurtle(Graph graph) throws Exception {
        Model model = ModelFactory.createModelForGraph(graph);
        model.write(System.out, "Turtle");
    }

    public static void main(String[] args) throws Exception {

        Graph graph = fromTurtle(new File("ldpatch/src/test/resources/timbl.ttl"));

        FileInputStream is = new FileInputStream("ldpatch/src/test/resources/example.ldpatch");
        LDPatch<Jena> patch = JenaPatch.fromInputStream(is, baseUri);

        Graph patchedGraph = JenaPatch.apply(patch, graph);

        dumpTurtle(patchedGraph);

    }


}
