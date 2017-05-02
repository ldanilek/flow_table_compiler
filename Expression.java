import java.util.*;

// evaluates to an integer
abstract class Expression {
    public ExpressionResult asFlowTables(FlowTable jumpTo) {
        return null;
    }

    private static int dummyVarCounter = 0;
    public String newDummyVar() {
        String dummy = "dummy"+dummyVarCounter;
        dummyVarCounter++;
        return dummy;
    }
}

class ExpressionResult {
    // to compute the thing
    public ArrayList<FlowTable> tables;
    // where is the thing now?
    public MatchableField field;
    public ExpressionResult(ArrayList<FlowTable> tables, MatchableField field) {
        this.tables = tables;
        this.field = field;
    }
}

class Variable extends Expression {
    public String variable;
    public Variable(String var) {
        variable = var;
    }

    @Override
    public ExpressionResult asFlowTables(FlowTable jumpTo) {
        return new ExpressionResult(new ArrayList<FlowTable>(), new MatchableField(variable));
    }
}

class PacketValue extends Expression {
    public PacketField field;
    public PacketValue(PacketField field) {
        this.field = field;
    }

    @Override
    public ExpressionResult asFlowTables(FlowTable jumpTo) {
        return new ExpressionResult(new ArrayList<FlowTable>(), new MatchableField(field));
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

    @Override
    public ExpressionResult asFlowTables(FlowTable jumpTo) {
        FlowTable table = new FlowTable(new Header(new MatchableField[0]), new Row[0]);
        ExpressionResult subResult = this.exp.asFlowTables(table);
        MatchableField[] matching = {subResult.field};
        table.header = new Header(matching);
        String output = this.newDummyVar();
        ArrayList<Row> rows = new ArrayList<Row>();
        for (Map.Entry<Integer, Integer> e : this.map.entrySet()) {
            Cell[] cells = {new Cell(e.getKey().intValue(), ~0, subResult.field)};
            Action[] actions = {new AssignVariableAction(output, e.getValue().intValue())};
            rows.add(new Row(1, actions, cells));
        }
        table.rows = (Row[])(rows.toArray());
        ArrayList<FlowTable> tables = new ArrayList<FlowTable>();
        tables.add(table);
        return new ExpressionResult(tables, new MatchableField(output));
    }
}

class Constant extends Expression {
    public int val;
    public Constant(int val) {
        this.val = val;
    }
    @Override
    public ExpressionResult asFlowTables(FlowTable jumpTo) {
        String output = this.newDummyVar();
        Action[] actions = {new AssignVariableAction(output, val)};
        Row[] rows = {new Row(1, actions, new Cell[0])};
        FlowTable table = new FlowTable(new Header(new MatchableField[0]), rows);
        ArrayList<FlowTable> tables = new ArrayList<FlowTable>();
        tables.add(table);
        return new ExpressionResult(tables, new MatchableField(output));
    }
}
