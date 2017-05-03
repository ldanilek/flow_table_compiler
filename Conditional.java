import java.util.*;

// evaluates to a boolean
abstract class Conditional {

  public ArrayList<FlowTable> asFlowTables(Integer thenJumpIndex, Integer elseJumpIndex) {
    return null;
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
    public ArrayList<FlowTable> asFlowTables(Integer thenJumpIndex, Integer elseJumpIndex) {

      FlowTable table = new FlowTable(new Header(new ArrayList<MatchableField>()), new ArrayList<Row>());
      ExpressionResult resolveExp = left.asFlowTables(table.index);
      table.header.fields.add(resolveExp.field);

      switch (op) {
        case LT:
        case GT:
        case LE:
        case GE:
        case NEQ:
        case EQ:
          Cell caseTrue = new Cell(right, ~0, resolveExp.field);
          Cell caseFalse = new Cell(right, 0, resolveExp.field);
          table.rows.add(new Row(2, new ArrayList(), Util.listWithObject(caseTrue), thenJumpIndex));
          table.rows.add(new Row(1, new ArrayList(), Util.listWithObject(caseFalse), elseJumpIndex));
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
    public ArrayList<FlowTable> asFlowTables(Integer thenJumpIndex, Integer elseJumpIndex) {

      FlowTable table = new FlowTable(new Header(new ArrayList<MatchableField>()), new ArrayList<Row>());
      ExpressionResult resolveExp = value.asFlowTables(table.index);

      // for (final int i : set) {
      //     if (i == valueField) {
      //         return true;
      //     }
      // }
      // return false;

      resolveExp.tables.add(table);
      return resolveExp.tables;

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
    public ArrayList<FlowTable> asFlowTables(Integer thenJumpIndex, Integer elseJumpIndex) {

      return left.asFlowTables(thenJumpIndex, elseJumpIndex);
    }

}

class Not extends Conditional {
    public Conditional cond;
    public Not(Conditional cond) {
        this.cond = cond;
    }

    @Override
    public ArrayList<FlowTable> asFlowTables(Integer thenJumpIndex, Integer elseJumpIndex) {
      return cond.asFlowTables(elseJumpIndex,thenJumpIndex);
    }
}
