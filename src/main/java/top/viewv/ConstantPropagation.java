package top.viewv;

import soot.Unit;
import soot.Value;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class ConstantPropagation extends ForwardFlowAnalysis<Unit, ValueMap> {
    public ConstantPropagation(DirectedGraph<Unit> graph) {
        super(graph);
        doAnalysis();
    }

    @Override
    protected void flowThrough(ValueMap in, Unit unit, ValueMap out) {

    }

    @Override
    protected ValueMap newInitialFlow() {
        return new ValueMap();
    }

    @Override
    protected void merge(ValueMap in1, ValueMap in2, ValueMap out) {
        out.clear();
        for (Value v : in1.keySet()) {
            Constant cin1 = in1.get(v);
            if (in2.containsKey(v)) {
                Constant cin2 = in2.get(v);
                if (cin1.isConst() && cin2.isConst()) {
                    if (cin1.equals(cin2)) {
                        out.put(v, cin1);
                    }else {
                        out.put(v, new Constant(Type.NAC));
                    }
                }else if (cin1.isConst() && cin2.isUndef()) {
                    out.put(v, cin1);
                }else if (cin2.isConst() && cin1.isUndef()) {
                    out.put(v, cin2);
                }else if (cin1.isUndef() && cin2.isUndef()) {
                    out.put(v, new Constant(Type.UNDEF));
                }else {
                    out.put(v, new Constant(Type.NAC));
                }
            }
        }
    }

    @Override
    protected void copy(ValueMap source, ValueMap dest) {
        dest.clear();
        dest.putAll(source);
    }
}
