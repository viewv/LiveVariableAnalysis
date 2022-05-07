package top.viewv;

import soot.Unit;

import java.util.HashMap;

public class DeadCodeMap extends HashMap<Unit, Boolean> {
    @Override
    public Boolean get(Object key) {
        if (containsKey(key)) {
            return super.get(key);
        }else {
            return false;
        }
    }
}
