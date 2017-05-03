// Written by Emon
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

class OptFlowTable extends FlowTable {
    public HashSet<Integer> children;
    public HashSet<Integer> parents;
    // List of Assigned Variables? Other useful information? 

    public OptFlowTable(FlowTable ft, HashSet<Integer> children, HashSet<Integer> parents) {
        super(ft.header, ft.rows, ft.index); 
        this.children = children;
        this.parents = parents;
    }
}

class OptimizableFlowTables {
    public HashMap<Integer, OptFlowTable> flowTables; 
    public ArrayList<Integer> indexes;
    public int numTables;

    public OptimizableFlowTables(ArrayList<FlowTable> fts) {
        indexes = new ArrayList();
        numTables = 0;
        flowTables = new HashMap();

        // Add OptFlowTable versions of each FlowTable to FlowTables, hashed by table index. 
        // Save all seen table indices and the number of tables. 
        // Determine the indexes of the children (tables jumped to) from each FlowTable. 
        for (FlowTable ft : fts) {
            indexes.add(ft.index);
            flowTables.put(ft.index, new OptFlowTable(ft, null, null));
        }

        this.computeParentsAndChildren();
    }

    // Helper function to return an ArrayList<Integer> of the table indices of
    // all tables that ft jumps to (children of ft). 
    private HashSet<Integer> getFlowTableChildren(FlowTable ft) {
        HashSet<Integer> children = new HashSet<Integer> ();
        for (Row r : ft.rows) {
            if (r.jumpIndex != null)
                children.add(r.jumpIndex);
        }
        return children;
    }

    // after changing jump instructions or adding/removing a table,
    // call this to recompute the parent and child pointers
    public void computeParentsAndChildren() {
        for (OptFlowTable oft : flowTables.values()) {
            oft.children = this.getFlowTableChildren(oft);
            oft.parents = new HashSet<Integer>();
        }
        // Add parents for each FlowTable
        for (OptFlowTable oft : flowTables.values()) {
            for (int c : oft.children) {
                flowTables.get(c).parents.add(new Integer(oft.index));
            }
        }
    }
}

public class Optimizations implements PlugInOptimization {

    // For flowtable with index childInd, if it matches on no fields, merge it
    // with all parent flowtables. 
    public boolean mergeAction (OptimizableFlowTables ofts, int childInd) {
        OptFlowTable child = ofts.flowTables.get(childInd);
        // Make sure that the child flowtable encodes an action with no matchfields.
        if (child.header.fields.size() > 0)
            return false;
        boolean optimized = false;

        // For each parent flowtable, find the rows that jump to the child
        // and append all of the child's actions. 
        ArrayList<Action> childActions = child.rows.get(0).actions;
        Integer childJump = child.rows.get(0).jumpIndex;
        for (int parentInd : child.parents) {
            optimized = true;
            OptFlowTable parent = ofts.flowTables.get(parentInd);
            for (Row r : parent.rows) {
                if (r.jumpIndex != null && r.jumpIndex.equals(childInd)) {
                    r.actions.addAll(childActions);
                    r.jumpIndex = childJump;
                }
            }
        }
        if (optimized) {
            ofts.flowTables.remove(childInd);
            ofts.indexes.remove(new Integer(childInd));
        }
        ofts.computeParentsAndChildren();
        return optimized;
    }

    // For flowtable with index childInd, merge it with any and all parent flowtables
    // that assign all variables that the child matches on (assuming the child
    // matches on at least 1 field).  
    public boolean mergeMatchAssigned (OptimizableFlowTables ofts, int childInd) {
        OptFlowTable child = ofts.flowTables.get(childInd);
        if (child.header.fields.size() < 1 || child.parents.size() == 0)
            return false;   
        boolean optimized = false;

        // Fields/vars that were assigned to ints in all rows jumping to child table.
        // Fields/vars that were assigned to ints in the current row.
        // Boolean encodes whether the child table was merged into all parent tables.
        ArrayList<MatchableField> parentToChildAssignFields = new ArrayList<MatchableField>();
        ArrayList<MatchableField> rowAssignFields = new ArrayList<MatchableField>();
        boolean allParentsMerged = true; 

        // Attempt to merge the child to each parent.  
        for (Integer parentInd : child.parents) {
            OptFlowTable parent = ofts.flowTables.get(parentInd);
            // Save whether we have yet seen a row that jumps to child for this table
            boolean seenChildJumpRow = false; 

            // Save the rows which jump to the child as well as the common fields
            // (parentToChildAssignFields) which are assigned in every such row. 
            ArrayList<Row> parentToChildRows = new ArrayList<Row> ();
            for (Row r : parent.rows) {
                // Skip this row if it doesn't jump to the child flowtable. 
                if (r.jumpIndex == null || !r.jumpIndex.equals(childInd))
                    continue;

                parentToChildRows.add(r);
                this.saveAssignedFields(rowAssignFields, r.actions); 
 
                // Save assigned fields that have been assigned in every row jumping to child. 
                if (!seenChildJumpRow) {
                    parentToChildAssignFields.addAll(rowAssignFields);
                    seenChildJumpRow = true;
                } else
                    parentToChildAssignFields.retainAll(rowAssignFields);
                rowAssignFields.clear(); 
            }
            
            // Don't merge child into parent if the parent doesn't assign all the children's match fields.
            if (!parentToChildAssignFields.containsAll(child.header.fields)) {
                allParentsMerged = false;
                continue;
            }
            optimized = true; // True if even 1 parent is merged with child
            // Only care about assigned fields that are matched on in the child. 
            parentToChildAssignFields.retainAll(child.header.fields);
            doAssignmentMerge(child, parentToChildRows, parentToChildAssignFields);
            parent.children.remove(new Integer(childInd));
            parentToChildRows.clear();
        }

        // Only remove the child flowtable from ofts if it was merged with all its parents. 
        if (allParentsMerged) {
            ofts.flowTables.remove(childInd); 
            ofts.indexes.remove(new Integer(childInd));
        }
        ofts.computeParentsAndChildren();
        return optimized;
    }

    // Given child, merge its actions into the relevant parent rows. 
    private void doAssignmentMerge(OptFlowTable child, ArrayList<Row> parentToChildRows, ArrayList<MatchableField> assignedFields) {
        // TODO: Is it a problem to sort rows in place? Do we need the order for anything else? 
        // Sort child rows in descending order of priority (highest priority first).
        child.rows.sort(Comparator.comparingInt((Row r)->r.priority).reversed());
        for (Row r : parentToChildRows) {
            ArrayList<Action> assignActions = this.retrieveAssignActions(r.actions, assignedFields);
            for (Row crow : child.rows) {
                if (this.assignmentsMatchRow(assignActions, crow)) {
                    r.actions.addAll(crow.actions);
                    r.jumpIndex = crow.jumpIndex;
                    break;
                }
            }
            assignActions.clear(); 
        }
    }
    
    public boolean optimize(OptimizableFlowTables ofts) {
        for (int i : ofts.indexes) {
            if (this.mergeAction(ofts, i)) {
                return true;
            }
        }
        for (int i : ofts.indexes) {
            if (this.mergeMatchAssigned(ofts, i)) {
                return true;
            }
        }
        return false;
    }

    // Helper: Save a list of fields that are assigned in actions to fields. 
    private void saveAssignedFields(ArrayList<MatchableField> fields, ArrayList<Action> actions) {
        for (Action a: actions) {
            if (a instanceof AssignFieldAction) {
                AssignFieldAction aa = (AssignFieldAction) a;
                fields.add(new MatchableField(aa.field));
            } else if (a instanceof AssignVariableAction) {
                AssignVariableAction aa = (AssignVariableAction) a;
                fields.add(new MatchableField(aa.variable));
            }
        }
    }

    // Helper: Retrieve a list of the actions that assign values to fields. 
    private ArrayList<Action> retrieveAssignActions(ArrayList<Action> actions, ArrayList<MatchableField> fields) {
        ArrayList<Action> assignActions = new ArrayList<Action> ();
        for (Action a : actions) {
            MatchableField field = null;
            if (a instanceof AssignFieldAction) {
                AssignFieldAction aa = (AssignFieldAction) a;
                field = new MatchableField(aa.field);
            } else if (a instanceof AssignVariableAction) {
                AssignVariableAction aa = (AssignVariableAction) a;
                field = new MatchableField(aa.variable);
            }

            if (field != null && fields.contains(field))
                assignActions.add(a);
        }

        return assignActions; 
    }

    // Helper: Return true if the variable/field assignments match with the row match properties. 
    private boolean assignmentsMatchRow(ArrayList<Action> assignActions, Row r) {
        boolean isMatch = true;        
        for (Cell c : r.cells) {
            
            // Retrieve the Integer value of the field. 
            Integer value = null;
            MatchableField field = null;
            for (Action a : assignActions) {
                if (a instanceof AssignFieldAction) {
                    AssignFieldAction aa = (AssignFieldAction) a;
                    field = new MatchableField(aa.field);
                    if (field.equals(c.field))
                        value = new Integer(aa.value);
                } else if (a instanceof AssignVariableAction) {
                    AssignVariableAction aa = (AssignVariableAction) a;
                    field = new MatchableField(aa.variable);
                    if (field.equals(c.field))
                        value = new Integer(aa.value);
                }
            }

            // If field not found as assigned value, return false (cannot match). 
            if (value == null)
                return false;

            // Have all the fields matched all the cells so far?
            isMatch = isMatch && Util.evaluateMatch(value.intValue(), c.bitVector, c.mask);
            
            // If we ever get a false match for a cell, we know the overall match failed. 
            if (!isMatch) 
                return isMatch;    
        }

        return isMatch; 
    }

}

