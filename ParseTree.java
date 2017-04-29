

public class ParseTree {
    public static void main(String[] args) {
        Statement tree =
            new IF(
                new Compare(new PacketValue(PacketField.InPort),
                    CompareOperation.LT, 80),
                new Drop(),
                new Sequence(
                    new AssignField(PacketField.TTL,
                        new Decrement(new PacketValue(PacketField.TTL))),
                    new Forward()));
        Statement simpleTree = new Drop();
        Action[] actions = {new DropAction()};
        Match[] matches = {new Match(0, 0, 1, actions)};
        FlowTable table = new FlowTable(0, matches);
        System.out.println(table.printable());
    }
}
