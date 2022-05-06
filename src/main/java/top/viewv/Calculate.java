package top.viewv;

import soot.Value;
import soot.jimple.BinopExpr;

public class Calculate {
    public static Constant calculate(BinopExpr expr, ValueMap valueMap) {
        Constant constant = new Constant();
        Value left = expr.getOp1();
        Value right = expr.getOp2();
        Constant leftConstant = valueMap.get(left);
        Constant rightConstant = valueMap.get(right);
        if (leftConstant.isConst() && rightConstant.isConst()) {
            constant.setType(Type.CONST);
            int result = 0;
            switch (expr.getSymbol()) {
                case "+":
                    result = leftConstant.getValue() + rightConstant.getValue();
                    break;
                case "-":
                    result = leftConstant.getValue() - rightConstant.getValue();
                    break;
                case "*":
                    result = leftConstant.getValue() * rightConstant.getValue();
                    break;
                case "/":
                    result = (rightConstant.getValue() == 0) ? 0 : leftConstant.getValue() / rightConstant.getValue();
                    break;
                case "%":
                    result = (rightConstant.getValue() == 0) ? 0 : leftConstant.getValue() % rightConstant.getValue();
                    break;
                case "&":
                    result = leftConstant.getValue() & rightConstant.getValue();
                    break;
                case "|":
                    result = leftConstant.getValue() | rightConstant.getValue();
                    break;
                case "^":
                    result = leftConstant.getValue() ^ rightConstant.getValue();
                    break;
                case "<<":
                    result = leftConstant.getValue() << rightConstant.getValue();
                    break;
                case ">>":
                    result = leftConstant.getValue() >> rightConstant.getValue();
                    break;
                case ">>>":
                    result = leftConstant.getValue() >>> rightConstant.getValue();
                    break;
            }
            constant.setValue(result);
        }
        return constant;
    }
}
