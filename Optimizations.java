// Written by Emon
import java.util.ArrayList;
import java.util.HashMap;

class OptFlowTable extends FlowTable {
    public ArrayList<Integer> children;
    public ArrayList<Integer> parents;
    // List of Assigned Variables? Other useful information? 

    public OptFlowTable(FlowTable ft, ArrayList<Integer> children, ArrayList<Integer> parents) {
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
            ArrayList<Integer> children = this.getFlowTableChildren(ft);
            ArrayList<Integer> parents = new ArrayList<Integer> ();
            indexes.add(ft.index);
            flowTables.put(ft.index, new OptFlowTable(ft, children, parents));
        }

        // Add parents for each FlowTable
        for (OptFlowTable oft : flowTables.values()) {
            for (int c : oft.children) {
                flowTables.get(c).parents.add(new Integer(oft.index));
            }
        }
    }

    // Helper function to return an ArrayList<Integer> of the table indices of
    // all tables that ft jumps to (children of ft). 
    private ArrayList<Integer> getFlowTableChildren(FlowTable ft) {
        ArrayList<Integer> children = new ArrayList<Integer> ();
        for (Row r : ft.rows) {
            if (r.jumpIndex != null)
                children.add(r.jumpIndex);
        }
        return children;
    } 

}

public class Optimizations implements PlugInOptimization {

    public boolean mergeAction (OptimizableFlowTables ofts, int childInd) {
        OptFlowTable child = ofts.flowTables.get(childInd);
        // Make sure that the child flowtable encodes an action with no matchfields.
        if (child.header.fields.size() > 0)
            return false;
        boolean optimized = false;

        // For each parent flowtable, find the rows that jump to the child
        // and append all of the child's actions. 
        ArrayList<Action> childActions = child.rows.get(0).actions;
        for (int parentInd : child.parents) {
            optimized = true;
            OptFlowTable parent = ofts.flowTables.get(parentInd);
            for (Row r : parent.rows) {
                if (r.jumpIndex != null && r.jumpIndex.equals(childInd)) {
                    r.actions.addAll(childActions);
                    r.jumpIndex = null;
                }
            }
            parent.children.remove(new Integer(childInd));
        }
        if (optimized) {
            ofts.flowTables.remove(childInd);
            ofts.indexes.remove(new Integer(childInd));
        }
        return optimized;
    }

    public boolean optimize(OptimizableFlowTables ofts) {
        for (int i : ofts.indexes) {
            if (this.mergeAction(ofts, i)) {
                return true;
            }
        }
        return false;
    }
}

