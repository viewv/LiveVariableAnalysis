package top.viewv;

import org.apache.commons.cli.*;
import soot.*;
import soot.jimple.BinopExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.Expr;
import soot.jimple.Stmt;

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
                    System.out.println("Method: " + method.getName());
                    Body body = method.retrieveActiveBody();
                    for (Unit unit : body.getUnits()) {
                        Stmt stmt = (Stmt) unit;
                        System.out.println("Unit: " + stmt.toString());
                        if (stmt instanceof DefinitionStmt){
                            DefinitionStmt defStmt = (DefinitionStmt) stmt;
                            Local left = (Local) defStmt.getLeftOp();
                            Value right = defStmt.getRightOp();
                            System.out.println("Left: " + left.toString());
                            System.out.println("Right: " + right.toString());
                            if (right instanceof Expr){
                                Expr expr = (Expr) right;
                                System.out.println("Expr: " + expr.toString());
                                if (expr instanceof BinopExpr) {
                                    BinopExpr binop = (BinopExpr) expr;
                                    Value op1 = binop.getOp1();
                                    Value op2 = binop.getOp2();
                                    String symbol = binop.getSymbol();
                                    System.out.println("Op1: " + op1.toString());
                                    System.out.println("Op2: " + op2.toString());
                                }
                            }
                        }
                    }
                }
            }
//            LiveVariableAnalysis lva = new LiveVariableAnalysis(path ,className);
//            lva.analysis();
//            SQLAnalysis sqlAnalysis = new SQLAnalysis(path, className);
//            sqlAnalysis.analysis();
            PackManager.v().writeOutput(); //must place after analysis
        }
    }
}
