import java.util.*;

abstract class Statement {
    // TODO: make methods to return inputs/outputs for dataflow
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
        // first is the entry flow table
        return new ArrayList<FlowTable>();
    }

    // helper functions for subclasses
    public FlowTable flowTableForActions(Action[] actions) {
        MatchableField dummyField = new MatchableField(PacketField.InPort);
        MatchableField[] dummyFields = {dummyField};
        Header header = new Header(dummyFields);
        Cell[] matchAll = {new Cell(0, 0, dummyField)};
        Row row = new Row(1, actions, matchAll);
        Row[] rows = {row};
        return new FlowTable(header, rows);
    }

    public Action[] actionsFollowedByJump(Action[] actions, FlowTable jumpTo) {
        if (jumpTo != null) {
            int length = actions.length;
            Action[] newActions = new Action[length+1];
            for (int i = 0; i < length; i++) {
                newActions[i] = actions[i];
            }
            newActions[length] = new JumpAction(jumpTo.index);
            actions = newActions;
        }
        return actions;
    }

    public ArrayList<FlowTable> flowTablesForAction(Action action, FlowTable jumpTo) {
        Action[] actions = {action};
        actions = this.actionsFollowedByJump(actions, jumpTo);
        FlowTable table = this.flowTableForActions(actions);
        ArrayList<FlowTable> tables = new ArrayList<FlowTable>();
        tables.add(table);
        return tables;
    }
}

class DecrementTTL extends Statement {
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

    /*public FlowTable[] asFlowTables() {

    }*/
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
        FlowTable table = new FlowTable(new Header(new MatchableField[0]), new Row[0]);
        ExpressionResult expResult = value.asFlowTables(table);
        Action[] actions = {new CopyVariableAction(variable, expResult.field)};
        actions = this.actionsFollowedByJump(actions, jumpTo);
        Row[] rows = {new Row(1, actions, new Cell[0])};
        table.rows = rows;
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
        FlowTable table = new FlowTable(new Header(new MatchableField[0]), new Row[0]);
        ExpressionResult expResult = value.asFlowTables(table);
        Action[] actions = {new AssignVariableToFieldAction(field, expResult.field)};
        actions = this.actionsFollowedByJump(actions, jumpTo);
        Row[] rows = {new Row(1, actions, new Cell[0])};
        table.rows = rows;
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
