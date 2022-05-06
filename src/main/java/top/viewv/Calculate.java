package top.viewv;

import soot.Value;
import soot.jimple.BinopExpr;

public class Calculate {
    public static Constant calculate(BinopExpr expr, ValueMap valueMap) {
        Constant constant = new Constant();
        Value left = expr.getOp1();
        Value right = expr.getOp2();
        String op = expr.getSymbol().trim();
        Constant leftConstant = valueMap.get(left);
        Constant rightConstant = valueMap.get(right);
        if (leftConstant.isConst() && rightConstant.isConst()) {
            constant.setType(Type.CONST);
            int result = calculate(leftConstant.getValue(), rightConstant.getValue(), op);
            constant.setValue(result);
        } else if (leftConstant.isNAC() || rightConstant.isNAC()) {
            constant.setType(Type.NAC);
        } else {
            constant.setType(Type.UNDEF);
        }
        return constant;
    }

    public static int calculate(int left, int right, String op) {
        int result = 0;
        switch (op) {
            case "+":
                result = left + right;
                break;
            case "-":
                result = left - right;
                break;
            case "*":
                result = left * right;
                break;
            case "/":
                result = (right == 0) ? 0 : left / right;
                break;
            case "%":
                result = (right == 0) ? 0 : left % right;
                break;
            case "&":
                result = left & right;
                break;
            case "|":
                result = left | right;
                break;
            case "^":
                result = left ^ right;
                break;
            case "<<":
                result = left << right;
                break;
            case ">>":
                result = left >> right;
                break;
            case ">>>":
                result = left >>> right;
                break;
        }
        return result;
    }
}
