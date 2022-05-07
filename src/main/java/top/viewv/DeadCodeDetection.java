package top.viewv;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.pointer.PASideEffectTester;
import soot.toolkits.graph.DirectedGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DeadCodeDetection {
    private Body body;
    private DirectedGraph<Unit> graph;
    private ReachMap reachMap;
    private ConstantPropagation constantPropagation;
    private LiveVariable lv;
    private ControlFlowUnreachable controlFlowUnreachable;

    private DeadCodeMap deadCodeMap = new DeadCodeMap();

    private HashMap<Unit, String> deadReason = new HashMap<>();

    private Set<Integer> liveCodeLines = new HashSet<>();

    private HashMap<Integer,String> deadCodeLines = new HashMap<>();

    private Set<Integer> sideEffectLines = new HashSet<>();

    //NaiveSideEffectTester sideEffectTester = new NaiveSideEffectTester();
    //PASideEffectTester sideEffectTester = new PASideEffectTester();

    public DeadCodeDetection(Body body, DirectedGraph<Unit> graph, ReachMap reachMap, ConstantPropagation constantPropagation, LiveVariable lv, ControlFlowUnreachable controlFlowUnreachable) {
        this.body = body;
        this.graph = graph;
        this.reachMap = reachMap;
        this.constantPropagation = constantPropagation;
        this.lv = lv;
        this.controlFlowUnreachable = controlFlowUnreachable;

        //sideEffectTester.newMethod(body.getMethod());

        this.analysis();
        this.generateLiveCodeLines();
        this.generateDeadCodeLines();
    }

    public DeadCodeMap getDeadCodeMap() {
        return deadCodeMap;
    }

    public Set<Integer> getLiveCodeLines() {
        return liveCodeLines;
    }

    public HashMap<Integer, String> getDeadCodeLines() {
        return deadCodeLines;
    }

    private void generateLiveCodeLines() {
        for (Unit unit : graph) {
            if (!deadCodeMap.get(unit)) {
                Stmt stmt = (Stmt) unit;
                if (stmt instanceof NopStmt || stmt instanceof GotoStmt) {
                    continue;
                }
                liveCodeLines.add(stmt.getJavaSourceStartLineNumber());
            }
        }
    }

    private void generateDeadCodeLines() {
        for (Unit unit : graph) {
            if (deadCodeMap.get(unit)) {
                Stmt stmt = (Stmt) unit;
                if (stmt instanceof NopStmt || stmt instanceof GotoStmt) {
                    continue;
                }
                String reason = deadReason.get(unit);
                int sourceCodeline = stmt.getJavaSourceStartLineNumber();
                if(!sideEffectLines.contains(sourceCodeline)){
                    deadCodeLines.put(sourceCodeline, reason);
                }
            }
        }
    }

    private void analysis() {
        for (Unit unit : graph) {
            Stmt stmt = (Stmt) unit;
            if (stmt instanceof NopStmt || stmt instanceof GotoStmt) {
                continue;
            }
            if (reachMap.get(unit)) {
                if (stmt instanceof DefinitionStmt) {
                    DefinitionStmt def = (DefinitionStmt) stmt;
                    Local left = (Local) def.getLeftOp();
                    Value right = def.getRightOp();
                    if (right instanceof InvokeExpr){
                        sideEffectLines.add(stmt.getJavaSourceStartLineNumber());
                    }else {
                        Set<Local> unitOutLocal = lv.getOutLocals(unit);
                        if (!unitOutLocal.contains(left)) {
                            deadReason.put(unit, "dead assignment");
                            deadCodeMap.put(unit, true);
                        }
                    }
                }
            }else {
                deadReason.put(unit, "unreachable branch");
                deadCodeMap.put(unit, true);
            }
        }
    }
}
