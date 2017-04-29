
class Match {
    public int bitVector;
    public int mask;
    public int priority;
    public Action[] actions;

    public Match(int bitVector, int mask, int priority, Action[] actions) {
        this.bitVector = bitVector;
        this.mask = mask;
        this.priority = priority;
        this.actions = actions;
    }

    public String printable() {
        String str = Integer.toBinaryString(bitVector) + "\t"
            + Integer.toBinaryString(mask)
            + "\t" + Integer.toString(priority) + "\t{";
        for (int i = 0; i < actions.length; i++) {
            str += actions[i].printable() + "; ";
        }
        return str + "}";
    }
}

class FlowTable {
    public int index;
    public Match[] matches;

    public FlowTable(int index, Match[] matches) {
        this.index = index;
        this.matches = matches;
    }

    public String printable() {
        String str = "Flow Table "+Integer.toString(index)+"\n";
        for (int i = 0; i < matches.length; i++) {
            str += matches[i].printable() + "\n";
        }
        return str;
    }
}
