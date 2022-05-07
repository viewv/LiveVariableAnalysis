package top.viewv;

import soot.Body;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.toolkits.graph.DirectedGraph;

import java.util.ArrayList;
import java.util.List;

public class UnreachableBranch {
    private DirectedGraph<Unit> graph;

    private Body body;

    private ReachMap reachMap = new ReachMap();

    private ConstantPropagation constantPropagation;

    Unit head, tail;

    public UnreachableBranch(DirectedGraph<Unit> graph, Body body ,ConstantPropagation constantPropagation) {
        this.graph = graph;
        this.body = body;
        this.constantPropagation = constantPropagation;
        // we assume only have one entry point and one exit point
        head = graph.getHeads().get(0);
        tail = graph.getTails().get(0);
        analysis(head);
        reachMap.put(tail, true);
    }

    public ReachMap getReachMap() {
        return reachMap;
    }

    public void analysis(Unit unit){
        while (this.graph.getSuccsOf(unit).size() > 0){
            Stmt stmt = (Stmt) unit;
            ValueMap inValueMap = constantPropagation.getFlowBefore(unit);
            if (stmt instanceof IfStmt){
                IfStmt ifStmt = (IfStmt) stmt;
                Value condition = ifStmt.getCondition();
                reachMap.put(unit, true);
                if (condition instanceof ConditionExpr){
                    ConditionExpr conditionExpr = (ConditionExpr) condition;
                    Value op1 = conditionExpr.getOp1();
                    Value op2 = conditionExpr.getOp2();
                    Constant constant1 = inValueMap.get(op1);
                    Constant constant2 = inValueMap.get(op2);
                    String op = conditionExpr.getSymbol().trim();
                    if (constant1.isConst() && constant2.isConst()){
                        int constant1Value = constant1.getValue();
                        int constant2Value = constant2.getValue();
                        boolean result = calculate(constant1Value, constant2Value, op);
                        if (result){
                            unit = ifStmt.getTarget();
                        }else {
                            unit = body.getUnits().getSuccOf(ifStmt);
                        }
                    }
                }
            }else if (stmt instanceof LookupSwitchStmt){
                reachMap.put(unit, true);
                LookupSwitchStmt lookupSwitchStmt = (LookupSwitchStmt) stmt;
                List<IntConstant> lookupValues = lookupSwitchStmt.getLookupValues();
                List<Integer> lookupIntegers = new ArrayList<>();
                lookupValues.forEach(lookupValue -> lookupIntegers.add(lookupValue.value));
                List<Unit> targets = lookupSwitchStmt.getTargets();
                Value key = lookupSwitchStmt.getKey();
                Constant constant = inValueMap.get(key);
                if (constant.isConst()){
                    Unit target;
                    if (lookupIntegers.contains(constant.getValue())){
                        int index = lookupIntegers.indexOf(constant.getValue());
                        target = targets.get(index);
                    }else {
                        target = lookupSwitchStmt.getDefaultTarget();
                    }
                    unit = target;
                }
            } else if (stmt instanceof TableSwitchStmt){
                reachMap.put(unit, true);
                TableSwitchStmt tableSwitchStmt = (TableSwitchStmt) stmt;
                int lowIndex = tableSwitchStmt.getLowIndex();
                int highIndex = tableSwitchStmt.getHighIndex();
                Value key = tableSwitchStmt.getKey();
                Constant constant = inValueMap.get(key);
                if (constant.isConst()){
                    int constantValue = constant.getValue();
                    Unit target;
                    if (constantValue >= lowIndex && constantValue <= highIndex){
                        target = tableSwitchStmt.getTarget(constant.getValue() - lowIndex);
                    }else {
                        target = tableSwitchStmt.getDefaultTarget();
                    }
                    unit = target;
                }
            }
            reachMap.put(unit, true);
            List<Unit> succeeds = this.graph.getSuccsOf(unit);
            if (succeeds.size() == 1){
                unit = succeeds.get(0);
            }else {
                for (Unit succeed : succeeds){
                    this.analysis(succeed);
                }
                // jump out of the loop
                unit = body.getUnits().getSuccOf(unit);
            }
        }
    }

    private boolean calculate(int constant1, int constant2, String op){
        boolean result = false;
        switch (op){
            case "==":
                result = constant1 == constant2;
                break;
            case "!=":
                result = constant1 != constant2;
                break;
            case ">":
                result = constant1 > constant2;
                break;
            case ">=":
                result = constant1 >= constant2;
                break;
            case "<":
                result = constant1 < constant2;
                break;
            case "<=":
                result = constant1 <= constant2;
                break;
        }
        return result;
    }
}
