
// evaluates to a boolean
abstract class Conditional {

}

enum CompareOperation {
    LT, GT, LE, GE, EQ, NEQ
}

enum LogicOperation {
    AND, OR, XOR
}

class Compare extends Conditional {
    public CompareOperation op;
    public Expression left;
    // only unary comparison allowed,
    // second operand must be compile-time constant
    public int right;

    public Compare(Expression left, CompareOperation op, int right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}

class Contains extends Conditional {
    // compile-time constant set of integers
    public int[] set;
    public Expression value;

    public Contains(int[] elements, Expression value) {
        set = elements;
        this.value = value;
    }
}

class Logic extends Conditional {
    public LogicOperation op;
    public Conditional left;
    public Conditional right;

    public Logic(LogicOperation op, Conditional left, Conditional right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }
}

class Not extends Conditional {
    public Conditional cond;
    public Not(Conditional cond) {
        this.cond = cond;
    }
}


