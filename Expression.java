import java.util.Map;

// evaluates to an integer
abstract class Expression {

}

class Variable extends Expression {
    public String variable;
    public Variable(String var) {
        variable = var;
    }
}

class PacketValue extends Expression {
    public PacketField field;
    public PacketValue(PacketField field) {
        this.field = field;
    }
}

// compile-time constant, but depends on which machine the flow tables are
// being compiled for.
class Switch extends Expression {
}

class LookUp extends Expression {
    public Map<Integer, Integer> map;
    public Expression exp;
    public LookUp(Map<Integer, Integer> map, Expression exp) {
        this.map = map;
        this.exp = exp;
    }
}

class Constant extends Expression {
    public int val;
    public Constant(int val) {
        this.val = val;
    }
}
