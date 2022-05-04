package top.viewv;

import soot.*;
import soot.jimple.Stmt;
import soot.jimple.internal.JIfStmt;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.util.Chain;

import java.io.*;
import java.util.*;

public class LiveVariableAnalysis {

    public String sourceDirectory;
    public String clsName;

    public LiveVariableAnalysis(String sourceDirectory, String clsName) {
        this.sourceDirectory = sourceDirectory;
        this.clsName = clsName;
        SootSetup.initSoot(sourceDirectory, clsName);
    }

    public void analysis() {
        SootClass sc = Scene.v().getSootClass(clsName);
        for (SootMethod method : sc.getMethods()) {
            if (!"<init>".equals(method.getName())) {
                System.out.println("Method: " + method.getName());

                Body body = method.retrieveActiveBody();
                Chain<Local> localChain = body.getLocals();
                List<Local> locals = new ArrayList<>();
                for (Local local : localChain) {
                    locals.add(local);
                }


                System.out.println("All locals: " + locals);

//                ClassicCompleteUnitGraph graph = new ClassicCompleteUnitGraph(body);
                CompleteUnitGraph graph = new CompleteUnitGraph(body);

                System.out.println("==================");

                // Calculate def-locals for each unit
                HashMap<Unit, Set<Local>> unitDefLocalsMap = new HashMap<>();
                for (Unit unit : body.getUnits()) {
                    Set<Local> defLocals = new HashSet<>();
                    for (ValueBox valueBox : unit.getDefBoxes()) {
                        if (valueBox.getValue() instanceof Local) {
                            defLocals.add((Local) valueBox.getValue());
                        }
                    }
                    unitDefLocalsMap.put(unit, defLocals);
                }

                // Calculate use-locals for each unit
                HashMap<Unit, Set<Local>> unitUseLocalsMap = new HashMap<>();
                for (Unit unit : body.getUnits()) {
                    Set<Local> useLocals = new HashSet<>();
                    for (ValueBox valueBox : unit.getUseBoxes()) {
                        if (valueBox.getValue() instanceof Local) {
                            useLocals.add((Local) valueBox.getValue());
                        }
                    }
                    unitUseLocalsMap.put(unit, useLocals);
                }

                HashMap<Unit, List<Unit>> unitPredUnitsMap = new HashMap<>();
                HashMap<Unit, List<Unit>> unitSuccUnitsMap = new HashMap<>();
                //HashMap<Unit, Integer> unitPathIndexMap = new HashMap<>();

                for (Unit unit : body.getUnits()) {
                    List<Unit> predUnits = graph.getPredsOf(unit);
                    List<Unit> succUnits = graph.getSuccsOf(unit);
                    unitPredUnitsMap.put(unit, predUnits);
                    unitSuccUnitsMap.put(unit, succUnits);
                    //unitPathIndexMap.put(unit, 0);
                }

                Iterator<Unit> unitIterator = graph.iterator();
                List<Unit> unitPath = new ArrayList<>();
                while (unitIterator.hasNext()) {
                    Unit unit = unitIterator.next();
                    unitPath.add(unit);
                }
                Collections.reverse(unitPath); // from tail to head

                HashMap<Unit, Set<Local>> unitInLocals = initUnitLocals(unitPath);
                HashMap<Unit, Set<Local>> unitOutLocals = initUnitLocals(unitPath);

                HashMap<Unit, Set<Local>> tempunitInLocals = initUnitLocals(unitPath);
                HashMap<Unit, Set<Local>> tempunitOutLocals = initUnitLocals(unitPath);

                while (true) {
                    for (Unit unit : unitPath) {
                        Set<Local> useLocals = new HashSet<>(unitUseLocalsMap.get(unit));
                        Set<Local> defLocals = new HashSet<>(unitDefLocalsMap.get(unit));

                        // Calculate out-locals
                        Set<Local> outLocals = new HashSet<>();
                        for (Unit predUnit : unitSuccUnitsMap.get(unit)) {
                            outLocals.addAll(tempunitInLocals.get(predUnit));
                        }
                        tempunitOutLocals.put(unit, new HashSet<>(outLocals));

                        // Calculate in-locals
                        outLocals.removeAll(defLocals);
                        useLocals.addAll(outLocals);
                        Set<Local> inLocals = new HashSet<>(useLocals);
                        tempunitInLocals.put(unit, new HashSet<>(inLocals));
                    }
                    if (!isChanged(unitInLocals, tempunitInLocals) && !isChanged(unitOutLocals, tempunitOutLocals)) {
                        break;
                    } else {
                        unitInLocals = new HashMap<>(tempunitInLocals);
                        unitOutLocals = new HashMap<>(tempunitOutLocals);
                    }
                }

                generateOutput(body, unitInLocals, unitOutLocals);

                System.out.println("===========================================================");
            }
        }
    }

    private void generateOutput(Body body, HashMap<Unit, Set<Local>> unitInLocals, HashMap<Unit, Set<Local>> unitOutLocals) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(sourceDirectory + File.separator + clsName + ".java"));
            String line;
            HashMap<Integer, String> lineMap = new HashMap<>();
            int lineNumber = 1;
            while ((line = bufferedReader.readLine()) != null) {
                lineMap.put(lineNumber, line);
                lineNumber++;
            }
            bufferedReader.close();

            HashMap<Integer, List<Unit>> lineUnitsMap = new HashMap<>();

            for (Unit unit : body.getUnits()) {
                Stmt stmt = (Stmt) unit;
                System.out.print(stmt.getClass());
                int lineNumberOfUnit = unit.getJavaSourceStartLineNumber();
                System.out.println(": " + unit + "\t\tin-locals: " + unitInLocals.get(unit) +
                        "\t\tout-locals: " + unitOutLocals.get(unit) +
                        "\t\tLineNumber: " + lineNumberOfUnit);
                if (lineUnitsMap.containsKey(lineNumberOfUnit)) {
                    List<Unit> units = lineUnitsMap.get(lineNumberOfUnit);
                    units.add(unit);
                }else {
                    List<Unit> units = new ArrayList<>();
                    units.add(unit);
                    lineUnitsMap.put(lineNumberOfUnit, units);
                }
            }

            HashMap<Integer, Set<Local>> lineLocals = new HashMap<>();

            for (int lineNumberOfUnit : lineUnitsMap.keySet()) {
                List<Unit> units = lineUnitsMap.get(lineNumberOfUnit);
                //boolean ifStmtFlag = false;
                Unit flagUnit = null;
                for (Unit unit : units) {
                    Stmt stmt = (Stmt) unit;
                    flagUnit = unit;
                    if (stmt instanceof JIfStmt) {
                        //ifStmtFlag = true;
                        break;
                    }
                }
                if (flagUnit != null) {
                    Set<Local> locals = unitOutLocals.get(flagUnit);
                    if (locals != null) {
                        lineLocals.put(lineNumberOfUnit, locals);
                    }
                }
            }

            System.out.println("===========================================================");
            for (int i: lineMap.keySet()) {
                //System.out.print(i + "| " + lineMap.get(i));
                System.out.print(lineMap.get(i));
                if (lineLocals.containsKey(i)) {
                    System.out.print("\t" + lineLocals.get(i));
                }
                System.out.println();
            }

            File sourceFile = new File(sourceDirectory + File.separator + clsName + ".java");
            File sourFileParent = new File(sourceFile.getParent());
            File outPutDir = new File(sourFileParent.getParent() + File.separator + "output");
            if (!outPutDir.exists()) {
                outPutDir.mkdir();
            }
            File outPutFile = new File(outPutDir + File.separator + clsName + ".txt");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outPutFile));

            for(int i:  lineUnitsMap.keySet()) {
                String lineStr = lineMap.get(i).trim()+ lineLocals.get(i) + "\n";
                bufferedWriter.write(lineStr);
            }
            bufferedWriter.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + sourceDirectory + ".java");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
    }


    private boolean isChanged(HashMap<Unit, Set<Local>> unitInLocals, HashMap<Unit, Set<Local>> unitOutLocals) {
        for (Unit unit : unitInLocals.keySet()) {
            if (!unitInLocals.get(unit).equals(unitOutLocals.get(unit))) {
                return true;
            }
        }
        return false;
    }

    private HashMap<Unit, Set<Local>> initUnitLocals(List<Unit> unitPath) {
        HashMap<Unit, Set<Local>> unitLocalsMap = new HashMap<>();
        for (Unit unit : unitPath) {
            unitLocalsMap.put(unit, new HashSet<>());
        }
        return unitLocalsMap;
    }
}
