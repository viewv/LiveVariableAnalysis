package top.viewv;

import soot.Body;
import soot.Local;
import soot.Unit;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;

import java.util.Set;

public class DeadCodeDetection {
    private Body body;
    private DirectedGraph<Unit> graph;
    private ReachMap reachMap;
    private ConstantPropagation constantPropagation;
    private LiveVariable lv;
    private ControlFlowUnreachable controlFlowUnreachable;

    private DeadCodeMap deadCodeMap = new DeadCodeMap();

    public DeadCodeDetection(Body body, DirectedGraph<Unit> graph, ReachMap reachMap, ConstantPropagation constantPropagation, LiveVariable lv, ControlFlowUnreachable controlFlowUnreachable) {
        this.body = body;
        this.graph = graph;
        this.reachMap = reachMap;
        this.constantPropagation = constantPropagation;
        this.lv = lv;
        this.controlFlowUnreachable = controlFlowUnreachable;
        this.analysis();
    }

    public DeadCodeMap getDeadCodeMap() {
        return deadCodeMap;
    }

    private void analysis() {
        for (Unit unit : graph) {
            if (reachMap.get(unit)) {
                Stmt stmt = (Stmt) unit;
                if (stmt instanceof DefinitionStmt) {
                    DefinitionStmt def = (DefinitionStmt) stmt;
                    Local left = (Local) def.getLeftOp();
                    Set<Local> unitOutLocal = lv.getOutLocals(unit);
                    if (!unitOutLocal.contains(left)) {
                        deadCodeMap.put(unit, true);
                    }
                }
            }else {
                deadCodeMap.put(unit, true);
            }
        }
    }
}
