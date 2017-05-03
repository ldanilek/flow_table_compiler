import java.util.*;
import java.lang.reflect.*;

public class Main {
    private static void printTables(ArrayList<FlowTable> tables) {
        for (FlowTable table : tables) {
            System.out.println(table.printable());
        }
    }

    private static void runTreeOnPacket(Statement s, HashMap<PacketField, Integer> packet, ArrayList<PlugInOptimization> opts) {
        ArrayList<FlowTable> tables = s.asFlowTables(null);
        tables = optimize(tables, opts);
        printTables(tables);
        FlowTable entryPoint = tables.get(0);
        ExecEnv env = new ExecEnv(packet);
        entryPoint.execInEnv(env, tables);
        for (String output : env.outputs) {
            System.out.println(output);
        }
    }

    private static ArrayList<FlowTable> optimize(ArrayList<FlowTable> tables, ArrayList<PlugInOptimization> optimizations) {
        OptimizableFlowTables ofts = new OptimizableFlowTables(tables);
        // was an optimization made?
        boolean o = true;
        while (o) {
            o = false;
            for (PlugInOptimization optimization : optimizations) {
                o = optimization.optimize(ofts) || o;
            }
        }
        // all done optimizing
        tables = new ArrayList();
        for (int i : ofts.indexes) {
            tables.add(ofts.flowTables.get(i));
        }
        return tables;
    }

    private static ArrayList<PlugInOptimization> plugInOptimizations(String[] names) {
        ArrayList<PlugInOptimization> optimizations = new ArrayList();
        for (String name : names) {
            System.out.println("Loading Optimizer '"+name+"'");
            // use reflection to load the optimizer
            try {
                Class optClass = Class.forName(name);
                Constructor optConstructor = optClass.getConstructor();
                Object opt = optConstructor.newInstance();
                // check that optimizer responds to the method
                if (opt instanceof PlugInOptimization) {
                    optimizations.add((PlugInOptimization)opt);
                } else {
                    System.err.println("ERROR: OPTIMIZER DOESN'T IMPLEMENT INTERFACE");
                }
            } catch (Exception ex) {
                System.err.println("ERROR: CAN'T LOAD OPTIMIZER "+name+" due to exception: "+ex);
            }
        }
        return optimizations;
    }

    public static void main(String[] args) {
        FlowTable.switchIndex = 2;

        ArrayList<PlugInOptimization> optimizations = plugInOptimizations(args);

        HashMap<PacketField, Integer> packet = new HashMap();
        packet.put(PacketField.InPort, 81);

        System.out.println("\nIF TEST:");
        Statement tree =
            new IF(
                new Logic(LogicOperation.AND,
                          new Compare(CompareOperation.EQ,
                                      new PacketValue(PacketField.InPort),
                                                      80),
                          new Compare(CompareOperation.NEQ,
                                      new PacketValue(PacketField.InPort),
                                                      80)),
                new Drop(),
                new Sequence(new DecrementTTL(), new Forward()));
        runTreeOnPacket(tree, packet, optimizations);

        System.out.println("\nSIMPLE TREE TEST:");
        Statement simpleTree = new Drop();
        runTreeOnPacket(simpleTree, packet, optimizations);

        System.out.println("\nSIMPLE SEQUENCE TEST:");
        Statement simpleSequence = new Sequence(new DecrementTTL(), new Forward());
        runTreeOnPacket(simpleSequence, packet, optimizations);

        System.out.println("\nHASH LOOKUP TEST:");
        HashMap<Integer, Integer> hashMap = new HashMap();
        hashMap.put(80, 90);
        hashMap.put(90, 100);
        hashMap.put(1024, 0);
        hashMap.put(2, 124);
        Statement hashLookup = new AssignField(PacketField.OutPort, new LookUp(hashMap, new PacketValue(PacketField.InPort)));
        runTreeOnPacket(hashLookup, packet, optimizations);

        System.out.println("\nSWITCH LOOKUP TEST:");
        Statement switchLookup = new AssignField(PacketField.OutPort, new LookUp(hashMap, new Switch()));
        runTreeOnPacket(switchLookup, packet, optimizations);

        System.out.println("\nASSIGN SEQUENCE TEST:");
        Statement assignTree = 
            new Sequence(
                new IF(
                    new Compare(CompareOperation.EQ,
                        new PacketValue(PacketField.InPort), 80),
                    new Assign("c", new Constant(2)),
                    new Assign("c", new Constant(3))),
                new IF(
                    new Compare(CompareOperation.EQ, new Variable("c"), 3),
                    new Forward(),
                    new Drop()));
        //runTreeOnPacket(assignTree, packet, new ArrayList<PlugInOptimization> ());
        runTreeOnPacket(assignTree, packet, optimizations);

        System.out.println("\nDEAD VAR REMOVAL TEST:");
        Statement ded = new Sequence(new Assign("x", new Constant(80)), new AssignField(PacketField.InPort, new Variable("x")));
        runTreeOnPacket(ded, packet, optimizations);

        System.out.println("\nLIVE VAR TEST:");
        Statement liv = new Sequence(new Sequence(new Assign("x", new Constant(80)), new AssignField(PacketField.InPort, new Variable("x"))),
                            new IF(new Compare(CompareOperation.EQ, new Variable("x"), 80), new Drop(), new Forward()));
        runTreeOnPacket(liv, packet, optimizations);

        System.out.println("\nFLOW TABLE PRINTING TEST:");
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
            new Row(0, new ArrayList<Action>(Arrays.asList(actionsA)), new ArrayList<Cell>(Arrays.asList(cells)), null),
            new Row(1, new ArrayList<Action>(Arrays.asList(actionsB)), new ArrayList<Cell>(Arrays.asList(cells)), null),
            new Row(2, new ArrayList<Action>(Arrays.asList(actionsC)), new ArrayList<Cell>(Arrays.asList(cells)), null)
        };
        FlowTable table = new FlowTable(header, new ArrayList<Row>(Arrays.asList(rows)));
        System.out.println(table.printable());
    }
}
