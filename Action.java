
abstract class Action { // lawsuit
    public String printable() {
        return "undefined-action";
    }
}

class AssignFieldAction extends Action {
    public PacketField field;
    public int value;
    public AssignFieldAction(PacketField field, int value) {
        this.field = field;
        this.value = value;
    }
    public String printable() {
        return "assign(packet."+field.name()+", "+Integer.toString(value)+")";
    }
}

class AssignVariableAction extends Action {
    public String variable;
    public int value;
    public AssignVariableAction(String var, int val) {
        variable = var;
        value = val;
    }
    public String printable() {
        return "assign("+variable+", "+Integer.toString(value)+")";
    }
}

class JumpAction extends Action {
    public int flowTableIndex;
    public JumpAction(int idx) {
        flowTableIndex = idx;
    }
    public String printable() {
        return "jump("+Integer.toString(flowTableIndex)+")";
    }
}

class ForwardAction extends Action {
    public String printable() {
        return "forward()";
    }
}

class DropAction extends Action {
    public String printable() {
        return "drop()";
    }
}

class DecrementTTLAction extends Action {
    public String printable() {
        return "packet.ttl--";
    }
}

class VerifyChecksumAction extends Action {
    public String printable() {
        return "verifyChecksum()";
    }
}

class RecomputeChecksumAction extends Action {
    public String printable() {
        return "recomputeChecksum()";
    }
}
