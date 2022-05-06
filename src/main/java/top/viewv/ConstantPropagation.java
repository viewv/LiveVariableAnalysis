package top.viewv;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
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
            Constant constant = new Constant();
            if (right instanceof Expr) {
                Expr expr = (Expr) right;
                if (expr instanceof BinopExpr) {
                    BinopExpr binop = (BinopExpr) expr;
                    constant = Calculate.calculate(binop, in);
                }
            } else if (right instanceof Local) {
                constant = in.get(right);
            } else if (right instanceof IntConstant) {
                constant = new Constant(((IntConstant) right).value);
            }
            out.put(left, constant);
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
