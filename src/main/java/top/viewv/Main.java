package top.viewv;

import org.apache.commons.cli.*;
import soot.*;
import soot.toolkits.graph.CompleteUnitGraph;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
                    System.out.println("Method: " + method.getName());

                    Body body = method.retrieveActiveBody();
                    CompleteUnitGraph graph = new CompleteUnitGraph(body);

                    ConstantPropagation cp = new ConstantPropagation(graph);

                    LiveVariable lv = new LiveVariable(graph, body);

                    ControlFlowUnreachable cf = new ControlFlowUnreachable(graph);

                    UnreachableBranch unreachableBranch = new UnreachableBranch(graph, body, cp);
                    ReachMap reachMap = unreachableBranch.getReachMap();

                    DeadCodeDetection deadCodeDetection = new DeadCodeDetection(body,graph,reachMap,cp,lv,cf);
                    //DeadCodeMap deadCodeMap = deadCodeDetection.getDeadCodeMap();

                    //Set<Integer> liveLines = deadCodeDetection.getLiveCodeLines();
                    //System.out.println("Live Lines: " + liveLines);

                    HashMap<Integer, String> deadLines = deadCodeDetection.getDeadCodeLines();

                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(path + File.separator + className + ".java"));
                        String line;
                        HashMap<Integer, String> lineMap = new HashMap<>();
                        int lineNumber = 1;
                        while ((line = bufferedReader.readLine()) != null) {
                            lineMap.put(lineNumber, line);
                            lineNumber++;
                        }
                        bufferedReader.close();
                        for (int i: lineMap.keySet()) {
                            if (deadLines.containsKey(i)) {
                                System.out.println("Line " + i + " : " + lineMap.get(i).trim() + " " + deadLines.get(i));
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Source file not found!");
                    }
                }
            }
            PackManager.v().writeOutput(); //must place after analysis
        }
    }
}
