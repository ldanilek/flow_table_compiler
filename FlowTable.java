/* Written by Tyler/Lee
 *
 * Note - may need to change some of these classes to public to make them
 * accessible outside this file!
 */

/* The header row for a flow table, specifying column vals */
class Header {
    public MatchableField[] fields;

    public Header(MatchableField[] fields){
        this.fields = fields;
    }

    public String printable(){
        String str = "Header Fields:\t";
        for(int i = 0; i < fields.length; i++){
           str += fields[i].printable();
        }

        return str;
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
}

/* An individual row of a flowtable - fields of cells must match those in
 * header! Any fields in the header that don't have corresponding cells should
 * just default to wildcard (i.e. don't match on that field) */
class Row {
    public int priority;
    public Action[] actions;
    public Cell[] cells;

    public Row(int priority, Action[] actions, Cell[] cells) {
        this.priority = priority;
        this.actions = actions;
        this.cells = cells;
    }

    public String printable() {
        String str = Integer.toString(priority) + "\t";
        for(int i = 0; i < cells.length; i++){
           str += Integer.toBinaryString(cells[i].bitVector) + "\t"
            + Integer.toBinaryString(cells[i].mask) + "\t";
        }
        str += "{";
        for (int i = 0; i < actions.length; i++) {
            str += actions[i].printable() + "; ";
        }
        return str + "}";
    }
}

class FlowTable {
    private static int nextIndex = 0;
    public int index;
    public Header header;
    public Row[] rows;

    public FlowTable(Header header, Row[] rows) {
        this.index = nextIndex;
        nextIndex++;
        this.header = header;
        this.rows = rows;
    }

    public String printable() {
        String str = "Flow Table "+Integer.toString(index)+"\n";
        str += header.printable() + "\n";
        for (int i = 0; i < rows.length; i++) {
            str += rows[i].printable() + "\n";
        }
        return str;
    }
}
