
// evaluates to a boolean
abstract class Conditional {

  public ConditionalResult asFlowTables(Integer thenJumpIndex, Integer elseJumpIndex) {
    return null;
  }

}

class ConditionalResult {
    // to compute the thing
    public ArrayList<FlowTable> tables;
    // where is the thing now?
    public MatchableField field;
    public ExpressionResult(ArrayList<FlowTable> tables, MatchableField field) {
        this.tables = tables;
        this.field = field;
    }
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

    @Override
    public ConditionalResult asFlowTables(Integer thenJumpIndex, Integer elseJumpIndex) {

      FlowTable table = new FlowTable(new Header(new ArrayList<MatchableField>()), new ArrayList<Row>());
      ExpressionResult resolveExp = left.asFlowTables(table.index);


      switch (op) {
        case LT: return (value > right);
        case GT: return (value > right);
        case LE: return (value <= right);
        case GE: return (value >= right);
        case EQ:

        case NEQ: return (value != right);
      }

      resolveExp.tables.add(table);
      return resolveExp.tables;
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

    @Override
    public ConditionalResult asFlowTables(Integer thenJumpIndex, Integer elseJumpIndex) {

      public MatchableField valueField = value.asFlowTables(null).field;

      for (final int i : set) {
          if (i == valueField) {
              return true;
          }
      }
      return false;

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

    @Override
    public ConditionalResult asFlowTables(Integer thenJumpIndex, Integer elseJumpIndex) {
      public Boolean valueLeft = left.result();
      public Boolean valueRight = right.result();

      switch (op) {
        case AND: return (valueLeft & valueRight);
        case OR: return (valueLeft | valueRight);
        case XOR: return (valueLeft ^ valueRight);
      }
    }

}

class Not extends Conditional {
    public Conditional cond;
    public Not(Conditional cond) {
        this.cond = cond;
    }

    @Override
    public ConditionalResult asFlowTables(Integer thenJumpIndex, Integer elseJumpIndex) {
      return cond.asFlowTables(elseJumpIndex,thenJumpIndex);
    }
}
