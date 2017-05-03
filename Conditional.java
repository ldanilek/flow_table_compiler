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

    public Compare(CompareOperation op, Expression left, int right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public ArrayList<FlowTable> asFlowTables(Integer thenJumpIndex, Integer elseJumpIndex) {

      FlowTable table = new FlowTable(new Header(new ArrayList<MatchableField>()), new ArrayList<Row>());
      ExpressionResult resolveExp = left.asFlowTables(table.index);
      table.header.fields.add(resolveExp.field);
      Cell caseTrue;
      Cell caseFalse;
      List<Integer> maskArray;

      switch (op) {
        case LE:
          right = right+1;
        case LT:
          maskArray = Util.genMasks(right, 1);
          for (int i : maskArray) {
            table.rows.add(new Row(2, new ArrayList(), Util.listWithObject(new Cell(0, i, resolveExp.field)), thenJumpIndex));
          }
          table.rows.add(new Row(1, new ArrayList(), Util.listWithObject(new Cell(0, 0, resolveExp.field)), elseJumpIndex));
          break;
        case GE:
          right = right-1;
        case GT:
          maskArray = Util.genMasks(right, 0);
          for (int i : maskArray) {
            table.rows.add(new Row(2, new ArrayList(), Util.listWithObject(new Cell(~0, i, resolveExp.field)), thenJumpIndex));
          }
          table.rows.add(new Row(1, new ArrayList(), Util.listWithObject(new Cell(0, 0, resolveExp.field)), elseJumpIndex));
          break;
        case NEQ:
          caseTrue = new Cell(right, ~0, resolveExp.field);
          caseFalse = new Cell(0, 0, resolveExp.field);
          table.rows.add(new Row(2, new ArrayList(), Util.listWithObject(caseTrue), elseJumpIndex));
          table.rows.add(new Row(1, new ArrayList(), Util.listWithObject(caseFalse), thenJumpIndex));
          break;
        case EQ:
          caseTrue = new Cell(right, ~0, resolveExp.field);
          caseFalse = new Cell(0, 0, resolveExp.field);
          table.rows.add(new Row(2, new ArrayList(), Util.listWithObject(caseTrue), thenJumpIndex));
          table.rows.add(new Row(1, new ArrayList(), Util.listWithObject(caseFalse), elseJumpIndex));
          break;
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
      table.header.fields.add(resolveExp.field);

      for (final int i : set) {
        table.rows.add(new Row(2, new ArrayList(), Util.listWithObject(new Cell(i, ~0, resolveExp.field)), thenJumpIndex));
      }

      table.rows.add(new Row(1, new ArrayList(), Util.listWithObject(new Cell(0, 0, resolveExp.field)), elseJumpIndex));

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

      ArrayList<FlowTable> t1Branch = right.asFlowTables(thenJumpIndex, elseJumpIndex);
      int table1idx = t1Branch.get(0).index;

      switch (op) {
        case AND:
          ArrayList<FlowTable> table1 = left.asFlowTables(table1idx, elseJumpIndex);
          table1.addAll(t1Branch);
          return table1;
        case OR:
          ArrayList<FlowTable> t1FalseBranch = right.asFlowTables(thenJumpIndex, elseJumpIndex);
          table1 = left.asFlowTables(thenJumpIndex, table1idx);
          table1.addAll(t1Branch);
          return table1;
        case XOR:
          ArrayList<FlowTable> t1NotBranch = right.asFlowTables(elseJumpIndex, thenJumpIndex);
          int table1Notidx = t1NotBranch.get(0).index;
          table1 = left.asFlowTables(table1Notidx, table1idx);
          table1.addAll(t1Branch);
          table1.addAll(t1NotBranch);
          return table1;
      }

      return null;
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
