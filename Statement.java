import java.util.*;

abstract class Statement {
    // TODO: make methods to return inputs/outputs for dataflow
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        // first is the entry flow table
        return new ArrayList<FlowTable>();
    }

    // helper functions for subclasses
    public FlowTable flowTableForActions(ArrayList<Action> actions, Integer jumpIndex) {
        Header header = new Header(new ArrayList());
        Row row = new Row(1, actions, new ArrayList(), jumpIndex);
        return new FlowTable(header, Util.listWithObject(row));
    }

    /*
    public void actionsFollowedByJump(ArrayList<Action> actions, FlowTable jumpTo) {
        if (jumpTo != null) {
            actions.add(new JumpAction(jumpTo.index));
        }
    }
    */

    public ArrayList<FlowTable> flowTablesForAction(Action action, FlowTable jumpTo) {
        ArrayList actions = Util.listWithObject(action);
        Integer jumpIndex = (jumpTo != null) ? jumpTo.index : null;
        FlowTable table = this.flowTableForActions(actions, jumpIndex);
        ArrayList<FlowTable> tables = new ArrayList<FlowTable>();
        tables.add(table);
        return tables;
    }
}

class DecrementTTL extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        return this.flowTablesForAction(new DecrementTTLAction(), jumpTo);
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
        Integer jumpIndex = (jumpTo != null) ? jumpTo.index : null;
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
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        FlowTable table = new FlowTable(new Header(new ArrayList()), new ArrayList());
        ExpressionResult expResult = value.asFlowTables(table);
        ArrayList actions = Util.listWithObject(new AssignVariableToFieldAction(field, expResult.field));
        Integer jumpIndex = (jumpTo != null) ? jumpTo.index : null;
        table.rows.add(new Row(1, actions, new ArrayList(), jumpIndex));
        expResult.tables.add(table);
        return expResult.tables;
    }
}

class VerifyChecksum extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        return this.flowTablesForAction(new VerifyChecksumAction(), jumpTo);
    }
}

class RecomputeCheckSum extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        return this.flowTablesForAction(new RecomputeChecksumAction(), jumpTo);
    }
}

class Drop extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        return this.flowTablesForAction(new DropAction(), jumpTo);
    }
}

class Forward extends Statement {
    @Override
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        return this.flowTablesForAction(new ForwardAction(), jumpTo);
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
