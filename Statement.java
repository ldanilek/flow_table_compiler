import java.util.*;

abstract class Statement {
    // TODO: make methods to return inputs/outputs for dataflow
    public ArrayList<FlowTable> asFlowTables(FlowTable jumpTo) {
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
            actions = new Action[length+1];
            actions[length] = new JumpAction(jumpTo.index);
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
        return this.flowTablesForAction(new RecomputeChecksumAction(), jumpTo);
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
}
