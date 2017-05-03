/* Written by Tyler/Lee/Emon
 *
 * Note - may need to change some of these classes to public to make them
 * accessible outside this file!
 */

import java.util.ArrayList;

/* The header row for a flow table, specifying column vals */
class Header {
    public ArrayList<MatchableField> fields;

    public Header(ArrayList<MatchableField> fields) {
        this.fields = fields;
    }

    // Make it look nice when it goes before rows!
    public String printable(){
        String str = "Pr:\t";
        for(int i = 0; i < fields.size(); i++){
           str += String.format("%16s%16s\t", fields.get(i).printable(), ""); // vector field and bitmask!
        }
        return str += "Actions";
    }
}

/* Contained within a row, the individual cells of a flowtable */
class Cell{
    public int bitVector;
    public int mask;
    public MatchableField field;

    public Cell (int bitVector, int mask, MatchableField field){
        this.field = field;
        this.bitVector = bitVector;
        this.mask = mask;
    }

    public boolean matchesEnv(ExecEnv env) {
        return Util.evaluateMatch(env.matchableValue(field), bitVector, mask);
    }
}

/* An individual row of a flowtable - fields of cells must match those in
 * header! Any fields in the header that don't have corresponding cells should
 * just default to wildcard (i.e. don't match on that field) */
class Row {
    public int priority;
    public ArrayList<Action> actions;
    public ArrayList<Cell> cells;
    public Integer jumpIndex;

    public Row(int priority, ArrayList<Action> actions, ArrayList<Cell> cells, Integer jumpIndex) {
        this.priority = priority;
        this.actions = actions;
        this.cells = cells;
        this.jumpIndex = jumpIndex;
    }

    public String printable() {
        String str = Integer.toString(priority) + "\t";
        for(int i = 0; i < cells.size(); i++){
           str += Util.prettyBinaryMatch(cells.get(i).bitVector, cells.get(i).mask) + "\t";

        }
        str += "{";
        for (int i = 0; i < actions.size(); i++) {
            str += actions.get(i).printable() + "; ";
        }
        // Print jump to FlowTable as action (if applicable).
        if (jumpIndex != null)
            str += "jump(" + jumpIndex.toString() + "); ";
        return str + "}";
    }

    public boolean matchesEnv(ExecEnv env) {
        for (Cell cell : cells) {
            if (!cell.matchesEnv(env)) {
                return false;
            }
        }
        return true;
    }

    public void execInEnv(ExecEnv env, ArrayList<FlowTable> tables) {
        for (Action action : actions) {
            action.execInEnv(env);
        }
        if (jumpIndex != null) {
            for (FlowTable table : tables) {
                if (table.index.intValue() == jumpIndex.intValue()) {
                    table.execInEnv(env, tables);
                    return;
                }
            }
            env.outputs.add("ERROR: NO FLOW TABLE WITH INDEX "+jumpIndex);
        }
    }
}

class FlowTable {
    public static int switchIndex = 0;
    private static int nextIndex = 0;
    public Integer index;
    public Header header;
    public ArrayList<Row> rows;

    public FlowTable(Header header, ArrayList<Row> rows) {
        this.index = new Integer(nextIndex);
        nextIndex++;
        this.header = header;
        this.rows = rows;
    }

    public FlowTable(Header header, ArrayList<Row> rows, int index) {
        this.index = index;
        this.header = header;
        this.rows = rows;
    }

    public String printable() {
        String str = "Flow Table "+Integer.toString(index)+"\n";
        str += header.printable() + "\n";
        for (int i = 0; i < rows.size(); i++) {
            str += rows.get(i).printable() + "\n";
        }
        return str;
    }

    public void execInEnv(ExecEnv env, ArrayList<FlowTable> tables) {
        Row bestRow = null;
        // NOTE this means regular priorities must be nonnegative
        int highestPriority = -1;
        // NOTE if two matches with the same priority, we go with the earlier one
        for (Row row : rows) {
            if (row.priority > highestPriority && row.matchesEnv(env)) {
                bestRow = row;
                highestPriority = row.priority;
            }
        }

        if (bestRow == null) {
            env.outputs.add("ERROR: NO ROW MATCH IN FLOWTABLE "+index);
        } else {
            bestRow.execInEnv(env, tables);
        }
    }
}
