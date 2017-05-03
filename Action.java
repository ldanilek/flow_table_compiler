
abstract class Action { // lawsuit
    public String printable() {
        return "undefined-action";
    }
    public void execInEnv(ExecEnv env) {
        env.outputs.add(this.printable());
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
        return "packet."+field.name()+" := "+value;
    }
    public void execInEnv(ExecEnv env) {
        super.execInEnv(env);
        env.packet.put(field, value);
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
        return variable+" := "+value;
    }
    public void execInEnv(ExecEnv env) {
        super.execInEnv(env);
        env.variables.put(variable, value);
    }
}

class CopyVariableAction extends Action {
    public String variable;
    public MatchableField value;
    public CopyVariableAction(String var, MatchableField val) {
        variable = var;
        value = val;
    }
    public String printable() {
        return variable+" := "+value.printable();
    }
    public void execInEnv(ExecEnv env) {
        super.execInEnv(env);
        env.variables.put(variable, env.matchableValue(value));
    }
}

class AssignVariableToFieldAction extends Action {
    public PacketField field;
    public MatchableField value;
    public AssignVariableToFieldAction(PacketField fld, MatchableField val) {
        field = fld;
        value = val;
    }
    public String printable() {
        return "packet."+field.name()+" := "+value.printable();
    }
    public void execInEnv(ExecEnv env) {
        super.execInEnv(env);
        env.packet.put(field, env.matchableValue(value));
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
