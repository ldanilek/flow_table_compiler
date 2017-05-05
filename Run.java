
import java.util.*;
import java.lang.reflect.*;

public class Run {
    private static void printTables(ArrayList<FlowTable> tables) {
        for (FlowTable table : tables) {
            System.out.println(table.printable());
        }
    }

    public static void runTreeOnPacket(Statement s, HashMap<PacketField, Integer> packet, ArrayList<PlugInOptimization> opts) {
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


}

