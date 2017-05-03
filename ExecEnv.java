// written by Lee
// execution environment, for executing flow table

import java.util.*;

public class ExecEnv {
    public HashMap<PacketField, Integer> packet;
    public HashMap<String, Integer> variables;
    public ArrayList<String> outputs;

    public ExecEnv(HashMap<PacketField, Integer> packet) {
        this.packet = packet;
        variables = new HashMap();
        outputs = new ArrayList();
    }

    public int matchableValue(MatchableField field) {
        if (field.getPacketField() != null) {
            return this.packet.get(field.getPacketField()).intValue();
        }
        return this.variables.get(field.getVariableField()).intValue();
    }
}
