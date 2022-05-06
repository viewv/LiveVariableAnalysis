package top.viewv;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.BinopExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.Expr;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class ConstantPropagation extends ForwardFlowAnalysis<Unit, ValueMap> {
    public ConstantPropagation(DirectedGraph<Unit> graph) {
        super(graph);
        doAnalysis();
    }

    @Override
    protected void flowThrough(ValueMap in, Unit unit, ValueMap out) {
        copy(in, out);
        Stmt stmt = (Stmt) unit;
        if (stmt instanceof DefinitionStmt) {
            DefinitionStmt def = (DefinitionStmt) stmt;
            Local left = (Local) def.getLeftOp();
            Value right = def.getRightOp();
            if (right instanceof Expr) {
                Expr expr = (Expr) right;
                if (expr instanceof BinopExpr) {
                    BinopExpr binop = (BinopExpr) expr;
                    Constant constant = Calculate.calculate(binop, in);
                }
            }
        }
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
                if (cin1.isConst() && cin2.isConst() && cin1.equals(cin2)) {
                    out.put(v, cin1);
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
