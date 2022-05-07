package top.viewv;

import org.apache.commons.cli.*;
import soot.*;
import soot.toolkits.graph.CompleteUnitGraph;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        List<String> argList = new ArrayList<>();
        try {
            CommandLine cmd = parser.parse(options, args);
            argList = cmd.getArgList();
        } catch (ParseException e) {
            System.out.println("Error options");
        }

        if (argList.size() == 0) {
            System.out.println("Please input the file path");
            return;
        }

        String path = argList.get(0);

        for (String className: SourceLocator.v().getClassesUnder(path)) {
            System.out.println("Class: " + className);
            SootSetup.initSoot(path, className);
            SootClass sc = Scene.v().getSootClass(className);
            for (SootMethod method : sc.getMethods()) {
                if (!"<init>".equals(method.getName())) {
                    Body body = method.retrieveActiveBody();
                    CompleteUnitGraph graph = new CompleteUnitGraph(body);

                    ConstantPropagation cp = new ConstantPropagation(graph);

                    LiveVariable lv = new LiveVariable(graph, body);

                    ControlFlowUnreachable cf = new ControlFlowUnreachable(graph);

                    UnreachableBranch unreachableBranch = new UnreachableBranch(graph, body, cp);
                    ReachMap reachMap = unreachableBranch.getReachMap();

                    DeadCodeDetection deadCodeDetection = new DeadCodeDetection(body,graph,reachMap,cp,lv,cf);
                    DeadCodeMap deadCodeMap = deadCodeDetection.getDeadCodeMap();

                    for (Unit unit : graph) {
                        if (!deadCodeMap.get(unit)) {
                            System.out.println(unit);
                        }
                    }
                }
            }
            PackManager.v().writeOutput(); //must place after analysis
        }
    }
}
