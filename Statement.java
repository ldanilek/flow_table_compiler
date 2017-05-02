import java.util.*;

abstract class Statement {
    // TODO: make methods to return inputs/outputs for dataflow
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        // first is the entry flow table
        return new ArrayList<FlowTable>();
    }

    // helper functions for subclasses
    public static FlowTable flowTableForActions(ArrayList<Action> actions) {
        Header header = new Header(new ArrayList());
        Row row = new Row(1, actions, new ArrayList());
        return new FlowTable(header, Util.listWithObject(row));
    }

    public static void actionsFollowedByJump(ArrayList<Action> actions, FlowTable jumpTo) {
        if (jumpTo != null) {
            actions.add(new JumpAction(jumpTo.index));
        }
    }

    public static ArrayList<FlowTable> flowTablesForAction(Action action, FlowTable jumpTo) {
        ArrayList actions = Util.listWithObject(action);
        Statement.actionsFollowedByJump(actions, jumpTo);
        FlowTable table = Statement.flowTableForActions(actions);
        ArrayList<FlowTable> tables = new ArrayList<FlowTable>();
        tables.add(table);
        return tables;
    }
}

class DecrementTTL extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        return Statement.flowTablesForAction(new DecrementTTLAction(), jumpTo);
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
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        // TODO: conditionals
        return null;
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
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        FlowTable table = new FlowTable(new Header(new ArrayList<MatchableField>()), new ArrayList<Row>());
        ExpressionResult expResult = value.asFlowTables(table);
        ArrayList actions = Util.listWithObject(new CopyVariableAction(variable, expResult.field));
        Statement.actionsFollowedByJump(actions, jumpTo);
        table.rows.add(new Row(1, actions, new ArrayList()));
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
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        FlowTable table = new FlowTable(new Header(new ArrayList()), new ArrayList());
        ExpressionResult expResult = value.asFlowTables(table);
        ArrayList actions = Util.listWithObject(new AssignVariableToFieldAction(field, expResult.field));
        Statement.actionsFollowedByJump(actions, jumpTo);
        table.rows.add(new Row(1, actions, new ArrayList()));
        expResult.tables.add(table);
        return expResult.tables;
    }
}

class VerifyChecksum extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        return Statement.flowTablesForAction(new VerifyChecksumAction(), jumpTo);
    }
}

class RecomputeCheckSum extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        return Statement.flowTablesForAction(new RecomputeChecksumAction(), jumpTo);
    }
}

class Drop extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        return Statement.flowTablesForAction(new DropAction(), jumpTo);
    }
}

class Forward extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        return Statement.flowTablesForAction(new ForwardAction(), jumpTo);
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
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        ArrayList<FlowTable> tables2 = stm2.asFlowTables(jumpTo);
        // note a statement must be turned into at least one flow table.
        FlowTable table2 = tables2.get(0);
        ArrayList<FlowTable> tables1 = stm1.asFlowTables(table2);
        tables1.addAll(tables2);
        return tables1;
    }
}
