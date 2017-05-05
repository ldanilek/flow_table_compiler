import java.util.*;
import java.lang.reflect.*;

public class Main {
    public static void main(String[] args) {
        ArrayList<PlugInOptimization> optimizations = Input.getOptimizations(args);
        FlowTable.switchIndex = Input.getSwitch();
        Statement tree = Input.getParseTree();

        while(true)
            Run.runTreeOnPacket(tree, Input.getPacket(), optimizations);

    }
}
