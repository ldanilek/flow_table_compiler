import java.util.*;

// evaluates to an integer
abstract class Expression {
    public ExpressionResult asFlowTables(Integer jumpIndex) {
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
    public ExpressionResult asFlowTables(Integer jumpIndex) {
        return new ExpressionResult(new ArrayList<FlowTable>(), new MatchableField(variable));
    }
}

class PacketValue extends Expression {
    public PacketField field;
    public PacketValue(PacketField field) {
        this.field = field;
    }

    @Override
    public ExpressionResult asFlowTables(Integer jumpIndex) {
        return new ExpressionResult(new ArrayList<FlowTable>(), new MatchableField(field));
    }
}

// compile-time constant, but depends on which machine the flow tables are
// being compiled for.
class Switch extends Expression {
    @Override
    public ExpressionResult asFlowTables(Integer jumpIndex) {
        String output = "switch";
        Action action = new AssignVariableAction(output, FlowTable.switchIndex);
        ArrayList<FlowTable> tables = Statement.flowTablesForAction(action, jumpIndex);
        return new ExpressionResult(tables, new MatchableField(output));
    }
}

class LookUp extends Expression {
    public Map<Integer, Integer> map;
    public Expression exp;
    public LookUp(Map<Integer, Integer> map, Expression exp) {
        this.map = map;
        this.exp = exp;
    }

    @Override
    public ExpressionResult asFlowTables(Integer jumpIndex) {
        FlowTable table = new FlowTable(new Header(new ArrayList()), new ArrayList());
        ExpressionResult subResult = this.exp.asFlowTables(table.index);
        table.header = new Header(Util.listWithObject(subResult.field));
        String output = this.newDummyVar();
        ArrayList<Row> rows = new ArrayList<Row>();
        for (Map.Entry<Integer, Integer> e : this.map.entrySet()) {
            ArrayList cells = Util.listWithObject(new Cell(e.getKey().intValue(), ~0, subResult.field));
            ArrayList actions = Util.listWithObject(new AssignVariableAction(output, e.getValue().intValue()));
            table.rows.add(new Row(1, actions, cells, jumpIndex));
        }
        subResult.tables.add(table);
        return new ExpressionResult(subResult.tables, new MatchableField(output));
    }
}

class Constant extends Expression {
    public int val;
    public Constant(int val) {
        this.val = val;
    }
    @Override
    public ExpressionResult asFlowTables(Integer jumpIndex) {
        String output = this.newDummyVar();
        Action action = new AssignVariableAction(output, val);
        ArrayList<FlowTable> tables = Statement.flowTablesForAction(action, jumpIndex);
        return new ExpressionResult(tables, new MatchableField(output));
    }
}
