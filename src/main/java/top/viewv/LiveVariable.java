package top.viewv;

import soot.Body;
import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.DirectedGraph;

import java.util.*;

public class LiveVariable {

    private DirectedGraph<Unit> graph;
    private Body body;

    private HashMap<Unit, Set<Local>> inLocals;
    private HashMap<Unit, Set<Local>> outLocals;

    public LiveVariable(DirectedGraph<Unit> graph, Body body) {
        this.graph = graph;
        this.body = body;
        analysis();
    }

    public Set<Local> getInLocals(Unit unit) {
        return inLocals.get(unit);
    }

    public Set<Local> getOutLocals(Unit unit) {
        return outLocals.get(unit);
    }

    public void analysis() {
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

        for (Unit unit : body.getUnits()) {
            List<Unit> predUnits = graph.getPredsOf(unit);
            List<Unit> succUnits = graph.getSuccsOf(unit);
            unitPredUnitsMap.put(unit, predUnits);
            unitSuccUnitsMap.put(unit, succUnits);
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

        this.inLocals = unitInLocals;
        this.outLocals = unitOutLocals;
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
