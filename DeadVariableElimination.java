// author: Lee
// eliminates dead variables and dummy variables only used in a single table
// example of how runtime-configurable optimizations work

import java.util.*;

/*
for example these actions can be condensed:
dummy0 := 5; packet.InPort := dummy0;
into this:
packet.InPort := 5;
This simplification doesn't reduce the number of flow tables,
but it makes actions simpler, which can enable other optimizations
*/

// helper class to deal with the four different kinds of assignment actions
class AssignableValue {
    public MatchableField field;
    public Integer value;
    public AssignableValue(MatchableField field, Integer value) {
        this.field = field;
        this.value = value;
    }
    public Action assignToVariable(String variable) {
        if (field == null) {
            return new AssignVariableAction(variable, value.intValue());
        } else {
            return new CopyVariableAction(variable, field);
        }
    }
    public Action assignToPacket(PacketField packetField) {
        if (field == null) {
            return new AssignFieldAction(packetField, value.intValue());
        } else {
            return new AssignVariableToFieldAction(packetField, field);
        }
    }
    public static String assignedVariable(Action a) {
        if (a instanceof CopyVariableAction) {
            return ((CopyVariableAction)a).variable;
        }
        if (a instanceof AssignVariableAction) {
            return ((AssignVariableAction)a).variable;
        }
        return null;
    }
    public static PacketField assignedPacket(Action a) {
        if (a instanceof AssignVariableToFieldAction) {
            return ((AssignVariableToFieldAction)a).field;
        }
        if (a instanceof AssignFieldAction) {
            return ((AssignFieldAction)a).field;
        }
        return null;
    }
    public static AssignableValue assignedValue(Action a) {
        if (a instanceof CopyVariableAction) {
            return new AssignableValue(((CopyVariableAction)a).value, null);
        }
        if (a instanceof AssignVariableAction) {
            return new AssignableValue(null, ((AssignVariableAction)a).value);
        }
        if (a instanceof AssignVariableToFieldAction) {
            return new AssignableValue(((AssignVariableToFieldAction)a).value, null);
        }
        if (a instanceof AssignFieldAction) {
            return new AssignableValue(null, ((AssignFieldAction)a).value);
        }
        return null;
    }
    public static AssignableValue assignedValueWithReplacements(Action action,
                        HashMap<String, AssignableValue> replacements) {
        AssignableValue assigned = AssignableValue.assignedValue(action);
        if (assigned != null && assigned.field != null && assigned.field.getVariableField() != null) {
            AssignableValue replaced = replacements.get(assigned.field.getVariableField());
            if (replaced != null) {
                return replaced;
            }
        }
        return assigned;
    }
}

public class DeadVariableElimination implements PlugInOptimization {

    private void useMatchableField(HashSet<String> vars, MatchableField field) {
        if (field.getVariableField() != null) {
            vars.add(field.getVariableField());
        }
    }

    private HashSet<String> variablesUsedInTable(FlowTable table) {
        HashSet<String> vars = new HashSet();
        for (MatchableField field : table.header.fields) {
            useMatchableField(vars, field);
        }
        for (Row row : table.rows) {
            for (Action action : row.actions) {
                if (action instanceof CopyVariableAction) {
                    useMatchableField(vars, ((CopyVariableAction)action).value);
                }
                if (action instanceof AssignVariableToFieldAction) {
                    useMatchableField(vars, ((AssignVariableToFieldAction)action).value);
                }
            }
        }
        return vars;
    }

    // a variable is active at a table if it is used at the table or
    // at a child table (the transitive closure of children)
    private HashSet<String> activeVariablesAtTable(OptimizableFlowTables ofts,
                HashMap<Integer, HashSet<String>> actives, Integer index) {
        HashSet<String> varsActive = actives.get(index);
        if (varsActive != null) {
            return varsActive;
        }
        varsActive = variablesUsedInTable(ofts.flowTables.get(index));
        for (Integer child : ofts.flowTables.get(index).children) {
            HashSet<String> childVars = activeVariablesAtTable(ofts, actives, child);
            for (String childVar : childVars) {
                varsActive.add(childVar);
            }
        }
        actives.put(index, varsActive);
        return varsActive;
    }

    // actually only care about those that are active on edges leaving the table
    private HashMap<Integer, HashSet<String>> activeVariables(OptimizableFlowTables ofts) {
        HashMap<Integer, HashSet<String>> actives = new HashMap();
        for (Integer i : ofts.indexes) {
            activeVariablesAtTable(ofts, actives, i);
        }
        HashMap<Integer, HashSet<String>> activeOut = new HashMap();
        for (Integer i : ofts.indexes) {
            HashSet<String> outEdges = new HashSet();
            for (Integer child : ofts.flowTables.get(i).children) {
                for (String childVar : actives.get(child)) {
                    outEdges.add(childVar);
                }
            }
            activeOut.put(i, outEdges);
        }
        return activeOut;
    }

    // convert dummy0 := 1; packet.InPort := dummy0; dummy1 := packet.InPort; dummy0 := dummy1; packet.OutPort := dummy0;
    // into packet.InPort := 1; packet.OutPort := packet.InPort;
    private boolean filterAssignments(ArrayList<Action> actions, HashSet<String> allowed) {
        boolean filtered = false;
        // actually simulate execution of the actions
        // keys are disallowed variables
        HashMap<String, AssignableValue> values = new HashMap();
        ArrayList<Action> newActions = new ArrayList<Action>();
        for (Action action : actions) {
            String assignedVariable = AssignableValue.assignedVariable(action);
            PacketField assignedPacket = AssignableValue.assignedPacket(action);
            AssignableValue assigned = AssignableValue.assignedValueWithReplacements(action, values);
            if (assignedVariable != null) {
                if (!allowed.contains(assignedVariable)) {
                    // do not add to newActions
                    filtered = true;
                    values.put(assignedVariable, assigned);
                } else {
                    // assignment to variable is allowed
                    newActions.add(assigned.assignToVariable(assignedVariable));
                }
            } else if (assignedPacket != null) {
                newActions.add(assigned.assignToPacket(assignedPacket));
            } else {
                newActions.add(action);
            }
        }
        actions.clear();
        actions.addAll(newActions);
        return filtered;
    }

    // filter all assign actions in the table which assign to variables that
    // aren't in the allowed set.
    private boolean filterAssignments(FlowTable table, HashSet<String> allowed) {
        boolean filtered = false;
        for (Row row : table.rows) {
            filtered = filterAssignments(row.actions, allowed) || filtered;
        }
        return filtered;
    }

    public boolean optimize(OptimizableFlowTables ofts) {
        boolean optimized = false;
        HashMap<Integer, HashSet<String>> activeVariables = activeVariables(ofts);
        for (Integer i : ofts.indexes) {
            optimized = filterAssignments(ofts.flowTables.get(i), activeVariables.get(i)) || optimized;
        }
        return optimized;
    }
}
