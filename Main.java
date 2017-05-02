import java.util.*;

public class Main {
    private static void printTablesForTree(Statement s) {
        ArrayList<FlowTable> tables = s.asFlowTables(null);
        for (FlowTable table : tables) {
            System.out.println(table.printable());
        }
    }

    public static void main(String[] args) {
        Statement tree =
            new IF(
                new Compare(new PacketValue(PacketField.InPort),
                    CompareOperation.LT, 80),
                new Drop(),
                new Sequence(new DecrementTTL(), new Forward()));
        Statement simpleTree = new Drop();
        printTablesForTree(simpleTree);
        Statement simpleSequence = new Sequence(new DecrementTTL(), new Forward());
        printTablesForTree(simpleSequence);
        HashMap<Integer, Integer> hashMap = new HashMap();
        hashMap.put(80, 90);
        hashMap.put(90, 100);
        hashMap.put(1024, 0);
        Statement hashLookup = new Assign("hashResult", new LookUp(hashMap, new PacketValue(PacketField.InPort)));
        printTablesForTree(hashLookup);

        MatchableField[] matchableFields = {
            new MatchableField(PacketField.IPv4Src),
            new MatchableField(PacketField.InPort)};
        Header header = new Header(new ArrayList<MatchableField>(Arrays.asList(matchableFields)));
        Action[] actionsA = {new DropAction()};
        Action[] actionsB = {new ForwardAction()};
        Action[] actionsC = {new DecrementTTLAction()};
        Cell[] cells = {
          new Cell(24, 152, new MatchableField(PacketField.IPv4Src)),
          new Cell(392, 100, new MatchableField(PacketField.InPort))
        };
        Row[] rows = {
            new Row(0, new ArrayList<Action>(Arrays.asList(actionsA)), new ArrayList<Cell>(Arrays.asList(cells))),
            new Row(1, new ArrayList<Action>(Arrays.asList(actionsB)), new ArrayList<Cell>(Arrays.asList(cells))),
            new Row(2, new ArrayList<Action>(Arrays.asList(actionsC)), new ArrayList<Cell>(Arrays.asList(cells)))
        };
        FlowTable table = new FlowTable(header, new ArrayList<Row>(Arrays.asList(rows)));
        System.out.println(table.printable());
    }
}
