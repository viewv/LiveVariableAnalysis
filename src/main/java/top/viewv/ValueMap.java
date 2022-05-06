package top.viewv;

import soot.Value;

import java.util.HashMap;

public class ValueMap extends HashMap<Value, Constant> {
    @Override
    public Constant get(Object key) {
        if (super.containsKey(key)){
            return super.get(key);
        }else {
            return new Constant(Type.NAC);
        }
    }
}
