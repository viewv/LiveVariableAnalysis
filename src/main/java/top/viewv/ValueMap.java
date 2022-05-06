package top.viewv;

import soot.Value;
import soot.jimple.IntConstant;

import java.util.HashMap;

public class ValueMap extends HashMap<Value, Constant> {
    @Override
    public Constant get(Object key) {
        if (super.containsKey(key)){
            return super.get(key);
        }else {
            //如果不存在，则返回一个新的 Constant 对象
            if (key instanceof IntConstant){
                return new Constant(((IntConstant) key).value);
            }
            return new Constant(Type.UNDEF);
        }
    }
}
