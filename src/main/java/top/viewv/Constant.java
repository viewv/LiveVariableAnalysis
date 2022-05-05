package top.viewv;

public class Constant {

    private Type type;
    private int value;

    public Constant(){
        type = Type.UNDEF;
        value = 0;
    }

    public Constant(int value){
        type = Type.CONST;
        this.value = value;
    }

    public Constant(Type type){
        this.type = type;
        value = 0;
    }

    public boolean isConst(){
        return type == Type.CONST;
    }

    public boolean isUndef(){
        return type == Type.UNDEF;
    }

    public boolean isNAC(){
        return type == Type.NAC;
    }

    @Override
    public int hashCode(){
        return value;
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof Constant){
            Constant c = (Constant) obj;
            return c.type == type && c.value == value;
        }
        return false;
    }

    @Override
    public String toString(){
        if (type == Type.CONST)
            return Integer.toString(value);
        else
            return type.toString();
    }
}
