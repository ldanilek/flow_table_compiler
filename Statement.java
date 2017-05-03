import java.util.*;

abstract class Statement {
    // TODO: make methods to return inputs/outputs for dataflow
    public ArrayList<FlowTable> asFlowTables(Integer jumpIndex) {
        // first is the entry flow table
        return new ArrayList<FlowTable>();
    }

    // helper functions for subclasses
    public static FlowTable flowTableForActions(ArrayList<Action> actions, Integer jumpIndex) {
        Header header = new Header(new ArrayList());
        Row row = new Row(1, actions, new ArrayList(), jumpIndex);
        return new FlowTable(header, Util.listWithObject(row));
    }

    public static ArrayList<FlowTable> flowTablesForAction(Action action, Integer jumpIndex) {
        ArrayList actions = Util.listWithObject(action);
        FlowTable table = Statement.flowTableForActions(actions, jumpIndex);
        ArrayList<FlowTable> tables = new ArrayList<FlowTable>();
        tables.add(table);
        return tables;
    }
}

class DecrementTTL extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(Integer jumpIndex) {
        return Statement.flowTablesForAction(new DecrementTTLAction(), jumpIndex);
    }
}

class IF extends Statement {
    public Conditional condition;
    public Statement thenBranch;
    public Statement elseBranch;

    public IF(Conditional cond, Statement then, Statement elseStm) {
        condition = cond;
        thenBranch = then;
        elseBranch = elseStm;
    }

    @Override
    public ArrayList<FlowTable> asFlowTables(Integer jumpIndex) {
      ArrayList<FlowTable> thenTables = thenBranch.asFlowTables(jumpIndex);
      ArrayList<FlowTable> elseTables = elseBranch.asFlowTables(jumpIndex);
      FlowTable thenTable = thenTables.get(0);
      FlowTable elseTable = elseTables.get(0);
      return condition.asFlowTables(thenTable, elseTable);
    }
}

class Assign extends Statement {
    public String variable;
    public Expression value;

    public Assign(String var, Expression val) {
        variable = var;
        value = val;
    }

    @Override
    public ArrayList<FlowTable> asFlowTables(Integer jumpIndex) {
        FlowTable table = new FlowTable(new Header(new ArrayList<MatchableField>()), new ArrayList<Row>());
        ExpressionResult expResult = value.asFlowTables(table.index);
        ArrayList actions = Util.listWithObject(new CopyVariableAction(variable, expResult.field));
        table.rows.add(new Row(1, actions, new ArrayList(), jumpIndex));
        expResult.tables.add(table);
        return expResult.tables;
    }
}

// changes a field of the packet
class AssignField extends Statement {
    public PacketField field;
    public Expression value;

    public AssignField(PacketField field, Expression val) {
        this.field = field;
        value = val;
    }

    @Override
    public ArrayList<FlowTable> asFlowTables(Integer jumpIndex) {
        FlowTable table = new FlowTable(new Header(new ArrayList()), new ArrayList());
        ExpressionResult expResult = value.asFlowTables(table.index);
        ArrayList actions = Util.listWithObject(new AssignVariableToFieldAction(field, expResult.field));
        table.rows.add(new Row(1, actions, new ArrayList(), jumpIndex));
        expResult.tables.add(table);
        return expResult.tables;
    }
}

class VerifyChecksum extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(Integer jumpIndex) {
        return Statement.flowTablesForAction(new VerifyChecksumAction(), jumpIndex);
    }
}

class RecomputeCheckSum extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(Integer jumpIndex) {
        return Statement.flowTablesForAction(new RecomputeChecksumAction(), jumpIndex);
    }
}

class Drop extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(Integer jumpIndex) {
        return Statement.flowTablesForAction(new DropAction(), jumpIndex);
    }
}

class Forward extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(Integer jumpIndex) {
        return Statement.flowTablesForAction(new ForwardAction(), jumpIndex);
    }
}

class Sequence extends Statement {
    public Statement stm1;
    public Statement stm2;

    public Sequence(Statement first, Statement second) {
        stm1 = first;
        stm2 = second;
    }

    @Override
    public ArrayList<FlowTable> asFlowTables(Integer jumpIndex) {
        ArrayList<FlowTable> tables2 = stm2.asFlowTables(jumpIndex);
        // note a statement must be turned into at least one flow table.
        FlowTable table2 = tables2.get(0);
        ArrayList<FlowTable> tables1 = stm1.asFlowTables(table2.index);
        tables1.addAll(tables2);
        return tables1;
    }
}
