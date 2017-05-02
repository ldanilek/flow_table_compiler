import java.util.*;

abstract class Statement {
    // TODO: make methods to return inputs/outputs for dataflow
    public ArrayList<FlowTable> asFlowTables() {
        return new FlowTable[0];
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
}

class ValidateCheckSum extends Statement {}

class RecomputeCheckSum extends Statement {}

class Drop extends Statement {
    public ArrayList<FlowTable> asFlowTables() {
        FlowTable table = new FlowTable();
        ArrayList<FlowTable> tables = new ArrayList<FlowTable>();
        tables.add(table);
        return tables;
    }
}

class Forward extends Statement {}

class Sequence extends Statement {
    public Statement stm1;
    public Statement stm2;

    public Sequence(Statement first, Statement second) {
        stm1 = first;
        stm2 = second;
    }
}
