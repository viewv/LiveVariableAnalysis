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

    public UnreachableBranch(DirectedGraph<Unit> graph, Body body ,ConstantPropagation constantPropagation) {
        this.graph = graph;
        this.body = body;
        this.constantPropagation = constantPropagation;
        analysis();
    }

    public void analysis(){
        for (Unit unit : graph){
            Stmt stmt = (Stmt) unit;
            ValueMap inValueMap = constantPropagation.getFlowBefore(unit);
            if (stmt instanceof IfStmt){
                System.out.println(stmt);
                IfStmt ifStmt = (IfStmt) stmt;
                Value condition = ifStmt.getCondition();
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
                            reachMap.put(ifStmt, true);
                            Unit target = ifStmt.getTarget();
                            graph.getSuccsOf(target).forEach(succeed -> reachMap.put(succeed, true));
                        }
                    }
                }
            }else if (stmt instanceof LookupSwitchStmt){
                LookupSwitchStmt lookupSwitchStmt = (LookupSwitchStmt) stmt;
                List<IntConstant> lookupValues = lookupSwitchStmt.getLookupValues();
                List<Integer> lookupIntegers = new ArrayList<>();
                lookupValues.forEach(lookupValue -> lookupIntegers.add(lookupValue.value));
                List<Unit> targets = lookupSwitchStmt.getTargets();
                Value key = lookupSwitchStmt.getKey();
                Constant constant = inValueMap.get(key);
                Unit target;
                if (lookupIntegers.contains(constant.getValue())){
                    int index = lookupIntegers.indexOf(constant.getValue());
                    target = targets.get(index);
                }else {
                    target = lookupSwitchStmt.getDefaultTarget();
                }
                graph.getSuccsOf(target).forEach(succeed -> reachMap.put(succeed, true));
                System.out.println(reachMap);
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
