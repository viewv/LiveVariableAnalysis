package top.viewv;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;

import java.util.HashSet;
import java.util.Set;

public class ControlFlowUnreachable {

    private DirectedGraph<Unit> graph;
    private Set<Integer> reachable = new HashSet<>();

    public ControlFlowUnreachable(DirectedGraph<Unit> graph) {
        this.graph = graph;
        analysis();
    }

    public Set<Integer> getReachable() {
        return reachable;
    }

    public void analysis(){
        for (Unit unit : graph) {
            reachable.add(unit.getJavaSourceStartLineNumber());
        }
    }
}
