import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Statement tree =
            new IF(
                new Compare(new PacketValue(PacketField.InPort),
                    CompareOperation.LT, 80),
                new Drop(),
                new Sequence(new DecrementTTL(), new Forward()));
        Statement simpleTree = new Drop();
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
