package top.viewv;

import org.apache.commons.cli.*;
import soot.PackManager;
import soot.SourceLocator;

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
//            LiveVariableAnalysis lva = new LiveVariableAnalysis(path ,className);
//            lva.analysis();
            SQLAnalysis sqlAnalysis = new SQLAnalysis(path, className);
            sqlAnalysis.analysis();
            PackManager.v().writeOutput(); //must place after analysis
        }
    }
}
